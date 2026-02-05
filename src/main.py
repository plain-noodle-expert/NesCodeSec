#!/usr/bin/env python3
"""
NesCodeSec CLI Tool

A command-line interface for running various security scenario tests.
"""

import os
import sys
import argparse
from pathlib import Path
from typing import Dict, Any

# Available scenarios mapping
SCENARIOS = {
    "v1": {
        "name": "V1-InsecureAlgorithmRecommendation",
        "module": "V1_algorithm",
        "description": "Test insecure algorithm recommendations",
    },
    "v2": {
        "name": "V2-RecentlyViewed",
        "module": "V2_recently_viewed",
        "description": "Test recently viewed code influence",
    },
    "v3": {
        "name": "V3-VulnerabilityPropagation",
        "module": "V3_vulnerability_propagation",
        "description": "Test vulnerability propagation",
    },
    "v4": {
        "name": "V4-Undo",
        "module": "V4_undo",
        "description": "Test undo operation vulnerabilities",
    },
    "v5": {
        "name": "V5-ContextMismatch",
        "module": "V5_context_mismatch",
        "description": "Test context mismatch vulnerabilities",
    },
    "v6": {
        "name": "V6-CrossFile",
        "module": "V6_cross_file",
        "description": "Test cross-file path traversal",
    },
    "v7": {
        "name": "V7-TransactionRefactor",
        "module": "V7_transaction",
        "description": "Test transaction refactoring issues",
    },
    "v8": {
        "name": "V8-MethodRefactor",
        "module": "V8_method_refactor",
        "description": "Test method refactoring security issues",
    },
    "v9": {
        "name": "V9-XXE",
        "module": "V9_xxe",
        "description": "Test XML external entity vulnerabilities",
    },
    "v10": {
        "name": "V10-PartialRemediationFallacy",
        "module": "V10_partial_remediation_fallacy",
        "description": "Test partial remediation fallacy",
    },
    "v11": {
        "name": "V11-NoOp",
        "module": "V11_no_op",
        "description": "Test trust drift / no-op iterations",
    },
    "v12": {
        "name": "V12-SequentialEdits",
        "module": "V12_sequential_edits",
        "description": "Test sequential edits",
    },
}


