import json
from openai import OpenAI
import difflib
import os
import re
from pathlib import Path
from loguru import logger
from tqdm import tqdm
from datetime import datetime
from typing import Dict, List, Tuple

from checker.java_parser import JavaSyntaxChecker
from request import PROMPT, create_event_batch, request_batch
from evaluation import evaluate_via_llm
from utils.batch_migrator import _udiff, strip_marker, debug_rebuild_from_migrate_full


client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")

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

BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "XXE"]
BASE_DIR = _subdir("base")
EVENT_DIR = _subdir("input_event")
MIGRATE_DIR = _subdir("migrate_full") # migrated full code
EXCERPT_DIR = _subdir("input_excerpt") # input excerpt to be completed
OUTPUT_DIR = _subdir("output")
REGEX_RULES_DIR = _root() / "regex_evaluation"
LLM_EVAL_DIR = _root() / "llm_evaluation"
JAVA_PATTERN_REGEX = re.compile(
    r'public\s+static\s+final\s+String\s+(\w+_PATTERN)\s*=\s*"((?:\\.|[^"\\])*)"',
    re.MULTILINE,
)

def get_response_content(response: str) -> str:
    return response.split("### Response:")[1][len("### Response:"):].strip()

def crop_response(filename: str, code_under_check: str, start: int, end: int, cursor_line: int) -> str:
    input_excerpt = code_under_check.splitlines()[start:cursor_line] + ["\t<|user_cursor_is_here|>"] + code_under_check.splitlines()[cursor_line:end+1]
    if not "<|editable_region_start|>" in input_excerpt:
        input_excerpt = ["<|editable_region_start|>"] + input_excerpt
    if not "<|start_of_file|>" in input_excerpt:
        input_excerpt = ["```<|start_of_file|>"] + input_excerpt
    if not filename in input_excerpt:
        input_excerpt = [filename] + input_excerpt
    if not "<|end_of_file|>" in input_excerpt:
        input_excerpt = input_excerpt + ["<|end_of_file|>```"]
        
    return "\n".join(input_excerpt)

def apply_change(start: int, end: int, new_text: str, original_text: str) -> str:
    original_lines = original_text.splitlines()
    return "\n".join(original_lines[:start] + [new_text] + original_lines[end+1:])

def request_zeta(client: OpenAI, prompt: str, original_text: str, start: int, end: int) -> str:
    try:
        resp = client.completions.create(
            model="zeta",
            prompt=prompt,
            max_tokens=10000,
            temperature=0.2,
        )
        response = strip_marker(resp.choices[0].text)
        code_under_check = apply_change(start, end, response, original_text)
        return code_under_check
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

SYNTAX_LOG_FILE = _subdir("syntax_log.json")

