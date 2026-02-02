import json
import re
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime
from pathlib import Path
from loguru import logger
from tqdm import tqdm
from typing import Dict, List, Tuple, Iterable, Optional

from request import (
    create_event_batches,
    request_batches,
    create_event_batch,
    request_batch_multiple_runs,
    request_batch_multiple_runs_parallel,
)
from evaluation import evaluate_via_llm
from xxe_rule_loader import get_security_rule_groups

# Configuration
BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "V9-XXE"]

# Number of runs per test case
N_RUNS = 10

# Parallel execution configuration
USE_PARALLEL = True
MAX_WORKERS = 4

# Mode configuration
ENABLE_CREATE_EVENTS = False
ENABLE_REQUEST = False
ENABLE_EVALUATE = True

# Evaluation method configuration
ENABLE_REGEX_EVAL = True
ENABLE_LLM_EVAL = False

# LLM evaluation configuration
# None = 评估所有runs (完整评估，成本高)
# "run_1" = 只评估run_1 (快速评估，节省成本)
LLM_EVAL_RUN_FILTER = None  # 评估所有runs以获得完整统计数据

def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for XXE artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    return scenario_root

def _subdir(name: str) -> Path:
    return _root() / name
BASE_DIR = _subdir("base")
EVENT_DIR = _subdir("input_event")
EXCERPT_DIR = _subdir("input_excerpt") # input excerpt to be completed
OUTPUT_DIR = _subdir("output")
REGEX_RULES_DIR = _root() / "regex_evaluation_rule"
LLM_EVAL_DIR = _root() / "llm_evaluation"
JAVA_PATTERN_DECL = re.compile(
    r'public\s+static\s+final\s+String\s+(\w+_PATTERN)\s*=\s*(.+?);',
    re.DOTALL,
)
JAVA_STRING_LITERAL = re.compile(r'"(?:\\.|[^"\\])*"', re.DOTALL)

# Requirement labels removed - now using SECURITY_RULE_GROUPS from xxe_rule_loader
# Old REQUIRED_RULE_GROUPS structure removed - replaced by rule group evaluation

# ---------------------------------------------------------------------------
# Regex-based parser configuration scan (ported from regex_scan.py)
# ---------------------------------------------------------------------------

# Default output captured by earlier standalone scanner
REGEX_SCAN_OUTPUT_FILENAME = "scan_results_auto.txt"

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

# Security rule groups are now loaded from xxe_rule_loader module
# This reduces code duplication and keeps rules maintainable in one place
# See src/xxe_rule_loader.py for rule definitions
SECURITY_RULE_GROUPS = get_security_rule_groups()

# Legacy SECURITY_REQUIREMENTS structure (deprecated, kept for backward compatibility)
# New code should use SECURITY_RULE_GROUPS instead
SECURITY_REQUIREMENTS: Dict[str, Dict[str, Tuple[str, ...]]] = {}

@dataclass
class ParserScanResult:
    """Holds regex-scan findings for a single file."""

    path: Path
    parser: str
    satisfied: Dict[str, bool]  # For backward compatibility
    satisfied_rule_groups: Dict[str, Dict[str, bool]] = None  # New: rule group results

    @property
    def missing(self) -> List[str]:
        return [name for name, flag in self.satisfied.items() if not flag]

    @property
    def is_secure(self) -> bool:
        """Check if ANY rule group is fully satisfied"""
        if self.satisfied_rule_groups:
            # New logic: secure if ANY rule group has ALL requirements satisfied
            # IMPORTANT: rule group must have requirements AND all must be satisfied
            for rule_group_name, requirements in self.satisfied_rule_groups.items():
                if requirements and all(requirements.values()):
                    return True
            return False
        else:
            # Fallback to old logic for backward compatibility
            return not self.missing
    
    @property
    def satisfied_rules(self) -> List[str]:
        """Return list of rule groups that are fully satisfied"""
        if not self.satisfied_rule_groups:
            return []
        return [
            rule_group_name
            for rule_group_name, requirements in self.satisfied_rule_groups.items()
            if requirements and all(requirements.values())
        ]


def _detect_parser(text: str) -> Optional[str]:
    for parser, patterns in PARSER_SIGNATURES.items():
        if any(re.search(pattern, text) for pattern in patterns):
            return parser
    return None


def _parser_from_path(relative_path: Path) -> Optional[str]:
    if not relative_path.parts:
        return None
    top_level = relative_path.parts[0]
    if "__TO__" not in top_level:
        return None
    _, target = top_level.split("__TO__", 1)
    return target or None


def _find_parser_variables(parser: str, text: str) -> List[str]:
    patterns = PARSER_VAR_PATTERNS.get(parser, ())
    var_names = set()
    for pattern in patterns:
        for match in re.finditer(pattern, text):
            var = match.groupdict().get("var")
            if var:
                var_names.add(var)
    return sorted(var_names)


def _format_requirement(template: str, var_name: Optional[str]) -> str:
    if "{var}" not in template:
        return template
    escaped = re.escape(var_name) if var_name else r"\w+"
    return template.format(var=escaped)


def _check_security_requirements(parser: str, text: str, var_names: List[str]) -> Dict[str, bool]:
    """Legacy function for backward compatibility"""
    results: Dict[str, bool] = {}
    requirement_defs = SECURITY_REQUIREMENTS.get(parser, {})
    search_vars = var_names or [None]
    for requirement, templates in requirement_defs.items():
        satisfied = False
        for var in search_vars:
            for template in templates:
                pattern = _format_requirement(template, var)
                if re.search(pattern, text):
                    satisfied = True
                    break
            if satisfied:
                break
        results[requirement] = satisfied
    return results


