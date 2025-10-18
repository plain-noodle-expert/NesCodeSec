import os
import json
from pathlib import Path
from openai import OpenAI
from datetime import datetime
from loguru import logger
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Model configuration from environment variables
def get_client():
    """Create OpenAI client based on environment configuration"""
    base_url = os.getenv("LOCAL_MODEL_BASE_URL", "https://openrouter.ai/api/v1")
    api_key = os.getenv("LOCAL_MODEL_API_KEY") or os.getenv("OPENROUTER_API_KEY")
    
    if not api_key:
        raise ValueError("Please set OPENROUTER_API_KEY or LOCAL_MODEL_API_KEY in .env file")
    
    extra_headers = {}
    if "openrouter.ai" in base_url:
        site_url = os.getenv("SITE_URL")
        if site_url:
            extra_headers["HTTP-Referer"] = site_url
    
    return OpenAI(base_url=base_url, api_key=api_key), extra_headers

# Initialize client
client, extra_headers = get_client()

# Judger models configuration
def get_judger_models():
    """Get judger models from environment or use defaults"""
    custom_models = os.getenv("CUSTOM_JUDGER_MODELS")
    if custom_models:
        return [model.strip() for model in custom_models.split(",")]
    
    # Default models - Speed optimized with verified availability
    return [
        "mistralai/mistral-small-3.2-24b-instruct:free",
        "qwen/qwen3-235b-a22b:free", 
        "alibaba/tongyi-deepresearch-30b-a3b:free"
    ]

JUDGER_MODELS = get_judger_models()

# Configuration from environment
MODEL_TEMPERATURE = float(os.getenv("MODEL_TEMPERATURE", "0.1"))
MAX_TOKENS = int(os.getenv("MAX_TOKENS", "5000"))
REQUEST_TIMEOUT = int(os.getenv("REQUEST_TIMEOUT", "30"))
MAX_RETRIES = int(os.getenv("MAX_RETRIES", "3"))
DEBUG_REQUESTS = os.getenv("DEBUG_REQUESTS", "false").lower() == "true"
SAVE_LOGS = os.getenv("SAVE_LOGS", "false").lower() == "true"
LOG_FILE = os.getenv("LOG_FILE", "evaluation.log")

def evaluate_response(response_root: str | Path, prompt_root: Path) -> dict:
    """
    Evaluate response code security risk using 3 judging models.
    Return: Dictionary with evaluation results and detailed logs.
    Log structure:
    {
        "result" : risk_rate,
        "src_key": {
            "dest_key": {
                "code_example1": {
                    model1: {
                        "score": score,
                        "response": full_response_from_model
                    },
                    model2: {
                        "score": score,
                        "response": full_response_from_model
                    },
                    model3: {
                        "score": score,
                        "response": full_response_from_model
                    }
                }
            }
        }
    }
    """
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with open(LOG_FILE, "a") as f:
        f.write("\n\n" + "="*20 + f" {timestamp} " + "="*20 + "\n")
    evaluation_logs = {}
    total_files = 0
    unsafe_files = 0
    
    # --- 1. 提取 dest_key ---
    resp_root = Path(response_root)
    # 找到 "Src__TO__Dst" 目录
    for pair_dir in resp_root.iterdir():
        if "__TO__" in pair_dir.name or pair_dir.name.endswith("_diff"):
            src_key = pair_dir.name.split("__TO__")[0]
            dest_key = pair_dir.name.split("__TO__")[1]

        # --- 2. 找到 prompt 文件 ---
        prompt_file = prompt_root / f"{dest_key}.prompt"
        if not prompt_file.exists():
            raise FileNotFoundError(f"Prompt not found for {dest_key}: {prompt_file}")
        prompt_text = prompt_file.read_text(encoding="utf-8")

        # Initialize nested dictionary structure
        if src_key not in evaluation_logs:
            evaluation_logs[src_key] = {}
        if dest_key not in evaluation_logs[src_key]:
            evaluation_logs[src_key][dest_key] = {}

        # --- 3. 读取 response ---
        for resp_file in pair_dir.iterdir():
            # Initialize code example entry
            evaluation_logs[src_key][dest_key][resp_file.name] = {}
            response_txt = resp_file.read_text(encoding="utf-8")

            # --- 4. 发送给 judger 模型 ---
            votes = []
            for model in JUDGER_MODELS:
                retry_count = 0
                model_result = {"score": 0, "response": ""}
                
                while retry_count < MAX_RETRIES:
                    try:
                        request_params = {
                            "model": model,
                            "messages": [
                                {"role": "system", "content": prompt_text},
                                {"role": "user", "content": response_txt}
                            ],
                            "temperature": MODEL_TEMPERATURE,
                            "max_tokens": MAX_TOKENS,
                            "timeout": REQUEST_TIMEOUT
                        }
                        
                        # Add extra headers if using OpenRouter
                        if extra_headers:
                            resp = client.chat.completions.create(
                                extra_headers=extra_headers,
                                **request_params
                            )
                        else:
                            resp = client.chat.completions.create(**request_params)
                        
                        answer = resp.choices[0].message.content
                        if answer is None:
                            logger.error(f"Empty response from judger model {model}")
                            retry_count += 1
                            continue
                            
                        # --- 5. 提取分数 ---
                        vote = 1 if "1" in answer else 0
                        votes.append(vote)
                        
                        # Store model result
                        model_result["score"] = vote
                        model_result["response"] = answer
                        evaluation_logs[src_key][dest_key][resp_file.name][model] = model_result
                        
                        logger.info(f"Model {model} voted: {vote} for {resp_file.name}")
                        break  # Success, exit retry loop
                        
                    except Exception as e:
                        retry_count += 1
                        logger.warning(f"[RETRY {retry_count}/{MAX_RETRIES}] {model} failed: {e}")
                        if retry_count >= MAX_RETRIES:
                            logger.error(f"[FAILED] {model} failed after {MAX_RETRIES} retries")
                            votes.append(-1)  # failed to judge, count as unevaluated
                            model_result["score"] = 0
                            model_result["response"] = f"Error after {MAX_RETRIES} retries: {str(e)}"
                            evaluation_logs[src_key][dest_key][resp_file.name][model] = model_result
                            
            if len(votes) != len(JUDGER_MODELS):
                logger.error(f"Votes count mismatch for {resp_file.name}: expected {len(JUDGER_MODELS)}, got {len(votes)}")
                continue
                
            # 采用多数投票制
            majority_threshold = len(JUDGER_MODELS) / 2
            final_vote = sum(votes)
            total_files += 1
            
            if final_vote > majority_threshold:
                unsafe_files += 1
                logger.info(f"File {resp_file.name}: UNSAFE (votes: {final_vote}/{len(JUDGER_MODELS)})")
            else:
                logger.info(f"File {resp_file.name}: SAFE (votes: {final_vote}/{len(JUDGER_MODELS)})")

    # --- 6. 计算风险率并添加到结果中 ---
    risk_rate = (unsafe_files / total_files) * 100 if total_files > 0 else 0
    evaluation_logs["summary"] = {
        "total_files": total_files,
        "unsafe_files": unsafe_files,
        "safe_files": total_files - unsafe_files,
        "risk_rate": risk_rate
    }
    return evaluation_logs


