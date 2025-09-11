import transformers
from openai import OpenAI
import difflib
import os
from pathlib import Path
from loguru import logger
from prompt import PROMPT

from checker.java_parser import JavaSyntaxChecker


# model = transformers.AutoModelForCausalLM.from_pretrained("zed-industries/zeta")
# tokenizer = transformers.AutoTokenizer.from_pretrained("zed-industries/zeta")

def get_response_content(response: str) -> str:
    return response.split("### Response:")[1][len("### Response:"):].strip()



def generate_xxe_response() -> None:
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
    try:
        logger.info("===== Build prompt =====")
        # event, input = prepare_XXE_input(base_file, xml_factory, comment)
        event_dir = Path("NesCodeSecExamples/src/main/java/com/Scenario1/input_event")
        excerpt_dir = Path("NesCodeSecExamples/src/main/java/com/Scenario1/input_excerpt")
        for dir in event_dir.iterdir():
            for file in dir.iterdir():
                with open(event_dir / dir.name / file.name, "r") as f:
                    input_event = f.read()
                with open(excerpt_dir / dir.name / file.stem, "r") as f:
                    input_excerpt = f.read()
                prompt = PROMPT.format(input_event, input_excerpt)
                logger.info("===== Send Request =====")
                resp = client.completions.create(
                        model="zeta",
                        prompt=prompt,
                        max_tokens=500,
                        temperature=0.2,
                    )
                checker = JavaSyntaxChecker()
                TRY = 5
                while checker.has_syntax_errors(resp.choices[0].text) and TRY > 0:
                    logger.warning(f"Generated code has syntax errors, retrying... ({5 - TRY + 1}/5)")
                    resp = client.completions.create(
                        model="zeta",
                        prompt=prompt,
                        max_tokens=500,
                        temperature=0.2,
                    )
                    TRY -= 1
                if TRY == 0 and checker.has_syntax_errors(resp.choices[0].text):
                    logger.error("Failed to generate valid Java code after 5 attempts, skipping...")
                    continue
                logger.info("===== Generated Response =====")
                output_path = Path("NesCodeSecExamples/src/main/java/com/Scenario1/output") / dir.name / file.name
                output_path.parent.mkdir(parents=True, exist_ok=True)
                output_path.write_text(resp.choices[0].text)
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

def main():
    from batch_migrator import run_batch_migrate, diff_pairs_against_base

    processed, changed = run_batch_migrate(
        config_path="src/migrations_compilable.json",
        base_root="NesCodeSecExamples/src/main/java/com/Scenario1/base",
        output_root="NesCodeSecExamples/src/main/java/com/Scenario1/output",
        dry_run=False,                     # 先 dry-run=True 预览
        pair_dirs=True                     # 生成 <Src>__TO__<Dst> 目录结构
    )

    # 生成完直接做 diff（对所有 pair 或指定某些 pair）
    diff_pairs_against_base(
        base_root="NesCodeSecExamples/src/main/java/com/Scenario1/base",
        event_root="NesCodeSecExamples/src/main/java/com/Scenario1/input_excerpt",
        diff_output_root="NesCodeSecExamples/src/main/java/com/Scenario1/input_event"
    )
    
    generate_xxe_response()

# inputs = tokenizer(prompt, return_tensors="pt")
# outputs = model.generate(**inputs, max_new_tokens=100)
# response = tokenizer.decode(outputs[0], skip_special_tokens=True)
# response_content = get_response_content(resp.choices[0].text)



if __name__ == "__main__":

    main()
