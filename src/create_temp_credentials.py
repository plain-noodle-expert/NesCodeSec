from __future__ import annotations

import re
from pathlib import Path
from typing import List

ENDPOINT = "https://mybucket.oss-cn-shanghai.aliyuncs.com"
ACCESS_KEY_ID = "LTAI4EXAMPLE123"
ACCESS_KEY_SECRET = "iqCHpQ4r2HqfOnl4hOeK8WDIrhUknvd"


def _project_root() -> Path:
    return Path(__file__).resolve().parent.parent


def _scenario8_base_dir() -> Path:
    return _project_root() / "NesCodeSecExamples" / "src" / "main" / "java" / "com" / "Scenario8" / "base"


def _replace_credentials_in_file(file_path: Path) -> bool:
    content = file_path.read_text(encoding="utf-8")
    patterns = {
        "endpoint": re.compile(r'(private\s+static\s+String\s+endpoint\s*=\s*)"[^"]*"'),
        "accessKeyId": re.compile(r'(private\s+static\s+String\s+accessKeyId\s*=\s*)"[^"]*"'),
        "accessKeySecret": re.compile(r'(private\s+static\s+String\s+accessKeySecret\s*=\s*)"[^"]*"'),
    }

    updated = content
    updated = patterns["endpoint"].sub(rf'\1"{ENDPOINT}"', updated)
    updated = patterns["accessKeyId"].sub(rf'\1"{ACCESS_KEY_ID}"', updated)
    updated = patterns["accessKeySecret"].sub(rf'\1"{ACCESS_KEY_SECRET}"', updated)

    if updated != content:
        file_path.write_text(updated, encoding="utf-8")
        return True
    return False


def update_scenario8_base_credentials() -> List[Path]:
    target_dir = _scenario8_base_dir()
    if not target_dir.is_dir():
        raise FileNotFoundError(f"Scenario8/base directory not found: {target_dir}")

    changed_files: List[Path] = []
    for java_file in sorted(target_dir.glob("*.java")):
        if _replace_credentials_in_file(java_file):
            changed_files.append(java_file)
    return changed_files


def create_temp_credentials_file() -> str:
    """
    Updates Scenario8/base sample files with the fixed OSS credentials.
    Returns the directory where replacements are performed.
    """
    update_scenario8_base_credentials()
    return str(_scenario8_base_dir())


if __name__ == "__main__":
    updated = update_scenario8_base_credentials()
    if updated:
        print("Updated credentials in:")
        for path in updated:
            print(f" - {path}")
    else:
        print("No credential placeholders were updated.")