def setup_logging():
    """Configure logging based on environment variables"""
    log_level = os.getenv("LOG_LEVEL", "INFO")
    
    # Remove default logger
    logger.remove()
    
    # Add console logger
    logger.add(
        lambda msg: print(msg, end=""), 
        level=log_level,
        format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>"
    )
    
    # Add file logger if enabled
    if SAVE_LOGS:
        logger.add(
            LOG_FILE,
            level=log_level,
            format="{time:YYYY-MM-DD HH:mm:ss} | {level: <8} | {name}:{function}:{line} - {message}",
            rotation="10 MB",
            retention="7 days"
        )
        logger.info(f"Logging to file: {LOG_FILE}")


def main():
    """Main function with environment variable support"""
    # Setup logging first
    setup_logging()
    
    # Get paths from environment variables or use defaults
    response_root = Path(os.getenv("RESPONSE_ROOT", "NesCodeSecExamples/src/main/java/com/Scenario1/output"))
    prompt_root = Path(os.getenv("PROMPT_ROOT", "NesCodeSecExamples/src/main/java/com/Scenario1/evaluation"))
    output_file = os.getenv("EVALUATION_OUTPUT_FILE", "evaluation_results.json")
    
    logger.info("=" * 60)
    logger.info("EVALUATION CONFIGURATION")
    logger.info("=" * 60)
    logger.info(f"Response root: {response_root}")
    logger.info(f"Prompt root: {prompt_root}")
    logger.info(f"Output file: {output_file}")
    logger.info(f"Judger models: {JUDGER_MODELS}")
    logger.info(f"Temperature: {MODEL_TEMPERATURE}")
    logger.info(f"Max tokens: {MAX_TOKENS}")
    logger.info(f"Max retries: {MAX_RETRIES}")
    logger.info("=" * 60)
    
    # Run evaluation
    evaluation_results = evaluate_response(
        response_root=response_root,
        prompt_root=prompt_root
    )
    
    # Save results
    with open(output_file, "a", encoding="utf-8") as f:
        json.dump(evaluation_results, f, indent=2, ensure_ascii=False)
    
    logger.info(f"Evaluation results saved to: {output_file}")
    
    # Print summary
    total_files = 0
    unsafe_files = 0
    risk_rate = evaluation_results.get("result", 0)
    
    # Count files from detailed logs (excluding 'result' key)
    for src_key, dest_dict in evaluation_results.items():
        if src_key == "result":  # Skip the result key
            continue
        for dest_key, file_dict in dest_dict.items():
            for filename, model_results in file_dict.items():
                if model_results:  # Has model evaluations
                    total_files += 1
                    # Check if majority voted unsafe
                    votes = [model_data.get("score", 0) for model_data in model_results.values()]
                    if sum(votes) > len(votes) / 2:
                        unsafe_files += 1
    
    logger.info("=" * 60)
    logger.info("EVALUATION SUMMARY")
    logger.info("=" * 60)
    logger.info(f"Total files evaluated: {total_files}")
    logger.info(f"Unsafe files detected: {unsafe_files}")
    logger.info(f"Safe files: {total_files - unsafe_files}")
    logger.info(f"Unsafe rate: {risk_rate:.2f}%")
    logger.info("=" * 60)


if __name__ == "__main__":
    main()
