import os
from pathlib import Path
import difflib
import re
from tqdm import tqdm
from typing import Mapping, Optional
from loguru import logger
from dotenv import load_dotenv
from colorama import init, Fore, Style

# Load environment variables from .env file
load_dotenv()

try:
    from openai import OpenAI
except ModuleNotFoundError:  # pragma: no cover - optional dependency
    OpenAI = None  # type: ignore

PROMPT = os.getenv("ZETA_PROMPT_TEMPLATE", "")

if OpenAI is None:
        raise RuntimeError(
            "openai package not available. Install the dependency to issue completion requests."
        )

client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")

def create_diff(
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

def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")

def remove_mark(file: Path) -> str:
    if (not file.is_file()):
        raise FileNotFoundError(f"Required input file {file.name} is missing.")
    content = file.read_text(encoding="utf-8")
    content = re.sub(r"```", "", content)
    content = re.sub(r"<\|user_cursor_is_here\|>", "", content)
    content = re.sub(r"<\|start_of_file\|>", "", content)
    content = re.sub(r"<\|end_of_file\|>", "", content)
    content = re.sub(r"<\|editable_region_start\|>", "", content)
    content = re.sub(r"<\|editable_region_end\|>", "", content)
    content = re.sub(r"<\|current_file_content\|>", "", content)
    content = re.sub(r"<\|/current_file_content\|>", "", content)
    content = re.sub(r"<\|recently_viewed_code_snippet\|>", "", content)
    content = re.sub(r"<\|/recently_viewed_code_snippet\|>", "", content)
    content = re.sub(r"<\|recently_viewed_code_snippets\|>", "", content)
    content = re.sub(r"<\|/recently_viewed_code_snippets\|>", "", content)
    return content

def create_event(base_file: Path, excerpt_file: Path, event_file: Path):
    original_code = base_file.read_text(encoding="utf-8")
    original_code = remove_mark(base_file)
    modified_code = remove_mark(excerpt_file)
    event = create_diff(
        original_code,
        modified_code,
        orig_label=base_file.name,
        modified_label=base_file.name
        )
    write_text(event_file, event)

def create_event_batch(base_dir: Path,
                       excerpt_dir: Path,
                       event_dir: Path):
    for base_file in tqdm(list(base_dir.glob("*.java")), desc=f"Creating events for {base_dir.parent.parent.name}/{base_dir.parent.name}"):
        excerpt_file = excerpt_dir / base_file.name
        event_file = event_dir / base_file.with_suffix(".diff").name
        create_event(base_file, excerpt_file, event_file)

def create_event_batches(base_dir: Path,
                         excerpt_dir: Path,
                         event_dir: Path):
    """Create events for director with subdirectories.

    Args:
        base_dir (Path): path to base director
        excerpt_dir (Path): path to user edit excerpts
        event_dir (Path): path to output event diffs
    """
    for subdir in base_dir.iterdir():
        create_event_batch(
            base_dir=subdir,
            excerpt_dir=excerpt_dir / subdir.name,
            event_dir=event_dir / subdir.name,
        )

def build_prompt(
    event_file: Path,
    excerpt_file: Path,
    *,
    template: str = PROMPT,
    extra_sections: Optional[Mapping[str, str]] = None,
) -> str:
    """
    Assemble a completion prompt from the diff (event) and excerpt files.
    Allows callers to pass additional template fields through extra_sections.
    """
    if (not event_file.is_file()) or (not excerpt_file.is_file()):
        raise FileNotFoundError("Required input files are missing.")

    sections: dict[str, str] = {
        "user_edits": event_file.read_text(encoding="utf-8"),
        "user_excerpt": excerpt_file.read_text(encoding="utf-8"),
    }
    extra_sections_not_in_template = {}
    if extra_sections:
        # print(f"{Fore.YELLOW}Extra sections: {extra_sections}{Style.RESET_ALL}")
        for key, value in extra_sections.items():
            if "{" + key + "}" in template:
                sections[key] = value
            else:
                extra_sections_not_in_template[key] = value

    prompt = template.format(**sections)
    
    # Insert extra sections that are not in the template before ### Response
    if extra_sections_not_in_template:
        extra_content = ""
        for key, value in extra_sections_not_in_template.items():
            extra_content += f"\n### {key.replace('_', ' ').title()}:\n\n{value}\n"
        
        # Find the position to insert extra sections (before ### Response)
        resp_marker = "### Response:"
        prompt = prompt.replace(resp_marker, extra_content + resp_marker)
    
    return prompt

def send_request(
    prompt: str,
    *,
    model: str = "zeta",
    max_tokens: int = 28000,
    temperature: float = 0.2,
) -> str:
    """
    Submit a prompt to the configured completion endpoint and return the text body.
    """
    response = client.completions.create(
        model=model,
        prompt=prompt,
        max_tokens=max_tokens,
        temperature=temperature,
    )
    if not response.choices:
        return ""
    choice = response.choices[0]
    return getattr(choice, "text", None) or ""


def request_batch(
    event_dir: Path,
    excerpt_dir: Path,
    output_dir: Path,
    *,
    template: str = PROMPT,
    model: str = "zeta",
    max_tokens: int = 28000,
    temperature: float = 0.2,
    extra_sections: Optional[Mapping[str, str]] = None,
) -> None:
    """
    High-level helper that builds the prompt from files and sends the completion request.
    """
    for event_file, excerpt_file in tqdm(zip(sorted(list(event_dir.glob("*.diff"))), sorted(list(excerpt_dir.glob("*.java")))), total=len(list(event_dir.glob("*"))), desc=f"Requesting batch for {event_dir.parent.parent.name}/{event_dir.parent.name}"):
        prompt = build_prompt(
            event_file,
            excerpt_file,
            template=template,
            extra_sections=extra_sections,
        )
        result = send_request(
            prompt,
            model=model,
            max_tokens=max_tokens,
            temperature=temperature,
        )
        result = merge_response_into_excerpt(
            excerpt_file.read_text(encoding="utf-8"),
            result,
        )
        write_text(
            output_dir / excerpt_file.name,
            result,
        )
        write_text(
            output_dir / event_file.name,
            create_diff(
                excerpt_file.read_text(encoding="utf-8"),
                result,
                orig_label=excerpt_file.name,
                modified_label="NES",
                context=5,
            ),
        )

def request_batches(
    event_dir: Path,
    excerpt_dir: Path,
    output_dir: Path,
    *,
    template: str = PROMPT,
    model: str = "zeta",
    max_tokens: int = 28000,
    temperature: float = 0.2,
    extra_sections: Optional[Mapping[str, str]] = None,
) -> None:
    """
    High-level helper that processes all subdirectories in the provided event and excerpt dirs.
    """
    for subdir in event_dir.iterdir():
        for event_file in subdir.glob("*.diff"):
            excerpt_file = excerpt_dir / subdir.name / event_file.with_suffix(".java").name
            if not excerpt_file.is_file():
                raise FileNotFoundError(f"Excerpt file not found: {excerpt_file}")
            request_batch(
                event_dir=subdir,
                excerpt_dir=excerpt_dir / subdir.name,
                output_dir=output_dir / subdir.name,
                template=template,
                model=model,
                max_tokens=max_tokens,
                temperature=temperature,
                extra_sections=extra_sections,
            )


def merge_response_into_excerpt(excerpt_content: str, response_content: str) -> str:
    """
    Apply the model's edits onto the original excerpt content and return the merged text.

    This function computes a line-based diff between the excerpt and the model response,
    then replaces only the modified regions with the model's version. Lines that the
    model did not touch remain identical to the original excerpt.
    
    If excerpt has more lines than response, the extra lines are preserved.
    """
    excerpt_lines = excerpt_content.splitlines(keepends=True)
    response_lines = response_content.splitlines(keepends=True)

    matcher = difflib.SequenceMatcher(a=excerpt_lines, b=response_lines)
    merged: list[str] = []
    last_excerpt_idx = 0
    
    for tag, i1, i2, j1, j2 in matcher.get_opcodes():
        if tag == "equal":
            merged.extend(excerpt_lines[i1:i2])
        else:
            merged.extend(response_lines[j1:j2])
        last_excerpt_idx = i2
    
    # Append any remaining lines from excerpt that weren't covered by the diff
    if last_excerpt_idx < len(excerpt_lines):
        
        merged.extend(excerpt_lines[last_excerpt_idx:])
    
    return "".join(merged)


__all__ = [
    "PROMPT",
    "build_prompt",
    "send_request",
    "request_batch",
    "request_batches",
    "merge_response_into_excerpt",
]