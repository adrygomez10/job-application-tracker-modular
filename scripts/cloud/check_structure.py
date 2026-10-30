"""Check the one-class-per-file convention without importing project code."""

import ast
import re
from pathlib import Path


def check_module(path: Path) -> None:
    """Reject grouped classes, mixed CLI modules and misleading file names."""
    module = ast.parse(path.read_text(encoding="utf-8"), filename=str(path))
    classes = [node for node in module.body if isinstance(node, ast.ClassDef)]
    if not classes:
        return
    if len(classes) != 1 or path.name == "__init__.py":
        raise ValueError(f"{path}: each class needs its own module")
    if any(
        isinstance(node, (ast.FunctionDef, ast.AsyncFunctionDef))
        for node in module.body
    ):
        raise ValueError(f"{path}: do not mix classes and module functions")
    expected_name = re.sub(r"(?<!^)(?=[A-Z])", "_", classes[0].name).lower()
    if path.stem != expected_name:
        raise ValueError(f"{path}: expected module name {expected_name}.py")


def main() -> None:
    """Check cloud helpers and their tests."""
    for path in Path("scripts/cloud").rglob("*.py"):
        check_module(path)


if __name__ == "__main__":
    main()
