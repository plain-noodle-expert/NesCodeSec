import difflib
import os
import re
from loguru import logger
from pathlib import Path
from typing import Match
from abc import ABC, abstractmethod
from prompt import PROMPT

class Preprocessor(ABC):
    def __init__(self):
        pass

    @abstractmethod
    def process(self, **kwargs) -> str:
        """
        Return the processed event and input excerpt.
        """
        pass
    
    def strip_marker(self, code: str) -> str:
        code = "\n".join(code.split("\n")[1:])
        return code.replace("<|user_cursor_is_here|>", "")\
                .replace("<|editable_region_start|>", "")\
                .replace("<|editable_region_end|>", "")\
                .replace("<|start_of_file|>", "")
                
# TODO: Refactor to have a common base class for preprocessors
class XXEPreprocessor(Preprocessor):
    def __init__(self):
        super().__init__()

    def replace_xml_factory(self, content: str, xml_factory: str, comment: str) -> str:
        """
        Update a Java file to use a different XML parser factory and associated comments.

        Supported parser factories (simple or fully-qualified):
        - javax.xml.stream.XMLInputFactory         (StAX)
        - javax.xml.parsers.SAXParserFactory       (SAX)
        - javax.xml.parsers.DocumentBuilderFactory  (DOM)
        - org.xml.sax.helpers.XMLReaderFactory     (SAX; creates XMLReader)
        - org.jdom2.input.SAXBuilder               (JDOM2)
        - org.dom4j.io.SAXReader                   (dom4j)
        - org.apache.commons.digester3.Digester    (Apache Commons Digester)

        Returns:
            Updated file content (string only).
        """
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

        def _make_decl(leading_ws: str, var: str) -> str:
            """Build the replacement declaration, add <|user_cursor_is_here|> for .format, and keep an anchor we will strip later."""
            comment_line = f"\n\n{leading_ws}// {comment.strip()}\n"
            if target_cls == "XMLReaderFactory":
                decl_line = f"{leading_ws}XMLReader {var} = XMLReaderFactory.createXMLReader();"
            elif target_cls in ("SAXBuilder", "SAXReader", "Digester"):
                decl_line = f"{leading_ws}{target_cls} {var} = new {target_cls}();"
            else:
                decl_line = f"{leading_ws}{target_cls} {var} = {target_cls}.newInstance();"
        
            return comment_line + decl_line + " <|user_cursor_is_here|> // " + ANCHOR

        def _decl_replacer(m: Match) -> str:
            leading_ws = m.group('leading_ws') or ''
            var = m.group('var')
            return _make_decl(leading_ws, var)

        content_new, subs = None, 0

        # Try all patterns regardless of target type to allow cross-type replacement
        patterns_to_try = [
            ("standard", pattern_with_comments),
            ("xmlreader", pattern_xmlreader),
            ("saxbuilder", pattern_saxbuilder),
            ("saxreader", pattern_saxreader),
            ("digester", pattern_digester),
        ]

        for _, pattern in patterns_to_try:
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

            def _simple_replacer(m: Match) -> str:
                leading_ws = m.group('leading_ws') or ''
                var = m.group('var')
                return _make_decl(leading_ws, var)

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
                    subs = subs_fallback
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

        # --- 5) Locate the anchor to remove it, but KEEP the trailing " {}" we injected ---
        lines = content.splitlines()
        for i, line in enumerate(lines):
            if ANCHOR in line:
                # line structure: "...; {} // __EDIT_ANCHOR__"
                lines[i] = line.replace(f" // {ANCHOR}", "")
                break

        content = "\n".join(lines)
        if not content.endswith("\n"):
            content += "\n"

        return content

    def process(self, **kwargs) -> str:
        """
        Args:
            p: Path object pointing to the Java file to be processed.
            xml_factory: Target XML parser factory class name (simple or fully-qualified).
            comment: Comment to insert above the parser instantiation line.
        """
        try:
            p: Path = kwargs["p"]
            template_code: str = p.read_text(encoding="utf-8")
        except KeyError as e:
            logger.error("Missing required argument 'p' (Path object): ", e)
            raise
        updated = self.replace_xml_factory(template_code, kwargs["xml_factory"], kwargs["comment"])
        # Diff base code and updated code to create the event
        event = f"""\nUser edited "{p.name}":\n\n"""
        event += "\n".join(difflib.Differ().compare(template_code.splitlines(), self.strip_marker(updated).splitlines()))
        logger.debug(event)
        # Mark up the code
        input_code = f"{p.name}\n\n" + updated
        logger.debug(input_code)
        
        # Save for reference
        base = "NesCodeSecExamples/src/main/java/com/XXE"
        with open(os.path.join(base, f"input_excerpt/{p.stem}To{kwargs['xml_factory']}{p.suffix}"), 'w', encoding="utf-8") as f:
            f.write(input_code)
        with open(os.path.join(base, f"input_event/{p.stem}To{kwargs['xml_factory']}{p.suffix}"), 'w', encoding="utf-8") as f:
            f.write(event)
            
        return PROMPT.format(event, input_code)

class DirInsertDelPreprocessor(Preprocessor):
    def __init__(self, ):
        super().__init__()
        
    def process(self, **kwargs) -> str:
        """
        Args:
            edit_code(str): The edited code with markers.
            template_code(str): The original code before editing.
        """
        template_code = kwargs["template_code"]
        edit_code = kwargs["edit_code"]
        event = f"""\nUser edited "{kwargs['file']}":\n\n"""
        event += "\n".join(difflib.Differ().compare(self.strip_marker(template_code).splitlines(), self.strip_marker(edit_code).splitlines()))
        logger.debug(event)
        logger.debug(edit_code)
        return PROMPT.format(event, edit_code)
    
    
if __name__ == "__main__":
    base_dir = "NesCodeSecExamples/src/main/java/com/XXE"
    base_files = ["Digester.java", "InputFactory.java", "DocumentBuilder.java", "SAXParserFactory.java", "SAXBuilder.java", "SAXReader.java"]
    xml_factories = ["Digester", "XMLInputFactory", "DocumentBuilderFactory", "SAXParserFactory", "SAXBuilder", "SAXReader"]
    xxe_preprocessor = XXEPreprocessor()
    for i, base_file in enumerate(base_files):
        p = Path(os.path.join(base_dir, base_file))
        for j, xml in enumerate(xml_factories):
            if i==j:
                continue
            event, input_code = xxe_preprocessor.process(p=p, xml_factory=xml, comment=f"Using {xml} parser factory now.")
            logger.info(f"Processed {base_file} to use {xml}, event and input excerpt generated.")
