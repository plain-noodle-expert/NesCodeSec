#!/usr/bin/env python3
"""
Summarize semgrep_scan_results.txt into per-file and per-migration counts.

Writes a human-readable report (semgrep_summary.txt) by default and prints a
brief recap to stdout. Accepts optional --input/--output flags.
"""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter, defaultdict
from pathlib import Path
from typing import Dict, List


SCRIPT_PATH = Path(__file__).resolve()
OUTPUT_DIR = SCRIPT_PATH.parent
DEFAULT_INPUT = OUTPUT_DIR / "semgrep_scan_results.txt"
DEFAULT_SUMMARY = OUTPUT_DIR / "semgrep_summary.txt"
DEFAULT_JSON = OUTPUT_DIR / "semgrep_summary.json"

LINE_PATTERN = re.compile(
    r"^(?P<path>[^:]+):(?P<line>\d+):(?P<column>\d+):"
    r"(?P<severity>[^:]+):(?P<snippet>.*?):(?P<message>.*)$"
)


def parse_semgrep_results(input_file: Path) -> Dict[str, object]:
    findings: List[dict] = []
    by_file: Counter[str] = Counter()
    by_migration: Counter[str] = Counter()

    if not input_file.exists():
        raise FileNotFoundError(f"Semgrep results file not found: {input_file}")

    for raw_line in input_file.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line:
            continue
        match = LINE_PATTERN.match(line)
        if not match:
            continue
        info = match.groupdict()
        rel_path = info["path"]
        migration = rel_path.split("/", 1)[0] if "/" in rel_path else "UNKNOWN"

        entry = {
            "path": rel_path,
            "line": int(info["line"]),
            "column": int(info["column"]),
            "severity": info["severity"],
            "message": info["message"].strip(),
        }
        findings.append(entry)
        by_file[rel_path] += 1
        by_migration[migration] += 1

    return {
        "findings": findings,
        "by_file": by_file,
        "by_migration": by_migration,
    }


def format_summary(data: Dict[str, object]) -> List[str]:
    findings: List[dict] = data["findings"]  # type: ignore[assignment]
    by_file: Counter[str] = data["by_file"]  # type: ignore[assignment]
    by_migration: Counter[str] = data["by_migration"]  # type: ignore[assignment]

    total_findings = len(findings)
    unique_files = len(by_file)

    lines: List[str] = []
    lines.append("=== Semgrep Scan Summary ===")
    lines.append(f"Total findings: {total_findings}")
    lines.append(f"Unique files flagged: {unique_files}")
    lines.append("")

    lines.append("=== Findings by Migration Directory ===")
    for migration, count in by_migration.most_common():
        lines.append(f"{migration}: {count}")
    if not by_migration:
        lines.append("No findings recorded.")
    lines.append("")

    lines.append("=== Top Files by Finding Count ===")
    for path, count in by_file.most_common(20):
        lines.append(f"{path}: {count}")
    if not by_file:
        lines.append("No files flagged.")
    lines.append("")

    return lines


def write_json_summary(data: Dict[str, object], json_path: Path) -> None:
    payload = {
        "total_findings": len(data["findings"]),  # type: ignore[index]
        "unique_files": len(data["by_file"]),  # type: ignore[index]
        "by_migration": dict(data["by_migration"]),  # type: ignore[index]
        "by_file": dict(data["by_file"]),  # type: ignore[index]
    }
    json_path.write_text(json.dumps(payload, indent=2, ensure_ascii=False), encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Summarize semgrep scan results")
    parser.add_argument("--input", type=Path, default=DEFAULT_INPUT, help="Path to semgrep_scan_results.txt")
    parser.add_argument("--output", type=Path, default=DEFAULT_SUMMARY, help="Path for human-readable summary")
    parser.add_argument(
        "--json-output",
        type=Path,
        default=DEFAULT_JSON,
        help="Optional JSON summary output path",
    )
    parser.add_argument("--no-json", action="store_true", help="Skip writing JSON summary")

    args = parser.parse_args()

    parsed = parse_semgrep_results(args.input)
    summary_lines = format_summary(parsed)
    args.output.write_text("\n".join(summary_lines), encoding="utf-8")
    print("\n".join(summary_lines[:10] + ["..."]))  # brief stdout snippet

    if not args.no_json:
        write_json_summary(parsed, args.json_output)


if __name__ == "__main__":
    main()
