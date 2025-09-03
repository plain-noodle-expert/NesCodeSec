import difflib
import os
import re
from loguru import logger
from pathlib import Path

def replace_xml_factory(content: str, xml_factory: str, comment: str) -> tuple[str, int]:
    """
    Update a Java file to use a different XML parser factory and associated comments.

    Supported parser factories (simple or fully-qualified):
      - javax.xml.stream.XMLInputFactory        (StAX)
      - javax.xml.parsers.SAXParserFactory      (SAX)
      - javax.xml.parsers.DocumentBuilderFactory (DOM)
      - org.xml.sax.helpers.XMLReaderFactory    (SAX; creates XMLReader)
      - org.jdom2.input.SAXBuilder              (JDOM2)
      - org.dom4j.io.SAXReader                  (dom4j)
      - org.apache.commons.digester3.Digester  (Apache Commons Digester)

    Returns:
        (updated_file_content, last_edited_line_index)
        last_edited_line_index is 0-based in the UPDATED content, or -1 if no decl was edited.
    """

    last_edited_line = -1
    ANCHOR = "__EDIT_ANCHOR__"  # temporary inline marker to locate the edited decl line

    # --- Parsers only ---
    known_imports = {
        "XMLInputFactory": "javax.xml.stream.XMLInputFactory",
        "SAXParserFactory": "javax.xml.parsers.SAXParserFactory",
        "DocumentBuilderFactory": "javax.xml.parsers.DocumentBuilderFactory",
        "XMLReaderFactory": "org.xml.sax.helpers.XMLReaderFactory",
        "SAXBuilder": "org.jdom2.input.SAXBuilder",
        "SAXReader": "org.dom4j.io.SAXReader",
        "Digester": "org.apache.commons.digester3.Digester",
    }
    known_class_names = list(known_imports.keys())

    # Normalize target class name + import
    if "." in xml_factory:
        target_fqn = xml_factory.strip()
        target_cls = target_fqn.split(".")[-1]
    else:
        target_cls = xml_factory.strip()
        target_fqn = known_imports.get(target_cls)
        if not target_fqn:
            raise ValueError(
                f"Unknown parser factory '{xml_factory}'. "
                f"Use one of {known_class_names} or a fully qualified class name."
            )

    # --- 1) Remove old parser factory imports and insert required imports for the new target ---
    # Build a dynamic alternation from known FQNs
    fqn_alts = "|".join(re.escape(fqn) for fqn in known_imports.values()) + "|" + re.escape("org.xml.sax.XMLReader")
    import_line_pattern = re.compile(
        rf'^\s*import\s+(?:{fqn_alts})\s*;\s*$',
        re.MULTILINE
    )
    content = import_line_pattern.sub('', content).rstrip() + "\n"

    # Required imports for certain targets
    required_imports = [target_fqn]
    if target_cls == "XMLReaderFactory":
        # LHS type is XMLReader
        required_imports.append("org.xml.sax.XMLReader")

    package_match = re.search(r'^\s*package\s+[\w.]+\s*;\s*$', content, re.MULTILINE)

    def _ensure_import(fqn: str):
        nonlocal content
        line = f"import {fqn};\n"
        if line not in content:
            if package_match is not None:
                insert_pos = package_match.end()
                content = content[:insert_pos] + "\n" + line + content[insert_pos:]
            else:
                content = line + content

    for fqn in required_imports:
        _ensure_import(fqn)

    # --- 2) Replace/create the comment above the parser instantiation and fix the decl ---
    # Standard newInstance() factories (XMLInputFactory / SAXParserFactory / DocumentBuilderFactory)
    pattern_with_comments = re.compile(
        r'(?P<comments>(?:\s*//.*\n)+)?'          # optional contiguous //-comment block above
        r'(?P<leading_ws>\s*)'
        r'(?P<type>[A-Za-z_][\w.]*)\s+'
        r'(?P<var>[A-Za-z_]\w*)\s*=\s*'
        r'[A-Za-z_][\w.]*\.newInstance\(\)\s*;'
    )
    # XMLReaderFactory.createXMLReader()
    pattern_xmlreader = re.compile(
        r'(?P<comments>(?:\s*//.*\n)+)?'
        r'(?P<leading_ws>\s*)'
        r'(?P<type>[A-Za-z_][\w.]*)\s+'
        r'(?P<var>[A-Za-z_]\w*)\s*=\s*'
        r'XMLReaderFactory\.createXMLReader\(\)\s*;'
    )
    # JDOM2: new SAXBuilder()
    pattern_saxbuilder = re.compile(
        r'(?P<comments>(?:\s*//.*\n)+)?'
        r'(?P<leading_ws>\s*)'
        r'(?P<type>[A-Za-z_][\w.]*)\s+'
        r'(?P<var>[A-Za-z_]\w*)\s*=\s*'
        r'new\s+SAXBuilder\(\)\s*;'
    )
    # dom4j: new SAXReader()
    pattern_saxreader = re.compile(
        r'(?P<comments>(?:\s*//.*\n)+)?'
        r'(?P<leading_ws>\s*)'
        r'(?P<type>[A-Za-z_][\w.]*)\s+'
        r'(?P<var>[A-Za-z_]\w*)\s*=\s*'
        r'new\s+SAXReader\(\)\s*;'
    )
    # Apache Commons Digester: new Digester()
    pattern_digester = re.compile(
        r'(?P<comments>(?:\s*//.*\n)+)?'
        r'(?P<leading_ws>\s*)'
        r'(?P<type>[A-Za-z_][\w.]*)\s+'
        r'(?P<var>[A-Za-z_]\w*)\s*=\s*'
        r'new\s+Digester\(\)\s*;'
    )

    def _decl_replacer(m: re.Match) -> str:
        leading_ws = m.group('leading_ws') or ''
        var = m.group('var')

        # keep line math stable (only one blank line before comment)
        new_comment = f"\n\n{leading_ws}// {comment.strip()}\n"

        if target_cls == "XMLReaderFactory":
            decl_line = f"{leading_ws}XMLReader {var} = XMLReaderFactory.createXMLReader();"
        elif target_cls in ("SAXBuilder", "SAXReader", "Digester"):
            decl_line = f"{leading_ws}{target_cls} {var} = new {target_cls}();"
        else:
            decl_line = f"{leading_ws}{target_cls} {var} = {target_cls}.newInstance();"

        return new_comment + decl_line + f" // {ANCHOR}"

    content_new, subs = None, 0
    
    # Try all patterns regardless of target type to allow cross-type replacement
    patterns_to_try = [
        ("standard", pattern_with_comments),
        ("xmlreader", pattern_xmlreader),
        ("saxbuilder", pattern_saxbuilder),
        ("saxreader", pattern_saxreader),
        ("digester", pattern_digester),
    ]
    
    for pattern_name, pattern in patterns_to_try:
        content_new, subs = pattern.subn(_decl_replacer, content, count=1)
        if subs > 0:
            break

    if subs == 0:
        # Fallback patterns (no pre-comment handling matched)
        pattern_decl_standard = re.compile(
            r'(?P<leading_ws>\s*)'
            r'(?P<type>[A-Za-z_][\w.]*)\s+(?P<var>[A-Za-z_]\w*)\s*=\s*'
            r'(?P<rhs_cls>[A-Za-z_][\w.]*)\.newInstance\(\)\s*;'
        )
        pattern_decl_xmlreader = re.compile(
            r'(?P<leading_ws>\s*)'
            r'(?P<type>[A-Za-z_][\w.]*)\s+(?P<var>[A-Za-z_]\w*)\s*=\s*'
            r'XMLReaderFactory\.createXMLReader\(\)\s*;'
        )
        pattern_decl_saxbuilder = re.compile(
            r'(?P<leading_ws>\s*)'
            r'(?P<type>[A-Za-z_][\w.]*)\s+(?P<var>[A-Za-z_]\w*)\s*=\s*'
            r'new\s+SAXBuilder\(\)\s*;'
        )
        pattern_decl_saxreader = re.compile(
            r'(?P<leading_ws>\s*)'
            r'(?P<type>[A-Za-z_][\w.]*)\s+(?P<var>[A-Za-z_]\w*)\s*=\s*'
            r'new\s+SAXReader\(\)\s*;'
        )
        pattern_decl_digester = re.compile(
            r'(?P<leading_ws>\s*)'
            r'(?P<type>[A-Za-z_][\w.]*)\s+(?P<var>[A-Za-z_]\w*)\s*=\s*'
            r'new\s+Digester\(\)\s*;'
        )

        def _simple_replacer(m: re.Match) -> str:
            leading_ws = m.group('leading_ws') or ''
            var = m.group('var')
            comment_line = f"\n\n{leading_ws}// {comment.strip()}\n"
            if target_cls == "XMLReaderFactory":
                decl_line = f"{leading_ws}XMLReader {var} = XMLReaderFactory.createXMLReader();"
            elif target_cls in ("SAXBuilder", "SAXReader", "Digester"):
                decl_line = f"{leading_ws}{target_cls} {var} = new {target_cls}();"
            else:
                decl_line = f"{leading_ws}{target_cls} {var} = {target_cls}.newInstance();"
            return comment_line + decl_line + f" // {ANCHOR}"

        # Try all fallback patterns regardless of target type
        fallback_patterns = [
            pattern_decl_standard,
            pattern_decl_xmlreader, 
            pattern_decl_saxbuilder,
            pattern_decl_saxreader,
            pattern_decl_digester,
        ]
        
        for fallback_pattern in fallback_patterns:
            content_new, subs_fallback = fallback_pattern.subn(_simple_replacer, content, count=1)
            if subs_fallback > 0:
                break

    # Ensure content_new is not None
    if content_new is None:
        content_new = content
    
    content = content_new

    # --- 3) Refresh typical parser names inside STRING LITERALS (logs / println) ---
    for old in known_class_names:
        if old != target_cls:
            content = re.sub(
                rf'(".*?){old}(.*?")',
                lambda m: m.group(1) + target_cls + m.group(2),
                content,
                flags=re.DOTALL
            )

    # --- 4) Tidy blank lines ---
    content = re.sub(r'\n{3,}', '\n\n', content)

    # --- 5) Locate the anchor to compute the edited declaration line index, then remove it ---
    lines = content.splitlines()
    for i, line in enumerate(lines):
        if ANCHOR in line:
            last_edited_line = i  # 0-based index in the FINAL content
            lines[i] = line.replace(f" // {ANCHOR}", "")
            break
    content = "\n".join(lines) + ("\n" if content.endswith("\n") else "")

    # with open(f"NesCodeSecExamples/src/main/java/com/XXE/input_code/{file}To{xml_factory}", 'w', encoding="utf-8") as f:
    #     f.write(content)

    return content, last_edited_line

