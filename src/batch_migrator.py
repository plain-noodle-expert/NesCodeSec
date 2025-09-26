# batch_migrator.py
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import annotations

import argparse
import json
import sys
import difflib
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from tree_sitter_languages import get_parser
from loguru import logger

# 依赖你的迁移引擎（需要 xml_any2any_migrator.py 与之同目录或在 PYTHONPATH 中）
try:
    from xml_any2any_migrator import run_recipe  # 只用这个函数
except Exception as e:
    print("[ERR] 请确保 xml_any2any_migrator.py 在 PYTHONPATH 中，并包含 run_recipe()", file=sys.stderr)
    raise

# 顶层目录名 <-> 规则 key
DIR_TO_KEY: Dict[str, str] = {
    "Digester": "digester",
    "DocumentBuilder": "jaxp_dom",
    "InputFactory": "stax",
    "SAXBuilder": "jdom2",
    "SAXParser": "sax",
    "SAXReader": "dom4j",
}
KEY_TO_DIR: Dict[str, str] = {v: k for k, v in DIR_TO_KEY.items()}

JAVA_GLOB = "*.java"


def run_batch_migrate(
    *,
    config_path: str | Path,
    base_root: str | Path,
    output_root: str | Path,
    dry_run: bool = False,
    only_src_dirs: Optional[List[str]] = None,   # 例如 ["SAXReader","SAXParser"]
    dst_keys: Optional[List[str]] = None,        # 例如 ["sax","stax"]
    env: Optional[Dict[str, str]] = None,        # 模板变量（默认包含 root_tag/list_tag）
    pair_dirs: bool = True,                      # True: 输出到 <Src>__TO__<Dst> 目录；False: 输出到 <Dst> 目录
) -> Tuple[int, int]:
    logger.info("===== Start Batch Migration =====")
    """
    可导入调用的批量迁移函数。

    返回: (processed_total, changed_total)
    """
    config_path = Path(config_path)
    base_root = Path(base_root)
    output_root = Path(output_root)

    if not config_path.exists():
        raise FileNotFoundError(f"Config not found: {config_path}")
    if not base_root.exists():
        raise FileNotFoundError(f"base_root not found: {base_root}")
    output_root.mkdir(parents=True, exist_ok=True)

    try:
        cfg = json.loads(config_path.read_text(encoding="utf-8"))
    except Exception as e:
        raise RuntimeError(f"Failed to load config JSON: {e}")

    merged_env: Dict[str, str] = {"root_tag": "beans", "list_tag": "bean"}
    if env:
        merged_env.update(env)

    processed, changed = 0, 0

    for src_dir_name, src_key in DIR_TO_KEY.items():
        if only_src_dirs and src_dir_name not in only_src_dirs:
            continue

        src_dir = base_root / src_dir_name
        if not src_dir.exists():
            logger.warning(f"missing dir: {src_dir}")
            continue

        # 目标 key 集合：默认五个（排除自身）
        targets = dst_keys or [k for k in KEY_TO_DIR.keys() if k != src_key]
        for dst_key in targets:
            if dst_key == src_key:
                continue

            recipe = cfg.get("migrations", {}).get(f"{src_key}->{dst_key}")
            if not recipe:
                logger.warning(f"No recipe for {src_key}->{dst_key}, skip {src_dir_name} -> {KEY_TO_DIR[dst_key]}")
                continue

            # 输出目录
            if pair_dirs:
                pair_name = f"{src_dir_name}__TO__{KEY_TO_DIR[dst_key]}"
                out_root = output_root / pair_name
            else:
                out_root = output_root / KEY_TO_DIR[dst_key]
            out_root.mkdir(parents=True, exist_ok=True)

            for src_path in src_dir.rglob(JAVA_GLOB):
                rel = src_path.relative_to(src_dir)
                dst_path = out_root / rel

                try:
                    src_text = src_path.read_text(encoding="utf-8", errors="ignore")
                except Exception as e:
                    logger.error(f"[ERR] Read {src_path}: {e}")
                    continue

                out_text = run_recipe(src_text, recipe, merged_env)
                processed += 1

                if out_text != src_text:
                    changed += 1
                    if dry_run:
                        print(_udiff(src_text, out_text, str(src_path), str(dst_path)))
                    else:
                        dst_path.parent.mkdir(parents=True, exist_ok=True)
                        try:
                            dst_path.write_text(f"{dst_path.name}\n```<|start_of_file|>\n<|editable_region_start|>\n"+out_text+"\n<|editable_region_end|>\n```", encoding="utf-8")
                            # logger.debug(f"[OK] {src_path.name}: {src_dir_name}({src_key})->{KEY_TO_DIR[dst_key]}({dst_key}) => {dst_path}")
                        except Exception as e:
                            logger.error(f"[ERR] Write {dst_path}: {e}")
                else:
                    if dry_run:
                        logger.info(f"[SKIP no-change] {src_path} -> {dst_path}")

    return processed, changed


