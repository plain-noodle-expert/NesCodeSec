#!/usr/bin/env python3
"""
One-off scanner for XML parser security configuration coverage.

This inspects every .java file underneath the XXE/output tree, identifies the
parser/processor family being instantiated, and checks whether each file configures
the minimum hardening knobs (DTD disablement, external entity blocking, parameter
entity blocking, entity expansion controls). Results are written to a plaintext log
inside the same directory for later reference.
"""

from __future__ import annotations

import re
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Tuple


BASE_DIR = Path(__file__).parent
SCAN_ROOT = BASE_DIR
OUTPUT_FILE = BASE_DIR / "scan_results_auto.txt"


PARSER_SIGNATURES: Dict[str, Tuple[str, ...]] = {
    "DocumentBuilder": (
        r"(?:[\w]+\.)*DocumentBuilderFactory\.",
        r"new\s+(?:[\w]+\.)*DocumentBuilder\s*\(",
        r"(?:[\w]+\.)*DocumentBuilder\s+\w+\s*=",
    ),
    "SAXParser": (
        r"(?:[\w]+\.)*SAXParserFactory\.",
        r"new\s+(?:[\w]+\.)*SAXParser\s*\(",
    ),
    "SAXBuilder": (
        r"new\s+(?:[\w]+\.)*SAXBuilder\s*\(",
    ),
    "SAXReader": (
        r"new\s+(?:[\w]+\.)*SAXReader\s*\(",
    ),
    "InputFactory": (
        r"(?:[\w]+\.)*XMLInputFactory\.",
        r"new\s+(?:[\w]+\.)*XMLInputFactory\s*\(",
    ),
    "Digester": (
        r"new\s+(?:[\w]+\.)*Digester\s*\(",
    ),
}


Requirement = Tuple[str, Tuple[str, ...]]


PARSER_VAR_PATTERNS: Dict[str, Tuple[str, ...]] = {
    "DocumentBuilder": (
        r"(?P<var>\w+)\s*=\s*(?:[\w\.]+)?DocumentBuilderFactory\.newInstance\s*\(",
        r"(?:[\w\.]*DocumentBuilderFactory)\s+(?P<var>\w+)\s*=",
    ),
    "SAXParser": (
        r"(?P<var>\w+)\s*=\s*(?:[\w\.]+)?SAXParserFactory\.newInstance\s*\(",
        r"(?:[\w\.]*SAXParserFactory)\s+(?P<var>\w+)\s*=",
    ),
    "SAXBuilder": (
        r"(?:(?:[\w\.]*SAXBuilder)\s+(?P<var>\w+)\s*=\s*)?new\s+(?:[\w\.]*SAXBuilder)\s*\(",
    ),
    "SAXReader": (
        r"(?:(?:[\w\.]*SAXReader)\s+(?P<var>\w+)\s*=\s*)?new\s+(?:[\w\.]*SAXReader)\s*\(",
    ),
    "InputFactory": (
        r"(?P<var>\w+)\s*=\s*(?:[\w\.]+)?XMLInputFactory\.newInstance\s*\(",
        r"(?:[\w\.]*XMLInputFactory)\s+(?P<var>\w+)\s*=",
    ),
    "Digester": (
        r"(?P<var>\w+)\s*=\s*new\s+(?:[\w\.]*Digester)\s*\(",
        r"(?P<var>\w+)\s*=\s*DigesterLoader\.newLoader[^\n;]+\.newDigester\s*\(",
    ),
}


