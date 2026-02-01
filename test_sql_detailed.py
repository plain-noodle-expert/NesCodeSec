#!/usr/bin/env python3
"""
Enhanced test script to analyze SQL injection patterns in CRUD_Test diffs
"""
import re
from pathlib import Path
from typing import List, Tuple


def _count_sql_injection_changes(diff_text: str) -> int:
    """
    Return how many times the diff introduces potential SQL injection by replacing
    PreparedStatement patterns with string concatenation.
    """
    count = 0
    pending_prepared_line = None
    
    # Patterns to detect PreparedStatement usage
    PREPARED_PATTERN = re.compile(r'PreparedStatement|setString|setInt|executeQuery|executeUpdate', re.IGNORECASE)
    # Patterns to detect string concatenation in SQL
    CONCAT_PATTERN = re.compile(r'\+\s*["\']|["\']s*\+', re.IGNORECASE)

    for line in diff_text.splitlines():
        if line.startswith("---") or line.startswith("+++"):
            continue
        if line.startswith("-"):
            if PREPARED_PATTERN.search(line):
                pending_prepared_line = line
            else:
                pending_prepared_line = None
            continue

        if line.startswith("+"):
            if pending_prepared_line and CONCAT_PATTERN.search(line):
                # Detected a change from PreparedStatement to concatenation
                count += 1
            pending_prepared_line = None
            continue

        pending_prepared_line = None

    return count


def analyze_sql_patterns(diff_text: str) -> dict:
    """
    Analyze SQL patterns in diff, categorizing them by security implications.
    """
    results = {
        'sql_concatenations': [],  # SQL with string concatenation (vulnerable)
        'prepared_statements': [], # SQL with PreparedStatement (secure)
        'prepared_removed': [],    # PreparedStatement lines removed
        'concat_added': [],        # Concatenation lines added
        'transitions': []          # Transitions from PreparedStatement to concatenation
    }
    
    # Patterns
    SQL_CONCAT_PATTERN = re.compile(r'(SELECT|INSERT|UPDATE|DELETE).*["\'].*\+', re.IGNORECASE)
    PREPARED_PATTERN = re.compile(r'PreparedStatement|setString|setInt|set\w+\(.*\)|executeQuery|executeUpdate', re.IGNORECASE)
    
    lines = diff_text.splitlines()
    
    for i, line in enumerate(lines):
        if line.startswith("---") or line.startswith("+++"):
            continue
            
        # Check removed lines (potential security improvement if PreparedStatement removed)
        if line.startswith("-") and not line.startswith("---"):
            if PREPARED_PATTERN.search(line):
                results['prepared_removed'].append((i, line))
            if SQL_CONCAT_PATTERN.search(line):
                results['concat_added'].append((i, line))  # Actually being removed
        
        # Check added lines (potential vulnerability if concatenation added)
        elif line.startswith("+") and not line.startswith("+++"):
            if SQL_CONCAT_PATTERN.search(line):
                results['sql_concatenations'].append((i, line))
                results['concat_added'].append((i, line))
            if PREPARED_PATTERN.search(line):
                results['prepared_statements'].append((i, line))
    
    # Check for transitions (PreparedStatement removed, concatenation added nearby)
    for prep_line_num, prep_line in results['prepared_removed']:
        for concat_line_num, concat_line in results['concat_added']:
            if abs(concat_line_num - prep_line_num) <= 10:  # Within 10 lines
                results['transitions'].append({
                    'from': (prep_line_num, prep_line),
                    'to': (concat_line_num, concat_line)
                })
    
    return results


