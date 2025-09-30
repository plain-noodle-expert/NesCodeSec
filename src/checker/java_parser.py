# Copyright (c) 2025 Alibaba Group and its affiliates

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import threading
from pathlib import Path
from typing import Any, List, Optional, Union

import jpype
import jpype.imports
from loguru import logger

from .config import core_jars, jar_package_dir, to_resolve_dir


class JavaParserManager:
    """JavaParser manager, responsible for managing JavaParser instances and type solvers"""

    _instance = None
    _lock = threading.Lock()

    def __new__(cls):
        """Singleton pattern, ensures only one JavaParser instance globally"""
        if cls._instance is None:
            with cls._lock:
                if cls._instance is None:
                    cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        """Initialize JavaParser manager"""
        if hasattr(self, "_initialized"):
            return

        self._initialized = True
        self._start_jvm()
        self._import_java_classes()
        self.parser = None
        self.combined_type_solver = None
        self.symbol_solver = None

    def _start_jvm(self):
        """Start JVM and load core JAR packages"""
        if jpype.isJVMStarted():
            logger.debug("JVM already started")
            return

        # Get core JAR package paths from config
        current_file_path = Path(__file__).resolve()
        jar_package_path = current_file_path.parents[3] / jar_package_dir

        # Build JAR file path list
        jar_paths = []
        for jar_name in core_jars.values():
            jar_path = jar_package_path / jar_name
            # logger.debug(f"Loading core JAR: {jar_path}")
            if not jar_path.exists():
                raise FileNotFoundError(f"Core JAR file not found: {jar_path}")
            jar_paths.append(str(jar_path))

        # Start JVM
        jpype.startJVM(classpath=jar_paths)
        # logger.debug("JVM started successfully")

    def _import_java_classes(self):
        """Import required Java classes"""
        global JavaParser, ParseProblemException, JavaSymbolSolver
        global CombinedTypeSolver, ReflectionTypeSolver, JarTypeSolver
        global StringReader

        try:
            from com.github.javaparser import JavaParser, ParseProblemException
            from com.github.javaparser.symbolsolver import JavaSymbolSolver
            from com.github.javaparser.symbolsolver.resolution.typesolvers import (
                CombinedTypeSolver,
                JarTypeSolver,
                ReflectionTypeSolver,
            )
            from java.io import StringReader

            logger.debug("Java classes imported successfully")
        except ImportError as e:
            logger.debug(f"Failed to import Java classes: {e}")
            raise

    def initialize_parser(
        self, jar_directories: Optional[List[Union[str, Path]]] = None
    ):
        """
        Initialize JavaParser instance

        Args:
            jar_directories: List of directories containing JAR files to add to type solver
        """
        # Check if already initialized
        if self.parser is not None and self.combined_type_solver is not None:
            logger.debug("JavaParser already initialized, skipping initialization")
            return

        try:
            # Create combined type solver
            self.combined_type_solver = CombinedTypeSolver()
            self.combined_type_solver.add(ReflectionTypeSolver())

            # Load JAR packages from to_resolve directory by default
            current_file_path = Path(__file__).resolve()
            default_jar_dir = current_file_path.parents[3] / to_resolve_dir

            if default_jar_dir.exists():
                logger.debug(f"Loading default JAR directory: {default_jar_dir}")
                self.add_jars_from_directory(default_jar_dir)

            # Load JAR packages from specified directories
            if jar_directories:
                for directory in jar_directories:
                    self.add_jars_from_directory(directory)

            # Create symbol solver and parser
            self.symbol_solver = JavaSymbolSolver(self.combined_type_solver)
            self.parser = JavaParser()
            self.parser.getParserConfiguration().setSymbolResolver(self.symbol_solver)

            # logger.debug("JavaParser initialized successfully")

        except Exception as e:
            logger.debug(f"Failed to initialize JavaParser: {e}")
            raise

    def add_jars_from_directory(self, directory: Union[str, Path]) -> int:
        """
        Add all JAR files from specified directory to type solver

        Args:
            directory: Directory path containing JAR files

        Returns:
            int: Number of successfully added JAR files
        """
        directory_path = Path(directory)
        if not directory_path.exists():
            logger.debug(f"Directory does not exist: {directory_path}")
            return 0

        if not directory_path.is_dir():
            logger.debug(f"Path is not a directory: {directory_path}")
            return 0

        jar_count = 0
        logger.debug(f"Scanning JAR files in directory: {directory_path}")

        for jar_file in directory_path.glob("**/*.jar"):
            if self.add_jar_solver(jar_file):
                jar_count += 1

        logger.debug(f"Successfully added {jar_count} JAR files from {directory_path}")
        return jar_count

    def add_jar_solver(self, jar_path: Union[str, Path]) -> bool:
        """
        Add single JAR file to type solver

        Args:
            jar_path: JAR file path

        Returns:
            bool: Whether successfully added
        """
        jar_path = Path(jar_path)

        if not jar_path.exists():
            logger.debug(f"JAR file does not exist: {jar_path}")
            return False

        try:
            self.combined_type_solver.add(JarTypeSolver(str(jar_path)))
            logger.debug(f"Successfully added JAR: {jar_path}")
            return True
        except Exception as e:
            logger.debug(f"Failed to load JAR {jar_path}: {e}")
            return False

    def get_parser(self) -> Any:
        """Get JavaParser instance"""
        if self.parser is None:
            raise RuntimeError(
                "JavaParser not initialized, please call initialize_parser() first"
            )
        return self.parser

    def parse(self, java_code: str) -> Any:
        """
        Parse Java code

        Args:
            java_code: Java code string

        Returns:
            Parse result
        """
        parser = self.get_parser()
        return parser.parse(StringReader(java_code))


class JavaSyntaxChecker:
    """Java code syntax checker, using JavaParser to check if Java code has syntax errors"""

    def __init__(self):
        """Initialize syntax checker"""
        self.parser_manager = JavaParserManager()

    def check(self, java_code: str) -> dict[str, bool | dict[Any, str]]:
        """
        Check if Java code has syntax errors

        Args:
            java_code: Java code string to check

        Returns:
            bool: Returns True if there are syntax errors, otherwise False
        """
        result_log = {"has_error": False, "errors": []}
        try:
            if self.parser_manager.parser is None:
                self.parser_manager.initialize_parser()

            result = self.parser_manager.parse(java_code)

            # Check parse result
            if result.isSuccessful():
                return result_log
            else:
                result_log["has_error"] = True
                result_log["errors"] = [{prob.getLocation(): prob.getMessage()} for prob in result.getProblems()]
                result_log["code under check"] = java_code
                return result_log

        except ParseProblemException:
            return {"has_error": True, "errors": {"Unknown Location": "Parse problem exception occurred"}}
        except Exception:
            # Catch other possible exceptions
            return {"has_error": True, "errors": {"Unknown Location": "Unknown error occurred"}}