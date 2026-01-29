#!/usr/bin/env python3
"""
Test script for LLM evaluation functionality in zeta_xxe.py

This script tests:
1. Loading LLM prompts from llm_evaluation directory
2. Running LLM evaluation for XXE migrations
"""

import sys
from pathlib import Path

# Add src directory to path
sys.path.insert(0, str(Path(__file__).parent.parent / "src"))

from zeta_xxe import load_llm_prompt, LLM_EVAL_DIR, OUTPUT_DIR

def test_load_prompts():
    """Test loading LLM prompts for all parsers"""
    print("=" * 80)
    print("TEST 1: Loading LLM Prompts")
    print("=" * 80)
    
    parsers = ["DocumentBuilder", "SAXParser", "SAXBuilder", "SAXReader", "InputFactory", "Digester"]
    
    all_loaded = True
    for parser in parsers:
        prompt = load_llm_prompt(parser)
        status = "✓" if len(prompt) > 0 else "✗"
        print(f"{status} {parser}: {len(prompt)} characters")
        if len(prompt) == 0:
            all_loaded = False
        else:
            # Print first few lines of prompt
            lines = prompt.split('\n')
            print(f"   First line: {lines[0][:60]}...")
    
    assert all_loaded, "All parsers should have prompts loaded"
    print("\n✓ Test passed: All parser prompts loaded successfully")

def test_prompt_structure():
    """Test that prompts contain required sections"""
    print("\n" + "=" * 80)
    print("TEST 2: Validating Prompt Structure")
    print("=" * 80)
    
    parser = "DocumentBuilder"
    prompt = load_llm_prompt(parser)
    
    required_sections = ["<role>", "<task>", "<output>", "<examples>"]
    
    for section in required_sections:
        if section in prompt:
            print(f"✓ Section '{section}' found")
        else:
            print(f"✗ Section '{section}' NOT found")
            assert False, f"Required section {section} not found in prompt"
    
    print("\n✓ Test passed: Prompt structure is valid")

def test_output_directories():
    """Test that output directories exist and have correct format"""
    print("\n" + "=" * 80)
    print("TEST 3: Checking Output Directories")
    print("=" * 80)
    
    output_dirs = [d for d in OUTPUT_DIR.iterdir() 
                   if d.is_dir() and "__TO__" in d.name and not d.name.endswith("_diff")]
    
    print(f"\nFound {len(output_dirs)} migration directories:")
    
    for i, dir in enumerate(sorted(output_dirs)[:5], 1):
        parts = dir.name.split("__TO__")
        java_files = list(dir.glob("*.java"))
        print(f"{i}. {dir.name}")
        print(f"   Source: {parts[0]}, Target: {parts[1]}")
        print(f"   Java files: {len(java_files)}")
    
    if len(output_dirs) > 5:
        print(f"   ... and {len(output_dirs) - 5} more")
    
    assert len(output_dirs) > 0, "Should find at least some output directories"
    print("\n✓ Test passed: Output directories are valid")

def main():
    """Run all tests"""
    print("\n" + "=" * 80)
    print("LLM EVALUATION TEST SUITE")
    print("=" * 80)
    
    try:
        # Test 1: Load prompts
        test_load_prompts()
        
        # Test 2: Validate prompt structure
        test_prompt_structure()
        
        # Test 3: Check output directories
        test_output_directories()
        
        print("\n" + "=" * 80)
        print("ALL TESTS PASSED ✓")
        print("=" * 80)
        print("\nYou can now run the LLM evaluation with:")
        print("  python src/zeta_xxe.py --eval")
        print("\nor from Python:")
        print("  from zeta_xxe import evaluate_via_llm_xxe")
        print("  results = evaluate_via_llm_xxe()")
        print("\nNote: Make sure to set OPENROUTER_API_KEY in your .env file")
        
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
