#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
xml_any2any_migrator.py
---------------------------------
通用“XML解析库 A -> B” 代码迁移引擎。

特性
- 用 JSON 配置（migrations_compilable.json）描述迁移步骤（steps）
- 支持 step 类型：
  * regex_replace  : 正则替换（跨行、多行、命名分组、模板 ${...}）
  * imports_add    : 批量插入 import（去重、放在 package/最后一个 import 后）
  * imports_remove : 批量移除 import（整行删除）
  * insert_after   : 在首次匹配处之后插入一段代码（带 guard 防重复）
  * append_once    : 末尾追加一次文本（带 guard 防重复）
- 缩进与模板：
  * 先对模板去公共缩进（dedent），再渲染 ${...}，避免“双倍缩进”
  * 行首缩进可通过 JSON 里的 (?P<indent>\\s*) + ${indent} 精准复用
  * indent_exact / indent_add（可选）控制每条 step 的额外缩进
- 非就地输出：
  * --out-root 指定输出根目录，保持与 --root 的相对子结构一致（原文件不动）
  * --only-changed 仅输出发生变化的 .java
  * --copy-nonjava 同步非 .java 文件
- dry-run 预览：
  * --dry-run 仅打印 diff；--diff-context 调整上下文行数

命令行示例
  python xml_any2any_migrator.py \
    --root path/to/java/src \
    --from jaxp_dom --to stax \
    --config migrations_compilable.json \
    --root-tag beans --list-tag bean \
    --out-root input_event \
    --dry-run

编程示例
  from xml_any2any_migrator import run_migration
  changed = run_migration(
      root="path/to/java/src",
      src_key="jaxp_dom",
      dst_key="sax",
      config_path="migrations_compilable.json",
      dry_run=True,
      env={"root_tag": "beans", "list_tag": "bean"},
      out_root="input_event",
  )
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import difflib
import textwrap
import shutil
from pathlib import Path
from typing import Dict, Any, List, Optional


# 默认空配置；实际迁移规则请用 --config 传入
DEFAULT_CONFIG: Dict[str, Any] = {"migrations": {}}


# -------------------- 正则 / 模板 --------------------

def _flags(flag_str: str | None) -> int:
    """将 'S', 'M', 'I' 组合映射为 re 标志位。"""
    flag_str = flag_str or ""
    f = 0
    for ch in flag_str:
        if ch == "S":
            f |= re.S
        elif ch == "M":
            f |= re.M
        elif ch == "I":
            f |= re.I
    return f


def _render_template(text: str, ctx: Dict[str, str]) -> str:
    """把 ${key} 占位符替换为 ctx[key]（简单占位替换，不支持条件/循环）。"""
    out = text
    for k, v in ctx.items():
        out = out.replace("${" + k + "}", v)
    return out


# -------------------- import 操作 --------------------

def add_imports(src: str, imports: List[str]) -> str:
    """在 package 或最后一个 import 后插入 import 声明；已有的不重复添加。"""
    if not imports:
        return src
    to_add = [imp for imp in imports if imp and f"import {imp};" not in src]
    if not to_add:
        return src

    # 插入位置：
    #  1) 默认 0
    #  2) 若有 package，则在其后
    #  3) 若已有 import，则在最后一个 import 后
    insert_pos = 0
    m_pkg = re.search(r"^\s*package\s+[^;]+;\s*", src, flags=re.M)
    if m_pkg:
        insert_pos = m_pkg.end()
    for m in re.finditer(r"^\s*import\s+[^;]+;\s*", src, flags=re.M):
        insert_pos = m.end()

    insertion = "".join(f"import {imp};\n" for imp in to_add)
    return src[:insert_pos] + insertion + src[insert_pos:]


def remove_imports(src: str, imports: List[str]) -> str:
    """整行删除 import；忽略不存在的条目。"""
    for imp in imports or []:
        # 删除以该 import 开头的一整行（可能有前导空白）
        src = re.sub(rf"^\s*import\s+{re.escape(imp)};\s*\r?\n", "", src, flags=re.M)
    return src


# -------------------- 缩进工具（补丁版） --------------------

def _line_start(src: str, idx: int) -> int:
    nl = src.rfind("\n", 0, idx)
    return 0 if nl < 0 else nl + 1


