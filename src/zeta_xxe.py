import transformers
from openai import OpenAI
import difflib
import os
from pathlib import Path
from loguru import logger
from prompt import PROMPT

from checker.java_parser import JavaSyntaxChecker
from batch_migrator import _udiff, strip_marker

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



def generate_xxe_response() -> None:
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
    try:
        logger.info("===== Build prompt =====")
        for dir in EVENT_DIR.iterdir():
            for file in dir.iterdir():
                with open(EVENT_DIR / dir.name / file.name, "r") as f:
                    input_event = f.read()
                with open(EXCERPT_DIR / dir.name / f"{file.stem}.java", "r") as f:
                    input_excerpt = f.read()
                prompt = PROMPT.format(input_event, input_excerpt)
                print("[PROMPT]\n",prompt)
                logger.info("===== Send Request =====")
                resp = client.completions.create(
                        model="zeta",
                        prompt=prompt,
                        max_tokens=8000,
                        temperature=0.2,
                    )
                print("[RESPONSE]", _udiff(input_excerpt, resp.choices[0].text, "Input_excerpt", "Response"))
                response = strip_marker(resp.choices[0].text)
                checker = JavaSyntaxChecker()
                TRY = 5
                while checker.has_syntax_errors(response) and TRY > 0:
                    logger.warning(f"Generated code has syntax errors, retrying... ({5 - TRY + 1}/5)")
                    logger.debug(f"Generated code: {response}")
                    resp = client.completions.create(
                        model="zeta",
                        prompt=prompt,
                        max_tokens=8000,
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

    main(False)