def generate_xxe_response() -> None:
    # 添加时间戳标记新的运行
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with open(SYNTAX_LOG_FILE, "a") as f:
        f.write("\n\n" + "="*20 + f" {timestamp} " + "="*20 + "\n")
    
    # 统计变量
    total_files = 0
    failed_syntax_files = 0
    
    try:
        # logger.info("===== Build prompt =====")
        for dir in tqdm(list(EVENT_DIR.iterdir()), desc=f"Build Prompt", unit="dir", bar_format='{l_bar}{bar:20}{r_bar}', ncols=100):
            for file in tqdm(list(dir.iterdir()), desc=f"Process {dir.name}", unit="file", bar_format='{l_bar}{bar:20}{r_bar}', ncols=100):
                total_files += 1
                
                with open(EVENT_DIR / dir.name / file.name, "r") as f:
                    input_event = f.read()
                with open(EXCERPT_DIR / dir.name / f"{file.stem}.java", "r") as f:
                    input_excerpt = f.read()
                    # Smart parsing of excerpt format: "start:end:content"
                    lines = input_excerpt.splitlines()
                    if not lines:
                        logger.warning(f"Empty excerpt file: {file.stem}.java")
                        failed_syntax_files += 1
                        continue
                    # Parse the range header (format: "start:end:content_start")
                    header_parts = lines[0].split(":")
                    if len(header_parts) < 4:
                        logger.error(f"Invalid excerpt format in {file.stem}.java: missing range info")
                        failed_syntax_files += 1
                        continue
                    try:
                        start, end, cursor_line = int(header_parts[0]), int(header_parts[1]), int(header_parts[2])
                        # Reconstruct content: header content + remaining lines
                        filename = [header_parts[3]] if len(header_parts) > 3 else []
                        filename.extend(lines[1:])
                        input_excerpt = "\n".join(filename)
                    except (ValueError, IndexError) as e:
                        logger.error(f"Failed to parse range from {file.stem}.java: {e}")
                        failed_syntax_files += 1
                        continue
                        
                prompt = PROMPT.format(input_event, input_excerpt)
                
                migrate_full_path = MIGRATE_DIR / dir.name / f"{file.stem}.java"
                migrate_full_text = migrate_full_path.read_text()
                code_under_check = request_zeta(client, prompt, migrate_full_text, start, end)
                # print(f"[CODE UNDER CHECK]\n{strip_marker(code_under_check)}")
                
                checker = JavaSyntaxChecker()
                MAX_RETRY = 3
                key = f"{dir.name}/{file.stem}"
                check_result = {}
                check_result[key] = checker.check(strip_marker(code_under_check))

                while check_result[key]["has_error"] and MAX_RETRY > 0:
                    input_excerpt = crop_response(f"{file.stem}.java", code_under_check, start, end, cursor_line)
                    code_under_check = request_zeta(client, prompt, migrate_full_text, start, end)
                    MAX_RETRY -= 1
                    check_result[key] = checker.check(strip_marker(code_under_check))

                # 只在 MAX_RETRY 耗尽且仍有错误时记录
                if MAX_RETRY == 0 and check_result[key]["has_error"]:
                    failed_syntax_files += 1
                    logger.error(f"Failed to generate valid Java code after 3 attempts for {key}, skipping...")
                    # 实时追加失败信息到 syntax_log.json
                    with open(SYNTAX_LOG_FILE, "a") as f:
                        f.write(json.dumps(check_result) + "\n")
                    continue
                
                output_path = OUTPUT_DIR / dir.name / f"{file.stem}.java"
                output_diff_path = OUTPUT_DIR / f"{dir.name}_diff" / f"{file.stem}.diff"
                output_path.parent.mkdir(parents=True, exist_ok=True)
                output_diff_path.parent.mkdir(parents=True, exist_ok=True)
                output_path.write_text(strip_marker(code_under_check))
                output_diff_path.write_text(_udiff(migrate_full_text, strip_marker(code_under_check), src_path="migrate_full", dst_path="zeta", context=5))
        
        # 输出统计结果
        success_files = total_files - failed_syntax_files
        success_rate = (success_files / total_files * 100) if total_files > 0 else 0
        
        logger.info("=" * 60)
        logger.info("SYNTAX CHECK STATISTICS")
        logger.info("=" * 60)
        logger.info(f"Total files processed: {total_files}")
        logger.info(f"Files passed syntax check: {success_files}")
        logger.info(f"Files failed syntax check: {failed_syntax_files}")
        logger.info(f"Success rate: {success_rate:.2f}%")
        logger.info("=" * 60)
        
    except Exception as e:
        logger.error("Failed to generate completion response: ", e)
        raise

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
    for match in JAVA_PATTERN_REGEX.finditer(content):
        rule_name = match.group(1)
        pattern_literal = match.group(2)
        
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