def _detect_indent_at(src: str, idx: int) -> str:
    i = _line_start(src, idx)
    j = i
    while j < len(src) and src[j] in (" ", "\t"):
        j += 1
    return src[i:j]


def _dedent_template(s: str) -> str:
    """去掉模板自身公共缩进与首尾多余换行，避免“双倍缩进”"""
    s = textwrap.dedent(s)
    if s and (s[0] == " " or s[0] == "\t"):
        s = s.lstrip()
    return s.strip("\n")


def _indent_block(text: str, indent: str) -> str:
    """对非空白行加上 indent 前缀，空白行保持原样。"""
    if not text:
        return text
    lines = text.splitlines(True)
    out = []
    for ln in lines:
        out.append(ln if ln.strip() == "" else indent + ln)
    return "".join(out)


# 默认额外缩进：都为 0（即不再默认 +4）
ENGINE_DEFAULT_REPLACE_ADD = 0
ENGINE_DEFAULT_INSERT_ADD = 0


def _compute_indent_for_replace(src: str, m: re.Match, step: Dict[str, Any], env: Dict[str, str]) -> str:
    if "indent_exact" in step:
        return step["indent_exact"]
    base = _detect_indent_at(src, m.start())
    add = int(step.get("indent_add", ENGINE_DEFAULT_REPLACE_ADD))
    return base + (" " * add)


def _compute_indent_for_insert(src: str, m: re.Match, step: Dict[str, Any], env: Dict[str, str]) -> str:
    like = step.get("indent_like", "match_line")  # "match_line" | "next_line"
    if like == "next_line":
        pos = m.end()
        nl = src.find("\n", pos)
        ref_idx = (nl + 1) if nl >= 0 else pos
    else:
        ref_idx = m.start()
    base = _detect_indent_at(src, ref_idx)
    add = int(step.get("indent_add", ENGINE_DEFAULT_INSERT_ADD))  # 默认 0
    if "indent_exact" in step:
        return step["indent_exact"]
    return base + (" " * add)


# -------------------- step 执行 --------------------

def apply_regex_replace(src: str, step: Dict[str, Any], env: Dict[str, str]) -> str:
    """
    正则替换：
    - 先对 step["repl"] 做 dedent，再渲染 ${...}，最后按匹配位置缩进整段
    - 支持 step["flags"]: 'S'=DOTALL, 'M'=MULTILINE, 'I'=IGNORECASE
    - 支持 step["count"]: 0=全部，1=仅第一次
    """
    pattern = re.compile(step["pattern"], _flags(step.get("flags")))
    count = int(step.get("count", 0))

    def repl_fn(m: re.Match) -> str:
        ctx = {**env, **m.groupdict()}
        tmpl = _dedent_template(step["repl"])    # 先去模板缩进
        raw = _render_template(tmpl, ctx)        # 再渲染变量（含 ${indent}）
        indent = _compute_indent_for_replace(src, m, step, env)
        return _indent_block(raw, indent)

    return re.sub(pattern, repl_fn, src, count=count)


def apply_insert_after(src: str, step: Dict[str, Any], env: Dict[str, str]) -> str:
    """
    在首次匹配处之后插入一段代码；可设置 guard 防重复。
    """
    pattern = re.compile(step["pattern"], _flags(step.get("flags")))
    m = pattern.search(src)
    if not m:
        return src

    guard = step.get("guard")
    if guard and guard in src:
        return src

    tmpl = _dedent_template(step["snippet"])  # 先去模板缩进
    raw = _render_template(tmpl, env)         # 再渲染变量
    indent = _compute_indent_for_insert(src, m, step, env)
    snippet = _indent_block(raw, indent)

    insert_pos = m.end()
    needs_prefix_nl = (insert_pos > 0 and src[insert_pos - 1] != "\n")
    prefix = "\n" if needs_prefix_nl else ""
    suffix = "\n" if (insert_pos < len(src) and src[insert_pos] != "\n") else ""
    return src[:insert_pos] + prefix + snippet + suffix + src[insert_pos:]


def apply_append_once(src: str, step: Dict[str, Any], env: Dict[str, str]) -> str:
    """在文件末尾追加一次文本；如提供 guard 且已存在，则跳过。"""
    guard = step.get("guard")
    if guard and guard in src:
        return src
    text = _render_template(step["text"], env)
    if not src.endswith("\n"):
        src += "\n"
    return src + text + ("\n" if not text.endswith("\n") else "")


