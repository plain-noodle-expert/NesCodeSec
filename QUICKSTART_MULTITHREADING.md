# 多线程评估快速入门

## ⚡ 快速开始

### 1. 默认运行（推荐）
```bash
python src/evaluation.py
```
- 自动使用 4 个线程
- 请求间隔 5 秒
- 适合 OpenRouter Free API

### 2. 高速模式
```bash
MAX_WORKERS=8 REQUEST_DELAY=3.0 python src/evaluation.py
```
- 8 个并发线程
- 3 秒请求间隔
- 适合付费 API

### 3. 保守模式（避免限速）
```bash
MAX_WORKERS=2 REQUEST_DELAY=10.0 python src/evaluation.py
```
- 2 个线程
- 10 秒间隔
- 遇到频繁 429 错误时使用

## 📊 查看进度

### 实时监控
```bash
# 另开一个终端窗口
tail -f evaluation_results.json.ndjson
```

### 统计完成数
```bash
wc -l evaluation_results.json.ndjson
```

## ⚙️ 环境变量配置

创建 `.env` 文件：
```bash
# 多线程配置
MAX_WORKERS=4
REQUEST_DELAY=5.0
RATE_LIMIT_WAIT=60

# API 配置
OPENROUTER_API_KEY=your_key_here

# 日志配置（可选）
SAVE_LOGS=true
LOG_FILE=evaluation.log
LOG_LEVEL=INFO
```

## 🎯 性能对比

| 场景 | 单线程 | 4线程 | 8线程 |
|------|--------|-------|-------|
| 10文件 | 3.0分钟 | 45秒 | 23秒 |
| 50文件 | 15分钟 | 3.8分钟 | 1.9分钟 |
| 100文件 | 30分钟 | 7.5分钟 | 3.8分钟 |

## 🔧 常见问题

### Q: 遇到很多 429 错误怎么办？
```bash
# 减少线程数和增加间隔
MAX_WORKERS=2 REQUEST_DELAY=10.0 python src/evaluation.py
```

### Q: 如何查看详细日志？
```bash
# 设置日志级别为 DEBUG
LOG_LEVEL=DEBUG python src/evaluation.py
```

### Q: 能否中断后继续？
可以！程序会自动跳过已完成的文件（基于 `.ndjson` 日志）

### Q: 本地模型如何配置？
```bash
# 本地模型通常没有限速
MAX_WORKERS=16 REQUEST_DELAY=1.0 python src/evaluation.py
```

## 📈 性能测试

运行性能对比工具：
```bash
python test/performance_comparison.py
```

## 📚 更多文档

- 详细配置: [MULTITHREADING_EVALUATION.md](MULTITHREADING_EVALUATION.md)
- 项目文档: [README.md](README.md)
