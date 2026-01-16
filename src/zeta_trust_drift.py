import difflib
from pathlib import Path
from tqdm import tqdm
from request import (
    PROMPT,
    build_prompt,
    create_event_batch,
    send_request,
)


BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "TrustDrift"]
BASE_SUBDIR = "base"
EXCERPT_SUBDIR = "input_excerpt"
EVENT_SUBDIR = "input_event"
OUTPUT_SUBDIR = "output"


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for Trust Drift artifacts.
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


def _prompt_template() -> str:
    if not PROMPT:
        raise RuntimeError("ZETA_PROMPT_TEMPLATE environment variable is not set.")
    return PROMPT

def request(event_file: Path, excerpt_file: Path, model: str = "zeta", max_tokens: int = 28000, temperature: float = 0.2) -> str:
    """
    Sends assembled Scenario8 prompts to a vLLM-hosted model and returns the responses.
    """
    prompt = build_prompt(
        event_file,
        excerpt_file,
        template=_prompt_template(),
    )
    return send_request(
        prompt,
        model=model,
        max_tokens=max_tokens,
        temperature=temperature,
    )


def _create_event_batch():
    
    base_dir = _subdir(BASE_SUBDIR)
    excerpt_dir = _subdir(EXCERPT_SUBDIR)
    event_dir = _subdir(EVENT_SUBDIR)
    create_event_batch(base_dir, excerpt_dir, event_dir)

def main() -> None:
    """
    Processes all excerpt files in TrustDrift (or the provided subset)
    and writes output plus diffs for each of them.
    """
    _create_event_batch()
    event_dir = _subdir(EVENT_SUBDIR)
    excerpt_dir = _subdir(EXCERPT_SUBDIR)
    output_dir = _subdir(OUTPUT_SUBDIR)
    for event_file in tqdm(list(event_dir.glob("*.diff")), desc="Requesting completions for Trust Drift"):
        excerpt_file = excerpt_dir / event_file.with_suffix(".java").name
        if not excerpt_file.is_file():
            raise FileNotFoundError(f"Missing excerpt for {event_file.name}: {excerpt_file}")
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
    main()