def run_one_pair(
    *,
    config_path: str | Path,
    base_root: str | Path,
    output_root: str | Path,
    src_top_dir: str,    # e.g. "SAXReader"
    dst_key: str,        # e.g. "sax"
    dry_run: bool = False,
    env: Optional[Dict[str, str]] = None,
    pair_dirs: bool = True,
) -> Tuple[int, int]:
    """
    只跑一个 pair：<src_top_dir> -> <dst_key>
    等价于 run_batch_migrate 的子集封装。
    """
    if src_top_dir not in DIR_TO_KEY:
        raise ValueError(f"Unknown src_top_dir: {src_top_dir} (must be one of {list(DIR_TO_KEY)})")
    return run_batch_migrate(
        config_path=config_path,
        base_root=base_root,
        output_root=output_root,
        dry_run=dry_run,
        only_src_dirs=[src_top_dir],
        dst_keys=[dst_key],
        env=env,
        pair_dirs=pair_dirs,
    )

# ---------------- Marker 工具 ----------------

def strip_marker(code: str) -> str:
    code = "\n".join(code.split("\n")[1:])
    return code.replace("<|user_cursor_is_here|>", "") \
               .replace("<|editable_region_start|>", "") \
               .replace("<|editable_region_end|>", "") \
               .replace("<|start_of_file|>", "") \
               .replace("```", "").strip()

def hide_marker(code: str) -> str:
    return "//"+code.replace("<|user_cursor_is_here|>", "/*<|user_cursor_is_here|>*/")\
                .replace("<|editable_region_start|>", "/*<|editable_region_start|>*/") \
                .replace("<|editable_region_end|>", "/*<|editable_region_end|>*/") \
                .replace("<|start_of_file|>", "/*<|start_of_file|>*/") \
                .replace("```", "").strip()

def find_cursor_line(text: str) -> Optional[int]:
    """找到 <|user_cursor_is_here|> 的行号 (1-based)"""
    for idx, line in enumerate(text.splitlines(), start=1):
        if "<|user_cursor_is_here|>" in line:
            return idx
    return None


# ---------------- Diff ----------------

def _udiff(a: str, b: str, src_path: str, dst_path: str, context: int = 0) -> str:
    """生成统一 diff 字符串，默认只保留修改行"""
    return "".join(
        difflib.unified_diff(
            a.splitlines(True),
            b.splitlines(True),
            fromfile=src_path,
            tofile=dst_path,
            n=context
        )
    )

def generate_event(
    *,
    base_root: str | Path,
    migrate_root: str | Path,
    diff_output_root: str | Path,
    encoding: str = "utf-8",
    context_lines: int = 3
) -> Dict[str, Dict[str, int]]:
    """
    对比 input_excerpt (去 marker) 和 base (完整)，生成精简 diff 到 input_event
    """
    base_root = Path(base_root)
    migrate_root = Path(migrate_root)
    diff_output_root = Path(diff_output_root)
    diff_output_root.mkdir(parents=True, exist_ok=True)

    stats_all = {}
    for pair in migrate_root.iterdir():
        if not pair.is_dir() or "__TO__" not in pair.name:
            continue
        src_top, _ = pair.name.split("__TO__", 1)

        out_pair_dir = diff_output_root / pair.name
        out_pair_dir.mkdir(parents=True, exist_ok=True)
        stats = {"total": 0, "changed": 0, "nochange": 0, "missing_base": 0}
        stats_all[pair.name] = stats

        for gen_file in pair.rglob("*.java"):
            rel = gen_file.relative_to(pair)
            base_file = base_root / src_top / rel
            out_diff_path = (out_pair_dir / rel).with_suffix(".diff")

            stats["total"] += 1
            if not base_file.exists():
                logger.warning(f"Base file missing: {base_file}")
                stats["missing_base"] += 1
                continue

            base_text = base_file.read_text(encoding=encoding, errors="ignore")
            gen_text = strip_marker(gen_file.read_text(encoding=encoding, errors="ignore"))

            if base_text == gen_text:
                stats["nochange"] += 1
                continue

            stats["changed"] += 1
            diff_str = _udiff(base_text, gen_text, str(base_file), str(gen_file), context_lines)
            out_diff_path.parent.mkdir(parents=True, exist_ok=True)
            out_diff_path.write_text(diff_str, encoding=encoding)

    return stats_all


