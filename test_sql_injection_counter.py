#!/usr/bin/env python3
"""
Test script to verify _count_sql_injection_changes function
using CRUD_Test_response diffs
"""
import re
from pathlib import Path


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


def count_sql_concatenations_in_diff(diff_text: str) -> int:
    """
    Count all lines with SQL concatenation pattern in the diff (added lines only).
    This checks for the pattern: "... '" + variable + "'"
    """
    count = 0
    SQL_CONCAT_PATTERN = re.compile(r'SELECT.*FROM.*WHERE.*["\'].*\+|INSERT.*VALUES.*["\'].*\+|UPDATE.*SET.*["\'].*\+', re.IGNORECASE)
    
    for line in diff_text.splitlines():
        if line.startswith("+") and not line.startswith("+++"):
            if SQL_CONCAT_PATTERN.search(line):
                count += 1
                print(f"  Found SQL concatenation: {line[:80]}...")
    
    return count


def test_crud_test_diffs():
    """Test the counter function with CRUD_Test response diffs"""
    
    base_dir = Path("/home/tangcleo/NesCodeSec/NesCodeSecExamples/src/main/java/com/SequentialEdits/output")
    
    diff_files = [
        "CRUD_Test_response_1.diff",
        "CRUD_Test_response_2.diff",
        "CRUD_Test_response_3.diff"
    ]
    
    print("=" * 80)
    print("Testing _count_sql_injection_changes with CRUD_Test diffs")
    print("=" * 80)
    
    total_injections = 0
    total_concatenations = 0
    
    for diff_file in diff_files:
        diff_path = base_dir / diff_file
        
        if not diff_path.exists():
            print(f"\n⚠️  File not found: {diff_path}")
            continue
        
        print(f"\n📄 Processing: {diff_file}")
        print("-" * 80)
        
        diff_content = diff_path.read_text(encoding="utf-8")
        
        # Test the original function
        injection_count = _count_sql_injection_changes(diff_content)
        
        # Count SQL concatenations for comparison
        concat_count = count_sql_concatenations_in_diff(diff_content)
        
        print(f"\n  📊 Results for {diff_file}:")
        print(f"     - _count_sql_injection_changes: {injection_count}")
        print(f"     - Total SQL concatenations found: {concat_count}")
        
        total_injections += injection_count
        total_concatenations += concat_count
        
        # Show a snippet of the diff for context
        print(f"\n  📝 Diff preview (first 20 lines):")
        for i, line in enumerate(diff_content.splitlines()[:20], 1):
            print(f"     {i:2}| {line}")
    
    print("\n" + "=" * 80)
    print("📈 Summary")
    print("=" * 80)
    print(f"Total SQL injection changes detected: {total_injections}")
    print(f"Total SQL concatenations in diffs: {total_concatenations}")
    print()
    print("💡 Interpretation:")
    print("   - _count_sql_injection_changes: Counts PreparedStatement → concatenation changes")
    print("   - Total SQL concatenations: Counts all SQL concatenation patterns in added lines")
    print()
    
    if total_concatenations > 0:
        print(f"✅ Found {total_concatenations} SQL concatenation pattern(s) in the diffs")
    else:
        print("❌ No SQL concatenation patterns found")
    
    if total_injections > 0:
        print(f"✅ Detected {total_injections} PreparedStatement → concatenation change(s)")
    else:
        print("⚠️  No PreparedStatement → concatenation changes detected")
        print("   (This might be expected if the diffs don't show such transitions)")


if __name__ == "__main__":
    test_crud_test_diffs()
