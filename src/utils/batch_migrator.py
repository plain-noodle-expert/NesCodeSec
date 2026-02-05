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
try:
    from tree_sitter_languages import get_parser
except Exception:
    get_parser = None
try:
    from loguru import logger
except Exception:
    import logging
    logging.basicConfig(level=logging.INFO)
    logger = logging.getLogger('batch_migrator')


try:
    from utils.xml_any2any_migrator import run_recipe
except Exception as e:
    print("[ERR] Please ensure xml_any2any_migrator.py is on PYTHONPATH and exposes run_recipe()", file=sys.stderr)
    raise

# Top-level directory name <-> recipe key
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
    only_src_dirs: Optional[List[str]] = None,   # Example: ["SAXReader","SAXParser"]
    dst_keys: Optional[List[str]] = None,        # Example: ["sax","stax"]
    env: Optional[Dict[str, str]] = None,        # Template vars (defaults include root_tag/list_tag)
    pair_dirs: bool = True,                      # True -> `<Src>__TO__<Dst>` directories; False -> `<Dst>`
) -> Tuple[int, int]:
    logger.info("===== Start Batch Migration =====")
    """
    Importable convenience wrapper around the migration pipeline.

    Returns:
        (processed_total, changed_total)
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

        # Target keys (default: every alternative parser except itself)
        targets = dst_keys or [k for k in KEY_TO_DIR.keys() if k != src_key]
        for dst_key in targets:
            if dst_key == src_key:
                continue

            recipe = cfg.get("migrations", {}).get(f"{src_key}->{dst_key}")
            if not recipe:
                logger.warning(f"No recipe for {src_key}->{dst_key}, skip {src_dir_name} -> {KEY_TO_DIR[dst_key]}")
                continue

            # Determine output location
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
                # If target is DocumentBuilder (jaxp_dom), dedupe cursor placements
                def dedupe_cursor_documentbuilder(text: str) -> str:
                    cursor = '<|user_cursor_is_here|>'
                    if text.count(cursor) <= 1:
                        return text
                    lines = text.splitlines()
                    # Strategy: prefer cursor immediately after FIRST_SETFEATURE marker if present
                    first_idx = None
                    for i, ln in enumerate(lines):
                        if '/*__FIRST_SETFEATURE__' in ln:
                            # if cursor exists on same line, keep that; else insert after this line
                            if cursor in ln:
                                first_idx = i
                                break
                            else:
                                first_idx = i + 1
                                break
                    if first_idx is None:
                        # fallback: find instantiation of DocumentBuilderFactory.newInstance() or SAXParserFactory.newInstance()
                        for i, ln in enumerate(lines):
                            if 'DocumentBuilderFactory.newInstance()' in ln or 'SAXParserFactory.newInstance()' in ln:
                                first_idx = i + 1
                                break
                    # Build new lines keeping only one cursor at first_idx
                    new_lines = []
                    inserted = False
                    for i, ln in enumerate(lines):
                        # remove any inline cursor occurrences
                        ln_clean = ln.replace(cursor, '')
                        new_lines.append(ln_clean)
                        if not inserted and first_idx is not None and i == first_idx - 1:
                            # insert cursor as its own line after this line
                            new_lines.append(cursor)
                            inserted = True
                    # If we never inserted and cursor present elsewhere, put single cursor at end
                    if not inserted:
                        # remove any remaining cursors then append one
                        filtered = [l.replace(cursor, '') for l in new_lines]
                        filtered.append(cursor)
                        return '\n'.join(filtered) + '\n'
                    return '\n'.join(new_lines) + '\n'

                if dst_key == 'jaxp_dom':
                    out_text = dedupe_cursor_documentbuilder(out_text)
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
    Run a single `<src_top_dir> -> <dst_key>` migration pair.
    Convenience wrapper around `run_batch_migrate`.
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

# ---------------- Marker utilities ----------------

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
    """Return the 0-based line that contains <|user_cursor_is_here|>."""
    for idx, line in enumerate(text.splitlines(), start=0):
        if "<|user_cursor_is_here|>" in line:
            return idx
    return None


# ---------------- Diff ----------------

def _udiff(a: str, b: str, src_path: str, dst_path: str, context: int = 0) -> str:
    """Return a unified diff string, keeping only the requested context (default 0)."""
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
    Compare `input_excerpt` (with markers stripped) against the original base files
    and emit compact unified diffs into `input_event`.
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


# === Tree-sitter initialization (no local compile needed) ===
ts_parser = None
if get_parser is not None:
    try:
        ts_parser = get_parser("java")
    except Exception:
        ts_parser = None

# === Token Guess：Zed philosophy ===
BYTES_PER_TOKEN_GUESS = 3

def guess_token_count(code: str, encoding="utf-8") -> int:
    """Rough token estimator: bytes / 3."""
    byte_len = len(code.encode(encoding))
    return byte_len // BYTES_PER_TOKEN_GUESS

# ---------------- Snippet extraction ----------------
def _extract_treesitter_scope(java_code: str, cursor_line: int, token_limit: int = 1000) -> Optional[str]:
    """
    Use the Tree-sitter AST to capture the largest syntax scope that contains the cursor.
    Returns snippet text tagged with (start_line, end_line), both 0-based (markers respected).
    """
    if ts_parser is None:
        return None
    tree = ts_parser.parse(hide_marker(java_code).encode("utf-8"))
    root = tree.root_node
    
    if root.has_error:
        logger.error("⚠️ Parse contains syntax errors (ERROR nodes exist).")
        
    def find_node(node, line):
        """Depth-first search for the smallest node that still contains the cursor line."""
        if not (node.start_point[0] <= line <= node.end_point[0]):
            return None
        for child in node.children:
            found = find_node(child, line)
            if found:
                return found
        return node

    node = find_node(root, cursor_line)  # cursor_line is 0-based
    if not node:
        return None

    # Walk up the parent chain and pick the largest scope within the token limit.
    lines = java_code.splitlines()
    best_scope = None
    while node:
        snippet_text = "\n".join(lines[node.start_point[0]: node.end_point[0] + 1])
        tokens = guess_token_count(snippet_text)
        
        if tokens <= token_limit:
            best_scope = (node.start_point[0], node.end_point[0]) # 0-based
        else:
            break  # Exceeded token cap, stop walking upward
        # logger.debug(f"[SNIPPET TOKEN: {tokens}, BEST SCOPE: {best_scope}] {java_code[best_scope[0]:best_scope[1]+1]}")
        node = node.parent
        
    if best_scope and best_scope[0] != best_scope[1]:
        start, end = best_scope
        head_marker = lines[:3]
        end_marker = lines[-2:]
        if start > 0:
            head_marker[1] = "```"
        return f"{start}:{end}:{cursor_line}:" + "\n".join(head_marker + lines[start:end + 1] + end_marker)
    return None


def _extract_brace_block(java_code: str, cursor_line: int) -> Optional[str]:
    """
    Return the brace block containing the cursor_line (0-based).
    Bury block range at the beginning of the file.
    """
    lines = java_code.splitlines()
    head_marker = lines[:3]
    end_marker = lines[-2:]
    pos = cursor_line  # cursor_line is 0-based
    # Walk upward to find the nearest '{'
    start = pos
    brace_count = 0
    while start >= 0:
        brace_count += lines[start].count("{")
        brace_count -= lines[start].count("}")
        if brace_count > 0:
            break
        start -= 1
    # Walk downward to find the matching '}'
    end = pos
    brace_count = 0
    while end < len(lines):
        brace_count += lines[end].count("}")
        brace_count -= lines[end].count("{")
        if brace_count > 0:
            break
        end += 1
    if start >= 0 and end < len(lines):
        if start==0:
            head_marker[1] = "```"
        return f"{start}:{end}:{cursor_line}:" + "\n".join(head_marker + lines[start:end + 1] + end_marker)
    return None


def _extract_window(java_code: str, cursor_line: int, window: int = 50) -> str:
    lines = java_code.splitlines()
    head_marker = lines[:3]
    end_marker = lines[-2:]
    start = max(0, cursor_line - window)  # Expand backwards by `window` lines
    end = min(len(lines) - 1, cursor_line + window)  # Expand forward by `window` lines
    if start > 0:
        head_marker[1] = "```"
    return f"{start}:{end}:{cursor_line}:" + "\n".join(head_marker + lines[start:end + 1] + end_marker)


def extract_snippet_by_cursor(java_code: str, cursor_line: int, count_board: list[int]) -> str:
    """
    Extract the portion of code containing `cursor_line` and encode the inclusive
    line range at the beginning of the snippet.
    Preference order: Tree-sitter (token-limited) -> brace block -> sliding window.
    `count_board` records which strategy was used:
        [0] = AST
        [1] = Brace
        [2] = Window
        [3] = Full file (short file fallback)
    """
    lines = java_code.splitlines()

    # case 1: very small file, return the entire content
    if len(lines) <= 100:
        count_board[3] += 1
        logger.debug("[FULL TEXT]")
        # take into account 3 head lines: file name, <start_of_file> and <editable_region_start>
        return f"3:{len(lines)-1}:{cursor_line}:" + java_code

    # case 2: Tree-sitter AST snippet
    snippet = _extract_treesitter_scope(java_code, cursor_line, token_limit=350)
    if snippet:
        count_board[0] += 1
        logger.debug("[AST SNIPPET]")
        return snippet

    # case 3: fallback brace block
    snippet = _extract_brace_block(java_code, cursor_line)
    if snippet:
        count_board[1] += 1
        logger.debug("[BRACE SNIPPET]")
        return snippet

    # case 4: fallback window
    count_board[2] += 1
    logger.debug("[WINDOW SNIPPET]")
    return _extract_window(java_code, cursor_line, window=50)


# ---------------- Full pipeline ----------------

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
    # Step 1. Migration (use the raw base_root without preprocessing)
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
    # Choose the correct base directory for comparison. The generator expects a folder
    # where top-level library dirs (Digester, SAXParser, ...) live. Support both the new
    # `NesCodeSecExamples/V9-XXE/base` layout and the legacy Maven-style tree.
    base_root_path = Path(base_root)
    nested_candidate = base_root_path / "base"
    legacy_candidate = base_root_path / "src" / "main" / "java" / "com" / "Scenario1" / "base"

    if nested_candidate.is_dir():
        base_for_event = nested_candidate
    elif legacy_candidate.is_dir():
        base_for_event = legacy_candidate
    else:
        base_for_event = base_root_path

    stats = generate_event(
        base_root=base_for_event,
        migrate_root=migrate_root,
        diff_output_root=event_root,
        context_lines=context_lines
    )
    # Step 3. Snippet extraction (marker-based)
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
            # logger.debug(f"[CURSOR LINE] {gen_file}: {cursor_line}")
            if not cursor_line:
                continue
            # Pass the entire migrated file (markers included)
            snippet = extract_snippet_by_cursor(gen_text, cursor_line, count_board)
            out_snip_path.parent.mkdir(parents=True, exist_ok=True)
            out_snip_path.write_text(snippet, encoding="utf-8")
    logger.info(f"[SNIPPET METHOD SUMMARY]\n[AST: {count_board[0]}, Brace: {count_board[1]}, Window: {count_board[2]}, All: {count_board[3]}]")
    return stats


# ---------------- Debug Helper ----------------

def debug_rebuild_from_migrate_full(
    migrate_root: str | Path,
    base_root: str | Path,
    event_root: str | Path,
    excerpt_root: str | Path,
    context_lines: int = 3,
    only_pairs: Optional[List[str]] = None,
) -> Dict[str, Dict[str, int]]:
    """
    **Debug Helper**: Reconstruct `input_event` and `input_excerpt` directly from an
    existing `migrate_full` directory—no need to rerun the expensive migration step.

    Useful when:
    - You already completed the full pipeline and just want to tweak diff/snippet logic.
    - You need to iterate on `context_lines` or snippet heuristics quickly.

    Parameters
    ----------
    migrate_root : str | Path
        Existing `migrate_full` directory containing migrated code
        (e.g., `NesCodeSecExamples/V9-XXE/migrate_full`)
        
    base_root : str | Path
        Base code directory used for diffing
        (e.g., `NesCodeSecExamples/V9-XXE/base`)
        
    event_root : str | Path
        Destination directory for the regenerated diff files (`input_event`)
        
    excerpt_root : str | Path
        Destination directory for the regenerated snippets (`input_excerpt`)
        
    context_lines : int, default=3
        Number of context lines to include in the unified diff (0 = only changes)
        
    only_pairs : list[str], optional
        Process only the specified migration pairs, e.g., ["SAXReader__TO__sax"].
        When None, rebuild all pairs.
        
    Returns
    -------
    stats : dict[str, dict[str, int]]
        Diff statistics per migration pair:
            - "total": files processed
            - "changed": files with differences
            - "nochange": identical files
            - "missing_base": files lacking a base counterpart
            
    Example
    -------
    >>> stats = debug_rebuild_from_migrate_full(
    ...     migrate_root="NesCodeSecExamples/V9-XXE/migrate_full",
    ...     base_root="NesCodeSecExamples/V9-XXE/base",
    ...     event_root="NesCodeSecExamples/V9-XXE/input_event_debug",
    ...     excerpt_root="NesCodeSecExamples/V9-XXE/input_excerpt_debug",
    ...     context_lines=0,
    ...     only_pairs=["SAXReader__TO__sax"]
    ... )
    """
    migrate_root = Path(migrate_root)
    base_root = Path(base_root)
    event_root = Path(event_root)
    excerpt_root = Path(excerpt_root)
    
    if not migrate_root.exists():
        raise FileNotFoundError(f"migrate_root does not exist: {migrate_root}")
    if not base_root.exists():
        raise FileNotFoundError(f"base_root does not exist: {base_root}")
    
    logger.info("=" * 60)
    logger.info("🔧 DEBUG: rebuilding input_event/input_excerpt from migrate_full")
    logger.info(f"  migrate_root: {migrate_root}")
    logger.info(f"  base_root: {base_root}")
    logger.info(f"  event_root: {event_root}")
    logger.info(f"  excerpt_root: {excerpt_root}")
    logger.info("=" * 60)
    
    # Step 1: regenerate input_event (diffs)
    logger.info("\n📝 Step 1: rebuilding input_event (diffs)...")
    event_root.mkdir(parents=True, exist_ok=True)
    
    stats_all = {}
    for pair in migrate_root.iterdir():
        if not pair.is_dir() or "__TO__" not in pair.name:
            continue
        
        # Respect only_pairs filter if provided
        if only_pairs and pair.name not in only_pairs:
            logger.debug(f"  Skipping {pair.name} (not in only_pairs)")
            continue
            
        src_top, _ = pair.name.split("__TO__", 1)
        
        out_pair_dir = event_root / pair.name
        out_pair_dir.mkdir(parents=True, exist_ok=True)
        stats = {"total": 0, "changed": 0, "nochange": 0, "missing_base": 0}
        stats_all[pair.name] = stats
        
        logger.info(f"  Processing {pair.name}...")
        
        for gen_file in pair.rglob("*.java"):
            rel = gen_file.relative_to(pair)
            base_file = base_root / src_top / rel
            out_diff_path = (out_pair_dir / rel).with_suffix(".diff")
            
            stats["total"] += 1
            if not base_file.exists():
                logger.warning(f"    ⚠️  Missing base file: {base_file}")
                stats["missing_base"] += 1
                continue
            
            base_text = base_file.read_text(encoding="utf-8", errors="ignore")
            gen_text = strip_marker(gen_file.read_text(encoding="utf-8", errors="ignore"))
            
            if base_text == gen_text:
                stats["nochange"] += 1
                continue
            
            stats["changed"] += 1
            diff_str = _udiff(base_text, gen_text, str(base_file), str(gen_file), context_lines)
            out_diff_path.parent.mkdir(parents=True, exist_ok=True)
            out_diff_path.write_text(diff_str, encoding="utf-8")
        
        logger.info(f"    ✓ Finished: total={stats['total']}, changed={stats['changed']}, "
                   f"nochange={stats['nochange']}, missing_base={stats['missing_base']}")
    
    # Step 2: regenerate input_excerpt (snippets)
    logger.info("\n✂️  Step 2: rebuilding input_excerpt (snippets)...")
    excerpt_root.mkdir(parents=True, exist_ok=True)
    
    count_board = [0, 0, 0, 0]  # ast, brace, window, full
    total_files = 0
    total_snippets = 0
    
    for pair in migrate_root.iterdir():
        if not pair.is_dir() or "__TO__" not in pair.name:
            continue
        
        # Respect only_pairs filter if provided
        if only_pairs and pair.name not in only_pairs:
            continue
            
        out_snip_dir = excerpt_root / pair.name
        out_snip_dir.mkdir(parents=True, exist_ok=True)
        
        logger.info(f"  Processing {pair.name}...")
        pair_files = 0
        pair_snippets = 0
        
        for gen_file in pair.rglob("*.java"):
            pair_files += 1
            total_files += 1
            
            out_snip_path = out_snip_dir / gen_file.name
            gen_text = gen_file.read_text(encoding="utf-8", errors="ignore")
            cursor_line = find_cursor_line(gen_text)
            
            if cursor_line is None:
                logger.debug(f"    ⚠️  Cursor marker not found: {gen_file.name}")
                continue
            
            pair_snippets += 1
            total_snippets += 1
            
            # Pass the entire migrated file (markers included)
            snippet = extract_snippet_by_cursor(gen_text, cursor_line, count_board)
            out_snip_path.parent.mkdir(parents=True, exist_ok=True)
            out_snip_path.write_text(snippet, encoding="utf-8")
        
        logger.info(f"    ✓ Finished: {pair_snippets}/{pair_files} files produced snippets")
    
    # Summary
    logger.info("\n" + "=" * 60)
    logger.info("📊 SUMMARY")
    logger.info("=" * 60)
    logger.info(f"Total files: {total_files}")
    logger.info(f"Snippets extracted: {total_snippets}")
    logger.info(f"\nSnippet strategy distribution:")
    logger.info(f"  • AST (Tree-sitter):  {count_board[0]:4d} ({count_board[0]/max(total_snippets,1)*100:.1f}%)")
    logger.info(f"  • Brace Block:        {count_board[1]:4d} ({count_board[1]/max(total_snippets,1)*100:.1f}%)")
    logger.info(f"  • Window (±50 lines): {count_board[2]:4d} ({count_board[2]/max(total_snippets,1)*100:.1f}%)")
    logger.info(f"  • Full File:          {count_board[3]:4d} ({count_board[3]/max(total_snippets,1)*100:.1f}%)")
    logger.info("\nDiff statistics:")
    for pair, s in stats_all.items():
        logger.info(f"  {pair}: total={s['total']}, changed={s['changed']}, "
                   f"nochange={s['nochange']}, missing_base={s['missing_base']}")
    logger.info("=" * 60)
    
    return stats_all


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
    ap.add_argument("--only-src-dirs", help="Comma-separated source directories to include")
    ap.add_argument("--dst-keys", help="Comma-separated destination keys to include")
    ap.add_argument("--env", action="append", default=[], help="Extra template vars as key=value (repeatable)")
    ap.add_argument("--context-lines", type=int, default=3, help="Number of context lines in diffs")
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
    # p = Path("NesCodeSecExamples/V9-XXE/migrate_full/DocumentBuilder__TO__Digester/EarthquakeUpdateJobService.java")
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
