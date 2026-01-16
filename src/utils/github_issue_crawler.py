from __future__ import annotations

import argparse
import json
import os
import sys
import time
from pathlib import Path
import re
from typing import Dict, Iterable, List, Optional
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

from openai import OpenAI


API_ROOT = "https://api.github.com"
USER_AGENT = "NesCodeSec-IssueCrawler"


def fetch_issues(
    owner: str,
    repo: str,
    labels: List[str],
    limit: int,
    token: Optional[str] = None,
    delay_seconds: float = 0.5,
) -> List[Dict]:
    """
    Retrieve up to `limit` issues from the given repository using the GitHub API.
    Pull requests are skipped so only issue records remain.
    """
    collected: List[Dict] = []
    seen_ids = set()
    labels_to_use = (labels or []) + [None]

    print(f"Fetching up to {limit} issues from {owner}/{repo} with labels: {labels_to_use}")
    for label in labels_to_use:
        page = 1
        while len(collected) < limit:
            remaining = limit - len(collected)
            page_size = min(100, remaining)
            query_parts = [f"repo:{owner}/{repo}", "state:all"]
            if label:
                query_parts.append(f"label:{_escape_search_token(label)}")
            query = " ".join(query_parts)
            params = urlencode({"q": query, "per_page": page_size, "page": page})
            url = f"{API_ROOT}/search/issues?{params}"
            print(f"Requesting page {page} (up to {page_size} items) from URL: {url}")
            headers = {
                "Accept": "application/vnd.github+json",
                "User-Agent": USER_AGENT,
            }
            if token:
                headers["Authorization"] = f"Bearer {token}"

            request = Request(url, headers=headers)
            try:
                with urlopen(request) as response:
                    payload = response.read().decode("utf-8")
            except HTTPError as exc:  # pragma: no cover - network-specific handling
                print(
                    f"GitHub API returned HTTP {exc.code} when requesting page {page}: {exc.reason}"
                )
                break
            except URLError as exc:  # pragma: no cover - network-specific handling
                print(f"Failed to reach GitHub: {exc.reason}")
                break

            parsed = json.loads(payload)
            page_items = parsed.get("items")
            if not isinstance(page_items, list):
                raise RuntimeError(f"Unexpected payload for page {page}: {parsed}")
            if not page_items:
                print(f"No more items found on page {page}.")
                break

            for item in page_items:
                # GitHub includes pull requests in the issues API; filter them out.
                if "pull_request" in item:
                    continue
                issue_id = item.get("id")
                if issue_id in seen_ids:
                    continue
                seen_ids.add(issue_id)
                collected.append(item)
                if len(collected) >= limit:
                    break

            page += 1
            if len(collected) < limit and delay_seconds > 0:
                time.sleep(delay_seconds)

    return collected


def save_issues(issues: List[Dict], output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(issues, indent=2), encoding="utf-8")