REQUIREMENTS: Dict[str, Dict[str, Tuple[str, ...]]] = {
    "DocumentBuilder": {
        "DTD": (r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",),
        "ExternalGeneral": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",),
        "ExternalParameter": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",),
        "EntityExpansion": (
            r"{var}\.setExpandEntityReferences\s*\(\s*false",
            r"{var}\.setXIncludeAware\s*\(\s*false",
        ),
    },
    "SAXParser": {
        "DTD": (r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",),
        "ExternalGeneral": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",),
        "ExternalParameter": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",),
        "EntityExpansion": (r"{var}\.setXIncludeAware\s*\(\s*false",),
    },
    "SAXBuilder": {
        "DTD": (r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",),
        "ExternalGeneral": (
            r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/external-general-entities\"\s*,\s*false",
            r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
        ),
        "ExternalParameter": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",),
        "EntityExpansion": (r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/nonvalidating/load-external-dtd\"\s*,\s*false",),
    },
    "SAXReader": {
        "DTD": (r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",),
        "ExternalGeneral": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",),
        "ExternalParameter": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",),
        "EntityExpansion": (r"{var}\.setEntityResolver\s*\(",),
    },
    "InputFactory": {
        "DTD": (
            r"{var}\.setProperty\s*\(\s*XMLInputFactory\.SUPPORT_DTD\s*,\s*false",
            r"{var}\.setProperty\s*\(\s*\"javax\.xml\.stream\.supportDTD\"\s*,\s*false",
        ),
        "ExternalGeneral": (
            r"{var}\.setProperty\s*\(\s*XMLInputFactory\.IS_SUPPORTING_EXTERNAL_ENTITIES\s*,\s*false",
            r"{var}\.setProperty\s*\(\s*\"javax\.xml\.stream\.isSupportingExternalEntities\"\s*,\s*false",
        ),
        "ExternalParameter": (
            r"{var}\.setProperty\s*\(\s*XMLInputFactory\.IS_SUPPORTING_EXTERNAL_ENTITIES\s*,\s*false",
            r"{var}\.setProperty\s*\(\s*\"javax\.xml\.stream\.isSupportingExternalEntities\"\s*,\s*false",
        ),
        "EntityExpansion": (
            r"{var}\.setProperty\s*\(\s*XMLConstants\.ACCESS_EXTERNAL_DTD\s*,\s*\"\"",
            r"{var}\.setProperty\s*\(\s*XMLConstants\.ACCESS_EXTERNAL_SCHEMA\s*,\s*\"\"",
        ),
    },
    "Digester": {
        "DTD": (r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",),
        "ExternalGeneral": (
            r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
            r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/external-general-entities\"\s*,\s*false",
        ),
        "ExternalParameter": (r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",),
        "EntityExpansion": (r"{var}\.setEntityResolver\s*\(",),
    },
}


@dataclass
class FileResult:
    path: Path
    parser: str
    satisfied: Dict[str, bool]

    @property
    def missing(self) -> List[str]:
        return [name for name, flag in self.satisfied.items() if not flag]

    @property
    def is_secure(self) -> bool:
        return not self.missing


def detect_parser(text: str) -> str | None:
    for parser, patterns in PARSER_SIGNATURES.items():
        if any(re.search(pattern, text) for pattern in patterns):
            return parser
    return None


def parser_from_path(relative_path: Path) -> str | None:
    if not relative_path.parts:
        return None
    top_level = relative_path.parts[0]
    if "__TO__" not in top_level:
        return None
    _, target = top_level.split("__TO__", 1)
    return target or None


def check_requirements(parser: str, text: str) -> Dict[str, bool]:
    results: Dict[str, bool] = {}
    for requirement, patterns in REQUIREMENTS.get(parser, ()):
        results[requirement] = any(re.search(pattern, text) for pattern in patterns)
    return results


def scan_files() -> Tuple[List[FileResult], Dict[str, Dict[str, int]], List[Path]]:
    file_results: List[FileResult] = []
    stats: Dict[str, Dict[str, int]] = defaultdict(lambda: defaultdict(int))
    skipped: List[Path] = []

    for java_path in sorted(SCAN_ROOT.rglob("*.java")):
        text = java_path.read_text(encoding="utf-8", errors="ignore")
        rel_path = java_path.relative_to(SCAN_ROOT)
        parser = parser_from_path(rel_path) or detect_parser(text)
        if not parser:
            skipped.append(rel_path)
            continue
        per_requirement = check_requirements(parser, text)
        result = FileResult(rel_path, parser, per_requirement)
        file_results.append(result)

        stats[parser]["files"] += 1
        if not result.is_secure:
            stats[parser]["at_risk"] += 1
        for name, ok in per_requirement.items():
            if ok:
                stats[parser][name] += 1

    return file_results, stats, skipped


def format_summary(stats: Dict[str, Dict[str, int]]) -> List[str]:
    lines: List[str] = []
    lines.append("=== Target Parser Risk Summary ===")
    for parser in sorted(stats.keys()):
        parser_stats = stats[parser]
        files = parser_stats.get("files", 0)
        at_risk = parser_stats.get("at_risk", 0)
        secure = files - at_risk
        lines.append(f"- {parser}: {files} files scanned, {secure} hardened, {at_risk} missing settings")
        for requirement in ("DTD", "ExternalGeneral", "ExternalParameter", "EntityExpansion"):
            satisfied = parser_stats.get(requirement, 0)
            rate = (satisfied / files * 100) if files else 0.0
            lines.append(f"    {requirement}: {satisfied}/{files} ({rate:.1f}%)")
    lines.append("")
    return lines


def format_details(results: Iterable[FileResult], limit: int = 50) -> List[str]:
    lines: List[str] = []
    lines.append(f"=== Sample At-Risk Files (showing up to {limit}) ===")
    count = 0
    for result in results:
        if result.is_secure:
            continue
        missing = ", ".join(result.missing) or "none"
        lines.append(f"{result.parser:14s} :: {result.path} :: missing [{missing}]")
        count += 1
        if count >= limit:
            break
    if count == 0:
        lines.append("All scanned files include the required security settings.")
    lines.append("")
    return lines

def format_skipped(skipped: List[Path]) -> List[str]:
    lines: List[str] = []
    lines.append("=== Files Without Recognized Parser Pattern ===")
    lines.append(f"Total: {len(skipped)}")
    for path in skipped:
        lines.append(str(path))
    lines.append("")
    return lines


def main() -> None:
    results, stats, skipped = scan_files()
    lines = []
    lines.append(f"Scan root: {SCAN_ROOT}")
    lines.append(f"Total files scanned: {len(results)} (parsers found)")
    lines.append(f"Files without recognized parser: {len(skipped)}")
    missing_total = sum(1 for r in results if not r.is_secure)
    lines.append(f"Files missing at least one requirement: {missing_total}")
    lines.append("")
    lines.extend(format_summary(stats))
    lines.extend(format_details(results))
    lines.extend(format_skipped(skipped))
    OUTPUT_FILE.write_text("\n".join(lines), encoding="utf-8")
    print(f"[xml-parser-security-scan] Results written to {OUTPUT_FILE.relative_to(BASE_DIR)}")


if __name__ == "__main__":
    main()
