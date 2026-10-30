"""Run the same cloud quality controls locally, in precommit and in CI."""

import os
import shutil
import subprocess
import sys
from pathlib import Path


def python_executable(root: Path) -> str:
    """Prefer the project's documented quality environment when installed."""
    relative_path = (
        ".venv-style/Scripts/python.exe"
        if os.name == "nt"
        else ".venv-style/bin/python"
    )
    candidate = root / relative_path
    return str(candidate) if candidate.is_file() else sys.executable


def bash_executable() -> str:
    """Use Git Bash on Windows instead of an unrelated WSL launcher."""
    if os.name == "nt":
        program_files = Path(
            os.environ.get("ProgramFiles", "C:/Program Files")
        )
        git_bash = program_files / "Git/bin/bash.exe"
        if git_bash.is_file():
            return str(git_bash)
    candidate = shutil.which("bash")
    if candidate is None:
        raise FileNotFoundError("Bash is required for cloud checks.")
    return candidate


def main() -> None:
    """Check Python, shell, workflows, Terraform and Kubernetes resources."""
    root = Path(__file__).resolve().parents[2]
    python = python_executable(root)
    environment = dict(os.environ)
    environment["PATH"] = (
        str(root / ".tools/aws-workshop")
        + os.pathsep
        + environment.get("PATH", "")
    )
    commands = [
        [python, "-m", "ruff", "check", "scripts/cloud"],
        [python, "-m", "ruff", "format", "--check", "scripts/cloud"],
        [python, "-m", "mypy"],
        [python, "scripts/cloud/check_structure.py"],
        [python, "-m", "unittest", "discover", "-s", "scripts/cloud/tests"],
        [bash_executable(), "scripts/cloud/check.sh"],
    ]
    for command in commands:
        subprocess.run(command, cwd=root, env=environment, check=True)


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as error:
        sys.exit(error.returncode)
