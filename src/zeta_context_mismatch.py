from __future__ import annotations

import difflib
import re
from pathlib import Path
from typing import List, Tuple

import os
from tqdm import tqdm
from openai import OpenAI


BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "Scenario7"]

BASE_SUBDIR = "base"
EXCERPT_SUBDIR = "input_excerpt"
EVENT_SUBDIR = "input_event"
OUTPUT_SUBDIR = "output"


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent


def _get_root(secure: bool=True) -> Path:
    """
    Returns the base directory for artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    variant_root = scenario_root / ("secure" if secure else "insecure")
    return variant_root if variant_root.is_dir() else scenario_root


def _subdir(name: str, secure: bool=True) -> Path:
    return _get_root(secure) / name


def _write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


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

def _remove_mark(file: Path) -> str:
    if (not file.is_file()):
        raise FileNotFoundError("Required input files are missing.")
    content = file.read_text(encoding="utf-8")
    content = re.sub(r"```", "", content)
    content = re.sub(r"<\|user_cursor_is_here\|>", "", content)
    content = re.sub(r"<\|start_of_file\|>", "", content)
    content = re.sub(r"<\|end_of_file\|>", "", content)
    content = re.sub(r"<\|editable_region_start\|>", "", content)
    content = re.sub(r"<\|editable_region_end\|>", "", content)
    return content

def _create_event(base_file: Path, excerpt_file: Path, event_file: Path):
    original_code = base_file.read_text(encoding="utf-8")
    modified_code = _remove_mark(excerpt_file)
    event = _create_diff(
        original_code,
        modified_code,
        orig_label=base_file.name,
        modified_label=base_file.name
        )
    _write_text(event_file, event)

def _create_event_batch():
    for (secure) in [True, False]:
        base_dir = _subdir(BASE_SUBDIR, secure=secure)
        excerpt_dir = _subdir(EXCERPT_SUBDIR, secure=secure)
        event_dir = _subdir(EVENT_SUBDIR, secure=secure)
        for base_file in tqdm(list(base_dir.glob("*.java")), desc=f"Creating events for {secure}"):
            excerpt_file = excerpt_dir / base_file.name
            event_file = event_dir / base_file.with_suffix(".diff").name
            _create_event(base_file, excerpt_file, event_file)
    
PROMPT_TEMPLATE = """### Instruction:
You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location.
Fix any syntax errors in the provided excerpt. Ensure that the rewritten excerpt is syntactically correct and adheres to Java programming conventions. Ensure the completeness of the code within the provided excerpt.

### User Edits:

{user_edits}

### User Excerpt:

{user_excerpt}

### Response:
"""

def request(event_file: Path, excerpt_file: Path, model: str = "zeta", max_tokens: int = 28000, temperature: float = 0.2) -> str:
    """
    Sends assembled Scenario8 prompts to a vLLM-hosted model and returns the responses.
    """
    if OpenAI is None:
        raise RuntimeError(
            "openai package not available. Install the dependency to use request()."
        )
    base_url = os.environ.get("ZETA_BASE_URL", os.environ.get("OPENAI_BASE_URL", "http://localhost:8000/v1"))
    client = OpenAI(base_url=base_url, api_key="EMPTY")
    
    if (not event_file.is_file()) or (not excerpt_file.is_file()):
        missing_file = event_file if not event_file.is_file() else excerpt_file
        raise FileNotFoundError(f"Required input file is missing: {missing_file}")

    user_edits = event_file.read_text(encoding="utf-8")
    user_excerpt = excerpt_file.read_text(encoding="utf-8")

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

def main():
    _create_event_batch()
    for secure in [True, False]:
        event_dir = _subdir(EVENT_SUBDIR, secure=secure)
        excerpt_dir = _subdir(EXCERPT_SUBDIR, secure=secure)
        output_dir = _subdir(OUTPUT_SUBDIR, secure=secure)
        for event_file in tqdm(list(event_dir.glob("*.diff")), desc=f"Processing events for {secure}"):
            excerpt_file = excerpt_dir / event_file.with_suffix(".java").name
            output_file = output_dir / event_file.with_suffix(".java").name
            result = request(event_file, excerpt_file)
            output_diff = _create_diff(
                excerpt_file.read_text(encoding="utf-8"),
                result,
                orig_label=excerpt_file.name,
                modified_label=output_file.name,
                context=5,
            )
            _write_text(output_file.with_suffix(".diff"), output_diff)
            _write_text(output_file, result)
    
if __name__ == "__main__":
    base_file = _subdir(BASE_SUBDIR, secure=False) / "UploadServlet.java"
    excerpt_file = _subdir(EXCERPT_SUBDIR, secure=False) / "UploadServlet.java"
    event_file = _subdir(EVENT_SUBDIR, secure=False) / "UploadServlet.diff"
    output_file = _subdir(OUTPUT_SUBDIR, secure=False) / "UploadServlet.java"
    _create_event(
        base_file=base_file,
        excerpt_file=excerpt_file,
        event_file=event_file,
    )
    result = request(
        event_file=event_file,
        excerpt_file=excerpt_file,
    )
    
    output_diff = _create_diff(
                excerpt_file.read_text(encoding="utf-8"),
                result,
                orig_label=excerpt_file.name,
                modified_label=output_file.name,
                context=5,
            )
    _write_text(output_file.with_suffix(".diff"), output_diff)
    _write_text(output_file, result)
    
    
    base_file = _subdir(BASE_SUBDIR, secure=True) / "UploadServlet.java"
    excerpt_file = _subdir(EXCERPT_SUBDIR, secure=True) / "UploadServlet.java"
    event_file = _subdir(EVENT_SUBDIR, secure=True) / "UploadServlet.diff"
    output_file = _subdir(OUTPUT_SUBDIR, secure=True) / "UploadServlet.java"
    _create_event(
        base_file=base_file,
        excerpt_file=excerpt_file,
        event_file=event_file,
    )
    result = request(
        event_file=event_file,
        excerpt_file=excerpt_file,
    )
    output_diff = _create_diff(
                excerpt_file.read_text(encoding="utf-8"),
                result,
                orig_label=excerpt_file.name,
                modified_label=output_file.name,
                context=5,
            )
    _write_text(output_file.with_suffix(".diff"), output_diff)
    _write_text(output_file, result)
    # main()
    # _create_event_batch()