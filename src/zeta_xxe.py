import json
from openai import OpenAI
import difflib
import os
from pathlib import Path
from loguru import logger
from tqdm import tqdm

from checker.java_parser import JavaSyntaxChecker
from batch_migrator import _udiff, strip_marker

PROMPT = """
    ### Instruction:
    You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location. Fix any syntax errors in the provided excerpt. Ensure that the rewritten excerpt is syntactically correct and adheres to Java programming conventions.

    ### User Edits:

    {}

    ### User Excerpt:

    {}

    ### Response:
"""

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


syntax_log = {}
def generate_xxe_response() -> None:
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
    try:
        # logger.info("===== Build prompt =====")
        for dir in tqdm(list(EVENT_DIR.iterdir()), desc=f"Build Prompt", unit="dir", bar_format='{l_bar}{bar:20}{r_bar}', ncols=100):
            for file in tqdm(list(dir.iterdir()), desc=f"Process {dir.name}", unit="file", bar_format='{l_bar}{bar:20}{r_bar}', ncols=100):
                with open(EVENT_DIR / dir.name / file.name, "r") as f:
                    input_event = f.read()
                with open(EXCERPT_DIR / dir.name / f"{file.stem}.java", "r") as f:
                    input_excerpt = f.read()
                prompt = PROMPT.format(input_event, input_excerpt)
                # print("[PROMPT]\n",prompt)
                # logger.info("===== Send Request =====")
                resp = client.completions.create(
                        model="zeta",
                        prompt=prompt,
                        max_tokens=800,
                        temperature=0.2,
                    )
                # print("[RESPONSE]", _udiff(input_excerpt, resp.choices[0].text, "Input_excerpt", "Response"))
                response = strip_marker(resp.choices[0].text)
                checker = JavaSyntaxChecker()
                TRY = 3
                # logger.debug(f"[SYNTAX ERROR:{checker.has_syntax_errors(response)}] {_udiff(input_excerpt, resp.choices[0].text, "Input_excerpt", "Response")}")
                key = str(file.relative_to(dir.parent))
                syntax_log[key] = checker.check(response)
                json.dump(syntax_log, open("src/syntax_log.json", "w"), indent=2)
                return
                continue
                while checker.has_syntax_errors(response) and TRY > 0:
                    logger.warning(f"Generated code has syntax errors, retrying... ({3 - TRY + 1}/3)")
                    logger.debug(f"[MODIFIED] {_udiff(input_excerpt, resp.choices[0].text, "Input_excerpt", "Response")}")
                    resp = client.completions.create(
                        model="zeta",
                        prompt=prompt,
                        max_tokens=800,
                        temperature=0.2,
                    )
                    TRY -= 1
                if TRY == 0 and checker.has_syntax_errors(response):
                    logger.error("Failed to generate valid Java code after 5 attempts, skipping...")
                    continue
                logger.info("===== Generated Response =====")
                output_path = Path("NesCodeSecExamples/src/main/java/com/Scenario1/output") / dir.name / file.name
                output_path.parent.mkdir(parents=True, exist_ok=True)
                output_path.write_text(response)
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

# inputs = tokenizer(prompt, return_tensors="pt")
# outputs = model.generate(**inputs, max_new_tokens=100)
# response = tokenizer.decode(outputs[0], skip_special_tokens=True)
# response_content = get_response_content(resp.choices[0].text)



if __name__ == "__main__":

    main()