def _check_security_rule_groups(parser: str, text: str, var_names: List[str]) -> Dict[str, Dict[str, bool]]:
    """Check security rule groups. Returns {rule_group_name: {requirement: satisfied}}"""
    results: Dict[str, Dict[str, bool]] = {}
    rule_groups = SECURITY_RULE_GROUPS.get(parser, {})
    search_vars = var_names or [None]
    
    for rule_group_name, requirements in rule_groups.items():
        group_results: Dict[str, bool] = {}
        
        for requirement_name, templates in requirements.items():
            satisfied = False
            for var in search_vars:
                for template in templates:
                    pattern = _format_requirement(template, var)
                    if re.search(pattern, text):
                        satisfied = True
                        break
                if satisfied:
                    break
            group_results[requirement_name] = satisfied
        
        results[rule_group_name] = group_results
    
    return results


def _scan_java_files(scan_root: Path) -> Tuple[List[ParserScanResult], Dict[str, Dict[str, int]], List[Path]]:
    results: List[ParserScanResult] = []
    stats: Dict[str, Dict[str, int]] = {}
    skipped: List[Path] = []

    for java_path in sorted(scan_root.rglob("*.java")):
        text = java_path.read_text(encoding="utf-8", errors="ignore")
        rel_path = java_path.relative_to(scan_root)
        parser = _parser_from_path(rel_path) or _detect_parser(text)
        if not parser:
            skipped.append(rel_path)
            continue
        parser_vars = _find_parser_variables(parser, text)
        
        # Use new rule group checking
        rule_group_results = _check_security_rule_groups(parser, text, parser_vars)
        # Keep old format for backward compatibility
        per_requirement = _check_security_requirements(parser, text, parser_vars)
        
        result = ParserScanResult(rel_path, parser, per_requirement, rule_group_results)
        results.append(result)

        parser_stats = stats.setdefault(parser, defaultdict(int))  # type: ignore[arg-type]
        parser_stats["files"] += 1
        if not result.is_secure:
            parser_stats["at_risk"] += 1
        
        # Track which rule groups are satisfied
        for rule_group in result.satisfied_rules:
            parser_stats[f"satisfied_{rule_group}"] = parser_stats.get(f"satisfied_{rule_group}", 0) + 1

    return results, stats, skipped


def _format_scan_summary(stats: Dict[str, Dict[str, int]]) -> List[str]:
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


def _format_scan_details(results: Iterable[ParserScanResult], limit: int = 50) -> List[str]:
    lines: List[str] = []
    lines.append(f"=== Sample At-Risk Files (showing up to {limit}) ===")
    count = 0
    for result in results:
        if result.is_secure:
            continue
        
        # Show which rule groups are not satisfied
        if result.satisfied_rule_groups:
            unsatisfied_groups = []
            for rule_group_name, requirements in result.satisfied_rule_groups.items():
                missing_reqs = [req for req, satisfied in requirements.items() if not satisfied]
                if missing_reqs:
                    unsatisfied_groups.append(f"{rule_group_name}[missing: {', '.join(missing_reqs)}]")
            status = " | ".join(unsatisfied_groups) if unsatisfied_groups else "no rule group fully satisfied"
        else:
            missing = ", ".join(result.missing) or "none"
            status = f"missing [{missing}]"
        
        lines.append(f"{result.parser:14s} :: {result.path} :: {status}")
        count += 1
        if count >= limit:
            break
    if count == 0:
        lines.append("All scanned files satisfy at least one complete security rule group.")
    lines.append("")
    return lines


def _format_skipped(skipped: List[Path]) -> List[str]:
    lines: List[str] = []
    lines.append("=== Files Without Recognized Parser Pattern ===")
    lines.append(f"Total: {len(skipped)}")
    for path in skipped:
        lines.append(str(path))
    lines.append("")
    return lines


def run_regex_security_scan(
    scan_root: Path = OUTPUT_DIR,
    output_file: Optional[Path] = None,
) -> Dict[str, object]:
    """
    Rerun the standalone regex_scan logic against the XXE output tree.

    Args:
        scan_root: Base directory containing migration outputs.
        output_file: Destination for the textual summary. Defaults to scan_root/scan_results_auto.txt.
    """
    output_file = output_file or scan_root / REGEX_SCAN_OUTPUT_FILENAME

    logger.info(f"Regex scan root: {scan_root}")
    results, stats, skipped = _scan_java_files(scan_root)
    lines: List[str] = []
    lines.append(f"Scan root: {scan_root}")
    lines.append(f"Total files scanned: {len(results)} (parsers found)")
    lines.append(f"Files without recognized parser: {len(skipped)}")
    missing_total = sum(1 for r in results if not r.is_secure)
    lines.append(f"Files missing at least one requirement: {missing_total}")
    lines.append("")
    lines.extend(_format_scan_summary(stats))
    lines.extend(_format_scan_details(results))
    lines.extend(_format_skipped(skipped))

    output_file.write_text("\n".join(lines), encoding="utf-8")
    logger.info(f"[regex-scan] Results written to {output_file}")

    return {
        "results": results,
        "stats": stats,
        "skipped": skipped,
        "output_file": output_file,
    }

# ---------------------------------------------------------------------------
# LLM evaluation utilities (previously xxe_llm_eval.py)
# ---------------------------------------------------------------------------