def run_recipe(src: str, recipe: Dict[str, Any], env: Dict[str, str]) -> str:
    """
    按 steps 顺序执行；返回修改后的源码文本。
    支持的 step.type: regex_replace / imports_add / imports_remove / insert_after / append_once
    """
    out = src
    for step in recipe.get("steps", []):
        t = step["type"]
        if t == "regex_replace":
            out = apply_regex_replace(out, step, env)
        elif t == "imports_add":
            out = add_imports(out, step.get("imports", []))
        elif t == "imports_remove":
            out = remove_imports(out, step.get("imports", []))
        elif t == "insert_after":
            out = apply_insert_after(out, step, env)
        elif t == "append_once":
            out = apply_append_once(out, step, env)
        else:
            raise ValueError(f"Unknown step type: {t}")
    return out


# -------------------- diff / 入口 --------------------

def udiff(a: str, b: str, path: str, n: int = 3) -> str:
    """生成统一 diff 文本；n 为上下文行数（0=只显示改动行）。"""
    return "".join(
        difflib.unified_diff(
            a.splitlines(True),
            b.splitlines(True),
            fromfile=f"{path} (before)",
            tofile=f"{path} (after)",
            n=n,
        )
    )


def run_migration(
    root: str | Path,
    src_key: str,
    dst_key: str,
    *,
    config_path: Optional[str | Path] = None,
    config_dict: Optional[Dict[str, Any]] = None,
    dry_run: bool = False,
    env: Optional[Dict[str, str]] = None,
    out_root: Optional[str | Path] = None,
    mirror_unmodified: bool = True,
    copy_nonjava: bool = False,
    diff_context: int = 3,
) -> int:
    """
    从 Python 代码中调用的便捷入口。
    返回“内容发生变化”的 Java 文件数量（dry_run 时为“将会变化”的数量）。

    参数：
      - root:       要遍历的 Java 根目录（递归处理 *.java）
      - src_key:    源库 key（如 'dom4j','jaxp_dom','jdom2','sax','stax','digester'）
      - dst_key:    目标库 key
      - config_path / config_dict: 二选一提供迁移规则
      - dry_run:    True=只打印 diff，不写回
      - env:        模板变量（默认提供 root_tag='beans', list_tag='bean'）
      - out_root:   若提供，则把输出写到该目录下的镜像路径（不改动原文件）
      - mirror_unmodified: out_root 模式下，未变化的 .java 也镜像写出
      - copy_nonjava: out_root 模式下，复制非 .java 文件
      - diff_context: 打印 diff 的上下文行数（默认 3；0=仅改动行）
    """
    root_p = Path(root)
    if not root_p.exists():
        raise FileNotFoundError(f"Root not found: {root_p}")

    if config_dict is not None:
        cfg = config_dict
    else:
        cfg = (
            json.loads(Path(config_path).read_text(encoding="utf-8"))
            if config_path
            else DEFAULT_CONFIG
        )

    mig_key = f"{src_key}->{dst_key}"
    recipe = cfg.get("migrations", {}).get(mig_key)
    if not recipe:
        raise ValueError(f"No recipe for {mig_key}. Please provide a config containing it.")

    merged_env: Dict[str, str] = {"root_tag": "beans", "list_tag": "bean"}
    if env:
        merged_env.update(env)

    out_root_p: Optional[Path] = Path(out_root).resolve() if out_root else None
    if out_root_p and dry_run:
        print(f"[INFO] dry-run: 不会写入磁盘；目标镜像目录将会是：{out_root_p}")

    java_files = list(root_p.rglob("*.java"))
    changed = 0

    for jf in java_files:
        src_txt = jf.read_text(encoding="utf-8", errors="ignore")
        out_txt = run_recipe(src_txt, recipe, merged_env)
        is_changed = (out_txt != src_txt)
        if is_changed:
            changed += 1

        if out_root_p:
            # 计算镜像写出路径
            rel = jf.relative_to(root_p)
            out_path = out_root_p / rel
            out_path.parent.mkdir(parents=True, exist_ok=True)
            if not dry_run:
                if is_changed or mirror_unmodified:
                    out_path.write_text(out_txt if is_changed else src_txt, encoding="utf-8")
            else:
                if is_changed:
                    print(udiff(src_txt, out_txt, f"{jf} -> {out_path}", n=diff_context))
        else:
            # 就地写回（兼容旧行为）
            if is_changed:
                if dry_run:
                    print(udiff(src_txt, out_txt, str(jf), n=diff_context))
                else:
                    jf.write_text(out_txt, encoding="utf-8")
                    print(f"[OK] {jf}")

    # 可选：复制非 .java（仅 out_root 模式且非 dry-run）
    if out_root_p and copy_nonjava and not dry_run:
        for p in root_p.rglob("*"):
            if p.is_file() and p.suffix.lower() != ".java":
                rel = p.relative_to(root_p)
                dst = out_root_p / rel
                dst.parent.mkdir(parents=True, exist_ok=True)
                try:
                    shutil.copy2(p, dst)
                except Exception:
                    shutil.copy(p, dst)

    return changed