def parse_args(argv: Optional[List[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Fetch GitHub issues into a JSON file.",
    )
    parser.add_argument(
        "repos",
        nargs="+",
        help="One or more repositories in the form owner/repo (example: psf/requests).",
    )
    parser.add_argument(
        "-l",
        "--labels",
        nargs="+",
        default=[],
        help="One or more labels to filter issues by (default: no label filtering).",
    )
    parser.add_argument(
        "-k",
        "--keywords",
        nargs="+",
        default=[],
        help="One or more keywords to filter issues by (default: common suggestion-related keywords).",
    )
    parser.add_argument(
        "-n",
        "--count",
        type=int,
        default=50,
        help="Number of issue records to retrieve (default: 50).",
    )
    parser.add_argument(
        "-o",
        "--output",
        type=Path,
        default=Path("issues.json"),
        help="Where to write the output JSON (default: issues.json in the current directory).",
    )
    parser.add_argument(
        "--token",
        default=os.environ.get("GITHUB_TOKEN"),
        help="GitHub token for higher rate limits; defaults to the GITHUB_TOKEN environment variable.",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=0.5,
        help="Seconds to wait between paginated requests (helps avoid rate limits).",
    )
    parser.add_argument(
        "--llm-provider",
        choices=["openai", "openai-compatible"],
        default=None,
        help="LLM provider to use for relevance+summary (default: disabled).",
    )
    parser.add_argument(
        "--llm-model",
        default=os.environ.get("LLM_MODEL") or os.environ.get("OPENAI_MODEL"),
        help="Model name for LLM relevance/summary (default: env LLM_MODEL or OPENAI_MODEL).",
    )
    parser.add_argument(
        "--llm-api-key",
        default=os.environ.get("LLM_API_KEY") or os.environ.get("OPENAI_API_KEY"),
        help="API key for the LLM provider (default: env LLM_API_KEY or OPENAI_API_KEY).",
    )
    parser.add_argument(
        "--llm-base-url",
        default=os.environ.get("LLM_BASE_URL") or os.environ.get("OPENAI_BASE_URL"),
        help="Base URL for the LLM API (for OpenAI-compatible endpoints).",
    )
    parser.add_argument(
        "--llm-timeout",
        type=float,
        default=30.0,
        help="Timeout (seconds) for LLM requests.",
    )
    parser.add_argument(
        "--require-llm",
        action="store_true",
        help="Fail if LLM is not configured instead of silently skipping LLM evaluation.",
    )
    return parser.parse_args(argv)


def main(args: argparse.Namespace) -> None:
    if args.count < 1:
        raise SystemExit("Count must be at least 1")

    keywords = args.keywords
    all_issues: List[Dict] = []
    llm_client = _build_llm_client(args)

    for repo_name in args.repos:
        if "/" not in repo_name:
            raise SystemExit(f"Repository must be provided as owner/repo (bad value: {repo_name})")

        owner, repo = repo_name.split("/", 1)
        raw_issues = fetch_issues(
            owner=owner,
            repo=repo,
            labels=args.labels,
            limit=args.count,
            token=args.token,
            delay_seconds=args.delay,
        )
        filtered: List[Dict] = []
        for issue in raw_issues:
            matches = _find_keyword_matches(issue, keywords)
            if matches:
                issue_copy = dict(issue)
                issue_copy["matched_patterns"] = matches
                if llm_client:
                    try:
                        llm_result = _llm_relevance_and_summary(
                            client=llm_client,
                            model=args.llm_model,
                            title=issue_copy.get("title") or "",
                            body=issue_copy.get("body") or "",
                            timeout=args.llm_timeout,
                        )
                        issue_copy["llm_relevant"] = llm_result["relevant"]
                        issue_copy["llm_summary"] = llm_result["summary"]
                    except Exception as exc:  # pragma: no cover - network-specific handling
                        print(f"LLM evaluation failed for issue {issue.get('number')}: {exc}")
                filtered.append(issue_copy)

        print(f"{owner}/{repo}: fetched {len(raw_issues)} issue(s), kept {len(filtered)} after filtering")
        all_issues.extend(filtered)

    save_issues(all_issues, args.output)
    print(f"Total kept issues: {len(all_issues)} -> {args.output}")


def _find_keyword_matches(issue: Dict, keywords: Iterable[str]) -> List[str]:
    """
    Return list of keywords whose patterns match the issue title or body.
    """
    title = issue.get("title") or ""
    body = _strip_mode_lines(issue.get("body") or "")
    haystack = f"{title}\n{body}"
    matches: List[str] = []
    for keyword in keywords:
        pattern = re.compile(rf"\b{re.escape(keyword)}\b", re.IGNORECASE)
        if pattern.search(haystack):
            matches.append(keyword)
    return matches


def _strip_mode_lines(body: str) -> str:
    """
    Remove lines that start with 'Mode' (case-insensitive) and drop the entire
    'Modes:' section until the next blank line to ignore template noise.
    """
    kept_lines = []
    in_modes_section = False

    for line in body.splitlines():
        stripped = line.lstrip().lower()

        if in_modes_section:
            if stripped == "":
                in_modes_section = False
            continue

        if stripped.startswith("modes:"):
            in_modes_section = True
            continue

        if stripped.startswith("mode"):
            continue

        kept_lines.append(line)

    return "\n".join(kept_lines)


def _escape_search_token(value: str) -> str:
    """
    Escape special characters for GitHub search query tokens.
    Wrap in quotes if whitespace is present and escape internal quotes.
    """
    escaped = value.replace('"', '\\"')
    if re.search(r"\s", value):
        return f'"{escaped}"'
    return escaped


def _build_llm_client(args: argparse.Namespace) -> Optional[OpenAI]:
    if not args.llm_provider:
        if args.require_llm:
            raise SystemExit("LLM provider not specified but --require-llm was set.")
        return None

    if args.llm_provider not in {"openai", "openai-compatible"}:
        raise SystemExit(f"Unsupported LLM provider: {args.llm_provider}")

    if not args.llm_model:
        if args.require_llm:
            raise SystemExit("LLM model not specified.")
        print("LLM model not specified; skipping LLM evaluation.")
        return None

    if not args.llm_api_key:
        if args.require_llm:
            raise SystemExit("LLM API key not specified.")
        print("LLM API key not specified; skipping LLM evaluation.")
        return None

    return OpenAI(api_key=args.llm_api_key, base_url=args.llm_base_url)


def _llm_relevance_and_summary(
    client: OpenAI,
    model: str,
    title: str,
    body: str,
    timeout: float,
    max_body_chars: int = 6000,
) -> Dict[str, str]:
    trimmed_body = _strip_mode_lines(body or "")
    trimmed_body = _truncate_text(trimmed_body, max_body_chars)
    prompt = (
        "You are a GitHub issue triage assistant. "
        "Decide if the issue is relevant to NES/inline suggestions/AI suggestions, "
        "and provide a concise topic summary. "
        "Respond with JSON: {\"relevant\": true|false, \"summary\": \"...\"}."
    )

    response = client.chat.completions.create(
        model=model,
        messages=[
            {"role": "system", "content": prompt},
            {
                "role": "user",
                "content": f"Title: {title}\n\nBody:\n{trimmed_body}",
            },
        ],
        max_tokens=200,
        temperature=0.2,
        response_format={"type": "json_object"},
        timeout=timeout,
    )
    content = response.choices[0].message.content
    try:
        parsed = json.loads(content)
    except json.JSONDecodeError:
        parsed = {"relevant": False, "summary": content}

    return {
        "relevant": bool(parsed.get("relevant")),
        "summary": str(parsed.get("summary") or "").strip(),
    }


def _truncate_text(text: str, max_chars: int) -> str:
    if len(text) <= max_chars:
        return text
    return text[: max_chars - 3] + "..."


if __name__ == "__main__":
    # cli_args = sys.argv[1:]
    # microsoft/vscode
    # cli_args = ["microsoft/vscode", "-k", "NES", "next edit suggestion", "inline suggestion", "ai suggestion", "-l", "inline-completions", "nes", "nes-ux", "-n", "1000", "--delay", "1", "-o", "copilot_issues.json"]
    # zed-industry/zed
    cli_args = ["zed-industries/zed", "-k", "edit predictions", "auto-suggestion", "edit prediction", "edit_prediction", "suggestion", "-l", "area:ai/edit prediction", "area:ai/zeta", "-n", "500", "--delay", "1", "-o", "zed_issues.json"]
    main(parse_args(cli_args))
