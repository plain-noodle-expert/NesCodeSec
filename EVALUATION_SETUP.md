# Evaluation.py 环境配置指南

## 🚀 快速开始

### 1. 环境设置

```bash
# 克隆项目后，首先设置环境
cp .env.example .env
```

### 2. 配置你的模型

编辑 `.env` 文件，根据你的需求选择配置方案：

#### 方案 A: 使用本地模型（推荐）
```env
LOCAL_MODEL_BASE_URL=http://localhost:8000/v1
LOCAL_MODEL_API_KEY=EMPTY
CUSTOM_JUDGER_MODELS=your-model-1,your-model-2,your-model-3
```

#### 方案 B: 使用 OpenRouter
```env
OPENROUTER_API_KEY=sk-or-v1-your-actual-key-here
CUSTOM_JUDGER_MODELS=deepseek/deepseek-chat-v3.1:free,qwen/qwen3-235b-a22b:free,openai/gpt-oss-120b:free
```

#### 方案 C: 混合使用
```env
LOCAL_MODEL_BASE_URL=http://localhost:8000/v1
LOCAL_MODEL_API_KEY=EMPTY
OPENROUTER_API_KEY=sk-or-v1-your-actual-key-here
CUSTOM_JUDGER_MODELS=local-model,deepseek/deepseek-chat-v3.1:free,openai/gpt-4o-mini
```

### 3. 安装依赖

```bash
# 安装 python-dotenv
conda install python-dotenv -c conda-forge
# 或
pip install python-dotenv
```

### 4. 运行评估

```bash
python src/evaluation.py
```

## 📁 文件说明

### `.env.example`
- **用途**: 配置模板文件，包含所有可用的环境变量
- **版本控制**: ✅ 应该提交到 Git
- **内容**: 包含示例值和详细注释

### `.env`
- **用途**: 实际的环境配置文件，包含真实的 API 密钥等敏感信息
- **版本控制**: ❌ 不应该提交到 Git（已在 .gitignore 中）
- **内容**: 你的实际配置值

## ⚙️ 配置参数详解

### 🔐 安全相关
```env
# API 密钥 - 请替换为真实值
OPENROUTER_API_KEY=sk-or-v1-your-actual-key-here
LOCAL_MODEL_API_KEY=EMPTY

# 本地服务器地址
LOCAL_MODEL_BASE_URL=http://localhost:8000/v1
```

### 🤖 模型配置
```env
# 评估模型列表（逗号分隔）
CUSTOM_JUDGER_MODELS=model1,model2,model3

# 模型参数
MODEL_TEMPERATURE=0.1    # 0.0-1.0, 越低越确定性
MAX_TOKENS=100          # 每次请求最大 token 数
REQUEST_TIMEOUT=30      # 请求超时时间（秒）
MAX_RETRIES=3          # 失败重试次数
```

### 📂 路径配置
```env
# 输入目录
RESPONSE_ROOT=NesCodeSecExamples/src/main/java/com/Scenario1/output
PROMPT_ROOT=NesCodeSecExamples/src/main/java/com/Scenario1/evaluation

# 输出文件
EVALUATION_OUTPUT_FILE=evaluation_results.json
```

### 📝 日志配置
```env
LOG_LEVEL=INFO                    # DEBUG, INFO, WARNING, ERROR
DEBUG_REQUESTS=false             # 是否记录详细的请求信息
SAVE_LOGS=false                  # 是否保存日志到文件
LOG_FILE=evaluation.log          # 日志文件名
```

## 🛠️ 最佳实践

### 1. 环境文件管理
- ✅ 总是从 `.env.example` 复制创建 `.env`
- ✅ 在 `.env.example` 中使用示例值，不要放真实密钥
- ✅ 确保 `.env` 在 `.gitignore` 中
- ✅ 团队成员各自维护自己的 `.env` 文件

### 2. 安全建议
- 🔐 永远不要将真实的 API 密钥提交到版本控制
- 🔐 使用环境变量而不是硬编码密钥
- 🔐 定期轮换 API 密钥
- 🔐 为不同环境（开发/测试/生产）使用不同的密钥

### 3. 配置建议
- 🎯 在开发时使用较低的 `MODEL_TEMPERATURE` 确保一致性
- 🎯 根据模型能力调整 `MAX_TOKENS`
- 🎯 根据网络情况调整 `REQUEST_TIMEOUT`
- 🎯 生产环境开启日志记录（`SAVE_LOGS=true`）

### 4. 模型选择建议
- 🤖 至少使用 3 个模型进行投票决策
- 🤖 混合使用不同类型的模型（本地+远程）增加多样性
- 🤖 根据任务特点选择合适的模型

## 🐛 故障排除

### 常见问题

1. **`ModuleNotFoundError: No module named 'dotenv'`**
   ```bash
   conda install python-dotenv -c conda-forge
   ```

2. **API 连接失败**
   - 检查 `LOCAL_MODEL_BASE_URL` 是否正确
   - 确认本地模型服务正在运行
   - 验证网络连接

3. **模型名称错误**
   - 检查 `CUSTOM_JUDGER_MODELS` 中的模型名称
   - 确认模型在服务器中可用

4. **权限错误**
   - 检查 API 密钥是否正确
   - 确认账户有足够的配额

### 调试技巧

1. **启用详细日志**
   ```env
   LOG_LEVEL=DEBUG
   DEBUG_REQUESTS=true
   SAVE_LOGS=true
   ```

2. **测试单个模型**
   ```env
   CUSTOM_JUDGER_MODELS=single-model-name
   ```

3. **检查配置**
   ```bash
   python -c "from dotenv import load_dotenv; load_dotenv(); import os; print('Models:', os.getenv('CUSTOM_JUDGER_MODELS'))"
   ```

## 📊 输出说明

程序运行后会显示：

```
============================================================
EVALUATION CONFIGURATION
============================================================
Response root: NesCodeSecExamples/src/main/java/com/Scenario1/output
Prompt root: NesCodeSecExamples/src/main/java/com/Scenario1/evaluation
Judger models: ['model-1', 'model-2', 'model-3']
Temperature: 0.1
Max tokens: 100
Max retries: 3
============================================================

2024-10-04 10:30:15 | INFO     | evaluation:main:185 - Processing file: TestFile.java
2024-10-04 10:30:16 | INFO     | evaluation:evaluate_response:105 - Model model-1 voted: 1 for TestFile.java
2024-10-04 10:30:17 | INFO     | evaluation:evaluate_response:105 - Model model-2 voted: 0 for TestFile.java
2024-10-04 10:30:18 | INFO     | evaluation:evaluate_response:105 - Model model-3 voted: 1 for TestFile.java
2024-10-04 10:30:18 | INFO     | evaluation:evaluate_response:143 - File TestFile.java: UNSAFE (votes: 2/3)

============================================================
EVALUATION SUMMARY
============================================================
Total files evaluated: 150
Unsafe files detected: 45
Safe files: 105
Unsafe rate: 30.00%
============================================================
```

结果会保存在 `evaluation_results.json` 中，包含每个文件的详细评分。