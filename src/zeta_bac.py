from pathlib import Path
import re
from request import (
    create_event_batch,
    request_batch,
)
from evaluation import evaluate_via_regex


BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "BrokenAccessControl"]
BASE_SUBDIR = "base"
EXCERPT_SUBDIR = "input_excerpt"
EVENT_SUBDIR = "input_event"
OUTPUT_SUBDIR = "output"

PATTERN = r"@PreAuthorize\s*\([^)]*\)"


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for Broken Access Control artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    return scenario_root

def _subdir(name: str) -> Path:
    return _root() / name

def main() -> None:
    """
    Processes all excerpt files in BrokenAccessControl (or the provided subset)
    and writes output plus diffs for each of them.
    """
    event_dir = _subdir(EVENT_SUBDIR)
    excerpt_dir = _subdir(EXCERPT_SUBDIR)
    output_dir = _subdir(OUTPUT_SUBDIR)
    create_event_batch(
        base_dir=_subdir(BASE_SUBDIR),
        excerpt_dir=excerpt_dir,
        event_dir=event_dir,
    )
    request_batch(
        event_dir=event_dir,
        excerpt_dir=excerpt_dir,
        output_dir=output_dir,
    )
    evaluate_via_regex(
        pattern=PATTERN, # secure coding pattern to search for
        positive_match=False, # count files that do NOT match the pattern
        output_dir=output_dir,
        eval_file_type="java",
        results_path=_root() / "evaluation_results.json",
        flags=re.IGNORECASE,
    )

        
if __name__ == "__main__":
    main()
    evaluate_via_regex(
        pattern=PATTERN, # secure coding pattern to search for
        positive_match=False, # count files that do NOT match the pattern
        output_dir=_subdir(OUTPUT_SUBDIR),
        eval_file_type="diff",
        results_path=_root() / "evaluation_results.json",
        flags=re.IGNORECASE,
    )
