import transformers
from openai import OpenAI
import difflib
import os
from pathlib import Path
from loguru import logger
from prepare_input import prepare_XXE_input


# model = transformers.AutoModelForCausalLM.from_pretrained("zed-industries/zeta")
# tokenizer = transformers.AutoTokenizer.from_pretrained("zed-industries/zeta")

def get_response_content(response: str) -> str:
    return response.split("### Response:")[1][len("### Response:"):].strip()



def generate_response(base_file:str, xml_factory:str, comment:str) -> None:
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
    prompt = """
        ### Instruction:
        You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location.

        ### User Edits:

        {}

        ### User Excerpt:

        {}

        ### Response:

    """
    try:
        logger.info("===== Build prompt =====")
        event, input = prepare_XXE_input(base_file, xml_factory, comment)
        prompt = prompt.format(event, input)
        logger.info("===== Send Request =====")
        resp = client.completions.create(
            model="zeta",
            prompt=prompt,
            max_tokens=500,
            temperature=0.2,
        )
        base = Path(base_file)
        output = f"NesCodeSecExamples/src/main/java/com/XXE/output/{base.stem}To{xml_factory}"
        diff = difflib.Differ().compare(input.splitlines(), resp.choices[0].text.splitlines())
        logger.info("===== Response Generated ======")
        resp_diff = "\n".join(diff)
        logger.debug(resp_diff)
        with open(output, "w") as f:
            f.write(resp_diff)

    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise
    



# inputs = tokenizer(prompt, return_tensors="pt")
# outputs = model.generate(**inputs, max_new_tokens=100)
# response = tokenizer.decode(outputs[0], skip_special_tokens=True)
# response_content = get_response_content(resp.choices[0].text)



if __name__ == "__main__":
    base_dir = "NesCodeSecExamples/src/main/java/com/XXE/base"
    base_files = ["Digester.java", "InputFactory.java", "DocumentBuilder.java", "SAXParserFactory.java", "SAXBuilder.java", "SAXReader.java"]
    xml_factories = ["Digester", "XMLInputFactory", "DocumentBuilderFactory", "SAXParserFactory", "SAXBuilder", "SAXReader"]
    for i, base_file in enumerate(base_files):
        for j, xml in enumerate(xml_factories):
            if i==j:
                continue
            generate_response(os.path.join(base_dir, base_file), xml_factory=xml, comment=f"Disgard {base_file.split('.')[0]} and use {xml} to parse XML")