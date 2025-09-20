#!/usr/bin/env python3
# -*- coding: utf-8 -*-

from __future__ import annotations

import argparse
import json
import sys
import difflib
from pathlib import Path
from typing import Dict, List, Optional, Tuple, Any
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


def _udiff(a: str, b: str, src_path: str, dst_path: str) -> str:
    return "".join(
        difflib.unified_diff(
            a.splitlines(True), b.splitlines(True),
            fromfile=src_path, tofile=dst_path
        )
    )

def strip_marker(code: str) -> str:
        code = "\n".join(code.split("\n")[1:])
        return code.replace("<|user_cursor_is_here|>", "")\
                .replace("<|editable_region_start|>", "")\
                .replace("<|editable_region_end|>", "")\
                .replace("<|start_of_file|>", "")\
                .replace("```", "").strip()

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
                            logger.debug(f"[OK] {src_path.name}: {src_dir_name}({src_key})->{KEY_TO_DIR[dst_key]}({dst_key}) => {dst_path}")
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

def generate_event(
    *,
    base_root: str | Path,
    # 这是你的“生成物根目录”（包含 <SrcTop>__TO__<DstTop> 子目录）
    excerpt_root: str | Path,
    # 这是“diff 输出根目录”，默认与你要求的一致；可改为任意路径
    diff_output_root: str | Path = "NesCodeSecExamples/src/main/java/com/Scenario1/input_event",
    pair_names: Optional[List[str]] = None,
    include_nochange: bool = True,
    encoding: str = "utf-8",
) -> Dict[str, Dict[str, int]]:
    """
    将 excerpt_root 下的 <SrcTop>__TO__<DstTop>/*.java 与 base_root/<SrcTop>/*.java 做 diff，
    并把 diff 写入 diff_output_root/<SrcTop>__TO__<DstTop>/<相对路径>.diff（结构与生成物一致）。

    返回：每个 pair 的统计 {"total": X, "changed": Y, "nochange": Z, "missing_base": K}
    """
    base_root = Path(base_root)
    excerpt_root = Path(excerpt_root)
    diff_output_root = Path(diff_output_root)
    diff_output_root.mkdir(parents=True, exist_ok=True)

    stats_all: Dict[str, Dict[str, int]] = {}

    if not excerpt_root.exists():
        print(f"[WARN] excerpt_root not found: {excerpt_root}")
        return stats_all

    # 自动发现 pair 目录（<SrcTop>__TO__<DstTop>）
    if pair_names is None:
        pairs: List[str] = []
        for p in excerpt_root.iterdir():
            if not p.is_dir():
                continue
            name = p.name
            if "__TO__" in name:
                src_top, dst_top = name.split("__TO__", 1)
                if src_top in DIR_TO_KEY and dst_top in DIR_TO_KEY:
                    pairs.append(name)
        pair_names = sorted(pairs)
    else:
        pair_names = [n for n in pair_names if (excerpt_root / n).is_dir()]

    for pair in pair_names:
        gen_pair_dir = excerpt_root / pair
        try:
            src_top, dst_top = pair.split("__TO__", 1)
        except ValueError:
            print(f"[WARN] invalid pair dir name: {pair}")
            continue
        if src_top not in DIR_TO_KEY or dst_top not in DIR_TO_KEY:
            print(f"[WARN] unknown top dir in pair: {pair}")
            continue

        out_pair_dir = diff_output_root / pair  # 镜像 pair 结构
        out_pair_dir.mkdir(parents=True, exist_ok=True)

        stats = {"total": 0, "changed": 0, "nochange": 0, "missing_base": 0}
        stats_all[pair] = stats

        for gen_file in gen_pair_dir.rglob(JAVA_GLOB):
            rel = gen_file.relative_to(gen_pair_dir)
            base_file = base_root / src_top / rel
            out_diff_path = (out_pair_dir / rel).with_suffix(rel.suffix + ".diff")

            stats["total"] += 1

            if not base_file.exists():
                stats["missing_base"] += 1
                print(f"[MISS base] {base_file} (for {gen_file})")
                continue

            try:
                base_text = base_file.read_text(encoding=encoding, errors="ignore")
                gen_text  = strip_marker(gen_file.read_text(encoding=encoding, errors="ignore"))
            except Exception as e:
                print(f"[ERR read] {base_file} / {gen_file}: {e}")
                continue

            if base_text == gen_text:
                stats["nochange"] += 1
                if include_nochange:
                    print(f"[NOCHANGE] {base_file} == {gen_file}")
                continue

            stats["changed"] += 1
            diff_str = _udiff(base_text, gen_text, str(base_file), str(gen_file))

            out_diff_path.parent.mkdir(parents=True, exist_ok=True)
            try:
                out_diff_path.write_text(diff_str, encoding=encoding)
                print(f"[WROTE] {out_diff_path}")
            except Exception as e:
                print(f"[ERR write diff] {out_diff_path}: {e}")

    # 汇总
    print("\n[SUMMARY]")
    for pair, s in stats_all.items():
        print(f"  {pair}: total={s['total']}, changed={s['changed']}, "
              f"nochange={s['nochange']}, missing_base={s['missing_base']}")
    return stats_all


# ---------------- CLI 包装：你也可以直接 python batch_migrator.py 运行 ----------------

def _split_csv(s: Optional[str]) -> Optional[List[str]]:
    if not s:
        return None
    return [x.strip() for x in s.split(",") if x.strip()]

def main():
    ap = argparse.ArgumentParser(description="Batch XML parser migrator (function-first).")
    ap.add_argument("--config", required=True, help="绝对路径：migrations_compilable.json")
    ap.add_argument("--base-root", required=True, help="绝对路径：.../base")
    ap.add_argument("--output-root", required=True, help="绝对路径：.../input_excerpt")
    ap.add_argument("--dry-run", action="store_true", help="仅打印 diff，不写文件")
    ap.add_argument("--only-src-dirs", help="逗号分隔：仅处理这些源顶层目录，如 SAXReader,SAXParser")
    ap.add_argument("--dst-keys", help="逗号分隔：限定目标库 key，如 sax,stax,jdom2,dom4j,digester,jaxp_dom")
    ap.add_argument("--env", action="append", default=[], help="额外模板变量，格式 key=value，可多次")
    ap.add_argument("--no-pair-dirs", action="store_true", help="输出不使用 <Src>__TO__<Dst>，而是直接 <Dst> 顶层目录")
    args = ap.parse_args()

    only_src_dirs = _split_csv(args.only_src_dirs)
    dst_keys = _split_csv(args.dst_keys)

    env: Dict[str, str] = {"root_tag": "beans", "list_tag": "bean"}
    for kv in args.env:
        if "=" in kv:
            k, v = kv.split("=", 1)
            env[k.strip()] = v.strip()

    processed, changed = run_batch_migrate(
        config_path=args.config,
        base_root=args.base_root,
        output_root=args.output_root,
        dry_run=args.dry_run,
        only_src_dirs=only_src_dirs,
        dst_keys=dst_keys,
        env=env,
        pair_dirs=not args.no_pair_dirs,
    )

    suffix = " (dry-run)" if args.dry_run else ""
    print(f"[DONE{suffix}] processed={processed}, changed={changed}")

if __name__ == "__main__":
    main()
