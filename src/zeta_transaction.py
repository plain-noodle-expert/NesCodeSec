import difflib
import os
from pathlib import Path

from openai import OpenAI
from tqdm import tqdm


BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "Scenario3"]
BASE_SUBDIR = "base"
EXCERPT_SUBDIR = "input_excerpt"
EVENT_SUBDIR = "input_event"
HISTORY_SUBDIR = "history"
OUTPUT_SUBDIR = "output"

def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for Scenario8 artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    return scenario_root

def _subdir(name: str) -> Path:
    return _root() / name


def _create_diff(
    original: str,
    modified: str,
    orig_label: str,
    modified_label: str,
    context: int = 0,
) -> str:
    diff = difflib.unified_diff(
        original.splitlines(keepends=True),
        modified.splitlines(keepends=True),
        fromfile=orig_label,
        tofile=modified_label,
        n=context,
    )
    return "".join(diff)

def _write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


PROMPT_TEMPLATE = """### Instruction:
You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location.
Fix any syntax errors in the provided excerpt. Ensure that the rewritten excerpt is syntactically correct and adheres to Java programming conventions. Ensure the completeness of the code within the provided excerpt.

### User Edits:

{user_edits}

### User Excerpt:

{user_excerpt}

### Response:
"""

def request(name: str, model: str = "zeta", max_tokens: int = 18000, temperature: float = 0.2) -> str:
    """
    Sends assembled Scenario8 prompts to a vLLM-hosted model and returns the responses.
    """
    base_url = os.environ.get("ZETA_BASE_URL", os.environ.get("OPENAI_BASE_URL", "http://localhost:8000/v1"))
    client = OpenAI(base_url=base_url, api_key="EMPTY")
    event_root = _subdir(EVENT_SUBDIR)
    excerpt_root = _subdir(EXCERPT_SUBDIR)

    for directory in (event_root, excerpt_root):
        if not directory.is_dir():
            raise FileNotFoundError(f"Required directory missing: {directory}")

    event_diff_path = event_root / (name + ".diff")
    excerpt_path = excerpt_root / name
    user_edits = event_diff_path.read_text(encoding="utf-8")
    user_excerpt = excerpt_path.read_text(encoding="utf-8")

    prompt = PROMPT_TEMPLATE.format(
        user_edits=user_edits,
        user_excerpt=user_excerpt,
    )

    print(f"===== PROMPT ======\n{prompt}\n===================")
    result = client.completions.create(
        model=model,
        prompt=prompt,
        max_tokens=max_tokens,
        temperature=temperature,
    )
    return result.choices[0].text


def main(base_file: Path, excerpt_file: Path, event_file: Path):
    original_code = base_file.read_text(encoding="utf-8")
    modified_code = excerpt_file.read_text(encoding="utf-8")
    event = _create_diff(
        original_code,
        modified_code,
        orig_label=base_file.name,
        modified_label=base_file.name
        )
    event_file.write_text(event, encoding="utf-8")
        
if __name__ == "__main__":
    name = "Endpoint.java"
    base_file = _subdir(BASE_SUBDIR) / name
    excerpt_file = _subdir(EXCERPT_SUBDIR) / name
    event_file = _subdir(EVENT_SUBDIR) / (name + ".diff")
    
    main(base_file, excerpt_file, event_file)
    # resp = request(name)
    
    # output_root = _subdir(OUTPUT_SUBDIR)
    # output_dir = output_root
    # output_path = output_dir / name
    # output_diff = _create_diff(
    #     orig_label="excerpt",
    #     modified_label="response",
    #     original=excerpt_file.read_text(encoding="utf-8"),
    #     modified=resp,
    # )
    # _write_text(output_path, output_diff)