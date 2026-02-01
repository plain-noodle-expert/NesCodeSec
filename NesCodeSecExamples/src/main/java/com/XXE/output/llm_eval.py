#!/usr/bin/env python3
"""
Standalone LLM evaluator for XXE migrations.

Mirrors the functionality exposed in src/zeta_xxe.py but can be run directly
from the XXE/output directory, similar to regex_scan.py. Use --run to trigger
fresh LLM judging, --summary to recompute aggregation from cached results.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path


def _find_repo_root(start: Path) -> Path:
    for candidate in [start] + list(start.parents):
        if (candidate / "pyproject.toml").exists():
            return candidate
    raise RuntimeError(f"Unable to locate repository root from path {start}")


SCRIPT_PATH = Path(__file__).resolve()
OUTPUT_DIR = SCRIPT_PATH.parent
XXE_DIR = OUTPUT_DIR.parent

REPO_ROOT = _find_repo_root(SCRIPT_PATH)
SRC_DIR = REPO_ROOT / "src"
if str(SRC_DIR) not in sys.path:
    sys.path.insert(0, str(SRC_DIR))

from zeta_xxe import evaluate_via_llm_xxe, summarize_llm_results_from_disk

LLM_EVAL_DIR = XXE_DIR / "llm_evaluation"


def main(run_eval: bool, summary_only: bool) -> None:
    if run_eval:
        evaluate_via_llm_xxe(OUTPUT_DIR, LLM_EVAL_DIR)
    if summary_only:
        summarize_llm_results_from_disk(OUTPUT_DIR)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="LLM evaluation helper for XXE migrations")
    parser.add_argument(
        "--run",
        action="store_true",
        help="Execute LLM judgers for every migration directory and update cached results",
    )
    parser.add_argument(
        "--summary",
        action="store_true",
        help="Recompute aggregated statistics from cached llm_evaluation_results.json files only",
    )
    args = parser.parse_args()

    # Default to summary-only if no explicit action is provided.
    if not args.run and not args.summary:
        args.summary = True

    main(args.run, args.summary)