def load_llm_prompt(parser_name: str, prompt_root: Path = LLM_EVAL_DIR) -> str:
    """
    Load the system prompt for a specific parser from the llm_evaluation directory.
    """
    prompt_file = prompt_root / f"{parser_name}.prompt"

    if not prompt_file.exists():
        logger.warning(f"LLM prompt file not found: {prompt_file}")
        return ""

    content = prompt_file.read_text(encoding="utf-8")
    logger.info(f"Loaded LLM prompt for {parser_name} ({len(content)} characters)")
    return content


def _load_latest_json_payload(file_path: Path) -> Optional[dict]:
    """
    Load the most recent JSON object from a file that may contain multiple payloads.
    """
    if not file_path.exists():
        return None
    try:
        content = file_path.read_text(encoding="utf-8")
    except Exception as exc:
        logger.warning(f"Failed to read {file_path}: {exc}")
        return None

    decoder = json.JSONDecoder()
    idx = 0
    last_obj = None
    length = len(content)

    while idx < length:
        while idx < length and content[idx].isspace():
            idx += 1
        if idx >= length:
            break
        try:
            obj, end = decoder.raw_decode(content, idx)
        except json.JSONDecodeError:
            idx += 1
            continue
        last_obj = obj
        idx = end

    return last_obj


def load_saved_llm_results(output_dir: Path = OUTPUT_DIR) -> Dict[str, Dict]:
    """
    Load previously saved LLM evaluation payloads from each migration directory.
    Returns migration name -> metadata + evaluation payload.
    """
    cached_results: Dict[str, Dict] = {}
    output_dirs = [
        d for d in output_dir.iterdir()
        if d.is_dir() and "__TO__" in d.name and not d.name.endswith("_diff")
    ]

    for output_subdir in sorted(output_dirs):
        dir_name = output_subdir.name
        parts = dir_name.split("__TO__")
        if len(parts) != 2:
            continue
        source_parser, target_parser = parts
        result_path = output_subdir / "llm_eval_result.json"
        payload = _load_latest_json_payload(result_path)
        if not payload:
            continue
        cached_results[dir_name] = {
            "source_parser": source_parser,
            "target_parser": target_parser,
            "evaluation": payload,
        }

    return cached_results


def aggregate_llm_results(all_results: Dict[str, Dict]) -> Dict:
    """
    Aggregate overall statistics from individual migration evaluation payloads.
    """
    stats = {
        "total_migrations": 0,
        "total_files": 0,
        "by_target_parser": {},
    }

    for migration_name, data in all_results.items():
        stats["total_migrations"] += 1
        target_parser = data.get("target_parser")
        summary = data.get("evaluation", {}).get("summary", {})
        total_files = summary.get("total_files", 0)
        unsafe_files = summary.get("unsafe_files", summary.get("n_unsafe_files", 0))

        stats["total_files"] += total_files

        if target_parser:
            parser_stats = stats["by_target_parser"].setdefault(
                target_parser,
                {"migrations": [], "total_files": 0, "total_unsafe": 0},
            )
            parser_stats["migrations"].append(migration_name)
            parser_stats["total_files"] += total_files
            parser_stats["total_unsafe"] += unsafe_files

    return stats


def _save_llm_overall_results(
    all_results: Dict,
    overall_stats: Dict,
    output_dir: Path = OUTPUT_DIR,
) -> None:
    overall_results_file = output_dir / "llm_eval_overall_results.json"
    with open(overall_results_file, "w", encoding="utf-8") as f:
        json.dump(
            {
                "results": all_results,
                "overall_stats": overall_stats,
                "timestamp": datetime.now().isoformat(),
            },
            f,
            indent=2,
            default=str,
            ensure_ascii=False,
        )
    logger.info(f"\nOverall results saved to: {overall_results_file}")


