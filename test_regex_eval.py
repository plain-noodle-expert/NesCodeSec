#!/usr/bin/env python3
"""
Test script for regex evaluation functionality in zeta_xxe.py

This script tests:
1. Loading regex rules from java files
2. Evaluating a sample file
3. Running full evaluation pipeline
"""

import sys
from pathlib import Path

# Add src directory to path
sys.path.insert(0, str(Path(__file__).parent.parent / "src"))

from zeta_xxe import load_regex_rules, evaluate_file_with_regex, REGEX_RULES_DIR, OUTPUT_DIR

def test_load_rules():
    """Test loading regex rules from DocumentBuilder_regex_rules.java"""
    print("=" * 80)
    print("TEST 1: Loading Regex Rules")
    print("=" * 80)
    
    parser_name = "DocumentBuilder"
    rules = load_regex_rules(parser_name)
    
    print(f"\nLoaded {len(rules)} rules for {parser_name}:")
    for rule_name in sorted(rules.keys()):
        print(f"  - {rule_name}")
    
    assert len(rules) > 0, "Should load at least some rules"
    assert "DISALLOW_DOCTYPE_PATTERN" in rules, "Should contain DISALLOW_DOCTYPE_PATTERN"
    print("\n✓ Test passed: Rules loaded successfully")
    return rules

def test_evaluate_sample_file():
    """Test evaluating a sample output file"""
    print("\n" + "=" * 80)
    print("TEST 2: Evaluating Sample File")
    print("=" * 80)
    
    # Find a sample file
    sample_dirs = list(OUTPUT_DIR.glob("*__TO__DocumentBuilder"))
    if not sample_dirs:
        print("\n⚠ Warning: No sample directories found. Skipping this test.")
        return
    
    sample_dir = sample_dirs[0]
    java_files = list(sample_dir.glob("*.java"))
    
    if not java_files:
        print(f"\n⚠ Warning: No Java files found in {sample_dir}. Skipping this test.")
        return
    
    sample_file = java_files[0]
    print(f"\nEvaluating file: {sample_file}")
    
    # Load DocumentBuilder rules
    rules = load_regex_rules("DocumentBuilder")
    
    # Evaluate the file
    result = evaluate_file_with_regex(sample_file, "DocumentBuilder", rules)
    
    print(f"\nResults:")
    print(f"  File: {result['file']}")
    print(f"  Parser: {result['parser']}")
    print(f"  Score: {result['score']:.2%}")
    print(f"  Matched: {result['matched_count']} / {result['total_rules']}")
    print(f"  Matched rules: {', '.join(result['matched_rules'][:5])}...")
    
    assert 0 <= result['score'] <= 1.0, "Score should be between 0 and 1"
    print("\n✓ Test passed: File evaluation successful")

def test_all_parsers_rules():
    """Test loading rules for all parsers"""
    print("\n" + "=" * 80)
    print("TEST 3: Loading Rules for All Parsers")
    print("=" * 80)
    
    parsers = ["DocumentBuilder", "SAXParser", "SAXBuilder", "SAXReader", "InputFactory", "Digester"]
    
    all_loaded = True
    for parser in parsers:
        rules = load_regex_rules(parser)
        status = "✓" if len(rules) > 0 else "✗"
        print(f"{status} {parser}: {len(rules)} rules")
        if len(rules) == 0:
            all_loaded = False
    
    assert all_loaded, "All parsers should have rules loaded"
    print("\n✓ Test passed: All parser rules loaded successfully")

def main():
    """Run all tests"""
    print("\n" + "=" * 80)
    print("REGEX EVALUATION TEST SUITE")
    print("=" * 80)
    
    try:
        # Test 1: Load rules
        rules = test_load_rules()
        
        # Test 2: Evaluate sample file
        test_evaluate_sample_file()
        
        # Test 3: Load all parser rules
        test_all_parsers_rules()
        
        print("\n" + "=" * 80)
        print("ALL TESTS PASSED ✓")
        print("=" * 80)
        print("\nYou can now run the full evaluation with:")
        print("  python src/zeta_xxe.py")
        print("\nor from Python:")
        print("  from zeta_xxe import evaluate_regex_all_parsers")
        print("  results = evaluate_regex_all_parsers()")
        
    except Exception as e:
        print("\n" + "=" * 80)
        print(f"TEST FAILED ✗")
        print("=" * 80)
        print(f"\nError: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()
