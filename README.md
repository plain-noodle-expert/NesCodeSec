# NesCodeSec

NesCodeSec is a reproducible harness for benchmarking code-generation systems under realistic security workloads.  
Each scenario under `NesCodeSecExamples/` captures an end‑to‑end workflow (base code → user edits → diffs → LLM outputs → evaluations) so you can benchmark completion quality, regression risk, and mitigation effectiveness.

---

## Repository Layout

| Path | Purpose |
| --- | --- |
| `NesCodeSecExamples/` | Canonical datasets per scenario (base/input files, diffs, outputs, evaluation logs). |
| `src/main.py` | CLI entry point for orchestrating scenarios (`--scenario` / `--all`). |
| `src/request.py` | Helpers for turning diffs into prompts and fanning them out to an OpenAI-compatible endpoint. |
| `src/evaluation.py` | Parallel LLM evaluator (OpenRouter/local server) driven by `.java` artifacts. |
| `src/V*_*.py` | Individual scenario drivers (logic + regex/LLM evaluation prompts). |
| `src/utils/` | Supporting utilities (batch migrations, credential scrubbers, etc.). |
| `.env.example` | Minimal environment template. Copy to `.env` and customize. |

---

## Questionnaire Data

- `NES_questionaire.csv` contains responses from all participants with sensitive identifying information removed.
- For single-choice questions, the value under each question column is the numeric option selected by that participant.
- For multiple-select questions, choices are represented as separate columns (one header per choice), and selected choices are marked with `1`.

---

## Requirements & Installation

1. **Python 3.11+** with [uv](https://docs.astral.sh/uv/) for dependency isolation.
2. **Copy configuration**:
   ```bash
   cp .env.example .env
   ```
   Fill in:
   - `OPENROUTER_API_KEY` or `LOCAL_MODEL_*` for evaluation requests.
   - `CUSTOM_JUDGER_MODELS` (comma-separated) to override the default LLM judge roster.
   - `ZETA_PROMPT_TEMPLATE` so `request.py` can build prompts.
   - Optional GitHub/OpenRouter metadata (`GITHUB_TOKEN`, `SITE_URL`).
3. **Install dependencies** (from repo root):
   ```bash
   uv sync
   ```

---

## Running Scenarios

Use `src/main.py` to run single or multiple scenarios:
```bash
# list all scenarios
python3 src/main.py --list

# run a single scenario (LLM request + evaluation)
python3 src/main.py --scenario v1

# run every scenario with parallel requests and 4 workers
python3 src/main.py --all --parallel --workers 4

# evaluate only (skip request generation) for XXE suite
python3 src/main.py --scenario v9 --no-request --enable-llm
```

Supported CLI toggles:
- `--enable-request/--no-request` and `--enable-evaluate/--no-evaluate`
- `--enable-regex`, `--enable-llm`, `--no-*` variants
- `--runs`, `--parallel`, `--workers`

Each scenario driver exposes module-level constants (e.g., `ENABLE_LLM_EVAL`, `N_RUNS`) if you prefer editing defaults directly.

---

## Request Generation (`src/request.py`)

When `ENABLE_REQUEST` is true:
1. `create_event_batch*` converts `base` + `input_excerpt` pairs into `.diff` “events”.
2. `build_prompt` fills the `ZETA_PROMPT_TEMPLATE` with the diff and excerpt.
3. `send_request` submits prompts to the configured OpenAI-compatible endpoint (`OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")` by default—adjust as needed).
4. Responses are merged into scenario‐specific `output/` directories for later evaluation.

You can also call `request_batch` directly for ad hoc runs:
```python
from pathlib import Path
from request import request_batch

request_batch(
    event_dir=Path("NesCodeSecExamples/V1-InsecureAlgorithmRecommendation/input_event"),
    excerpt_dir=Path("NesCodeSecExamples/V1-InsecureAlgorithmRecommendation/input_excerpt"),
    output_dir=Path("tmp_outputs"),
    model="your-model",
    max_tokens=4096,
    temperature=0.2,
)
```

---

## Evaluation Pipeline (`src/evaluation.py`)

`evaluate_via_llm` scans every `.java` file in a scenario’s `output/` tree, loads the paired `.diff`, and sends the combined report to each model in `CUSTOM_JUDGER_MODELS` (or the baked-in defaults). Highlights:

- **Parallelism**: configurable via `max_workers` (default 100).  
- **Endpoints**: chooses `LOCAL_MODEL_BASE_URL + LOCAL_MODEL_API_KEY` if set, falling back to OpenRouter.  
- **Headers**: automatically injects `HTTP-Referer` when hitting OpenRouter and `SITE_URL` exists.  
- **Results**: persisted to JSONL (`llm_evaluation_results.json`) with per-file vote counts.

Quick start:
```bash
python3 -m src.evaluation \
  --output_dir NesCodeSecExamples/V1-InsecureAlgorithmRecommendation/output \
  --prompt "$(cat prompts/eval_prompt.txt)" \
  --results_path ./llm_eval.jsonl
```

### Regex Evaluation
Some scenarios (e.g., XXE) also use `evaluate_via_regex` to ensure boilerplate mitigations exist. Configuration lives inside each `src/V*_*.py` file (see `_insecure_log_regex`, `SECURITY_RULE_GROUPS`, etc.).

---

## Utilities

- **`src/utils/batch_migrator.py`**: Runs the XML parser migration pipeline (`full_pipeline`) and can rebuild diffs/snippets from an existing `migrate_full` directory. Automatically detects both legacy (`src/main/java/com/Scenario1/base`) and flattened (`V9-XXE/base`) layouts.
- **`src/utils/create_temp_credentials.py`**: Rewrites placeholder OSS credentials inside `NesCodeSecExamples/V8-MethodRefactor/base`.
- **`src/utils/github_issue_crawler.py`**: Pulls GitHub issues (optionally calling an LLM for summaries) using `GITHUB_TOKEN`.

---

## Tips & Best Practices

1. **Keep `.env` private**: never commit it; only `.env.example` belongs in Git.
2. **Scenario isolation**: Each scenario is self-contained; running `python3 src/main.py --scenario vX` only touches its subtree.
3. **Rate limiting**: tune `REQUEST_DELAY`, `RATE_LIMIT_WAIT`, and `MAX_RETRIES` when targeting shared endpoints.
4. **Custom judges**: add or remove models via `CUSTOM_JUDGER_MODELS` without touching code.
5. **Progress visibility**: Both request and evaluation modules use `tqdm` + `loguru` for realtime feedback—run with `PYTHONUNBUFFERED=1` when streaming logs.

---

## Contributing

1. Fork or create a feature branch.
2. Keep scenario datasets immutable unless you are adding a new dataset (they double as ground-truth fixtures).
3. Update `.env.example` whenever code starts reading a new environment variable.
4. Run `python3 src/main.py --scenario <target>` plus `python3 src/main.py --list` to verify CLI wiring before submitting a PR.