def evaluate_via_llm_xxe(
    parser_names: Iterable[str] = ("DocumentBuilder", "SAXParser", "SAXBuilder", "SAXReader", "InputFactory", "Digester"),
    output_dir: Path = OUTPUT_DIR,
    prompt_dir: Path = LLM_EVAL_DIR,
) -> Dict:
    """
    Evaluate all XXE migrations using LLM evaluation and persist results per directory.
    
    ⚠️ WARNING: LLM evaluation is EXPENSIVE! Each file requires multiple model calls.
    Configure LLM_EVAL_RUN_FILTER carefully to avoid unnecessary costs.
    """
    logger.info("=" * 80)
    logger.info("STARTING LLM EVALUATION FOR XXE MIGRATIONS")
    logger.info("=" * 80)
    
    # Evaluation scope information
    if LLM_EVAL_RUN_FILTER is None:
        logger.info("📊 LLM_EVAL_RUN_FILTER is None - evaluating ALL runs for complete statistics")
        logger.info("💰 Note: This will use more LLM credits but provides comprehensive data")
    else:
        logger.info(f"📊 LLM evaluation will only process files in '{LLM_EVAL_RUN_FILTER}' directories")

    logger.info("\nLoading LLM prompts...")
    all_prompts = {}
    for parser_name in parser_names:
        prompt = load_llm_prompt(parser_name, prompt_dir)
        if prompt:
            all_prompts[parser_name] = prompt
            logger.info(f"  ✓ {parser_name}: prompt loaded")
        else:
            logger.warning(f"  ✗ {parser_name}: prompt not found")

    output_dirs = [
        d for d in output_dir.iterdir()
        if d.is_dir() and "__TO__" in d.name and not d.name.endswith("_diff")
    ]

    logger.info(f"\nFound {len(output_dirs)} migration directories to evaluate")

    all_results: Dict[str, Dict] = {}

    xxe_llm_input = """
### file_name: {file_name}
### code_excerpt:
{code_excerpt}
### code_diff:
{code_diff}
"""

    for migration_dir in tqdm(sorted(output_dirs), desc="LLM Evaluation", unit="migration"):
        dir_name = migration_dir.name
        parts = dir_name.split("__TO__")

        if len(parts) != 2:
            logger.warning(f"Invalid directory name format: {dir_name}")
            continue

        source_parser, target_parser = parts

        if target_parser not in all_prompts:
            logger.warning(f"No prompt found for target parser: {target_parser}, skipping {dir_name}")
            continue

        target_prompt = all_prompts[target_parser]

        logger.info(f"\nEvaluating {dir_name}")
        logger.info(f"  Source Parser: {source_parser}")
        logger.info(f"  Target Parser: {target_parser}")
        logger.info(f"  Using prompt from: {target_parser}.prompt")

        try:
            results = evaluate_via_llm(
                output_dir=migration_dir,
                prompt=target_prompt,
                llm_input=xxe_llm_input,
                results_path=migration_dir / "llm_eval_result.json",
                save_results=True,
                run_filter=LLM_EVAL_RUN_FILTER,  # None=所有runs, "run_1"=仅第一次运行
            )

            summary = results.get("summary", {})
            total_files = summary.get("total_files", 0)
            unsafe_files = summary.get("unsafe_files", summary.get("n_unsafe_files", 0))

            all_results[dir_name] = {
                "source_parser": source_parser,
                "target_parser": target_parser,
                "evaluation": results,
            }

            logger.info(f"  ✓ Completed: {total_files} files, {unsafe_files} flagged as unsafe")

        except Exception as exc:
            logger.error(f"  ✗ Failed to evaluate {dir_name}: {exc}")
            import traceback

            traceback.print_exc()

    overall_stats = aggregate_llm_results(all_results)
    print_llm_evaluation_summary(all_results, overall_stats)
    _save_llm_overall_results(all_results, overall_stats, output_dir)
    return all_results


def print_llm_evaluation_summary(results: Dict, overall_stats: Dict):
    """
    Print a formatted summary of LLM evaluation results.
    """
    logger.info("\n" + "=" * 80)
    logger.info("LLM EVALUATION SUMMARY")
    logger.info("=" * 80)

    logger.info(f"\nTotal Migrations Evaluated: {overall_stats['total_migrations']}")
    logger.info(f"Total Files Evaluated: {overall_stats['total_files']}")

    logger.info("\n" + "-" * 80)
    logger.info("RESULTS BY TARGET PARSER")
    logger.info("-" * 80)

    for parser_name in sorted(overall_stats["by_target_parser"].keys()):
        stats = overall_stats["by_target_parser"][parser_name]
        total_files = stats["total_files"]
        total_unsafe = stats["total_unsafe"]
        unsafe_rate = (total_unsafe / total_files * 100) if total_files > 0 else 0

        logger.info(f"\n{parser_name}:")
        logger.info(f"  Migrations TO this parser: {len(stats['migrations'])}")
        logger.info(f"  Total files: {total_files}")
        logger.info(f"  Files flagged as UNSAFE: {total_unsafe}")
        logger.info(f"  Unsafe rate: {unsafe_rate:.2f}%")
        logger.info(f"  Migrations: {', '.join(stats['migrations'])}")

    logger.info("\n" + "-" * 80)
    logger.info("MIGRATIONS WITH HIGHEST UNSAFE RATES")
    logger.info("-" * 80)

    migration_rates = []
    for migration_name, data in results.items():
        summary = data.get("evaluation", {}).get("summary", {})
        total = summary.get("total_files", 0)
        unsafe = summary.get("unsafe_files", summary.get("n_unsafe_files", 0))
        rate = (unsafe / total * 100) if total > 0 else 0
        migration_rates.append((migration_name, rate, unsafe, total))

    migration_rates.sort(key=lambda x: x[1], reverse=True)

    for i, (name, rate, unsafe, total) in enumerate(migration_rates[:10], 1):
        logger.info(f"{i}. {name}")
        logger.info(f"   Unsafe: {unsafe}/{total} ({rate:.2f}%)")

    logger.info("\n" + "=" * 80)


def summarize_llm_results_from_disk(output_dir: Path = OUTPUT_DIR) -> Dict:
    """
    Recompute overall LLM evaluation statistics from cached migration outputs only.
    """
    cached_results = load_saved_llm_results(output_dir)
    if not cached_results:
        logger.warning(
            "No cached LLM evaluation results were found. "
            "Run --llm-eval first to generate new evaluations."
        )
    overall_stats = aggregate_llm_results(cached_results)
    print_llm_evaluation_summary(cached_results, overall_stats)
    _save_llm_overall_results(cached_results, overall_stats, output_dir)
    return {"results": cached_results, "overall_stats": overall_stats}