# === Tree-sitter 初始化（免编译） ===
# JAVA_LANGUAGE = get_language("java")
ts_parser = get_parser("java")

# === Token Guess：Zed philosophy ===
BYTES_PER_TOKEN_GUESS = 3

def guess_token_count(code: str, encoding="utf-8") -> int:
    """粗略估算 token 数：字节数 / 3"""
    byte_len = len(code.encode(encoding))
    return byte_len // BYTES_PER_TOKEN_GUESS

# ---------------- Snippet 截取 ----------------
def _extract_treesitter_scope(java_code: str, cursor_line: int, token_limit: int = 350) -> Optional[Tuple[int, int]]:
    """
    使用 Tree-sitter AST 截取 cursor 所在的最大语法作用域。
    返回 (start_line, end_line)，行号 1-based。
    """
    tree = ts_parser.parse(java_code.encode("utf-8"))
    root = tree.root_node
    
    if root.has_error:
        logger.error("⚠️ Parse contains syntax errors (ERROR nodes exist).")
        
    def find_node(node, line):
        """深度优先，找到包含 cursor 的最小节点"""
        if not (node.start_point[0] <= line <= node.end_point[0]):
            return None
        for child in node.children:
            found = find_node(child, line)
            if found:
                return found
        return node

    node = find_node(root, cursor_line - 1)  # cursor_line 是 1-based
    if not node:
        return None

    # 遍历父节点链，找符合 token_limit 的最大 scope
    best_scope = None
    while node:
        snippet_text = "\n".join(java_code.splitlines()[node.start_point[0]: node.end_point[0] + 1])
        tokens = guess_token_count(snippet_text)
        
        if tokens <= token_limit:
            best_scope = (node.start_point[0], node.end_point[0]) # 0-based
        else:
            break  # 超过了就不用再往上
        logger.debug(f"[SNIPPET TOKEN: {tokens}, BEST SCOPE: {best_scope}] {snippet_text}")
        node = node.parent
    return best_scope


def _extract_brace_block(java_code: str, cursor_line: int) -> Optional[str]:
    lines = java_code.splitlines()
    head_marker = [lines[0], "```", lines[2]]
    end_marker = lines[-2:]
    pos = cursor_line - 1
    # 向上找最近的 '{'
    start = pos
    brace_count = 0
    while start >= 0:
        brace_count += lines[start].count("{")
        brace_count -= lines[start].count("}")
        if brace_count > 0:
            break
        start -= 1
    # 向下找匹配 '}'
    end = pos
    brace_count = 0
    while end < len(lines):
        brace_count += lines[end].count("}")
        brace_count -= lines[end].count("{")
        if brace_count > 0:
            break
        end += 1
    if start >= 0 and end < len(lines):
        return "\n".join(head_marker + lines[start:end + 1] + end_marker)
    return None


def _extract_window(java_code: str, cursor_line: int, window: int = 50) -> str:
    lines = java_code.splitlines()
    head_marker = lines[:3]
    end_marker = lines[-2:]
    start = max(0, cursor_line - window - 1)
    end = min(len(lines), cursor_line + window)
    if start > 0:
        head_marker[1] = "```"
    return "\n".join(head_marker + lines[start:end] + end_marker)


def extract_snippet_by_cursor(java_code: str, cursor_line: int, count_board: list[int]) -> str:
    """
    截取包含 cursor_line 的代码片段。
    优先使用 Tree-sitter AST（带 token 限制），失败则回退到 brace block 或 window。
    count_board: 记录使用了哪种策略
        [0] = AST
        [1] = Brace
        [2] = Window
        [3] = Full file (short file fallback)
    """
    lines = java_code.splitlines()

    # case 1: 文件很小，直接返回
    if len(lines) <= 100:
        count_board[3] += 1
        return java_code

    # case 2: Tree-sitter AST 截取
    scope = _extract_treesitter_scope(hide_marker(java_code), cursor_line, token_limit=350)
    if scope and scope[0] != scope[1]:
        start, end = scope
        count_board[0] += 1
        head_marker = lines[:3]
        end_marker = lines[-2:]
        if start > 1:
            head_marker[1] = "```"
        return "\n".join(head_marker + lines[start - 1:end] + end_marker)

    # case 3: fallback brace block
    snippet = _extract_brace_block(java_code, cursor_line)
    if snippet:
        count_board[1] += 1
        return snippet

    # case 4: fallback window
    count_board[2] += 1
    return _extract_window(java_code, cursor_line, window=50)

