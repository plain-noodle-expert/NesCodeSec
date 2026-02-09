#!/usr/bin/env python3
"""
Validate regex pattern rule files for all XML parsers

This script checks each parser's regex rule file to ensure:
1. File exists
2. Contains all required pattern definitions
3. Patterns compile correctly as regular expressions
"""

import re
from pathlib import Path
from typing import Dict, Set

# Define patterns that each parser should contain (based on REQUIRED_RULE_GROUPS in zeta_xxe.py)
REQUIRED_PATTERNS: Dict[str, Set[str]] = {
    "DocumentBuilder": {
        "DISALLOW_DOCTYPE_PATTERN",
        "EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN",
        "LOAD_EXTERNAL_DTD_DISABLED_PATTERN",
        "EXPAND_ENTITY_REFERENCES_DISABLED_PATTERN",
        "FEATURE_SECURE_PROCESSING_PATTERN",
    },
    "SAXParser": {
        "DISALLOW_DOCTYPE_PATTERN",
        "EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN",
        "LOAD_EXTERNAL_DTD_DISABLED_PATTERN",
        "XINCLUDE_DISABLED_PATTERN",
        "FEATURE_SECURE_PROCESSING_PATTERN",
    },
    "SAXBuilder": {
        "DISALLOW_DOCTYPE_PATTERN",
        "APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN",
        "LOAD_EXTERNAL_DTD_DISABLED_PATTERN",
        "FEATURE_SECURE_PROCESSING_PATTERN",
    },
    "SAXReader": {
        "DISALLOW_DOCTYPE_PATTERN",
        "EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN",
        "LOAD_EXTERNAL_DTD_DISABLED_PATTERN",
        "ENTITY_RESOLVER_NULL_PATTERN",
        "ENTITY_RESOLVER_CUSTOM_PATTERN",
        "FEATURE_SECURE_PROCESSING_PATTERN",
    },
    "InputFactory": {
        "SUPPORT_DTD_DISABLED_PATTERN",
        "SUPPORT_DTD_DISABLED_STRING_PATTERN",
        "IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN",
        "IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN",
        "ACCESS_EXTERNAL_DTD_RESTRICTED_PATTERN",
        "ACCESS_EXTERNAL_DTD_RESTRICTED_STRING_PATTERN",
    },
    "Digester": {
        "DISALLOW_DOCTYPE_PATTERN",
        "SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN",
        "EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN",
        "LOAD_EXTERNAL_DTD_DISABLED_PATTERN",
        "ENTITY_RESOLVER_PATTERN",
    },
}

# Regex for Java pattern definitions
JAVA_PATTERN_DECL = re.compile(
    r'public\s+static\s+final\s+String\s+(\w+_PATTERN)\s*=\s*(.+?);',
    re.DOTALL,
)

JAVA_STRING_LITERAL = re.compile(r'"(?:\\.|[^"\\])*"', re.DOTALL)


def extract_patterns_from_java_file(file_path: Path) -> Dict[str, str]:
    """Extract all pattern definitions from Java file."""
    if not file_path.exists():
        return {}
    
    content = file_path.read_text(encoding='utf-8')
    patterns = {}
    
    for match in JAVA_PATTERN_DECL.finditer(content):
        pattern_name = match.group(1)
        pattern_expr = match.group(2)
        
        # Extract string literals
        string_literals = JAVA_STRING_LITERAL.findall(pattern_expr)
        if string_literals:
            # Concatenate all string literals (remove quotes)
            pattern_value = ''.join(s[1:-1] for s in string_literals)
            patterns[pattern_name] = pattern_value
    
    return patterns


def validate_pattern(pattern_str: str) -> tuple[bool, str]:
    """Validate if pattern is a valid regular expression."""
    try:
        re.compile(pattern_str)
        return True, "Valid"
    except re.error as e:
        return False, str(e)


def main():
    script_dir = Path(__file__).resolve().parent
    
    print("=" * 80)
    print("XML Parser Regex Pattern Validation")
    print("=" * 80)
    print()
    
    all_valid = True
    summary = []
    
    for parser_name, required_patterns in REQUIRED_PATTERNS.items():
        print(f"\n{'='*80}")
        print(f"Parser: {parser_name}")
        print(f"{'='*80}")
        
        rules_file = script_dir / f"{parser_name}_regex_rules.java"
        
        if not rules_file.exists():
            print(f"❌ ERROR: Rules file not found: {rules_file}")
            all_valid = False
            summary.append(f"❌ {parser_name}: Rules file missing")
            continue
        
        print(f"✅ Rules file found: {rules_file.name}")
        
        # Extract all patterns
        defined_patterns = extract_patterns_from_java_file(rules_file)
        print(f"   Found {len(defined_patterns)} pattern definitions")
        
        # Check required patterns
        missing_patterns = required_patterns - set(defined_patterns.keys())
        extra_patterns = set(defined_patterns.keys()) - required_patterns
        
        if missing_patterns:
            print("\n  Missing required patterns:")
            for pattern in sorted(missing_patterns):
                print(f"   - {pattern}")
            all_valid = False
        
        if extra_patterns:
            print("\n Additional patterns (not required but available):")
            for pattern in sorted(extra_patterns):
                print(f"   - {pattern}")
        
        # Validate regex syntax for each pattern
        print(f"\n Validating pattern regex syntax:")
        invalid_count = 0
        for pattern_name, pattern_value in sorted(defined_patterns.items()):
            is_valid, message = validate_pattern(pattern_value)
            if is_valid:
                print(f"   ✅ {pattern_name}")
            else:
                print(f"   ❌ {pattern_name}: {message}")
                invalid_count += 1
                all_valid = False
        
        # Summary for this parser
        if missing_patterns or invalid_count > 0:
            summary.append(f"❌ {parser_name}: {len(missing_patterns)} missing, {invalid_count} invalid")
        else:
            summary.append(f"✅ {parser_name}: All patterns valid")
    
    # Overall summary
    print("\n" + "=" * 80)
    print("VALIDATION SUMMARY")
    print("=" * 80)
    print()
    for line in summary:
        print(line)
    
    print("\n" + "=" * 80)
    if all_valid:
        print("✅ All regex pattern files are valid and complete!")
    else:
        print("❌ Some issues found. Please review the output above.")
    print("=" * 80)
    
    return 0 if all_valid else 1


if __name__ == "__main__":
    exit(main())