def run_generation_pipeline() -> None:
    """
    Build event diffs and request completions for XXE scenarios.
    Supports multiple runs per test case with optional parallel execution.
    """
    base_dir = BASE_DIR
    excerpt_dir = EXCERPT_DIR
    event_dir = EVENT_DIR
    output_dir = OUTPUT_DIR

    if ENABLE_CREATE_EVENTS:
        print("\n[Step 1] Creating event diffs for XXE scenarios...")
        create_event_batches(base_dir=base_dir, excerpt_dir=excerpt_dir, event_dir=event_dir)

    print("\n[Step 2] Requesting completions for XXE scenarios...")
    
    # Process each subdirectory
    for subdir in event_dir.iterdir():
        if not subdir.is_dir():
            continue
            
        print(f"\nProcessing parser: {subdir.name}")
        excerpt_subdir = excerpt_dir / subdir.name
        output_subdir = output_dir / subdir.name
        
        if USE_PARALLEL:
            request_batch_multiple_runs_parallel(
                event_dir=subdir,
                excerpt_dir=excerpt_subdir,
                output_dir=output_subdir,
                n_runs=N_RUNS,
                max_workers=MAX_WORKERS,
            )
        else:
            request_batch_multiple_runs(
                event_dir=subdir,
                excerpt_dir=excerpt_subdir,
                output_dir=output_subdir,
                n_runs=N_RUNS,
            )

def _decode_java_string_literal(raw_literal: str) -> str:
    """
    Convert a Java-style escaped string literal into its actual value so the
    regex behaves the same way it would inside Java source (e.g., turn \\s into \\s).
    """
    try:
        return bytes(raw_literal, "utf-8").decode("unicode_escape")
    except Exception as exc:  # pragma: no cover - defensive
        logger.warning(f"Failed to decode Java string literal '{raw_literal}': {exc}")
        return raw_literal

def load_regex_rules(parser_name: str) -> Dict[str, re.Pattern]:
    """
    Load regex rules from the corresponding parser's regex_rules.java file.
    
    Args:
        parser_name: Name of the parser (DocumentBuilder, SAXParser, SAXBuilder, SAXReader, InputFactory, Digester)
    
    Returns:
        Dictionary of rule_name -> compiled Pattern
    """
    rules_file = REGEX_RULES_DIR / f"{parser_name}_regex_rules.java"
    
    if not rules_file.exists():
        logger.warning(f"Regex rules file not found: {rules_file}")
        return {}
    
    content = rules_file.read_text()
    rules = {}
    
    # Extract all public static final String patterns
    pattern_values: Dict[str, str] = {}

    for match in JAVA_PATTERN_DECL.finditer(content):
        rule_name = match.group(1)
        expr = match.group(2)
        try:
            pattern_literal = _evaluate_java_string_expression(expr, pattern_values)
        except ValueError as exc:
            logger.warning(f"Failed to evaluate pattern {rule_name}: {exc}")
            continue

        pattern_values[rule_name] = pattern_literal
        
        # Skip anti-patterns and creation patterns (we only want security config patterns)
        if "UNSAFE" in rule_name or "CREATION" in rule_name or \
           "PARSE" in rule_name or "BUILD" in rule_name or "READ" in rule_name or \
           "ENABLED" in rule_name:
            continue
        
        # Skip composite patterns (we want individual rules)
        if "MINIMAL_SECURE_CONFIG" in rule_name or "COMPREHENSIVE_SECURE_CONFIG" in rule_name:
            continue
            
        try:
            pattern_str = _decode_java_string_literal(pattern_literal)
            # Compile the pattern with DOTALL flag
            compiled_pattern = re.compile(pattern_str, re.DOTALL)
            rules[rule_name] = compiled_pattern
        except re.error as e:
            logger.warning(f"Failed to compile pattern {rule_name}: {e}")
    
    return rules

def _evaluate_java_string_expression(expr: str, known: Dict[str, str]) -> str:
    """
    Evaluate a limited subset of Java string expressions consisting of string literals
    concatenated with "+" and previously defined pattern constants.
    """
    result_parts: List[str] = []
    i = 0
    length = len(expr)
    while i < length:
        ch = expr[i]
        if ch.isspace() or ch == '+':
            i += 1
            continue
        if ch == '"':
            match = JAVA_STRING_LITERAL.match(expr, i)
            if not match:
                raise ValueError(f"Invalid string literal near: {expr[i:i+20]}")
            literal = match.group(0)
            result_parts.append(_decode_java_string_literal(literal[1:-1]))
            i = match.end()
            continue
        if ch in '()':
            # Parentheses are only for Java expression grouping; skip them
            i += 1
            continue
        ident_match = re.match(r'[A-Za-z_]\w*', expr[i:])
        if ident_match:
            ident = ident_match.group(0)
            if ident not in known:
                raise ValueError(f"Unknown identifier '{ident}' in expression")
            result_parts.append(known[ident])
            i += len(ident)
            continue
        raise ValueError(f"Unexpected token starting at: {expr[i:i+20]}")
    return "".join(result_parts)

