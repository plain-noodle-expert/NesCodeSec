#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
GitHub File Crawler for Scenario8
Extract filenames from Scenario8 files and download complete files from GitHub
"""

import os
import re
import time
import requests
from pathlib import Path
from typing import Optional, List


# GitHub API config
GITHUB_API_BASE = "https://api.github.com"
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")

# Rate limit config
RATE_LIMIT_DELAY = 2


def extract_filename_from_java_file(file_path: Path) -> Optional[str]:
    """Extract filename from the first line of a Java file"""
    try:
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            first_line = f.readline().strip()
            
        pattern = r'<filename>(.+?)<fim_prefix>'
        match = re.search(pattern, first_line)
        
        if match:
            filename = match.group(1).strip()
            print(f"  ✓ Extracted: {filename}")
            return filename
        else:
            print(f"  ✗ Cannot extract from: {first_line[:100]}")
            return None
            
    except Exception as e:
        print(f"  ✗ Read error: {e}")
        return None


def search_in_known_repos(base_filename: str, full_path: str, max_results: int = 1) -> List[dict]:
    """Search in known repositories (no token needed)"""
    known_repos = [
        "aliyun/aliyun-oss-java-sdk",
    ]
    
    results = []
    
    for repo in known_repos:
        try:
            api_url = f"{GITHUB_API_BASE}/repos/{repo}/contents/{full_path}"
            
            print(f"    🔍 Checking: {repo}/{full_path}")
            
            headers = {"Accept": "application/vnd.github.v3+json"}
            response = requests.get(api_url, headers=headers, timeout=10)
            
            if response.status_code == 200:
                data = response.json()
                result = {
                    "name": base_filename,
                    "path": full_path,
                    "url": api_url,
                    "html_url": data.get("html_url", ""),
                    "repository": {
                        "full_name": repo,
                        "name": repo.split('/')[-1],
                    },
                    "_content_url": data.get("download_url", ""),
                }
                results.append(result)
                print(f"    ✓ Found")
                
                if len(results) >= max_results:
                    break
                    
        except Exception as e:
            print(f"    ✗ Check failed: {e}")
            continue
    
    if results:
        print(f"  ✓ Found {len(results)} results in known repos")
    else:
        print(f"  ✗ Not found in known repos")
    
    return results


def search_github_file(filename: str, max_results: int = 1) -> List[dict]:
    """Search for file on GitHub"""
    base_filename = os.path.basename(filename)
    
    if not GITHUB_TOKEN:
        print(f"  🔍 Searching in known repos (no GITHUB_TOKEN)")
        return search_in_known_repos(base_filename, filename, max_results)
    
    search_url = f"{GITHUB_API_BASE}/search/code"
    query = f"filename:{base_filename} language:java"
    
    headers = {
        "Accept": "application/vnd.github.v3+json",
        "Authorization": f"token {GITHUB_TOKEN}"
    }
    
    params = {
        "q": query,
        "per_page": max_results,
    }
    
    try:
        print(f"  🔍 Searching GitHub: {query}")
        response = requests.get(search_url, headers=headers, params=params, timeout=30)
        
        if response.status_code == 200:
            data = response.json()
            items = data.get("items", [])
            print(f"  ✓ Found {len(items)} results")
            return items
        elif response.status_code == 403 or response.status_code == 401:
            print(f"  ✗ Auth/rate limit, trying fallback...")
            return search_in_known_repos(base_filename, filename, max_results)
        else:
            print(f"  ✗ Search failed: HTTP {response.status_code}")
            return search_in_known_repos(base_filename, filename, max_results)
            
    except Exception as e:
        print(f"  ✗ Search exception: {e}")
        return search_in_known_repos(base_filename, filename, max_results)


def download_github_file_content(file_info: dict) -> Optional[str]:
    """Download GitHub file content"""
    try:
        if '_content_url' in file_info and file_info['_content_url']:
            print(f"    📥 Using direct download link")
            response = requests.get(file_info['_content_url'], timeout=30)
            if response.status_code == 200:
                return response.text
        
        api_url = file_info.get("url")
        if not api_url:
            return None
        
        headers = {
            "Accept": "application/vnd.github.v3.raw",
        }
        
        if GITHUB_TOKEN:
            headers["Authorization"] = f"token {GITHUB_TOKEN}"
        
        response = requests.get(api_url, headers=headers, timeout=30)
        
        if response.status_code == 200:
            return response.text
        else:
            print(f"    ✗ Download failed: HTTP {response.status_code}")
            return None
            
    except Exception as e:
        print(f"    ✗ Download exception: {e}")
        return None


def find_best_match(filename: str, search_results: List[dict]) -> Optional[dict]:
    """Find best matching file from search results"""
    if not search_results:
        return None
    
    filename_lower = filename.lower()
    path_parts = filename_lower.split('/')
    
    for result in search_results:
        if result['path'].lower() == filename_lower:
            print(f"    ✓ Exact path match: {result['repository']['full_name']}/{result['path']}")
            return result
    
    for result in search_results:
        result_path_parts = result['path'].lower().split('/')
        common_parts = set(path_parts) & set(result_path_parts)
        if len(common_parts) >= 2:
            print(f"    ✓ Partial path match: {result['repository']['full_name']}/{result['path']}")
            return result
    
    for result in search_results:
        repo_name = result['repository']['full_name'].lower()
        if any(keyword in repo_name for keyword in ['aliyun', 'oss', 'aliyun-oss']):
            print(f"    ✓ Repo name match: {result['repository']['full_name']}/{result['path']}")
            return result
    
    result = search_results[0]
    print(f"    ⚠ Using first result: {result['repository']['full_name']}/{result['path']}")
    return result


def process_scenario8_files(scenario8_dir: Path, output_dir: Path, dry_run: bool = False):
    """Process all files in Scenario8 directory"""
    if not scenario8_dir.exists():
        print(f"❌ Directory not found: {scenario8_dir}")
        return
    
    output_dir.mkdir(parents=True, exist_ok=True)
    
    java_files = sorted(scenario8_dir.glob("*.java"))
    
    print(f"📂 Found {len(java_files)} Java files")
    print(f"📥 Output directory: {output_dir}")
    print("=" * 80)
    
    success_count = 0
    failed_count = 0
    
    for idx, java_file in enumerate(java_files, start=1):
        print(f"\n[{idx}/{len(java_files)}] Processing: {java_file.name}")
        print("-" * 80)
        
        target_filename = extract_filename_from_java_file(java_file)
        if not target_filename:
            print(f"  ⚠ Skipped (cannot extract filename)")
            failed_count += 1
            continue
        
        search_results = search_github_file(target_filename, max_results=5)
        if not search_results:
            print(f"  ⚠ Skipped (no search results)")
            failed_count += 1
            time.sleep(RATE_LIMIT_DELAY)
            continue
        
        best_match = find_best_match(target_filename, search_results)
        if not best_match:
            print(f"  ⚠ Skipped (no best match)")
            failed_count += 1
            time.sleep(RATE_LIMIT_DELAY)
            continue
        
        if dry_run:
            print(f"  🔵 [DRY RUN] Would download: {best_match['repository']['full_name']}/{best_match['path']}")
            success_count += 1
        else:
            print(f"  📥 Downloading: {best_match['repository']['full_name']}/{best_match['path']}")
            content = download_github_file_content(best_match)
            
            if content:
                output_file = output_dir / java_file.name
                output_file.write_text(content, encoding='utf-8')
                print(f"  ✅ Saved: {output_file}")
                success_count += 1
            else:
                print(f"  ✗ Download failed")
                failed_count += 1
        
        time.sleep(RATE_LIMIT_DELAY)
    
    print("\n" + "=" * 80)
    print(f"📊 Processing complete:")
    print(f"  ✅ Success: {success_count}")
    print(f"  ✗ Failed: {failed_count}")
    print(f"  📁 Total: {len(java_files)}")
    
    if not GITHUB_TOKEN:
        print("\n💡 Tip: Set GITHUB_TOKEN environment variable for higher rate limits")
        print("   export GITHUB_TOKEN='your_github_token'")


def main():
    """Main function"""
    import argparse
    
    parser = argparse.ArgumentParser(
        description="Extract filenames from Scenario8 and download from GitHub"
    )
    parser.add_argument(
        "--scenario8-dir",
        type=Path,
        default=Path(__file__).parent / "Scenario8",
        help="Scenario8 source directory"
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path(__file__).parent / "Scenario8",
        help="Output directory"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Test mode (no actual download)"
    )
    
    args = parser.parse_args()
    
    print("🚀 GitHub File Crawler for Scenario8")
    print("=" * 80)
    
    process_scenario8_files(
        scenario8_dir=args.scenario8_dir,
        output_dir=args.output_dir,
        dry_run=args.dry_run
    )


if __name__ == "__main__":
    main()
