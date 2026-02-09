# xml_any2any_migrator.py
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
xml_any2any_migrator.py
---------------------------------
Universal "XML Parser Library A -> B" code migration engine.

Features
- Use JSON config (migrations_compilable.json) to describe migration steps
- Supported step types:
  * regex_replace  : Regex replacement (cross-line, multiline, named groups, ${...} templates)
  * imports_add    : Batch insert imports (deduplicated, placed after package/last import)
  * imports_remove : Batch remove imports (delete entire lines)
  * insert_after   : Insert code after first match (with guard to prevent duplication)
  * append_once    : Append text once at end of file (with guard to prevent duplication)
- Indentation and templates:
  * First dedent the template's common indentation, then render ${...}, avoiding "double indentation"
  * Leading indentation can be precisely reused via (?P<indent>\\s*) + ${indent} in JSON
  * indent_exact / indent_add (optional) control additional indentation for each step
- Non-in-place output:
  * --out-root specifies output root directory, maintaining consistent relative structure with --root (original files unchanged)
  * --only-changed outputs only changed .java files
  * --copy-nonjava syncs non-.java files
- Dry-run preview:
  * --dry-run only prints diff; --diff-context adjusts context lines

Command-line example
  python xml_any2any_migrator.py \
    --root path/to/java/src \
    --from jaxp_dom --to stax \
    --config migrations_compilable.json \
    --root-tag beans --list-tag bean \
    --out-root input_event \
    --dry-run

Programming example
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


DEFAULT_CONFIG: Dict[str, Any] = {"migrations": {}}


def _flags(flag_str: str | None) -> int:
    """Map 'S', 'M', 'I' combination to re flags."""
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
    """Replace ${key} placeholders with ctx[key] (simple placeholder substitution, no conditionals/loops)."""
    out = text
    for k, v in ctx.items():
        out = out.replace("${" + k + "}", v)
    return out


# -------------------- import operations --------------------

def add_imports(src: str, imports: List[str]) -> str:
    """Insert import declarations after package or last import; skip duplicates."""
    if not imports:
        return src
    to_add = [imp for imp in imports if imp and f"import {imp};" not in src]
    if not to_add:
        return src

    # Insertion position:
    #  1) Default: 0
    #  2) If has package, after it
    #  3) If has imports, after last import
    insert_pos = 0
    m_pkg = re.search(r"^\s*package\s+[^;]+;\s*", src, flags=re.M)
    if m_pkg:
        insert_pos = m_pkg.end()
    for m in re.finditer(r"^\s*import\s+[^;]+;\s*", src, flags=re.M):
        insert_pos = m.end()

    insertion = "".join(f"import {imp};\n" for imp in to_add)
    return src[:insert_pos] + insertion + src[insert_pos:]


def remove_imports(src: str, imports: List[str]) -> str:
    """Delete entire import lines; ignore non-existent entries."""
    for imp in imports or []:
        src = re.sub(rf"^\s*import\s+{re.escape(imp)};\s*\r?\n", "", src, flags=re.M)
    return src


# -------------------- indentation utilities --------------------

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
    """Remove template's common indentation and leading/trailing newlines to avoid 'double indentation'."""
    s = textwrap.dedent(s)
    if s and (s[0] == " " or s[0] == "\t"):
        s = s.lstrip()
    return s.strip("\n")


def _indent_block(text: str, indent: str) -> str:
    """Add indent prefix to non-empty lines, keep empty lines as-is."""
    if not text:
        return text
    lines = text.splitlines(True)
    out = []
    for ln in lines:
        out.append(ln if ln.strip() == "" else indent + ln)
    return "".join(out)


# Default additional indentation: all 0 (no longer default +4)
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
    add = int(step.get("indent_add", ENGINE_DEFAULT_INSERT_ADD))
    if "indent_exact" in step:
        return step["indent_exact"]
    return base + (" " * add)


# -------------------- step execution --------------------

def apply_regex_replace(src: str, step: Dict[str, Any], env: Dict[str, str]) -> str:
    """
    Regex replacement:
    - First dedent step["repl"], then render ${...}, finally indent the entire block by match position
    - Supports step["flags"]: 'S'=DOTALL, 'M'=MULTILINE, 'I'=IGNORECASE
    - Supports step["count"]: 0=all, 1=first only
    """
    pattern = re.compile(step["pattern"], _flags(step.get("flags")))
    count = int(step.get("count", 0))

    def repl_fn(m: re.Match) -> str:
        ctx = {**env, **m.groupdict()}
        tmpl = _dedent_template(step["repl"])
        raw = _render_template(tmpl, ctx)
        indent = _compute_indent_for_replace(src, m, step, env)
        return _indent_block(raw, indent)

    return re.sub(pattern, repl_fn, src, count=count)