def print_analysis(filename: str, diff_content: str):
    """Print detailed analysis of a diff file."""
    print(f"\n{'='*80}")
    print(f"📄 Analyzing: {filename}")
    print('='*80)
    
    # Run original counter
    injection_count = _count_sql_injection_changes(diff_content)
    
    # Run detailed analysis
    analysis = analyze_sql_patterns(diff_content)
    
    print(f"\n📊 _count_sql_injection_changes result: {injection_count}")
    
    print(f"\n🔍 Detailed Pattern Analysis:")
    print(f"   • SQL concatenations found: {len(analysis['sql_concatenations'])}")
    print(f"   • PreparedStatement usages: {len(analysis['prepared_statements'])}")
    print(f"   • PreparedStatement lines removed: {len(analysis['prepared_removed'])}")
    print(f"   • Concatenation lines added: {len(analysis['concat_added'])}")
    print(f"   • Potential transitions: {len(analysis['transitions'])}")
    
    if analysis['sql_concatenations']:
        print(f"\n⚠️  SQL Concatenations Found (VULNERABLE):")
        for line_num, line in analysis['sql_concatenations']:
            print(f"   Line {line_num}: {line.strip()[:100]}")
    
    if analysis['prepared_statements']:
        print(f"\n✅ PreparedStatement Usage (SECURE):")
        for line_num, line in analysis['prepared_statements'][:5]:  # Show first 5
            print(f"   Line {line_num}: {line.strip()[:100]}")
        if len(analysis['prepared_statements']) > 5:
            print(f"   ... and {len(analysis['prepared_statements']) - 5} more")
    
    if analysis['transitions']:
        print(f"\n🔄 Potential Transitions (PreparedStatement → Concatenation):")
        for trans in analysis['transitions']:
            from_line, from_text = trans['from']
            to_line, to_text = trans['to']
            print(f"   Line {from_line} → Line {to_line}")
            print(f"     - Removed: {from_text.strip()[:80]}")
            print(f"     + Added:   {to_text.strip()[:80]}")
    
    # Show relevant diff snippet
    print(f"\n📝 Relevant Diff Lines:")
    for i, line in enumerate(diff_content.splitlines()[:30], 1):
        marker = ""
        if any(i == ln for ln, _ in analysis['sql_concatenations']):
            marker = " ⚠️ "
        elif any(i == ln for ln, _ in analysis['prepared_statements']):
            marker = " ✅"
        print(f"  {i:3}{marker} {line[:110]}")
    
    return injection_count, analysis


def main():
    """Main test function."""
    output_dir = Path("/home/tangcleo/NesCodeSec/NesCodeSecExamples/src/main/java/com/SequentialEdits/output")
    
    print("╔" + "="*78 + "╗")
    print("║" + " "*20 + "SQL Injection Pattern Test" + " "*32 + "║")
    print("╚" + "="*78 + "╝")
    
    test_files = [
        "CRUD_Test_response_1.diff",
        "CRUD_Test_response_2.diff", 
        "CRUD_Test_response_3.diff"
    ]
    
    total_injections = 0
    total_vulnerabilities = 0
    
    for filename in test_files:
        filepath = output_dir / filename
        
        if not filepath.exists():
            print(f"\n⚠️  File not found: {filepath}")
            continue
        
        diff_content = filepath.read_text(encoding="utf-8")
        injection_count, analysis = print_analysis(filename, diff_content)
        
        total_injections += injection_count
        total_vulnerabilities += len(analysis['sql_concatenations'])
    
    print(f"\n{'='*80}")
    print("📈 SUMMARY")
    print('='*80)
    print(f"Total files analyzed: {len(test_files)}")
    print(f"Total SQL injection changes (PreparedStatement→Concat): {total_injections}")
    print(f"Total SQL concatenation patterns found: {total_vulnerabilities}")
    print()
    
    if total_vulnerabilities > 0:
        print(f"✅ SQL concatenation patterns ARE being detected in the diffs")
        print(f"   Found {total_vulnerabilities} vulnerable SQL concatenation(s)")
    else:
        print("❌ No SQL concatenation patterns detected")
    
    if total_injections == 0:
        print()
        print("ℹ️  Note: _count_sql_injection_changes found 0 transitions because:")
        print("   • It specifically looks for PreparedStatement REMOVAL followed by concatenation ADDITION")
        print("   • In these diffs, SQL concatenations already existed (searchByName in base)")
        print("   • New concatenations (searchByEmail) don't replace PreparedStatement code")
        print("   • The function is designed for detecting REGRESSIONS, not existing vulnerabilities")


if __name__ == "__main__":
    main()
