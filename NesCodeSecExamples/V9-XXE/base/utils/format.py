import os
from pathlib import Path

def format():
    path = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))  # current directory
    for root, dirs, files in os.walk(path):
        if root.endswith("base") or root.endswith("utils"):
            continue
        print(f"📂 Directory: {root}")
        for file in files:
            if file.endswith('.py'):
                continue
            p = Path(os.path.join(root, file))
            code = p.read_text(encoding='utf-8')
            # code = code.replace("<fim_middle>", "<|editable_region_end|>\n```").replace("<fim_suffix>", "<|user_cursor_is_here|>")
            # print(code.splitlines())
            # code = "\n".join(code.splitlines()[1:])
            code = f"{p.name}\n```<|start_of_file|>\n<|editable_region_start|>\n"+code+"\n<|editable_region_end|>\n```"
            p.write_text(code, encoding='utf-8')
            print(f" - Formatted file: {p}")
format()