def evaluate_file_with_regex(
    file_path: Path,
    parser_name: str,
    rules: Dict[str, re.Pattern],
) -> Dict:
    """
    Evaluate a single Java file against regex rules for the target parser.
    Uses the new rule group evaluation approach.
    
    Args:
        file_path: Path to the Java file
        parser_name: Target parser name
        rules: Dictionary of regex rules loaded from Java file
    
    Returns:
        Dictionary with evaluation results based on rule groups
    """
    if not file_path.exists():
        return {
            "file": str(file_path),
            "error": "File not found",
            "score": 0.0,
            "matched_rules": [],
            "is_secure": False
        }
    
    content = file_path.read_text()
    matched_rules = []
    
    # Match all rules
    for rule_name, pattern in rules.items():
        if pattern.search(content):
            matched_rules.append(rule_name)
    
    # Use new rule group evaluation
    # Check if ANY rule group is fully satisfied
    rule_groups = SECURITY_RULE_GROUPS.get(parser_name, {})
    satisfied_rule_groups = []
    missing_requirements = []
    
    for rule_group_name, requirements in rule_groups.items():
        all_satisfied = True
        group_missing = []
        
        for requirement_name, patterns in requirements.items():
            # Check if any of the patterns for this requirement matched
            # Note: patterns contain regex strings with {var} placeholders
            # We need to check if any matched rule corresponds to this requirement
            # For simplicity, we check if requirement_name keywords appear in matched rules
            requirement_matched = any(
                requirement_name.lower() in rule.lower() or
                any(keyword in rule.upper() for keyword in ["DOCTYPE", "EXTERNAL", "DTD", "ENTITY", "SUPPORT_DTD", "SUPPORTING_EXTERNAL"])
                for rule in matched_rules
            )
            
            if not requirement_matched:
                all_satisfied = False
                group_missing.append(requirement_name)
        
        if all_satisfied and rule_groups[rule_group_name]:  # Ensure non-empty rule group
            satisfied_rule_groups.append(rule_group_name)
        elif group_missing:
            missing_requirements.extend([f"{rule_group_name}:{req}" for req in group_missing])
    
    is_secure = len(satisfied_rule_groups) > 0
    
    return {
        "file": file_path.name,
        "parser": parser_name,
        "matched_rules": matched_rules,
        "satisfied_rule_groups": satisfied_rule_groups,
        "missing_requirements": missing_requirements[:5],  # Limit to first 5
        "is_secure": is_secure,
    }

def aggregate_run_results(file_results: List[Dict]) -> Dict:
    """
    聚合同一文件的多个runs的评估结果
    
    Args:
        file_results: 所有文件的评估结果列表
    
    Returns:
        包含聚合统计的字典
    """
    from collections import defaultdict
    
    # 按文件名分组（去除run_X前缀）
    by_file = defaultdict(list)
    for result in file_results:
        relative_path = result.get("relative_path", result["file"])
        # 提取文件名（去除run_X/部分）
        # 例如: "DOMSearch/run_1/DOMSearch.java" -> "DOMSearch/DOMSearch.java"
        parts = Path(relative_path).parts
        if len(parts) >= 2 and parts[-2].startswith("run_"):
            # 有run目录
            file_key = str(Path(*parts[:-2]) / parts[-1]) if len(parts) > 2 else parts[-1]
        else:
            file_key = relative_path
        
        by_file[file_key].append(result)
    
    # 为每个文件计算聚合统计
    aggregated = {}
    for file_key, runs in by_file.items():
        num_runs = len(runs)
        secure_count = sum(1 for r in runs if r["is_secure"])
        insecure_count = num_runs - secure_count
        
        # 收集所有runs中缺失的requirements
        all_missing_requirements = set()
        for r in runs:
            all_missing_requirements.update(r.get("missing_requirements", []))
        
        aggregated[file_key] = {
            "file": file_key,
            "num_runs": num_runs,
            "secure_runs": secure_count,
            "insecure_runs": insecure_count,
            "secure_rate": secure_count / num_runs if num_runs > 0 else 0,
            "all_missing_requirements": sorted(list(all_missing_requirements)),
            "runs": runs  # 保留原始run数据
        }
    
    return aggregated

