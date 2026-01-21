import difflib
from pathlib import Path
from tqdm import tqdm
from request import (
    PROMPT,
    create_diff,
    write_text,
    build_prompt,
    create_event_batch,
    send_request,
)
from evaluation import evaluate_via_regex


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

def _prompt_template() -> str:
    if not PROMPT:
        raise RuntimeError("ZETA_PROMPT_TEMPLATE environment variable is not set.")
    return PROMPT

def request(event_file: Path, excerpt_file: Path, model: str = "zeta", max_tokens: int = 28000, temperature: float = 0.2) -> str:
    """
    Sends single assembled prompt to a vLLM-hosted model and returns the responses.
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

def sequential_request(
    base_dir: Path,
    event_dir: Path,
    excerpt_dir: Path,
    output_dir: Path,
    times: int=3,
    model: str = "zeta",
    max_tokens: int = 8000,
    temperature: float = 0.2) -> None:
    """
    Execute repeated completion requests where each subsequent request uses
    the previous response as the new excerpt input.

    Results are saved with suffixes indicating the request iteration
    (e.g., Foo_request_1.java, Foo_request_1.diff, ...).
    """
    if times < 1:
        raise ValueError("times must be >= 1")
    
    for event_file in tqdm(sorted(event_dir.glob("*.diff")), desc="Sequential requests for Trust Drift"):
        excerpt_file = excerpt_dir / event_file.with_suffix(".java").name
        base_file = base_dir / event_file.with_suffix(".java").name
        if not excerpt_file.is_file():
            raise FileNotFoundError(f"Missing excerpt for {event_file.name}: {excerpt_file}")
        if not base_file.is_file():
            raise FileNotFoundError(f"Missing base source for {event_file.name}: {base_file}")

        base_content = base_file.read_text(encoding="utf-8")
        current_excerpt_path = excerpt_file
        current_event_path = event_file

        for iteration in range(1, times + 1):
            previous_content = current_excerpt_path.read_text(encoding="utf-8")
            result = request(
                current_event_path,
                current_excerpt_path,
                model=model,
                max_tokens=max_tokens,
                temperature=temperature,
            )

            suffix = f"_{iteration}_request"
            output_java = output_dir / f"{excerpt_file.stem}{suffix}.java"
            output_diff = output_dir / f"{excerpt_file.stem}{suffix}.diff"

            write_text(output_java, result)
            diff_text = create_diff(
                previous_content,
                result,
                orig_label=current_excerpt_path.name,
                modified_label=output_java.name,
                context=5,
            )
            write_text(output_diff, diff_text)

            if iteration < times:
                updated_event = create_diff(
                    base_content,
                    result,
                    orig_label=base_file.name,
                    modified_label=output_java.name,
                    context=5,
                )
                next_event_path = output_dir / f"{excerpt_file.stem}{suffix}.event.diff"
                write_text(next_event_path, updated_event)
                current_event_path = next_event_path
                current_excerpt_path = output_java

def main() -> None:
    """
    Processes all excerpt files in TrustDrift (or the provided subset)
    and writes output plus diffs for each of them.
    """
    base_dir = _subdir(BASE_SUBDIR)
    event_dir = _subdir(EVENT_SUBDIR)
    excerpt_dir = _subdir(EXCERPT_SUBDIR)
    output_dir = _subdir(OUTPUT_SUBDIR)
    create_event_batch(
        base_dir=base_dir,
        excerpt_dir=excerpt_dir,
        event_dir=event_dir,
    )
    sequential_request(
        base_dir=base_dir,
        event_dir=event_dir,
        excerpt_dir=excerpt_dir,
        output_dir=output_dir,
        times=3,
    )
        
if __name__ == "__main__":
    main()
