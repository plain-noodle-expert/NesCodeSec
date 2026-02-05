#!/usr/bin/env python3
"""
验证所有XML parser的regex pattern规则文件

这个脚本检查每个parser的regex规则文件，确保：
1. 文件存在
2. 包含所有必需的pattern定义
3. Pattern能够正确编译为正则表达式
"""

import re
from pathlib import Path
from typing import Dict, List, Set

# 定义每个parser应该包含的pattern（基于zeta_xxe.py中的REQUIRED_RULE_GROUPS）
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

# Java pattern定义的正则表达式
JAVA_PATTERN_DECL = re.compile(
    r'public\s+static\s+final\s+String\s+(\w+_PATTERN)\s*=\s*(.+?);',
    re.DOTALL,
)

JAVA_STRING_LITERAL = re.compile(r'"(?:\\.|[^"\\])*"', re.DOTALL)


def extract_patterns_from_java_file(file_path: Path) -> Dict[str, str]:
    """从Java文件中提取所有pattern定义"""
    if not file_path.exists():
        return {}
    
    content = file_path.read_text(encoding='utf-8')
    patterns = {}
    
    for match in JAVA_PATTERN_DECL.finditer(content):
        pattern_name = match.group(1)
        pattern_expr = match.group(2)
        
        # 提取字符串字面量
        string_literals = JAVA_STRING_LITERAL.findall(pattern_expr)
        if string_literals:
            # 合并所有字符串字面量（去掉引号）
            pattern_value = ''.join(s[1:-1] for s in string_literals)
            patterns[pattern_name] = pattern_value
    
    return patterns


def validate_pattern(pattern_str: str) -> tuple[bool, str]:
    """验证pattern是否是有效的正则表达式"""
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
        
        # 提取所有patterns
        defined_patterns = extract_patterns_from_java_file(rules_file)
        print(f"   Found {len(defined_patterns)} pattern definitions")
        
        # 检查必需的patterns
        missing_patterns = required_patterns - set(defined_patterns.keys())
        extra_patterns = set(defined_patterns.keys()) - required_patterns
        
        if missing_patterns:
            print(f"\n⚠️  Missing required patterns:")
            for pattern in sorted(missing_patterns):
                print(f"   - {pattern}")
            all_valid = False
        
        if extra_patterns:
            print(f"\n📝 Additional patterns (not required but available):")
            for pattern in sorted(extra_patterns):
                print(f"   - {pattern}")
        
        # 验证每个pattern的正则表达式
        print(f"\n🔍 Validating pattern regex syntax:")
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
