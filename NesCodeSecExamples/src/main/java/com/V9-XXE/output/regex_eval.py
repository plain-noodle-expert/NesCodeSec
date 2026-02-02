#!/usr/bin/env python3
"""
Convenience wrapper to run the regex-based parser security scan from zeta_xxe.py.
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

REPO_ROOT = _find_repo_root(SCRIPT_PATH)
SRC_DIR = REPO_ROOT / "src"
if str(SRC_DIR) not in sys.path:
    sys.path.insert(0, str(SRC_DIR))

from zeta_xxe import run_regex_security_scan  # noqa: E402


def main(scan_root: Path, out_file: Path | None) -> None:
    run_regex_security_scan(scan_root=scan_root, output_file=out_file)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Regex-based XML parser security scan helper")
    parser.add_argument(
        "--root",
        type=Path,
        default=OUTPUT_DIR,
        help="Root directory to scan (defaults to this script's directory)",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="Optional output file path; defaults to <root>/scan_results_auto.txt",
    )
    args = parser.parse_args()
    main(args.root, args.output)
