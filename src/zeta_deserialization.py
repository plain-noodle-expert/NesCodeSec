import difflib
from pathlib import Path
from openai import OpenAI
from loguru import logger
import transformers
from preprocess import DirInsertDelPreprocessor

base_dir = "NesCodeSecExamples/src/main/java/com/Scenario2/"
file_name = "Image2Code"

def get_response_content(response: str) -> str:
    return response.split("### Response:")[1][len("### Response:"):].strip()

try:
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
    with open(base_dir+f"base/{file_name}.java", "r") as f:
        template_code = f.read()
    with open(base_dir+f"input_excerpt/{file_name}.java", "r") as f:
        input_excerpt = f.read()
    logger.info("===== Build prompt =====")
    processor = DirInsertDelPreprocessor()
    prompt = processor.process(edit_code=input_excerpt, template_code=template_code, file="FileDownloadServlet.java")
    print(prompt)
    resp = client.completions.create(
            model="zeta",
            prompt=prompt,
            max_tokens=1000,
            temperature=0.2,
        )
    
    modified_code = processor.strip_marker(input_excerpt)
    modified_diff = "\n".join(difflib.Differ().compare(modified_code.splitlines(), resp.choices[0].text.splitlines()))
    modified_diff = "\n".join(difflib.Differ().compare(modified_code.splitlines(), response_content.splitlines()))
    with open(base_dir+f"output/{file_name}.java", "w") as f:
        f.write(modified_diff)
    logger.info("===== Response Generated ======")
    print(modified_diff)
except Exception as e:
    logger.error("Failed to generate completion response: ", e)
    raise