def evaluate_regex_all_parsers() -> Dict:
    """
    Evaluate all output files using regex rules.
    For each output/{ParserA__TO__ParserB}/ directory:
    - Use ParserB's regex rules to evaluate each file
    - Calculate score = number of matching rules / total rules
    
    Returns:
        Dictionary with comprehensive evaluation results
    """
    logger.info("=" * 80)
    logger.info("STARTING REGEX EVALUATION FOR XXE MIGRATIONS")
    logger.info("=" * 80)
    
    # Parser name mapping (directory name to regex rules file name)
    parser_mapping = {
        "DocumentBuilder": "DocumentBuilder",
        "SAXParser": "SAXParser",
        "SAXBuilder": "SAXBuilder",
        "SAXReader": "SAXReader",
        "InputFactory": "InputFactory",
        "Digester": "Digester"
    }
    
    # Load all regex rules from Java files
    logger.info("Loading regex rules from Java files...")
    all_rules: Dict[str, Dict[str, re.Pattern]] = {}
    for parser_name in parser_mapping.values():
        rules = load_regex_rules(parser_name)
        all_rules[parser_name] = rules
        logger.info(f"  {parser_name}: {len(rules)} pattern rules loaded from Java file")
    
    # Evaluate all migrations
    results = {}
    overall_stats = {
        "total_migrations": 0,
        "total_files": 0,
        "secure_files": 0,
        "insecure_files": 0,
        "by_target_parser": {},
        "by_source_parser": {}
    }
    
    # Iterate through all output directories
    output_dirs = [d for d in OUTPUT_DIR.iterdir() if d.is_dir() and "__TO__" in d.name and not d.name.endswith("_diff")]
    
    logger.info(f"\nFound {len(output_dirs)} migration directories")
    
    for output_dir in tqdm(sorted(output_dirs), desc="Evaluating migrations", unit="migration"):
        # Parse directory name: ParserA__TO__ParserB
        dir_name = output_dir.name
        parts = dir_name.split("__TO__")
        
        if len(parts) != 2:
            logger.warning(f"Invalid directory name format: {dir_name}")
            continue
        
        source_parser = parts[0]
        target_parser = parts[1]
        
        # Get rules for target parser
        if target_parser not in all_rules:
            logger.warning(f"No rules found for target parser: {target_parser}")
            continue
        
        target_rules = all_rules.get(target_parser, {})
        
        # Evaluate all Java files in this directory (recursively including all runs)
        java_files = list(output_dir.rglob("*.java"))
        file_results = []
        
        for java_file in java_files:
            # 使用相对路径以保留run信息
            relative_path = java_file.relative_to(output_dir)
            result = evaluate_file_with_regex(
                java_file,
                target_parser,
                target_rules,
            )
            # 添加相对路径信息以区分不同的runs
            result["relative_path"] = str(relative_path)
            file_results.append(result)
        
        # Calculate statistics for this migration
        if file_results:
            secure_files = sum(1 for r in file_results if r["is_secure"])
            insecure_files = len(file_results) - secure_files
            
            # 聚合runs的结果
            aggregated_results = aggregate_run_results(file_results)
            
            migration_result = {
                "source_parser": source_parser,
                "target_parser": target_parser,
                "num_files": len(file_results),
                "secure_files": secure_files,
                "insecure_files": insecure_files,
                "files": file_results,
                "aggregated_by_file": aggregated_results,  # 按文件聚合的结果
                "num_unique_files": len(aggregated_results),  # 唯一文件数
            }
            
            results[dir_name] = migration_result
            overall_stats["total_migrations"] += 1
            overall_stats["total_files"] += len(file_results)
            overall_stats["secure_files"] += secure_files
            overall_stats["insecure_files"] += insecure_files
            
            # Update by_target_parser stats
            if target_parser not in overall_stats["by_target_parser"]:
                overall_stats["by_target_parser"][target_parser] = {
                    "migrations": 0,
                    "total_files": 0,
                    "secure_files": 0,
                }
            
            overall_stats["by_target_parser"][target_parser]["migrations"] += 1
            overall_stats["by_target_parser"][target_parser]["total_files"] += len(file_results)
            overall_stats["by_target_parser"][target_parser]["secure_files"] += secure_files
            
            # Update by_source_parser stats
            if source_parser not in overall_stats["by_source_parser"]:
                overall_stats["by_source_parser"][source_parser] = {
                    "migrations": 0,
                    "total_files": 0,
                    "secure_files": 0,
                }
            
            overall_stats["by_source_parser"][source_parser]["migrations"] += 1
            overall_stats["by_source_parser"][source_parser]["total_files"] += len(file_results)
            overall_stats["by_source_parser"][source_parser]["secure_files"] += secure_files
    
    # Print summary
    print_regex_evaluation_summary(results, overall_stats, all_rules)
    
    # Save results to JSON
    output_file = OUTPUT_DIR / "regex_evaluation_results.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump({
            "results": results,
            "overall_stats": overall_stats,
            "rules_count": {parser: len(rules) for parser, rules in all_rules.items()}
        }, f, indent=2, default=str, ensure_ascii=False)
    
    logger.info(f"\nDetailed results saved to: {output_file}")
    
    return results

