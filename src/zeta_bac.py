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

def _root(secure: bool=True) -> Path:
    """
    Returns the base directory for artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    variant_root = scenario_root / ("secure" if secure else "insecure")
    return variant_root if variant_root.is_dir() else scenario_root

def _subdir(name: str, secure: bool=True) -> Path:
    return _root(secure=secure) / name

def main() -> None:
    """
    Processes all excerpt files in BrokenAccessControl (or the provided subset)
    and writes output plus diffs for each of them.
    """
    for secure in [True, False]:
        event_dir = _subdir(EVENT_SUBDIR, secure=secure)
        excerpt_dir = _subdir(EXCERPT_SUBDIR, secure=secure)
        output_dir = _subdir(OUTPUT_SUBDIR, secure=secure)
        create_event_batch(
            base_dir=_subdir(BASE_SUBDIR, secure=secure),
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
            excerpt_dir=excerpt_dir,
            results_path=_root(secure=secure) / "evaluation_results.json",
            flags=re.IGNORECASE,
        )

        
if __name__ == "__main__":
    # main()
    for secure in [False]:
        event_dir = _subdir(EVENT_SUBDIR, secure=secure)
        excerpt_dir = _subdir(EXCERPT_SUBDIR, secure=secure)
        output_dir = _subdir(OUTPUT_SUBDIR, secure=secure)
        create_event_batch(
            base_dir=_subdir(BASE_SUBDIR, secure=secure),
            excerpt_dir=excerpt_dir,
            event_dir=event_dir,
        )
        # evaluate_via_regex(
        #     pattern=PATTERN, # secure coding pattern to search for
        #     positive_match=False, # count files that do NOT match the pattern
        #     output_dir=_subdir(OUTPUT_SUBDIR),
        #     excerpt_dir=_subdir(EXCERPT_SUBDIR),
        #     results_path=_root() / "evaluation_results.json",
        #     flags=re.IGNORECASE,
        # )
