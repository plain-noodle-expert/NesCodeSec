"""
XXE Security Rule Loader
Loads regex rules from Java files in the regex_evaluation_rule directory.
"""

import re
from pathlib import Path
from typing import Dict, Tuple
from loguru import logger


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent


def _regex_rules_dir() -> Path:
    """Get the regex_evaluation_rule directory"""
    return _project_root() / "NesCodeSecExamples" / "V9-XXE" / "regex_evaluation_rule"


def _load_java_patterns(java_file: Path) -> Dict[str, str]:
    """
    Load pattern constants from a Java file.
    Returns {PATTERN_NAME: pattern_string}
    """
    if not java_file.exists():
        logger.warning(f"Java pattern file not found: {java_file}")
        return {}
    
    content = java_file.read_text(encoding="utf-8")
    patterns = {}
    
    # Match pattern: public static final String PATTERN_NAME = "regex_string";
    pattern_regex = re.compile(
        r'public\s+static\s+final\s+String\s+(\w+_PATTERN)\s*=\s*"([^"]+)";',
        re.MULTILINE
    )
    
    for match in pattern_regex.finditer(content):
        pattern_name = match.group(1)
        pattern_value = match.group(2)
        patterns[pattern_name] = pattern_value
    
    return patterns


# Define rule groups for each parser
# Rule a: Disable DOCTYPE (recommended)
# Rule b: Disable external entities + external parameter entities + external DTD

SECURITY_RULE_GROUPS: Dict[str, Dict[str, Dict[str, Tuple[str, ...]]]] = {
    "DocumentBuilder": {
        "rule_a_disable_doctype": {
            "DisallowDOCTYPE": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",
            ),
        },
        "rule_b_disable_external_entities": {
            "DisableExternalGeneral": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
            ),
            "DisableExternalParameter": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",
            ),
            "DisableLoadExternalDTD": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/nonvalidating/load-external-dtd\"\s*,\s*false",
            ),
        },
    },
    "SAXParser": {
        "rule_a_disable_doctype": {
            "DisallowDOCTYPE": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",
            ),
        },
        "rule_b_disable_external_entities": {
            "DisableExternalGeneral": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
            ),
            "DisableExternalParameter": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",
            ),
            "DisableLoadExternalDTD": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/nonvalidating/load-external-dtd\"\s*,\s*false",
            ),
        },
    },
    "SAXBuilder": {
        "rule_a_disable_doctype": {
            "DisallowDOCTYPE": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",
            ),
        },
        "rule_b_disable_external_entities": {
            "DisableExternalGeneral": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
            ),
            "DisableExternalParameter": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",
            ),
            "DisableLoadExternalDTD": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/nonvalidating/load-external-dtd\"\s*,\s*false",
            ),
        },
    },
    "SAXReader": {
        "rule_a_disable_doctype": {
            "DisallowDOCTYPE": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",
            ),
        },
        "rule_b_disable_external_entities": {
            "DisableExternalGeneral": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
            ),
            "DisableExternalParameter": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",
            ),
            "DisableLoadExternalDTD": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/nonvalidating/load-external-dtd\"\s*,\s*false",
            ),
        },
    },
    "InputFactory": {
        "rule_a_disable_dtd": {
            "DisableDTDSupport": (
                r"{var}\.setProperty\s*\(\s*XMLInputFactory\.SUPPORT_DTD\s*,\s*false",
                r"{var}\.setProperty\s*\(\s*\"javax\.xml\.stream\.supportDTD\"\s*,\s*false",
            ),
        },
        "rule_b_disable_external_entities": {
            "DisableExternalEntities": (
                r"{var}\.setProperty\s*\(\s*XMLInputFactory\.IS_SUPPORTING_EXTERNAL_ENTITIES\s*,\s*false",
                r"{var}\.setProperty\s*\(\s*\"javax\.xml\.stream\.isSupportingExternalEntities\"\s*,\s*false",
            ),
        },
    },
    "Digester": {
        "rule_a_disable_doctype": {
            "DisallowDOCTYPE": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/disallow-doctype-decl\"\s*,\s*true",
            ),
        },
        "rule_b_disable_external_entities": {
            "DisableExternalGeneral": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-general-entities\"\s*,\s*false",
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/external-general-entities\"\s*,\s*false",
            ),
            "DisableExternalParameter": (
                r"{var}\.setFeature\s*\(\s*\"http://xml\.org/sax/features/external-parameter-entities\"\s*,\s*false",
            ),
            "DisableLoadExternalDTD": (
                r"{var}\.setFeature\s*\(\s*\"http://apache\.org/xml/features/nonvalidating/load-external-dtd\"\s*,\s*false",
            ),
        },
    },
}


def get_security_rule_groups() -> Dict[str, Dict[str, Dict[str, Tuple[str, ...]]]]:
    """
    Get security rule groups for all parsers.
    Returns the rule groups dictionary.
    """
    return SECURITY_RULE_GROUPS


def load_parser_patterns(parser_name: str) -> Dict[str, str]:
    """
    Load all pattern constants for a specific parser from its Java file.
    
    Args:
        parser_name: Name of the parser (e.g., "DocumentBuilder", "SAXParser")
    
    Returns:
        Dictionary of {PATTERN_NAME: pattern_string}
    """
    rules_dir = _regex_rules_dir()
    java_file = rules_dir / f"{parser_name}_regex_rules.java"
    
    patterns = _load_java_patterns(java_file)
    logger.info(f"Loaded {len(patterns)} patterns from {parser_name}_regex_rules.java")
    
    return patterns


def load_all_parser_patterns() -> Dict[str, Dict[str, str]]:
    """
    Load patterns for all parsers.
    
    Returns:
        Dictionary of {parser_name: {PATTERN_NAME: pattern_string}}
    """
    parsers = ["DocumentBuilder", "SAXParser", "SAXBuilder", "SAXReader", "InputFactory", "Digester"]
    all_patterns = {}
    
    for parser in parsers:
        all_patterns[parser] = load_parser_patterns(parser)
    
    return all_patterns


if __name__ == "__main__":
    # Test loading
    rule_groups = get_security_rule_groups()
    print(f"Loaded rule groups for {len(rule_groups)} parsers:")
    for parser, rules in rule_groups.items():
        print(f"\n{parser}:")
        for rule_name, requirements in rules.items():
            print(f"  - {rule_name}: {len(requirements)} requirements")
    
    print("\n" + "="*80)
    print("Testing pattern loading from Java files:")
    all_patterns = load_all_parser_patterns()
    for parser, patterns in all_patterns.items():
        print(f"{parser}: {len(patterns)} patterns loaded")
