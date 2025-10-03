import json
from openai import OpenAI
import difflib
import os
from pathlib import Path
from loguru import logger
from tqdm import tqdm

from checker.java_parser import JavaSyntaxChecker
from batch_migrator import _udiff, strip_marker, guess_token_count

PROMPT = """
    ### Instruction:
    You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location. 
    Fix any syntax errors in the provided excerpt. Ensure that the rewritten excerpt is syntactically correct and adheres to Java programming conventions. Ensure the completeness of the code within the provided excerpt.

    ### User Edits:

    {}

    ### User Excerpt:

    {}

    ### Response:
"""

client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
SCENARIO1_DIR = Path("NesCodeSecExamples/src/main/java/com/Scenario1")
BASE_DIR = SCENARIO1_DIR / "base"
EVENT_DIR = SCENARIO1_DIR / "input_event"
MIGRATE_DIR = SCENARIO1_DIR / "migrate_full" # migrated full code
EXCERPT_DIR = SCENARIO1_DIR / "input_excerpt" # input excerpt to be completed
OUTPUT_DIR = SCENARIO1_DIR / "output"

# model = transformers.AutoModelForCausalLM.from_pretrained("zed-industries/zeta")
# tokenizer = transformers.AutoTokenizer.from_pretrained("zed-industries/zeta")

def get_response_content(response: str) -> str:
    return response.split("### Response:")[1][len("### Response:"):].strip()

def crop_response(filename: str, code_under_check: str, start: int, end: int, cursor_line: int) -> str:
    input_excerpt = code_under_check.splitlines()[start:cursor_line] + ["\t<|user_cursor_is_here|>"] + code_under_check.splitlines()[cursor_line:end+1]
    if not "<|editable_region_start|>" in input_excerpt:
        input_excerpt = ["<|editable_region_start|>"] + input_excerpt
    if not "<|start_of_file|>" in input_excerpt:
        input_excerpt = ["```<|start_of_file|>"] + input_excerpt
    if not filename in input_excerpt:
        input_excerpt = [filename] + input_excerpt
    if not "<|end_of_file|>" in input_excerpt:
        input_excerpt = input_excerpt + ["<|end_of_file|>```"]
        
    return "\n".join(input_excerpt)

def apply_change(start: int, end: int, new_text: str, original_text: str) -> str:
    original_lines = original_text.splitlines()
    # print("===== [ APPLY CHANGE ] =====")
    # print(f"[ORIGINAL START] \n{"\n".join(original_lines[:start])}")
    # print(f"[NEW TEXT] \n{new_text}")
    # print(f"[ORIGINAL END] \n{"\n".join(original_lines[end+1:])}")
    # print("============================")
    return "\n".join(original_lines[:start] + [new_text] + original_lines[end+1:])

def request_zeta(client: OpenAI, prompt: str, original_text: str, start: int, end: int) -> str:
    try:
        resp = client.completions.create(
            model="zeta",
            prompt=prompt,
            max_tokens=1000,
            temperature=0.2,
        )
        response = strip_marker(resp.choices[0].text)
        code_under_check = apply_change(start, end, response, original_text)
        return code_under_check
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

syntax_log = {}