def apply_insert_after(src: str, step: Dict[str, Any], env: Dict[str, str]) -> str:
    """
    Insert code after first match; can set guard to prevent duplication.
    Also: if snippet contains <|user_cursor_is_here|> and the file already has this placeholder,
    skip insertion by default to avoid duplication.
    """
    pattern = re.compile(step["pattern"], _flags(step.get("flags")))
    m = pattern.search(src)
    if not m:
        return src

    # Render insertion snippet
    tmpl = _dedent_template(step["snippet"])
    raw = _render_template(tmpl, env)
    
    # Explicit guard: if provided and already exists, skip
    guard = step.get("guard")
    if guard and guard in src:
        return src
    
    # Default anti-duplication: if insertion snippet contains user cursor placeholder and source already has it, skip
    CURSOR_TAG = "<|user_cursor_is_here|>"
    if CURSOR_TAG in raw and CURSOR_TAG in src:
        return src

    indent = _compute_indent_for_insert(src, m, step, env)
    snippet = _indent_block(raw, indent)

    insert_pos = m.end()
    needs_prefix_nl = (insert_pos > 0 and src[insert_pos - 1] != "\n")
    prefix = "\n" if needs_prefix_nl else ""
    suffix = "\n" if (insert_pos < len(src) and src[insert_pos] != "\n") else ""
    return src[:insert_pos] + prefix + snippet + suffix + src[insert_pos:]


def apply_append_once(src: str, step: Dict[str, Any], env: Dict[str, str]) -> str:
    """Append text once at end of file; if guard is provided and exists, skip."""
    guard = step.get("guard")
    if guard and guard in src:
        return src
    text = _render_template(step["text"], env)
    if not src.endswith("\n"):
        src += "\n"
    return src + text + ("\n" if not text.endswith("\n") else "")