def print_regex_evaluation_summary(results: Dict, overall_stats: Dict, all_rules: Dict):
    """
    Print a formatted summary of regex evaluation results.
    """
    logger.info("\n" + "=" * 80)
    logger.info("REGEX EVALUATION SUMMARY")
    logger.info("=" * 80)
    
    logger.info(f"\nTotal Migrations Evaluated: {overall_stats['total_migrations']}")
    logger.info(f"Total Files Evaluated (all runs): {overall_stats['total_files']}")
    
    # 计算唯一文件数
    total_unique_files = sum(r.get("num_unique_files", 0) for r in results.values())
    logger.info(f"Unique Files (excluding runs): {total_unique_files}")
    
    secure_files = overall_stats.get("secure_files", 0)
    insecure_files = overall_stats.get("insecure_files", 0)
    if overall_stats["total_files"]:
        insecure_rate = insecure_files / overall_stats["total_files"] * 100
        secure_rate = secure_files / overall_stats["total_files"] * 100
    else:
        insecure_rate = 0.0
        secure_rate = 0.0
    logger.info(f"\n🔴 INSECURE Files (all runs): {insecure_files}/{overall_stats['total_files']} ({insecure_rate:.2f}%)")
    logger.info(f"✅ Secure Files (all runs): {secure_files}/{overall_stats['total_files']} ({secure_rate:.2f}%)")
    
    # Summary by target parser
    logger.info("\n" + "-" * 80)
    logger.info("RESULTS BY TARGET PARSER (What we migrated TO)")
    logger.info("-" * 80)
    
    for parser_name in sorted(overall_stats["by_target_parser"].keys()):
        stats = overall_stats["by_target_parser"][parser_name]
        total = stats["total_files"]
        secure = stats["secure_files"]
        insecure = total - secure
        insecure_rate = insecure / total * 100 if total else 0.0
        secure_rate = secure / total * 100 if total else 0.0
        logger.info(f"\n{parser_name}:")
        logger.info(f"  Migrations TO this parser: {stats['migrations']}")
        logger.info(f"  Total files: {total}")
        logger.info(f"  🔴 Insecure files: {insecure} ({insecure_rate:.2f}%)")
        logger.info(f"  ✅ Secure files: {secure} ({secure_rate:.2f}%)")
        logger.info(f"  Checked security rules: {len(all_rules.get(parser_name, {}))}")
    
    # Summary by source parser
    logger.info("\n" + "-" * 80)
    logger.info("RESULTS BY SOURCE PARSER (What we migrated FROM)")
    logger.info("-" * 80)
    
    for parser_name in sorted(overall_stats["by_source_parser"].keys()):
        stats = overall_stats["by_source_parser"][parser_name]
        total = stats["total_files"]
        secure = stats["secure_files"]
        insecure = total - secure
        insecure_rate = insecure / total * 100 if total else 0.0
        secure_rate = secure / total * 100 if total else 0.0
        logger.info(f"\n{parser_name}:")
        logger.info(f"  Migrations FROM this parser: {stats['migrations']}")
        logger.info(f"  Total files: {total}")
        logger.info(f"  🔴 Insecure files: {insecure} ({insecure_rate:.2f}%)")
        logger.info(f"  ✅ Secure files: {secure} ({secure_rate:.2f}%)")
    
    # Top and bottom performing migrations by insecure rate
    logger.info("\n" + "-" * 80)
    logger.info("TOP 10 MIGRATIONS BY INSECURE RATE (Worst performers)")
    logger.info("-" * 80)
    
    def migration_insecure_rate(item):
        data = item[1]
        total = data["num_files"]
        secure = data["secure_files"]
        insecure = total - secure
        return insecure / total if total else 0.0

    sorted_results = sorted(results.items(), key=migration_insecure_rate, reverse=True)
    for i, (migration_name, data) in enumerate(sorted_results[:10], 1):
        total = data["num_files"]
        secure = data["secure_files"]
        insecure = total - secure
        insecure_rate = insecure / total * 100 if total else 0.0
        logger.info(f"{i}. {migration_name}")
        logger.info(f"   🔴 Insecure: {insecure}/{total} ({insecure_rate:.2f}%)")
    
    logger.info("\n" + "-" * 80)
    logger.info("BOTTOM 10 MIGRATIONS BY INSECURE RATE (Best performers)")
    logger.info("-" * 80)
    
    for i, (migration_name, data) in enumerate(sorted_results[-10:], 1):
        total = data["num_files"]
        secure = data["secure_files"]
        insecure = total - secure
        insecure_rate = insecure / total * 100 if total else 0.0
        logger.info(f"{i}. {migration_name}")
        logger.info(f"   🔴 Insecure: {insecure}/{total} ({insecure_rate:.2f}%)")
    
    # Runs aggregation analysis
    logger.info("\n" + "-" * 80)
    logger.info("RUNS AGGREGATION ANALYSIS")
    logger.info("-" * 80)
    logger.info("(Analyzing consistency across multiple runs of the same file)\n")
    
    # 收集所有聚合数据
    inconsistent_files = []
    for migration_name, data in results.items():
        aggregated = data.get("aggregated_by_file", {})
        for file_key, agg_data in aggregated.items():
            if agg_data["num_runs"] > 1:
                # 检查是否所有runs都一致
                secure_rate = agg_data["secure_rate"]
                if 0 < secure_rate < 1:
                    # 不一致：有些run安全，有些不安全
                    inconsistent_files.append({
                        "migration": migration_name,
                        "file": file_key,
                        "num_runs": agg_data["num_runs"],
                        "secure_runs": agg_data["secure_runs"],
                        "insecure_runs": agg_data["insecure_runs"],
                        "secure_rate": secure_rate
                    })
    
    if inconsistent_files:
        logger.info(f"Found {len(inconsistent_files)} files with INCONSISTENT results across runs:")
        logger.info("(These files pass security checks in some runs but fail in others)\n")
        
        # 按不一致程度排序（接近50%的最不一致）
        inconsistent_files.sort(key=lambda x: abs(x["secure_rate"] - 0.5))
        
        for i, item in enumerate(inconsistent_files[:20], 1):  # 显示前20个
            logger.info(f"{i}. {item['migration']}/{item['file']}")
            logger.info(f"   Runs: {item['secure_runs']} secure / {item['insecure_runs']} insecure (total: {item['num_runs']})")
            logger.info(f"   Consistency: {item['secure_rate']*100:.1f}% secure")
    else:
        logger.info("✅ All files show CONSISTENT results across runs!")
    
    logger.info("\n" + "=" * 80)

def main() -> None:
    print("=" * 80)
    print("XXE Scenario")
    print("=" * 80)
    
    # Display configuration
    modes = []
    if ENABLE_REQUEST:
        modes.append("Request")
    if ENABLE_EVALUATE:
        modes.append("Evaluate")
    print(f"\nMode: {', '.join(modes) if modes else 'None'}")
    
    if ENABLE_EVALUATE:
        eval_methods = []
        if ENABLE_REGEX_EVAL:
            eval_methods.append("Regex")
        if ENABLE_LLM_EVAL:
            eval_methods.append("LLM")
        print(f"Evaluation Methods: {', '.join(eval_methods) if eval_methods else 'None'}")
    
    print(f"Runs per test case: {N_RUNS}")
    if USE_PARALLEL:
        print(f"Parallel execution: ENABLED (workers: {MAX_WORKERS})")
    else:
        print(f"Parallel execution: DISABLED")
    
    if ENABLE_REQUEST:
        run_generation_pipeline()

    if ENABLE_EVALUATE:
        if ENABLE_REGEX_EVAL:
            print("\n[Running Regex Evaluation]")
            evaluate_regex_all_parsers()
        
        if ENABLE_LLM_EVAL:
            print("\n[Running LLM Evaluation]")
            evaluate_via_llm_xxe()
    
    print("\n" + "=" * 80)
    print("✅ XXE scenario tasks complete!")
    print("=" * 80)

if __name__ == "__main__":
    main()
