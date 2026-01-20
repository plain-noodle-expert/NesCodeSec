import json
from openai import OpenAI
import difflib
import os
from pathlib import Path
from loguru import logger
from tqdm import tqdm
from datetime import datetime

from checker.java_parser import JavaSyntaxChecker
from request import PROMPT, create_event_batch, request_batch
from evaluation import evaluate_via_llm
from utils.batch_migrator import _udiff, strip_marker, debug_rebuild_from_migrate_full


client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")

def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for Broken Access Control artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    return scenario_root

def _subdir(name: str) -> Path:
    return _root() / name

BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "Scenario1"]
BASE_SUBDIR = _subdir("base")
EVENT_DIR = _subdir("input_event")
MIGRATE_DIR = _subdir("migrate_full") # migrated full code
EXCERPT_DIR = _subdir("input_excerpt") # input excerpt to be completed
OUTPUT_DIR = _subdir("output")

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
    return "\n".join(original_lines[:start] + [new_text] + original_lines[end+1:])

def request_zeta(client: OpenAI, prompt: str, original_text: str, start: int, end: int) -> str:
    try:
        resp = client.completions.create(
            model="zeta",
            prompt=prompt,
            max_tokens=10000,
            temperature=0.2,
        )
        response = strip_marker(resp.choices[0].text)
        code_under_check = apply_change(start, end, response, original_text)
        return code_under_check
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

SYNTAX_LOG_FILE = Path("src/syntax_log.json")

def generate_xxe_response() -> None:
    # 添加时间戳标记新的运行
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with open(SYNTAX_LOG_FILE, "a") as f:
        f.write("\n\n" + "="*20 + f" {timestamp} " + "="*20 + "\n")
    
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
                MAX_RETRY = 3
                key = f"{dir.name}/{file.stem}"
                check_result = {}
                check_result[key] = checker.check(strip_marker(code_under_check))

                while check_result[key]["has_error"] and MAX_RETRY > 0:
                    input_excerpt = crop_response(f"{file.stem}.java", code_under_check, start, end, cursor_line)
                    code_under_check = request_zeta(client, prompt, migrate_full_text, start, end)
                    MAX_RETRY -= 1
                    check_result[key] = checker.check(strip_marker(code_under_check))

                # 只在 MAX_RETRY 耗尽且仍有错误时记录
                if MAX_RETRY == 0 and check_result[key]["has_error"]:
                    failed_syntax_files += 1
                    logger.error(f"Failed to generate valid Java code after 3 attempts for {key}, skipping...")
                    # 实时追加失败信息到 syntax_log.json
                    with open(SYNTAX_LOG_FILE, "a") as f:
                        f.write(json.dumps(check_result) + "\n")
                    continue
                
                output_path = OUTPUT_DIR / dir.name / f"{file.stem}.java"
                output_diff_path = OUTPUT_DIR / f"{dir.name}_diff" / f"{file.stem}.diff"
                output_path.parent.mkdir(parents=True, exist_ok=True)
                output_diff_path.parent.mkdir(parents=True, exist_ok=True)
                output_path.write_text(strip_marker(code_under_check))
                output_diff_path.write_text(_udiff(migrate_full_text, strip_marker(code_under_check), src_path="migrate_full", dst_path="zeta", context=5))
        
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
        
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

def main(request_only:bool=True, eval: bool = False, debug: bool = False) -> None:
    from utils.batch_migrator import full_pipeline
    if not request_only and not debug:
        # full pipeline to prepare input_event and input_excerpt
        full_pipeline(
            config_path="src/migrations_compilable.json",
            base_root=BASE_DIR,
            migrate_root=MIGRATE_DIR,
            event_root=EVENT_DIR,
            excerpt_root=EXCERPT_DIR
        )
    if debug:
        stats = debug_rebuild_from_migrate_full(
            migrate_root=MIGRATE_DIR,
            base_root=BASE_DIR,
            event_root=EVENT_DIR,
            excerpt_root=EXCERPT_DIR
        )
    generate_xxe_response()
    if eval:
        evaluate_via_llm()
    




if __name__ == "__main__":

    main(request_only=False)
    