def evaluate_file_with_regex(file_path: Path, parser_name: str, rules: Dict[str, re.Pattern]) -> Dict:
    """
    Evaluate a single Java file against regex rules for the target parser.
    
    Args:
        file_path: Path to the Java file
        parser_name: Target parser name (used in result)
        rules: Dictionary of regex rules
    
    Returns:
        Dictionary with evaluation results
    """
    if not file_path.exists():
        return {
            "file": str(file_path),
            "error": "File not found",
            "score": 0.0,
            "matched_rules": [],
            "total_rules": len(rules)
        }
    
    content = file_path.read_text()
    matched_rules = []
    
    for rule_name, pattern in rules.items():
        if pattern.search(content):
            matched_rules.append(rule_name)
    
    score = len(matched_rules) / len(rules) if len(rules) > 0 else 0.0
    
    return {
        "file": file_path.name,
        "parser": parser_name,
        "score": score,
        "matched_rules": matched_rules,
        "total_rules": len(rules),
        "matched_count": len(matched_rules)
    }

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
    
    # Load all regex rules
    logger.info("Loading regex rules...")
    all_rules = {}
    for parser_name in parser_mapping.values():
        rules = load_regex_rules(parser_name)
        all_rules[parser_name] = rules
        logger.info(f"  {parser_name}: {len(rules)} security rules loaded")
    
    # Evaluate all migrations
    results = {}
    overall_stats = {
        "total_migrations": 0,
        "total_files": 0,
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
        
        target_rules = all_rules[target_parser]
        
        # Evaluate all Java files in this directory
        java_files = list(output_dir.glob("*.java"))
        file_results = []
        
        for java_file in java_files:
            result = evaluate_file_with_regex(java_file, target_parser, target_rules)
            file_results.append(result)
        
        # Calculate statistics for this migration
        if file_results:
            avg_score = sum(r["score"] for r in file_results) / len(file_results)
            total_matched = sum(r["matched_count"] for r in file_results)
            total_possible = len(file_results) * len(target_rules)
            
            migration_result = {
                "source_parser": source_parser,
                "target_parser": target_parser,
                "num_files": len(file_results),
                "avg_score": avg_score,
                "total_matched_rules": total_matched,
                "total_possible_rules": total_possible,
                "overall_coverage": total_matched / total_possible if total_possible > 0 else 0.0,
                "files": file_results
            }
            
            results[dir_name] = migration_result
            overall_stats["total_migrations"] += 1
            overall_stats["total_files"] += len(file_results)
            
            # Update by_target_parser stats
            if target_parser not in overall_stats["by_target_parser"]:
                overall_stats["by_target_parser"][target_parser] = {
                    "count": 0,
                    "total_files": 0,
                    "avg_scores": [],
                    "coverage_rates": []
                }
            
            overall_stats["by_target_parser"][target_parser]["count"] += 1
            overall_stats["by_target_parser"][target_parser]["total_files"] += len(file_results)
            overall_stats["by_target_parser"][target_parser]["avg_scores"].append(avg_score)
            overall_stats["by_target_parser"][target_parser]["coverage_rates"].append(migration_result["overall_coverage"])
            
            # Update by_source_parser stats
            if source_parser not in overall_stats["by_source_parser"]:
                overall_stats["by_source_parser"][source_parser] = {
                    "count": 0,
                    "total_files": 0,
                    "avg_scores": [],
                    "coverage_rates": []
                }
            
            overall_stats["by_source_parser"][source_parser]["count"] += 1
            overall_stats["by_source_parser"][source_parser]["total_files"] += len(file_results)
            overall_stats["by_source_parser"][source_parser]["avg_scores"].append(avg_score)
            overall_stats["by_source_parser"][source_parser]["coverage_rates"].append(migration_result["overall_coverage"])
    
    # Calculate summary statistics
    for parser_stats in overall_stats["by_target_parser"].values():
        if parser_stats["avg_scores"]:
            parser_stats["mean_score"] = sum(parser_stats["avg_scores"]) / len(parser_stats["avg_scores"])
            parser_stats["mean_coverage"] = sum(parser_stats["coverage_rates"]) / len(parser_stats["coverage_rates"])
    
    for parser_stats in overall_stats["by_source_parser"].values():
        if parser_stats["avg_scores"]:
            parser_stats["mean_score"] = sum(parser_stats["avg_scores"]) / len(parser_stats["avg_scores"])
            parser_stats["mean_coverage"] = sum(parser_stats["coverage_rates"]) / len(parser_stats["coverage_rates"])
    
    # Print summary
    print_regex_evaluation_summary(results, overall_stats, all_rules)
    
    # Save results to JSON
    output_file = OUTPUT_DIR / "regex_evaluation_results.json"
    with open(output_file, "w") as f:
        json.dump({
            "results": results,
            "overall_stats": overall_stats,
            "rules_count": {parser: len(rules) for parser, rules in all_rules.items()}
        }, f, indent=2, default=str)
    
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
    logger.info(f"Total Files Evaluated: {overall_stats['total_files']}")
    
    # Summary by target parser
    logger.info("\n" + "-" * 80)
    logger.info("RESULTS BY TARGET PARSER (What we migrated TO)")
    logger.info("-" * 80)
    
    for parser_name in sorted(overall_stats["by_target_parser"].keys()):
        stats = overall_stats["by_target_parser"][parser_name]
        logger.info(f"\n{parser_name}:")
        logger.info(f"  Number of migrations TO this parser: {stats['count']}")
        logger.info(f"  Total files: {stats['total_files']}")
        logger.info(f"  Average score: {stats['mean_score']:.2%}")
        logger.info(f"  Average coverage: {stats['mean_coverage']:.2%}")
        logger.info(f"  Total security rules: {len(all_rules.get(parser_name, {}))}")
    
    # Summary by source parser
    logger.info("\n" + "-" * 80)
    logger.info("RESULTS BY SOURCE PARSER (What we migrated FROM)")
    logger.info("-" * 80)
    
    for parser_name in sorted(overall_stats["by_source_parser"].keys()):
        stats = overall_stats["by_source_parser"][parser_name]
        logger.info(f"\n{parser_name}:")
        logger.info(f"  Number of migrations FROM this parser: {stats['count']}")
        logger.info(f"  Total files: {stats['total_files']}")
        logger.info(f"  Average score across all target parsers: {stats['mean_score']:.2%}")
        logger.info(f"  Average coverage across all target parsers: {stats['mean_coverage']:.2%}")
    
    # Top and bottom performing migrations
    logger.info("\n" + "-" * 80)
    logger.info("TOP 10 MIGRATIONS BY COVERAGE")
    logger.info("-" * 80)
    
    sorted_results = sorted(results.items(), key=lambda x: x[1]["overall_coverage"], reverse=True)
    for i, (migration_name, data) in enumerate(sorted_results[:10], 1):
        logger.info(f"{i}. {migration_name}")
        logger.info(f"   Coverage: {data['overall_coverage']:.2%} | Avg Score: {data['avg_score']:.2%} | Files: {data['num_files']}")
    
    logger.info("\n" + "-" * 80)
    logger.info("BOTTOM 10 MIGRATIONS BY COVERAGE")
    logger.info("-" * 80)
    
    for i, (migration_name, data) in enumerate(sorted_results[-10:], 1):
        logger.info(f"{i}. {migration_name}")
        logger.info(f"   Coverage: {data['overall_coverage']:.2%} | Avg Score: {data['avg_score']:.2%} | Files: {data['num_files']}")
    
    logger.info("\n" + "=" * 80)

def load_llm_prompt(parser_name: str) -> str:
    """
    Load the system prompt for a specific parser from llm_evaluation directory.
    
    Args:
        parser_name: Name of the parser (DocumentBuilder, SAXParser, etc.)
    
    Returns:
        The content of the .prompt file as a string
    """
    prompt_file = LLM_EVAL_DIR / f"{parser_name}.prompt"
    
    if not prompt_file.exists():
        logger.warning(f"LLM prompt file not found: {prompt_file}")
        return ""
    
    content = prompt_file.read_text(encoding="utf-8")
    logger.info(f"Loaded LLM prompt for {parser_name} ({len(content)} characters)")
    return content

def evaluate_via_llm_xxe() -> Dict:
    """
    Evaluate all XXE migrations using LLM evaluation.
    For each output/{ParserA__TO__ParserB}/ directory:
    - Load ParserA's system prompt from llm_evaluation/ParserA.prompt
    - Use that prompt to evaluate all files in the directory
    - Save results to output/{ParserA__TO__ParserB}/llm_evaluation_results.json
    
    Returns:
        Dictionary with evaluation results for all migrations
    """
    logger.info("=" * 80)
    logger.info("STARTING LLM EVALUATION FOR XXE MIGRATIONS")
    logger.info("=" * 80)
    
    # Parser name mapping
    parser_names = ["DocumentBuilder", "SAXParser", "SAXBuilder", "SAXReader", "InputFactory", "Digester"]
    
    # Load all prompts
    logger.info("\nLoading LLM prompts...")
    all_prompts = {}
    for parser_name in parser_names:
        prompt = load_llm_prompt(parser_name)
        if prompt:
            all_prompts[parser_name] = prompt
            logger.info(f"  ✓ {parser_name}: prompt loaded")
        else:
            logger.warning(f"  ✗ {parser_name}: prompt not found")
    
    # Find all migration directories
    output_dirs = [d for d in OUTPUT_DIR.iterdir() 
                   if d.is_dir() and "__TO__" in d.name and not d.name.endswith("_diff")]
    
    logger.info(f"\nFound {len(output_dirs)} migration directories to evaluate")
    
    all_results = {}
    overall_stats = {
        "total_migrations": 0,
        "total_files": 0,
        "by_source_parser": {}
    }
    
    # Custom LLM input format for XXE evaluation
    xxe_llm_input = """
### file_name: {file_name}
### code_excerpt:
{code_excerpt}
"""
    
    for output_dir in tqdm(sorted(output_dirs), desc="LLM Evaluation", unit="migration"):
        # Parse directory name: ParserA__TO__ParserB
        dir_name = output_dir.name
        parts = dir_name.split("__TO__")
        
        if len(parts) != 2:
            logger.warning(f"Invalid directory name format: {dir_name}")
            continue
        
        source_parser = parts[0]
        target_parser = parts[1]
        
        # Get prompt for SOURCE parser (we evaluate FROM the source parser's perspective)
        if source_parser not in all_prompts:
            logger.warning(f"No prompt found for source parser: {source_parser}, skipping {dir_name}")
            continue
        
        source_prompt = all_prompts[source_parser]
        
        logger.info(f"\nEvaluating {dir_name}")
        logger.info(f"  Source Parser: {source_parser}")
        logger.info(f"  Target Parser: {target_parser}")
        logger.info(f"  Using prompt from: {source_parser}.prompt")
        
        # Evaluate this migration directory using the source parser's prompt
        try:
            results = evaluate_via_llm(
                output_dir=output_dir,
                prompt=source_prompt,
                llm_input=xxe_llm_input,
                results_path=output_dir / "llm_evaluation_results.json",
                save_results=True
            )
            
            all_results[dir_name] = {
                "source_parser": source_parser,
                "target_parser": target_parser,
                "evaluation": results
            }
            
            # Update statistics
            overall_stats["total_migrations"] += 1
            overall_stats["total_files"] += results.get("total_files", 0)
            
            if source_parser not in overall_stats["by_source_parser"]:
                overall_stats["by_source_parser"][source_parser] = {
                    "migrations": [],
                    "total_files": 0,
                    "total_unsafe": 0
                }
            
            overall_stats["by_source_parser"][source_parser]["migrations"].append(dir_name)
            overall_stats["by_source_parser"][source_parser]["total_files"] += results.get("total_files", 0)
            overall_stats["by_source_parser"][source_parser]["total_unsafe"] += results.get("n_unsafe_files", 0)
            
            logger.info(f"  ✓ Completed: {results.get('total_files', 0)} files, "
                       f"{results.get('n_unsafe_files', 0)} flagged as unsafe")
            
        except Exception as e:
            logger.error(f"  ✗ Failed to evaluate {dir_name}: {e}")
            import traceback
            traceback.print_exc()
    
    # Print summary
    print_llm_evaluation_summary(all_results, overall_stats)
    
    # Save overall results
    overall_results_file = OUTPUT_DIR / "llm_evaluation_overall_results.json"
    with open(overall_results_file, "w", encoding="utf-8") as f:
        json.dump({
            "results": all_results,
            "overall_stats": overall_stats,
            "timestamp": datetime.now().isoformat()
        }, f, indent=2, default=str)
    
    logger.info(f"\nOverall results saved to: {overall_results_file}")
    
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
    
    # Summary by source parser
    logger.info("\n" + "-" * 80)
    logger.info("RESULTS BY SOURCE PARSER")
    logger.info("-" * 80)
    
    for parser_name in sorted(overall_stats["by_source_parser"].keys()):
        stats = overall_stats["by_source_parser"][parser_name]
        total_files = stats["total_files"]
        total_unsafe = stats["total_unsafe"]
        unsafe_rate = (total_unsafe / total_files * 100) if total_files > 0 else 0
        
        logger.info(f"\n{parser_name}:")
        logger.info(f"  Migrations FROM this parser: {len(stats['migrations'])}")
        logger.info(f"  Total files: {total_files}")
        logger.info(f"  Files flagged as UNSAFE: {total_unsafe}")
        logger.info(f"  Unsafe rate: {unsafe_rate:.2f}%")
        logger.info(f"  Migrations: {', '.join(stats['migrations'])}")
    
    # Show migrations with highest unsafe rates
    logger.info("\n" + "-" * 80)
    logger.info("MIGRATIONS WITH HIGHEST UNSAFE RATES")
    logger.info("-" * 80)
    
    migration_rates = []
    for migration_name, data in results.items():
        eval_data = data.get("evaluation", {})
        total = eval_data.get("total_files", 0)
        unsafe = eval_data.get("n_unsafe_files", 0)
        rate = (unsafe / total * 100) if total > 0 else 0
        migration_rates.append((migration_name, rate, unsafe, total))
    
    migration_rates.sort(key=lambda x: x[1], reverse=True)
    
    for i, (name, rate, unsafe, total) in enumerate(migration_rates[:10], 1):
        logger.info(f"{i}. {name}")
        logger.info(f"   Unsafe: {unsafe}/{total} ({rate:.2f}%)")
    
    logger.info("\n" + "=" * 80)

def main(request_only:bool=True, eval: bool = False, debug: bool = False, regex_eval: bool = False) -> None:
    from utils.batch_migrator import full_pipeline
    
    # if not request_only and not debug:
    #     # full pipeline to prepare input_event and input_excerpt
    #     full_pipeline(
    #         config_path="src/migrations_compilable.json",
    #         base_root=BASE_DIR,
    #         migrate_root=MIGRATE_DIR,
    #         event_root=EVENT_DIR,
    #         excerpt_root=EXCERPT_DIR
    #     )
    # if debug:
    #     stats = debug_rebuild_from_migrate_full(
    #         migrate_root=MIGRATE_DIR,
    #         base_root=BASE_DIR,
    #         event_root=EVENT_DIR,
    #         excerpt_root=EXCERPT_DIR
    #     )
    
    # Only run code generation if not doing regex_eval only
    if not regex_eval:
        generate_xxe_response()
    
    if eval:
        evaluate_via_llm_xxe()
    
    # Run regex evaluation
    if regex_eval:
        evaluate_regex_all_parsers()
    




if __name__ == "__main__":

    # Run with regex evaluation
    main(request_only=False, regex_eval=True)
    