def select_excerpt_2phase(java_code: str, cursor_line: int, editable_limit: int = 350, context_limit: int = 150):
    """
    Mimic Zed's two-phase snippet selection:
    1. Syntax-aware selection (AST)
    2. Line-based bidirectional expansion
    """
    lines = java_code.splitlines()
    head_marker = lines[:3]
    end_marker = lines[-2:]
    
    # --- Phase 1: AST syntax-aware selection ---
    scope = _extract_treesitter_scope(hide_marker(java_code), cursor_line, token_limit=editable_limit)
    if scope is None or scope[0] == scope[1]:
        # fallback: just take cursor line as starting region
        start, end = cursor_line, cursor_line
    else:
        start, end = scope

    # --- Phase 2: Line expansion for context ---
    total_limit = editable_limit + context_limit
    while True:
        if start > 0:
            candidate = "\n".join(lines[start - 1:end + 1])
            if guess_token_count(candidate) <= total_limit:
                start -= 1
            else:
                break
        if end < len(lines):
            candidate = "\n".join(lines[start:end + 2])
            if guess_token_count(candidate) <= total_limit:
                end += 1
            else:
                break

    if start > 1:
        head_marker[1] = "```"
    return "\n".join(head_marker + lines[start - 1:end] + end_marker)



# ---------------- 全流程 ----------------

def full_pipeline(
    config_path: str | Path,
    base_root: str | Path,
    migrate_root: str | Path,
    event_root: str | Path,
    excerpt_root: str | Path,
    only_src_dirs: Optional[List[str]] = None,
    dst_keys: Optional[List[str]] = None,
    env: Optional[Dict[str, str]] = None,
    context_lines: int = 3
):
    """
    Run the **complete NES migration pipeline**:
    1. Migrate original code (base) to multiple alternative XML parsing libraries.
    2. Generate diffs between base and migrated code.
    3. Extract code snippets around the <|user_cursor_is_here|> marker.

    Parameters
    ----------
    config_path : str | Path
        Path to the JSON config file (e.g. `migrations_compilable.json`) that
        defines the migration recipes between XML libraries.

    base_root : str | Path
        Path to the directory containing the **original base code**.
        This directory must contain subfolders named after libraries
        (e.g. `SAXParser/`, `Digester/`, etc.).

    migrate_root : str | Path
        Path to the directory where the **full version migrated code** will be written.
        This is the output of the migration step (`input_excerpt`).

    event_root : str | Path
        Path to the directory where the **diff results** will be written.
        Each file will be a unified diff between base and migrated code
        (`input_event`).

    excerpt_root : str | Path
        Path to the directory where the **snippets** will be written.
        Snippets are extracted from migrated code files based on the
        `<|user_cursor_is_here|>` marker (`input_snippet`).

    only_src_dirs : list[str], optional
        Restrict the migration to only these source top-level directories.
        Example: `["SAXReader", "SAXParser"]`.
        If None, all source libraries are processed.

    dst_keys : list[str], optional
        Restrict the migration to only these target library keys.
        Example: `["sax", "stax"]`.
        If None, all alternative targets are processed.

    env : dict[str, str], optional
        Additional template variables for migration recipes.
        By default includes:
            - `"root_tag": "beans"`
            - `"list_tag": "bean"`

    context_lines : int, default=3
        Number of context lines to include in the unified diff.
        - `0` → only changed lines are shown.
        - `3` (default) → changed lines plus 3 lines of context.

    Returns
    -------
    stats : dict[str, dict[str, int]]
        A dictionary keyed by `<Src>__TO__<Dst>` pair directory, with statistics:
            - `"total"` : number of processed files
            - `"changed"` : number of files with differences
            - `"nochange"` : number of files identical to base
            - `"missing_base"` : number of files with no matching base file

    Side Effects
    ------------
    - Writes migrated code to `migrate_root`.
    - Writes diffs to `event_root`.
    - Writes extracted snippets to `excerpt_root`.
    """
    # Step 1. 迁移
    run_batch_migrate(
        config_path=config_path,
        base_root=base_root,
        output_root=migrate_root,
        dry_run=False,
        only_src_dirs=only_src_dirs,
        dst_keys=dst_keys,
        env=env,
        pair_dirs=True,
    )
    # Step 2. diff
    stats = generate_event(
        base_root=base_root,
        migrate_root=migrate_root,
        diff_output_root=event_root,
        context_lines=context_lines
    )
    # Step 3. snippet（基于 marker）
    count_board = [0, 0, 0, 0]  # ast, brace, window, all
    migrate_root = Path(migrate_root)
    excerpt_root = Path(excerpt_root)
    excerpt_root.mkdir(parents=True, exist_ok=True)
    for pair in migrate_root.iterdir():
        if not pair.is_dir() or "__TO__" not in pair.name:
            continue
        out_snip_dir = excerpt_root / pair.name
        out_snip_dir.mkdir(parents=True, exist_ok=True)
        for gen_file in pair.rglob("*.java"):
            out_snip_path = out_snip_dir / gen_file.name
            gen_text = gen_file.read_text(encoding="utf-8", errors="ignore")
            cursor_line = find_cursor_line(gen_text)
            logger.debug(f"[CURSOR LINE] {gen_file}: {cursor_line}")
            if not cursor_line:
                continue
            snippet = extract_snippet_by_cursor(gen_text, cursor_line, count_board)
            out_snip_path.parent.mkdir(parents=True, exist_ok=True)
            out_snip_path.write_text(snippet, encoding="utf-8")
    logger.info(f"[SNIPPET METHOD SUMMARY]\n[AST: {count_board[0]}, Brace: {count_board[1]}, Window: {count_board[2]}, All: {count_board[3]}]")
    return stats