def generate_xxe_response() -> None:
    # 统计变量
    total_files = 0
    failed_syntax_files = 0
    
    try:
        # logger.info("===== Build prompt =====")
        for dir in tqdm(list(EVENT_DIR.iterdir()), desc=f"Build Prompt", unit="dir", bar_format='{l_bar}{bar:20}{r_bar}', ncols=100):
            for file in tqdm(list(dir.iterdir()), desc=f"Process {dir.name}", unit="file", bar_format='{l_bar}{bar:20}{r_bar}', ncols=100):
                total_files += 1
                
                with open(EVENT_DIR / dir.name / file.name, "r") as f:
                    input_event = f.read()
                with open(EXCERPT_DIR / dir.name / f"{file.stem}.java", "r") as f:
                    input_excerpt = f.read()
                    # Smart parsing of excerpt format: "start:end:content"
                    lines = input_excerpt.splitlines()
                    if not lines:
                        logger.warning(f"Empty excerpt file: {file.stem}.java")
                        failed_syntax_files += 1
                        continue
                    # Parse the range header (format: "start:end:content_start")
                    header_parts = lines[0].split(":")
                    if len(header_parts) < 4:
                        logger.error(f"Invalid excerpt format in {file.stem}.java: missing range info")
                        failed_syntax_files += 1
                        continue
                    try:
                        start, end, cursor_line = int(header_parts[0]), int(header_parts[1]), int(header_parts[2])
                        # Reconstruct content: header content + remaining lines
                        filename = [header_parts[3]] if len(header_parts) > 3 else []
                        filename.extend(lines[1:])
                        input_excerpt = "\n".join(filename)
                    except (ValueError, IndexError) as e:
                        logger.error(f"Failed to parse range from {file.stem}.java: {e}")
                        failed_syntax_files += 1
                        continue
                        
                prompt = PROMPT.format(input_event, input_excerpt)
                
                migrate_full_path = MIGRATE_DIR / dir.name / f"{file.stem}.java"
                migrate_full_text = migrate_full_path.read_text()
                code_under_check = request_zeta(client, prompt, migrate_full_text, start, end)
                # print(f"[CODE UNDER CHECK]\n{strip_marker(code_under_check)}")
                
                checker = JavaSyntaxChecker()
                TRY = 3
                key = f"{dir.name}/{file.stem}"
                syntax_log[key] = checker.check(strip_marker(code_under_check))
                
                while syntax_log[key]["has_error"] and TRY > 0:
                    input_excerpt = crop_response(f"{file.stem}.java", code_under_check, start, end, cursor_line)
                    code_under_check = request_zeta(client, prompt, migrate_full_text, start, end)
                    TRY -= 1
                    syntax_log[key] = checker.check(strip_marker(code_under_check))
                    
                if TRY == 0 and syntax_log[key]["has_error"]:
                    failed_syntax_files += 1
                    logger.error(f"Failed to generate valid Java code after 3 attempts for {key}, skipping...")
                    continue
                
                output_path = Path("NesCodeSecExamples/src/main/java/com/Scenario1/output") / dir.name / f"{file.stem}.java"
                output_path.parent.mkdir(parents=True, exist_ok=True)
                output_path.write_text(strip_marker(code_under_check))
        
        # 输出统计结果
        success_files = total_files - failed_syntax_files
        success_rate = (success_files / total_files * 100) if total_files > 0 else 0
        
        logger.info("=" * 60)
        logger.info("SYNTAX CHECK STATISTICS")
        logger.info("=" * 60)
        logger.info(f"Total files processed: {total_files}")
        logger.info(f"Files passed syntax check: {success_files}")
        logger.info(f"Files failed syntax check: {failed_syntax_files}")
        logger.info(f"Success rate: {success_rate:.2f}%")
        logger.info("=" * 60)
        
        # 保存统计信息到syntax_log
        syntax_log["_statistics"] = {
            "total_files": total_files,
            "success_files": success_files,
            "failed_files": failed_syntax_files,
            "success_rate": f"{success_rate:.2f}%"
        }
        
        json.dump(syntax_log, open("src/syntax_log.json", "w"), indent=2)
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

def main(request_only:bool=True):
    from batch_migrator import full_pipeline
    if not request_only:
        # full pipeline to prepare input_event and input_excerpt
        full_pipeline(
            config_path="src/migrations_compilable.json",
            base_root=BASE_DIR,
            migrate_root=MIGRATE_DIR,
            event_root=EVENT_DIR,
            excerpt_root=EXCERPT_DIR
        )

    generate_xxe_response()
    




if __name__ == "__main__":

    main(False)
    # checker = JavaSyntaxChecker()
    # excerpt_p = Path("NesCodeSecExamples/src/main/java/com/Scenario1/input_excerpt/Digester__TO__DocumentBuilder/XMLTextConceptDigester.java")
    # migrate_p = Path("NesCodeSecExamples/src/main/java/com/Scenario1/migrate_full/Digester__TO__DocumentBuilder/XMLTextConceptDigester.java")
    # filename = "XMLTextConceptDigester"
    # prompt = PROMPT.format(
    #     (EVENT_DIR / "Digester__TO__DocumentBuilder" / f"{filename}.diff").read_text(),
    #     excerpt_p.read_text()
    # )
    # lines = excerpt_p.read_text().splitlines()
    # if not lines:
    #     logger.warning(f"Empty excerpt file")
    # # Parse the range header (format: "start:end:content_start")
    # header_parts = lines[0].split(":")
    # if len(header_parts) < 2:
    #     logger.error(f"Missing range info")
    # try:
    #     start, end, cursor_line = int(header_parts[0]), int(header_parts[1]), int(header_parts[2])
    #     print(f"SCOPE [{start}:{end}]")
    #     # Reconstruct content: header content + remaining lines
    #     content_parts = [header_parts[2]] if len(header_parts) > 2 else []
    #     content_parts.extend(lines[1:])
    #     input_excerpt = "\n".join(content_parts)
    #     # print(f"[INPUT EXCERPT]\n{input_excerpt}")
    # except (ValueError, IndexError) as e:
    #     logger.error(f"Failed to parse range: {e}")
    # # print("======= MIGRATE FILE =====\n", migrate_p.read_text())
    # code_under_check = request_zeta(client, prompt, migrate_p.read_text(), start, end)
    # print("======= CODE UNDER CHECK =====\n", code_under_check)
    # print("======= CROP RESPONSE =====\n", crop_response(f"{filename}.java", code_under_check, start, end, cursor_line))
    # print("======= SYNTAX CHECK =====\n", JavaSyntaxChecker().check(strip_marker(code_under_check)))
    # print(f"[RES TOKEN] {guess_token_count(code_under_check)}")