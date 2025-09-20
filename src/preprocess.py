# from __future__ import annotations
# import difflib
# import os
# import re
# import json
# import textwrap
# from pathlib import Path
# from typing import Dict, Any, List, Optional, Tuple
# from loguru import logger
# from abc import ABC, abstractmethod
# from batch_migrator import diff_pairs_against_base
# from prompt import PROMPT

# class Preprocessor(ABC):
#     def __init__(self):
#         pass

#     @abstractmethod
#     def process(self, **kwargs) -> str:
#         """
#         Return the processed event and input excerpt.
#         """
#         pass

#     def strip_marker(self, code: str) -> str:
#         code = "\n".join(code.split("\n")[1:])
#         return code.replace("<|user_cursor_is_here|>", "")\
#                 .replace("<|editable_region_start|>", "")\
#                 .replace("<|editable_region_end|>", "")\
#                 .replace("<|start_of_file|>", "")

# # TODO: Refactor to have a common base class for preprocessors
# class XXEPreprocessor(Preprocessor):
#     from batch_migrator import diff_pairs_against_base

#     diff_pairs_against_base(
#         base_root="/abs/path/NesCodeSecExamples/src/main/java/com/Scenario1/base",
#         generated_root="/abs/path/NesCodeSecExamples/src/main/java/com/Scenario1/input_event",  # 生成物根
#         # 默认就会把 .diff 写到同一个 input_event 下、镜像 pair 结构
#         # 如需输出到别处，改 diff_output_root：
#         # diff_output_root="/abs/path/NesCodeSecExamples/src/main/java/com/Scenario1/diff_event",
#         include_nochange=False
#     )


# if __name__ == "__main__":
#     base_dir = "NesCodeSecExamples/src/main/java/com/XXE"
#     base_files = ["Digester.java", "InputFactory.java", "DocumentBuilder.java", "SAXParserFactory.java", "SAXBuilder.java", "SAXReader.java"]
#     xml_factories = ["Digester", "XMLInputFactory", "DocumentBuilderFactory", "SAXParserFactory", "SAXBuilder", "SAXReader"]
#     xxe_preprocessor = XXEPreprocessor()
#     for i, base_file in enumerate(base_files):
#         p = Path(os.path.join(base_dir, base_file))
#         for j, xml in enumerate(xml_factories):
#             if i==j:
#                 continue
#             event, input_code = xxe_preprocessor.process(p=p, xml_factory=xml, comment=f"Using {xml} parser factory now.")
#             logger.info(f"Processed {base_file} to use {xml}, event and input excerpt generated.")