# -------------------- CLI --------------------

def main():
    ap = argparse.ArgumentParser(
        description="Generic XML parser migrator (A -> B). "
                    "Use with a JSON config (e.g., migrations_compilable.json)."
    )
    ap.add_argument("--root", required=True, help="Root directory to scan for .java files (recursive).")
    ap.add_argument("--from", dest="src_key", required=True,
                    help="Source library key (e.g., dom4j, jaxp_dom, jdom2, sax, stax, digester).")
    ap.add_argument("--to", dest="dst_key", required=True, help="Target library key.")
    ap.add_argument("--config", help="Path to JSON config with migrations (recommended).")
    ap.add_argument("--dry-run", action="store_true", help="Print unified diff without writing files.")
    ap.add_argument("--diff-context", type=int, default=3,
                    help="Unified diff context lines (0 = only changed lines).")
    ap.add_argument("--root-tag", default="beans", help="Template variable: the root element tag name.")
    ap.add_argument("--list-tag", default="bean", help="Template variable: the repeated child element tag name.")
    # 附加自定义模板变量：--env key=value（可多次）
    ap.add_argument("--env", action="append", default=[],
                    help="Extra template variables, format key=value (can be specified multiple times).")

    # 非就地输出（镜像写出）
    ap.add_argument("--out-root", help="Write outputs under this directory, mirroring structure from --root.")
    ap.add_argument("--only-changed", action="store_true",
                    help="When --out-root is set, write only changed .java files (default: mirror all .java).")
    ap.add_argument("--copy-nonjava", action="store_true",
                    help="When --out-root is set, also copy non-.java files as-is.")

    args = ap.parse_args()

    # 组合 env
    env: Dict[str, str] = {"root_tag": args.root_tag, "list_tag": args.list_tag}
    for kv in args.env:
        if "=" in kv:
            k, v = kv.split("=", 1)
            env[k.strip()] = v.strip()

    # 加载配置
    if args.config:
        try:
            cfg = json.loads(Path(args.config).read_text(encoding="utf-8"))
        except Exception as e:
            print(f"[ERR] Failed to load config: {args.config} ({e})", file=sys.stderr)
            sys.exit(2)
    else:
        cfg = DEFAULT_CONFIG

    mig_key = f"{args.src_key}->{args.dst_key}"
    if mig_key not in cfg.get("migrations", {}):
        print(f"[ERR] No recipe for {mig_key}. Provide it via --config.", file=sys.stderr)
        sys.exit(2)

    root_p = Path(args.root)
    if not root_p.exists():
        print(f"[ERR] Root not found: {root_p}", file=sys.stderr)
        sys.exit(2)

    # 执行
    try:
        changed = run_migration(
            root=root_p,
            src_key=args.src_key,
            dst_key=args.dst_key,
            config_dict=cfg,
            dry_run=args.dry_run,
            env=env,
            out_root=args.out_root,
            mirror_unmodified=not args.only_changed,
            copy_nonjava=args.copy_nonjava,
            diff_context=args.diff_context,
        )
    except Exception as e:
        print(f"[ERR] {e}", file=sys.stderr)
        sys.exit(2)

    if args.dry_run:
        print(f"[DONE] (dry-run) {changed} file(s) would be modified.")
    else:
        print(f"[DONE] {changed} file(s) modified.")


if __name__ == "__main__":
    main()