def run_scenario(scenario_module: str, config: Dict[str, Any]) -> None:
    """
    Dynamically import and run a scenario module with given configuration.
    
    Args:
        scenario_module: Module name (e.g., 'V1_algorithm')
        config: Configuration dictionary with parameters
    """
    try:
        # Import the module
        module = __import__(scenario_module, fromlist=['main'])
        
        # Set configuration parameters as module-level variables
        for key, value in config.items():
            if hasattr(module, key):
                setattr(module, key, value)
                print(f"  Set {key} = {value}")
        
        # Run the main function
        print(f"\n{'=' * 80}")
        print(f"Running {scenario_module}...")
        print('=' * 80)
        module.main()
        
    except ImportError as e:
        print(f"Error: Could not import module '{scenario_module}': {e}")
        sys.exit(1)
    except AttributeError as e:
        print(f"Error: Module '{scenario_module}' does not have a main() function: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"Error running scenario: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


def list_scenarios() -> None:
    """Print all available scenarios."""
    print("\nAvailable scenarios:")
    print("=" * 80)
    for key, info in sorted(SCENARIOS.items()):
        print(f"  {key:15} - {info['description']}")
    print("=" * 80)


def main():
    parser = argparse.ArgumentParser(
        description="NesCodeSec: Run security scenario tests",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Run v1 scenario with default settings (request and evaluate enabled)
  python main.py --scenario v1
  
  # Run all scenarios
  python main.py --all
  
  # Enable parallel execution (much faster!)
  python main.py --scenario v5 --parallel --workers 4
  
  # Run all scenarios with parallel execution
  python main.py --all --parallel --workers 6
  
  # Run with custom configuration
  python main.py --scenario v9 --no-request --enable-llm
  
  # Run only evaluation (skip requests)
  python main.py --scenario v6 --no-request
  
  # Run only regex evaluation
  python main.py --scenario v4 --no-llm
  
  # Set custom number of runs
  python main.py --scenario v1 --runs 5
  
  # Parallel execution with fewer runs (fast testing)
  python main.py --scenario v5 --runs 3 --parallel --workers 2
  
  # List all available scenarios
  python main.py --list
"""
    )
    
    # Scenario selection
    parser.add_argument(
        "--scenario", "-s",
        type=str,
        choices=list(SCENARIOS.keys()),
        help="Scenario to run"
    )
    
    parser.add_argument(
        "--all", "-a",
        action="store_true",
        help="Run all available scenarios"
    )
    
    parser.add_argument(
        "--list", "-l",
        action="store_true",
        help="List all available scenarios"
    )
    
    # Mode configuration
    parser.add_argument(
        "--enable-request",
        action="store_true",
        default=True,
        help="Enable request mode (generate LLM responses) [default: enabled]"
    )
    
    parser.add_argument(
        "--no-request",
        action="store_true",
        help="Disable request mode"
    )
    
    parser.add_argument(
        "--enable-evaluate",
        action="store_true",
        default=True,
        help="Enable evaluation mode [default: enabled]"
    )
    
    parser.add_argument(
        "--no-evaluate",
        action="store_true",
        help="Disable evaluation mode"
    )
    
    # Evaluation method configuration
    parser.add_argument(
        "--enable-regex",
        action="store_true",
        default=None,
        help="Enable regex evaluation"
    )
    
    parser.add_argument(
        "--no-regex",
        action="store_true",
        help="Disable regex evaluation"
    )
    
    parser.add_argument(
        "--enable-llm",
        action="store_true",
        default=None,
        help="Enable LLM evaluation"
    )
    
    parser.add_argument(
        "--no-llm",
        action="store_true",
        help="Disable LLM evaluation"
    )
    
    # Other parameters
    parser.add_argument(
        "--runs", "-n",
        type=int,
        help="Number of runs per test case (default: 10)"
    )
    
    parser.add_argument(
        "--parallel", "-p",
        action="store_true",
        help="Enable parallel execution for multiple runs (much faster)"
    )
    
    parser.add_argument(
        "--workers", "-w",
        type=int,
        default=4,
        help="Number of parallel workers when --parallel is enabled (default: 4)"
    )
    
    args = parser.parse_args()
    
    # Handle --list
    if args.list:
        list_scenarios()
        return
    
    # Handle --all
    if args.all:
        scenarios_to_run = list(SCENARIOS.keys())
    elif args.scenario:
        scenarios_to_run = [args.scenario]
    else:
        parser.print_help()
        print("\nError: --scenario or --all is required (or use --list to see available scenarios)")
        sys.exit(1)
    
    # Build configuration
    config = {}
    
    # Handle request mode (default: True)
    if args.no_request:
        config['ENABLE_REQUEST'] = False
    elif args.enable_request or not hasattr(args, 'no_request'):
        config['ENABLE_REQUEST'] = True
    
    # Handle evaluate mode (default: True)
    if args.no_evaluate:
        config['ENABLE_EVALUATE'] = False
    elif args.enable_evaluate or not hasattr(args, 'no_evaluate'):
        config['ENABLE_EVALUATE'] = True
    
    # Handle regex evaluation
    if args.no_regex:
        config['ENABLE_REGEX_EVAL'] = False
    elif args.enable_regex:
        config['ENABLE_REGEX_EVAL'] = True
    
    # Handle LLM evaluation
    if args.no_llm:
        config['ENABLE_LLM_EVAL'] = False
    elif args.enable_llm:
        config['ENABLE_LLM_EVAL'] = True
    
    # Handle number of runs
    if args.runs is not None:
        config['N_RUNS'] = args.runs
    
    # Handle parallel execution
    if args.parallel:
        config['USE_PARALLEL'] = True
        config['MAX_WORKERS'] = args.workers
    
    # Run scenarios
    for scenario_key in scenarios_to_run:
        # Get scenario info
        scenario_info = SCENARIOS[scenario_key]
        
        # Print configuration (only for first scenario to avoid repetition)
        if scenario_key == scenarios_to_run[0]:
            print("\nConfiguration:")
            if config:
                for key, value in config.items():
                    print(f"  {key} = {value}")
            else:
                print("  Using default settings from module")
        
        # Run the scenario
        run_scenario(scenario_info['module'], config)


if __name__ == "__main__":
    main()