def insert_marker(code: str, edited_line: int) -> str:
    try:    
        # Split the code into lines
        lines = code.splitlines()
        
        # Add cursor marker at the end of the edited line
        if 0 <= edited_line < len(lines):
            lines[edited_line] = lines[edited_line] + "<|user_cursor_is_here|>"
        
        # Rejoin the lines with the cursor marker
        modified_code = '\n'.join(lines)
        
        return f"```<|start_of_file|>\n<|editable_region_start|>\n{modified_code}\n<|editable_region_end|>\n```"
    except Exception as e:
        print(e)
        raise

def prepare_XXE_input(base_file: str, xml_factory: str, comment: str) -> tuple[str, str]:
    p = Path(base_file)
    base_code = p.read_text(encoding="utf-8")
    updated, last_edited_line = replace_xml_factory(base_code, xml_factory, comment)
    event = f"""\nUser edited "{p.name}":\n\n"""
    event += "\n".join(difflib.Differ().compare(base_code.splitlines(), updated.splitlines()))
    logger.debug(event)
    input_code = f"{p.name}\n\n" + insert_marker(updated, last_edited_line)
    logger.debug(input_code)

    base = "NesCodeSecExamples/src/main/java/com/XXE"
    with open(os.path.join(base, f"input_code/{p.stem}To{xml_factory}{p.suffix}"), 'w', encoding="utf-8") as f:
        f.write(input_code)
    with open(os.path.join(base, f"input_event/{p.stem}To{xml_factory}{p.suffix}"), 'w', encoding="utf-8") as f:
        f.write(event)
        
    return event, input_code
    
    
    
if __name__ == "__main__":
    base_dir = "NesCodeSecExamples/src/main/java/com/XXE"
    prepare_XXE_input(os.path.join(base_dir, "base/DocumentBuilder.java"), "SAXBuilder", "使用安全配置的 SAXBuilder 实例（禁用外部实体等）")