def run_recipe(src: str, recipe: Dict[str, Any], env: Dict[str, str]) -> str:
    """
    Execute steps in order; return modified source code text.
    Supported step.type: regex_replace / imports_add / imports_remove / insert_after / append_once
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
    
    # Clean preprocessing markers: remove /*__FIRST_SETFEATURE__ var=xxx*/ comments (keep surrounding spaces and newlines)
    out = re.sub(r' /\*__FIRST_SETFEATURE__[^*]*\*/', '', out)
    
    # Smart cursor positioning: adjust cursor position based on whether there are security settings
    out = _adjust_cursor_position(out)
    
    return out


def _adjust_cursor_position(src: str) -> str:
    """
    Intelligently adjust cursor position:
    - If has security settings (setFeature or security-related setProperty): move cursor to the beginning
      of the same line as the first security setting (before the code)
    - If no security settings: keep cursor at current position (next line after factory initialization)
    
    Security-related setProperty characteristics:
    - Property name contains "accessExternal", "http://", "feature", "DTD", "entity", etc.
    
    Examples:
      With security settings: <|user_cursor_is_here|>digester.setFeature(...);
      Without security settings: cursor on its own line after factory initialization
    """
    CURSOR_TAG = "<|user_cursor_is_here|>"
    
    if CURSOR_TAG not in src:
        return src
    
    # Find the first security setting
    # 1. setFeature is always a security setting
    # 2. setProperty only counts when property name contains security keywords (accessExternal, http://, feature, DTD, entity)
    
    setfeature_pattern = re.compile(
        r'(?:^|\n)(?P<full_line>(?P<indent>[ \t]*)(?P<var>\w+)\.setFeature\s*\([^\n]*)',
        re.MULTILINE
    )
    
    # Find security-related setProperty (check if first parameter contains security keywords)
    setproperty_pattern = re.compile(
        r'(?:^|\n)(?P<full_line>(?P<indent>[ \t]*)(?P<var>\w+)\.setProperty\s*\(\s*["\'](?P<prop_name>[^"\']*(?:accessExternal|http://|feature|DTD|entity|schema)[^"\']*)["\'][^\n]*)',
        re.MULTILINE | re.IGNORECASE
    )
    
    # Find first match (setFeature or security-related setProperty)
    feature_match = setfeature_pattern.search(src)
    property_match = setproperty_pattern.search(src)
    
    # Select the one that appears first
    security_match = None
    if feature_match and property_match:
        security_match = feature_match if feature_match.start() < property_match.start() else property_match
    elif feature_match:
        security_match = feature_match
    elif property_match:
        security_match = property_match
    
    if security_match:
        # Has security settings: move cursor to the beginning of the same line as the first security setting
        # 1. Remove all existing cursors (including possible newlines)
        src_no_cursor = src.replace(CURSOR_TAG, "").replace(f"\n{CURSOR_TAG}", "").replace(f"{CURSOR_TAG}\n", "")
        # Remove possible extra blank lines
        src_no_cursor = re.sub(r'\n\s*\n\s*\n', '\n\n', src_no_cursor)
        
        # 2. Re-find first security setting (since src has changed)
        feature_match_new = setfeature_pattern.search(src_no_cursor)
        property_match_new = setproperty_pattern.search(src_no_cursor)
        
        security_match_new = None
        if feature_match_new and property_match_new:
            security_match_new = feature_match_new if feature_match_new.start() < property_match_new.start() else property_match_new
        elif feature_match_new:
            security_match_new = feature_match_new
        elif property_match_new:
            security_match_new = property_match_new
        
        if security_match_new:
            # full_line is like "        reader.setFeature(...)" complete content (no leading newline)
            # We need to insert cursor at the beginning of full_line
            full_line_start = security_match_new.start('full_line')
            
            # Insert cursor at line beginning (immediately before code, no newline)
            src = src_no_cursor[:full_line_start] + CURSOR_TAG + src_no_cursor[full_line_start:]
    
    # If no security settings, keep cursor at original position (after factory initialization)
    return src


# -------------------- diff / entry point --------------------

def udiff(a: str, b: str, path: str, n: int = 3) -> str:
    """Generate unified diff text; n is number of context lines (0=only changed lines)."""
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
    Convenient entry point for calling from Python code.
    Returns the number of Java files whose "content changed" (or "will change" in dry_run mode).

    Parameters:
      - root:       Java root directory to traverse (recursively processes *.java)
      - src_key:    Source library key (e.g., 'dom4j','jaxp_dom','jdom2','sax','stax','digester')
      - dst_key:    Target library key
      - config_path / config_dict: Choose one to provide migration rules
      - dry_run:    True=only print diff, don't write back
      - env:        Template variables (default provides root_tag='beans', list_tag='bean')
      - out_root:   If provided, write output to mirrored path under this directory (don't modify original files)
      - mirror_unmodified: In out_root mode, also mirror unchanged .java files
      - copy_nonjava: In out_root mode, copy non-.java files
      - diff_context: Number of context lines to print in diff (default 3; 0=only changed lines)
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
        print(f"[INFO] dry-run: will not write to disk; target mirror directory will be: {out_root_p}")

    java_files = list(root_p.rglob("*.java"))
    changed = 0

    for jf in java_files:
        src_txt = jf.read_text(encoding="utf-8", errors="ignore")
        out_txt = run_recipe(src_txt, recipe, merged_env)
        is_changed = (out_txt != src_txt)
        if is_changed:
            changed += 1

        if out_root_p:
            # Calculate mirrored output path
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
            # Write back in-place (compatible with old behavior)
            if is_changed:
                if dry_run:
                    print(udiff(src_txt, out_txt, str(jf), n=diff_context))
                else:
                    jf.write_text(out_txt, encoding="utf-8")
                    print(f"[OK] {jf}")

    # Optional: copy non-.java files (only out_root mode and non dry-run)
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
    # Additional custom template variables: --env key=value (can be specified multiple times)
    ap.add_argument("--env", action="append", default=[],
                    help="Extra template variables, format key=value (can be specified multiple times).")

    # Non-in-place output (mirror write)
    ap.add_argument("--out-root", help="Write outputs under this directory, mirroring structure from --root.")
    ap.add_argument("--only-changed", action="store_true",
                    help="When --out-root is set, write only changed .java files (default: mirror all .java).")
    ap.add_argument("--copy-nonjava", action="store_true",
                    help="When --out-root is set, also copy non-.java files as-is.")

    args = ap.parse_args()

    # Combine env variables
    env: Dict[str, str] = {"root_tag": args.root_tag, "list_tag": args.list_tag}
    for kv in args.env:
        if "=" in kv:
            k, v = kv.split("=", 1)
            env[k.strip()] = v.strip()

    # Load configuration
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

    # Execute
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
