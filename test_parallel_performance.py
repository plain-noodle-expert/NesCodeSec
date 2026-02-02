#!/usr/bin/env python3
"""
并行执行性能测试脚本

测试串行 vs 并行执行的性能差异
"""

import time
import argparse
from pathlib import Path

def test_serial_vs_parallel():
    """比较串行和并行执行的时间"""
    
    print("="*80)
    print("性能测试：串行 vs 并行")
    print("="*80)
    
    # 测试配置
    test_scenario = "v5"  # ContextMismatch
    test_runs = 5  # 使用较少的runs进行快速测试
    
    print(f"\n测试场景: {test_scenario}")
    print(f"运行次数: {test_runs}")
    
    # 1. 串行执行
    print("\n" + "-"*80)
    print("1. 串行执行测试")
    print("-"*80)
    serial_start = time.time()
    
    # TODO: 实际运行命令
    print("  命令: python main.py --scenario {} --runs {} --no-evaluate".format(
        test_scenario, test_runs
    ))
    print("  (需要手动运行)")
    
    serial_time = time.time() - serial_start
    print(f"  串行执行时间: {serial_time:.2f}秒")
    
    # 2. 并行执行
    print("\n" + "-"*80)
    print("2. 并行执行测试 (4 workers)")
    print("-"*80)
    parallel_start = time.time()
    
    # TODO: 实际运行命令
    print("  命令: python main.py --scenario {} --runs {} --parallel --workers 4 --no-evaluate".format(
        test_scenario, test_runs
    ))
    print("  (需要手动运行)")
    
    parallel_time = time.time() - parallel_start
    print(f"  并行执行时间: {parallel_time:.2f}秒")
    
    # 3. 对比
    print("\n" + "="*80)
    print("性能对比")
    print("="*80)
    if serial_time > 0:
        speedup = serial_time / parallel_time if parallel_time > 0 else float('inf')
        print(f"串行时间: {serial_time:.2f}秒")
        print(f"并行时间: {parallel_time:.2f}秒")
        print(f"提速倍数: {speedup:.2f}x")
        print(f"时间节省: {((1 - parallel_time/serial_time) * 100):.1f}%")
    
    print("\n建议:")
    print("• 对于少量test cases (<5), 串行执行即可")
    print("• 对于中等规模 (5-20 test cases), 使用4个workers")
    print("• 对于大规模 (>20 test cases), 使用6-8个workers")


def main():
    parser = argparse.ArgumentParser(description="并行执行性能测试")
    parser.add_argument("--scenario", default="v5", help="测试场景")
    parser.add_argument("--runs", type=int, default=5, help="运行次数")
    
    args = parser.parse_args()
    
    print("\n⚠️  请分别手动运行以下命令并记录时间:\n")
    print("1. 串行:")
    print(f"   time python main.py --scenario {args.scenario} --runs {args.runs} --no-evaluate\n")
    print("2. 并行 (4 workers):")
    print(f"   time python main.py --scenario {args.scenario} --runs {args.runs} --parallel --workers 4 --no-evaluate\n")
    print("3. 并行 (8 workers):")
    print(f"   time python main.py --scenario {args.scenario} --runs {args.runs} --parallel --workers 8 --no-evaluate\n")


if __name__ == "__main__":
    main()
