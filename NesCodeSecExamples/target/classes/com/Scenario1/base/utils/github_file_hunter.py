#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import argparse
import base64
import json
import os
import re
import sys
import time
from pathlib import Path
from typing import Dict, List, Tuple, Optional, Iterable

import requests

SEARCH_API = "https://api.github.com/search/code"

def gh_get(url: str, token: Optional[str], params=None, timeout=30):
    headers = {"Accept": "application/vnd.github.v3+json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    r = requests.get(url, headers=headers, params=params, timeout=timeout)
    # 简单的 rate limit 处理
    if r.status_code == 403 and "rate limit" in r.text.lower():
        reset = r.headers.get("X-RateLimit-Reset")
        if reset and reset.isdigit():
            sleep_s = max(0, int(reset) - int(time.time()) + 2)
            print(f"[WAIT] Rate limit. Sleeping {sleep_s}s…", flush=True)
            time.sleep(sleep_s)
            r = requests.get(url, headers=headers, params=params, timeout=timeout)
    return r

def search_by_path_suffix(path_suffix: str, token: Optional[str], per_path_limit: int) -> List[dict]:
    """用 in:path + filename 进行全站搜索，并在本地做 endswith 精确过滤。"""
    filename = Path(path_suffix).name
    q = f'in:path filename:"{filename}" "{path_suffix}"'
    items: List[dict] = []
    page = 1
    per_page = min(100, max(10, per_path_limit * 2))
    norm_suffix = path_suffix.replace("\\", "/").lower()

    while True:
        params = {"q": q, "per_page": per_page, "page": page}
        r = gh_get(SEARCH_API, token, params=params)
        if r.status_code != 200:
            print(f"[ERR] search status={r.status_code} body={r.text[:400]}")
            break
        data = r.json()
        raw_items = data.get("items", [])
        for it in raw_items:
            p = (it.get("path") or "").replace("\\", "/").lower()
            if p.endswith(norm_suffix):
                items.append(it)
        if len(items) >= per_path_limit:
            return items[:per_path_limit]
        if "next" not in r.links:
            break
        page += 1
        time.sleep(0.3)
    return items[:per_path_limit]

def fetch_content(contents_api_url: str, token: Optional[str]) -> Optional[bytes]:
    r = gh_get(contents_api_url, token)
    if r.status_code != 200:
        print(f"[ERR] contents fetch {contents_api_url} status={r.status_code}")
        return None
    data = r.json()
    if isinstance(data, dict):
        if data.get("encoding") == "base64" and "content" in data:
            try:
                return base64.b64decode(data["content"])
            except Exception as e:
                print(f"[ERR] base64 decode error: {e}")
                return None
        if data.get("download_url"):
            r2 = gh_get(data["download_url"], token)
            if r2.status_code == 200:
                return r2.content
            print(f"[ERR] direct download status={r2.status_code}")
            return None
    return None

def sanitize_component(name: str) -> str:
    """仅用于防止奇怪字符导致的本地文件系统错误，不改变语义。"""
    return re.sub(r'[\\:*?"<>|]+', "_", name)

def ensure_dir(p: Path):
    p.mkdir(parents=True, exist_ok=True)

def save_with_no_overwrite(dir_path: Path, basename: str, content: bytes) -> Path:
    dir_path = dir_path.resolve()
    ensure_dir(dir_path)
    base = sanitize_component(Path(basename).name)
    out = dir_path / base
    if out.exists():
        stem, ext = out.stem, out.suffix
        k = 2
        while True:
            candidate = dir_path / f"{stem}({k}){ext}"
            if not candidate.exists():
                out = candidate
                break
            k += 1
    out.write_bytes(content)
    return out

def load_mapping(mapping_file: Path) -> List[Tuple[Path, str]]:
    """
    读取 JSON 映射：
    {
      "local/dir/A": "src/a.py",
      "local/dir/B": "docs/guide/start.md"
    }
    也支持 value 为列表：
    {
      "local/dir/A": ["src/a.py", "lib/b.c"]
    }
    """
    data = json.loads(mapping_file.read_text(encoding="utf-8"))
    pairs: List[Tuple[Path, str]] = []
    for save_dir, gh_paths in data.items():
        if isinstance(gh_paths, str):
            pairs.append((Path(save_dir), gh_paths))
        elif isinstance(gh_paths, list):
            for p in gh_paths:
                if isinstance(p, str):
                    pairs.append((Path(save_dir), p))
                else:
                    raise ValueError(f"List item for key '{save_dir}' must be string.")
        else:
            raise ValueError(f"Value for key '{save_dir}' must be string or list of strings.")
    return pairs

def main():
    ap = argparse.ArgumentParser(
        description="Given a JSON dict of {local_dir: github_path}, search and download files to indicated directories."
    )
    ap.add_argument("--mapping", required=True, help="Path to JSON mapping file.")
    ap.add_argument("--per-path-limit", type=int, default=1, help="How many matches to download per GitHub path (default 1).")
    ap.add_argument("--first-only", action="store_true", help="Alias for --per-path-limit=1.")
    ap.add_argument("--token", default=os.getenv("GH_TOKEN"), help="GitHub token (recommended).")
    args = ap.parse_args()

    if args.first_only:
        args.per_path_limit = 1

    mapping_file = Path(args.mapping)
    try:
        tasks = load_mapping(mapping_file)
    except Exception as e:
        print(f"[ERR] load mapping failed: {e}")
        sys.exit(2)

    summary = {"ok": [], "miss": [], "errors": []}

    for idx, (save_dir, gh_rel_path) in enumerate(tasks, 1):
        print(f"\n[{idx}/{len(tasks)}] {gh_rel_path}  ->  {save_dir}")
        try:
            items = search_by_path_suffix(gh_rel_path, args.token, args.per_path_limit)
        except Exception as e:
            print(f"[ERR] search failed: {e}")
            summary["errors"].append(gh_rel_path)
            continue

        if not items:
            print(f"[MISS] No match for {gh_rel_path}")
            summary["miss"].append(gh_rel_path)
            continue

        downloaded = 0
        for it_idx, it in enumerate(items, 1):
            owner = it["repository"]["owner"]["login"]
            repo = it["repository"]["name"]
            gh_path = it["path"]
            contents_api_url = it["url"]

            content = fetch_content(contents_api_url, args.token)
            if content is None:
                summary["errors"].append(f"{owner}/{repo}:{gh_path}")
                continue

            basename = Path(gh_path).name  # 保持“原始文件名”
            out_path = save_with_no_overwrite(save_dir, basename, content)
            print(f"[OK] {owner}/{repo}:{gh_path}  ->  {out_path}")
            summary["ok"].append(str(out_path))
            downloaded += 1

        if downloaded == 0:
            summary["errors"].append(gh_rel_path)

        time.sleep(0.2)

    print("\n== Summary ==")
    print(json.dumps(summary, ensure_ascii=False, indent=2))
    if summary["errors"]:
        sys.exit(2)

if __name__ == "__main__":
    main()
