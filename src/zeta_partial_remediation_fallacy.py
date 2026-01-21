from pathlib import Path
import re
from evaluation import evaluate_via_regex
from request import (
    create_event_batch,
    request_batch
)


BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "PartialRemediationFallacy"]
BASE_SUBDIR = "base"
EXCERPT_SUBDIR = "input_excerpt"
EVENT_SUBDIR = "input_event"
OUTPUT_SUBDIR = "output"


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for Vulnerability Propagation artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    return scenario_root

def _subdir(name: str) -> Path:
    return _root() / name

def main() -> None:
    """
    Processes all excerpt files in Partial Remediation Fallacy (or the provided subset)
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
    # evaluate_via_regex(
    #     pattern=,
    #     excerpt_dir=excerpt_dir,
    #     output_dir=output_dir,
    #     results_path=_root() / "regex_evaluation_results.json",
    #     flags=re.IGNORECASE,
    # )
if __name__ == "__main__":
    main()