# ---------------- CLI ----------------

def _split_csv(s: Optional[str]) -> Optional[List[str]]:
    if not s:
        return None
    return [x.strip() for x in s.split(",") if x.strip()]

def main():
    ap = argparse.ArgumentParser(description="NES pipeline: migrate -> diff -> snippet")
    ap.add_argument("--config", required=True, help="migrations_compilable.json")
    ap.add_argument("--base-root", required=True, help=".../base")
    ap.add_argument("--migrate-root", required=True, help=".../migrate_full")
    ap.add_argument("--event-root", required=True, help=".../input_event")
    ap.add_argument("--excerpt-root", required=True, help=".../input_excerpt")
    ap.add_argument("--only-src-dirs", help="仅处理指定源目录，逗号分隔")
    ap.add_argument("--dst-keys", help="仅迁移到指定库 key，逗号分隔")
    ap.add_argument("--env", action="append", default=[], help="额外模板变量，key=value，可多次")
    ap.add_argument("--context-lines", type=int, default=3, help="diff 上下文行数")
    args = ap.parse_args()

    only_src_dirs = _split_csv(args.only_src_dirs)
    dst_keys = _split_csv(args.dst_keys)
    env: Dict[str, str] = {"root_tag": "beans", "list_tag": "bean"}
    for kv in args.env:
        if "=" in kv:
            k, v = kv.split("=", 1)
            env[k.strip()] = v.strip()

    stats = full_pipeline(
        config_path=args.config,
        base_root=args.base_root,
        migrate_root=args.migrate_root,
        event_root=args.event_root,
        excerpt_root=args.excerpt_root,
        only_src_dirs=only_src_dirs,
        dst_keys=dst_keys,
        env=env,
        context_lines=args.context_lines,
    )

    print("\n[SUMMARY]")
    for pair, s in stats.items():
        print(f"{pair}: total={s['total']}, changed={s['changed']}, "
              f"nochange={s['nochange']}, missing_base={s['missing_base']}")

if __name__ == "__main__":
    main()
    # p = Path("/Users/tt/Documents/NesCodeSec/NesCodeSecExamples/src/main/java/com/Scenario1/migrate_full/DocumentBuilder__TO__Digester/EarthquakeUpdateJobService.java")
    # _extract_treesitter_scope(
    #     p.read_text(encoding="utf-8", errors="ignore"),
    #     109
    # )
    # gen_text = p.read_text(encoding="utf-8", errors="ignore")
    # lines = gen_text.splitlines()
    # cursor_line = 109
    # snippet = select_excerpt_2phase(
    #     gen_text,
    #     cursor_line,
    #     editable_limit=350,
    #     context_limit=150
    # )
    
    # snippet = extract_snippet_by_cursor(
    #     gen_text,
    #     cursor_line,
    #     [0,0,0,0]
    # )
    # print(snippet)
    # print(hide_marker(p.read_text(encoding="utf-8", errors="ignore")))