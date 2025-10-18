# Next Edit Prediction Code Security Evaluation

## Recommend Development Environment
### IDE
We strongly recommend you use a AI IDE for your coding work. Please use one of the following:
- [cursor](https://cursor.com/)
- [windsurf](https://windsurf.com/editor)
- [Trae](https://www.trae.ai/)
- [Vscode Copilot](https://code.visualstudio.com/docs/copilot/overview)
- ...

But one exception is Java language. If you need to write any java code or testcases, please see [Java Environment](NesCodeSecExamples/README.md).

### IDE Plugins
The following are some useful IDE plugins are available in the above IDEs:
- Markdown Preview Enhanced: For displaying markdown text.
- Dev Containers: For development in a docker container.
- Python/C/C++/: The language support.
- Black Formatter: Help you format you code.
- git-commit-plugin: Help you write a commit message.
- ....

## Project Structure
- NesCodeSecExamples: The examples or tesecases we tested.
- src: Your source code of this framework here.

## Usage
We use [uv](https://docs.astral.sh/uv/) to manage the project dependencies.

### Install the UV
```
curl -LsSf https://astral.sh/uv/install.sh | sh
```

### How to use UV manage project
See: https://docs.astral.sh/uv/getting-started/features/#features

## 🚀 Performance Features

### Multithreaded Evaluation
The evaluation system (`src/evaluation.py`) now supports **parallel processing** for significant speed improvements:

- **Default**: 4 concurrent worker threads
- **Speedup**: 4-8x faster than single-threaded evaluation
- **Thread-safe**: All operations protected with proper locking mechanisms
- **Progress tracking**: Real-time progress updates during evaluation

#### Quick Start
```bash
# Run with default settings (4 workers)
python src/evaluation.py

# Customize thread count
MAX_WORKERS=8 python src/evaluation.py

# Adjust API request delay
MAX_WORKERS=4 REQUEST_DELAY=3.0 python src/evaluation.py
```

#### Configuration Options
Set these environment variables in `.env` or command line:
- `MAX_WORKERS`: Number of parallel threads (default: 4)
- `REQUEST_DELAY`: Seconds between API requests (default: 5.0)
- `RATE_LIMIT_WAIT`: Wait time when hitting rate limits (default: 60)

#### Performance Examples
| Files | Workers | Time (Single) | Time (Multi) | Speedup |
|-------|---------|---------------|--------------|---------|
| 10    | 4       | 3.0m          | 45s          | 4x      |
| 50    | 4       | 15.0m         | 3.8m         | 4x      |
| 100   | 8       | 30.0m         | 3.8m         | 8x      |
| 200   | 8       | 1.0h          | 7.5m         | 8x      |

For detailed configuration and tuning guide, see [MULTITHREADING_EVALUATION.md](MULTITHREADING_EVALUATION.md)
