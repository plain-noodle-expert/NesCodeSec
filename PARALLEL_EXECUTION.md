# 并行执行优化 ⚡

## 🎯 问题分析

之前的10次request操作是**串行执行**的，每个test case的10次运行按顺序执行，导致：
- **耗时长**：假设每次request需要5秒，10次就是50秒，10个test cases就是500秒（~8分钟）
- **资源利用率低**：CPU和网络资源未充分利用

## ✅ 解决方案

### 1. 并行执行 (推荐 ⭐)

使用 `ThreadPoolExecutor` 实现多线程并行处理，可以同时发送多个请求。

#### 🚀 快速开始

```bash
# 启用并行执行（默认4个workers）
python main.py --scenario v5 --parallel

# 自定义workers数量（推荐4-8个）
python main.py --scenario v5 --parallel --workers 8

# 对所有场景启用并行
python main.py --all --parallel --workers 6

# 结合其他选项
python main.py --scenario v5 --runs 5 --parallel --workers 4 --no-llm
```

#### 速度提升

- **4 workers**: 理论提速 ~4倍
- **8 workers**: 理论提速 ~8倍（取决于服务器性能和并发限制）

**示例**：
- 串行：10个test cases × 10次runs × 5秒 = **500秒**
- 并行(4 workers)：(10×10)/4 × 5秒 = **125秒** ⚡

### 2. 减少runs数量

如果不需要10次运行，可以减少数量：

```bash
# 只运行5次
python main.py --scenario v5 --runs 5

# 结合并行
python main.py --scenario v5 --runs 5 --parallel --workers 4
```

### 3. 性能建议

#### Workers数量选择

| Workers | 适用场景 | 内存占用 |
|---------|---------|---------|
| 2-4 | 开发测试 | 低 |
| 4-8 | 正常运行 | 中等 |
| 8-16 | 高性能服务器 | 高 |

**注意事项**：
- workers数量不要超过CPU核心数的2倍
- 注意LLM服务器的并发限制
- 大量并行请求可能触发rate limiting

#### 最佳实践

```bash
# 开发调试：少量runs + 少量workers
python main.py --scenario v5 --runs 3 --parallel --workers 2

# 正常评估：标准配置
python main.py --scenario v5 --parallel --workers 4

# 批量运行：全场景并行
python main.py --all --parallel --workers 6
```

## 技术实现

### 核心函数

在 `request.py` 中新增：
```python
request_batch_multiple_runs_parallel(
    event_dir=event_dir,
    excerpt_dir=excerpt_dir,
    output_dir=output_dir,
    n_runs=10,
    max_workers=4,  # 并发线程数
)
```

### 已更新的文件

#### 支持并行的场景
- ✅ `zeta_context_mismatch.py` (V5-ContextMismatch)
- ⏳ 其他场景需要类似更新

#### 配置参数
每个zeta_*.py文件新增：
```python
USE_PARALLEL = False  # 默认关闭
MAX_WORKERS = 4       # 并行worker数量
```

## 下一步

为所有场景添加并行支持：
1. 在imports中添加 `request_batch_multiple_runs_parallel`
2. 添加 `USE_PARALLEL` 和 `MAX_WORKERS` 配置变量
3. 在request步骤中根据 `USE_PARALLEL` 选择函数

## 示例对比

### 串行执行
```bash
python main.py --scenario v5
# 耗时：~8分钟（假设10 test cases，10 runs，5秒/request）
```

### 并行执行
```bash
python main.py --scenario v5 --parallel --workers 4
# 耗时：~2分钟（4倍提速）⚡⚡⚡
```
