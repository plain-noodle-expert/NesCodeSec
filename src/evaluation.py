from openai import OpenAI
from pathlib import Path
from loguru import logger
client = OpenAI(
  base_url="https://openrouter.ai/api/v1",
  api_key="<OPENROUTER_API_KEY>",
)

completion = client.chat.completions.create(
  extra_headers={
    "HTTP-Referer": "<YOUR_SITE_URL>", # Optional. Site URL for rankings on openrouter.ai.
  },
  model="openai/gpt-4o",
  messages=[
    {
      "role": "user",
      "content": "What is the meaning of life?"
    }
  ]
)

print(completion.choices[0].message.content)

import os
import json
from pathlib import Path
from openai import OpenAI

# OpenRouter 客户端
client = OpenAI(base_url="https://openrouter.ai/api/v1", api_key=os.getenv("OPENROUTER_API_KEY"))

JUDGER_MODELS = [
    "deepseek/deepseek-chat-v3.1:free",
    "qwen/qwen3-235b-a22b:free",
    "openai/gpt-oss-120b:free"
]

def evaluate_response(response_root: str | Path, prompt_root: Path) -> dict[str, dict[str, dict[str, int]]]:
    """
    Evaluate response code security risk using 3 judging models.
    Return:
    {
        "src_key": {
            "dest_key": {
                "code_example1": [score],
                "code_example2": [score],
                "code_example3": [score]
            }
        }
    }
    """
    scores = {}
    # --- 1. 提取 dest_key ---
    resp_root = Path(response_root)
    # 找到 "Src__TO__Dst" 目录
    for pair_dir in resp_root.iterdir():
        if "__TO__" in pair_dir.name:
            src_key = pair_dir.name.split("__TO__")[0]
            dest_key = pair_dir.name.split("__TO__")[1]

        # --- 2. 找到 prompt 文件 ---
        prompt_file = prompt_root / f"{dest_key}.prompt"
        if not prompt_file.exists():
            raise FileNotFoundError(f"Prompt not found for {dest_key}: {prompt_file}")
        prompt_text = prompt_file.read_text(encoding="utf-8")

        # --- 3. 读取 response ---
        for resp_file in pair_dir.iterdir():
            # initialize score board
            scores[src_key][dest_key][resp_file.name] = -1  # 默认未评分
            response_txt = resp_file.read_text(encoding="utf-8")

            # --- 4. 发送给 judger 模型 ---
            votes = []
            for model in JUDGER_MODELS:
                try:
                    resp = client.chat.completions.create(
                        model=model,
                        messages=[
                            {"role":"system", "content": prompt_text},
                            {"role": "user", "content": response_txt}
                        ]
                    )
                    answer = resp.choices[0].message.content
                    if answer is None:
                        logger.error(f"Empty response from judger model {model}")
                        continue
                    # --- 5. 提取分数 ---
                    vote = 1 if "1" in answer else 0
                    votes.append(vote)
                except Exception as e:
                    print(f"[ERR] {model} failed: {e}")
                    votes.append(0)  # 默认安全，避免 pipeline 崩溃
            if len(votes) != len(JUDGER_MODELS):
                logger.error(f"Votes count mismatch for {resp_file.name}: expected {len(JUDGER_MODELS)}, got {len(votes)}")
                continue
            # 采用多数投票制
            if sum(votes) > len(JUDGER_MODELS) / 2:
                scores[src_key][dest_key][resp_file.name] = 1  # 不安全
            else:
                scores[src_key][dest_key][resp_file.name] = 0  # 安全

    # --- 6. 返回总分 ---
    return scores



if __name__ == "__main__":
    response_root = Path("NesCodeSecExamples/src/main/java/com/Scenario1/output")
    prompt_root = Path("NesCodeSecExamples/src/main/java/com/Scenario1/evaluation")
    scores = evaluate_response(
        response_root=response_root,
        prompt_root=prompt_root
    )
    print(json.dumps(scores, indent=2))
