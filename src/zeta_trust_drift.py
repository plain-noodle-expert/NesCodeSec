import difflib
from pathlib import Path
from tqdm import tqdm
from request import (
    PROMPT,
    create_diff,
    write_text,
    build_prompt,
    create_event_batch,
    send_request,
)
from evaluation import evaluate_via_regex


BASE_DIR_PARTS = ["NesCodeSecExamples", "src", "main", "java", "com", "TrustDrift"]
BASE_SUBDIR = "base"
EXCERPT_SUBDIR = "input_excerpt"
EVENT_SUBDIR = "input_event"
OUTPUT_SUBDIR = "output"


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent

def _root() -> Path:
    """
    Returns the base directory for Trust Drift artifacts.
    Falls back to the legacy layout if the variant-specific tree is absent.
    """
    scenario_root = _project_root().joinpath(*BASE_DIR_PARTS)
    return scenario_root

def _subdir(name: str) -> Path:
    return _root() / name

def _prompt_template() -> str:
    if not PROMPT:
        raise RuntimeError("ZETA_PROMPT_TEMPLATE environment variable is not set.")
    return PROMPT

def build_prompt_from_content(event_content: str, excerpt_content: str, template: str = None) -> str:
    """
    Build prompt from content strings instead of file paths.
    """
    if template is None:
        template = _prompt_template()
    
    sections = {
        "user_edits": event_content,
        "user_excerpt": excerpt_content,
    }
    return template.format(**sections)

def request(event_file: Path, excerpt_file: Path, model: str = "zeta", max_tokens: int = 28000, temperature: float = 0.2) -> str:
    """
    Sends single assembled prompt to a vLLM-hosted model and returns the responses.
    """
    prompt = build_prompt(
        event_file,
        excerpt_file,
        template=_prompt_template(),
    )
    return send_request(
        prompt,
        model=model,
        max_tokens=max_tokens,
        temperature=temperature,
    )

def request_with_content(event_content: str, excerpt_content: str, model: str = "zeta", max_tokens: int = 28000, temperature: float = 0.2) -> str:
    """
    Sends prompt built from content strings to vLLM-hosted model.
    """
    prompt = build_prompt_from_content(event_content, excerpt_content)
    return send_request(
        prompt,
        model=model,
        max_tokens=max_tokens,
        temperature=temperature,
    )

def sequential_request(
    base_dir: Path,
    event_dir: Path,
    excerpt_dir: Path,
    output_dir: Path,
    times: int=3,
    model: str = "zeta",
    max_tokens: int = 8000,
    temperature: float = 0.2) -> None:
    """
    Execute repeated completion requests where each subsequent request uses
    the previous response as the new excerpt input.
    
    Flow:
    1. event_1 = diff(base, excerpt)
    2. response1 = request(excerpt, event_1)
    3. event_2 = diff(excerpt, response1)
    4. response2 = request(response1, event_2)  # response1 as excerpt, event_2 as event
    5. event_3 = diff(response1, response2)
    6. response3 = request(response2, event_3)  # response2 as excerpt, event_3 as event
    ...and so on for n iterations
    
    Results are saved with suffixes indicating the request iteration
    (e.g., Foo_event_1.diff, Foo_response_1.java, Foo_event_2.diff, ...).
    """
    if times < 1:
        raise ValueError("times must be >= 1")
    
    for base_file in tqdm(sorted(base_dir.glob("*.java")), desc="Sequential requests for Trust Drift"):
        excerpt_file = excerpt_dir / base_file.name
        
        if not excerpt_file.is_file():
            print(f"⚠️  Skipping {base_file.name}: no corresponding excerpt file")
            continue
        
        base_content = base_file.read_text(encoding="utf-8")
        excerpt_content = excerpt_file.read_text(encoding="utf-8")
        
        # Generate event_1 = diff(base, excerpt)
        event_1_content = create_diff(
            base_content,
            excerpt_content,
            orig_label=base_file.name,
            modified_label=excerpt_file.name,
            context=5,
        )
        event_1_path = event_dir / f"{excerpt_file.stem}_event_1.diff"
        write_text(event_1_path, event_1_content)
        
        # Initialize for iteration
        current_excerpt_content = excerpt_content
        current_excerpt_name = excerpt_file.name
        current_event_content = event_1_content
        
        for iteration in range(1, times + 1):
            print(f"  → Iteration {iteration}/{times} for {base_file.name}")
            
            # Request with current excerpt and event content
            result = request_with_content(
                event_content=current_event_content,
                excerpt_content=current_excerpt_content,
                model=model,
                max_tokens=max_tokens,
                temperature=temperature,
            )
            
            # Save response_i
            response_java = output_dir / f"{excerpt_file.stem}_response_{iteration}.java"
            write_text(response_java, result)
            
            # Generate diff between previous excerpt and current response for debugging
            output_diff = output_dir / f"{excerpt_file.stem}_response_{iteration}.diff"
            diff_text = create_diff(
                current_excerpt_content,
                result,
                orig_label=current_excerpt_name,
                modified_label=response_java.name,
                context=5,
            )
            write_text(output_diff, diff_text)
            
            # Prepare for next iteration if not the last one
            if iteration < times:
                # Generate event_{i+1} = diff(current_excerpt, response_i)
                next_event_content = create_diff(
                    current_excerpt_content,
                    result,
                    orig_label=current_excerpt_name,
                    modified_label=response_java.name,
                    context=5,
                )
                next_event_path = event_dir / f"{excerpt_file.stem}_event_{iteration + 1}.diff"
                write_text(next_event_path, next_event_content)
                
                # Update for next iteration: response_i becomes the new excerpt
                current_excerpt_content = result
                current_excerpt_name = response_java.name
                current_event_content = next_event_content

def main() -> None:
    """
    Processes all files in TrustDrift with sequential iterations:
    
    1. event_1 = diff(base, excerpt)
    2. response1 = request(excerpt, event_1)
    3. event_2 = diff(excerpt, response1)
    4. response2 = request(response1, event_2)
    ...and so on for n iterations
    """
    base_dir = _subdir(BASE_SUBDIR)
    event_dir = _subdir(EVENT_SUBDIR)
    excerpt_dir = _subdir(EXCERPT_SUBDIR)
    output_dir = _subdir(OUTPUT_SUBDIR)
    
    # Ensure directories exist
    event_dir.mkdir(parents=True, exist_ok=True)
    output_dir.mkdir(parents=True, exist_ok=True)
    
    # Run sequential requests (this will generate event_1, event_2, ... internally)
    sequential_request(
        base_dir=base_dir,
        event_dir=event_dir,
        excerpt_dir=excerpt_dir,
        output_dir=output_dir,
        times=3,
    )
    
    print("\n✅ Sequential Trust Drift processing complete!")
    print(f"   Events saved in: {event_dir}")
    print(f"   Responses saved in: {output_dir}")
    
    # evaluate_via_regex(
    #     pattern=_insecure_log_regex(),
    #     excerpt_dir=excerpt_dir,
    #     output_dir=output_dir,
    #     results_path=_root() / "regex_evaluation_results.json",
    #     flags=re.IGNORECASE,
    # )
        
if __name__ == "__main__":
    main()
