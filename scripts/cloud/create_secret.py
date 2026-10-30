"""Create workshop credentials without reading the local environment file."""

import argparse
import getpass
import json
import re
import secrets
import subprocess


def run_tool(command: list[str], input_text: str | None = None) -> str:
    """Capture tool output and send sensitive input through stdin."""
    result = subprocess.run(
        command,
        input=input_text,
        check=True,
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    return result.stdout.strip()


def ensure_secret_absent(context: str) -> None:
    """Refuse credential rotation before asking for a password."""
    existing = run_tool(
        [
            "kubectl",
            "--context",
            context,
            "-n",
            "tracker",
            "get",
            "secret",
            "tracker-runtime",
            "--ignore-not-found",
            "-o",
            "name",
        ]
    )
    if existing:
        raise ValueError(
            "Secret already exists; credentials were not changed."
        )


def validate_password(password: str) -> None:
    """Require twelve characters within bcrypt's UTF-8 byte limit."""
    if len(password) < 12 or len(password.encode("utf-8")) > 72:
        raise ValueError(
            "Password needs 12+ characters and at most 72 UTF-8 bytes."
        )


def read_credentials() -> tuple[str, str]:
    """Read credentials without displaying the password."""
    username = input("Workshop username: ").strip()
    if not re.fullmatch(r"[A-Za-z0-9._-]{1,64}", username):
        raise ValueError("Invalid username.")

    password = getpass.getpass("Workshop password (12+ characters): ")
    if password != getpass.getpass("Repeat password: "):
        raise ValueError("Passwords differ.")
    validate_password(password)
    return username, password


def encode_password(password: str, image: str) -> str:
    """Hash the password with the application's existing bcrypt helper."""
    encoded = run_tool(
        [
            "docker",
            "run",
            "--rm",
            "-i",
            "--entrypoint",
            "java",
            image,
            "-Dloader.main=dev.adriangm.tracker.tools.PasswordHash",
            "-cp",
            "/app/app.jar",
            "org.springframework.boot.loader.launch.PropertiesLauncher",
        ],
        password + "\n",
    )
    if not re.fullmatch(r"\{bcrypt\}\$2[aby]\$.{56}", encoded):
        raise ValueError("Password hash generation failed.")
    return encoded


def create_secret(context: str, username: str, encoded_password: str) -> None:
    """Create credentials atomically without overwriting an existing Secret."""
    manifest = {
        "apiVersion": "v1",
        "kind": "Secret",
        "metadata": {"name": "tracker-runtime", "namespace": "tracker"},
        "type": "Opaque",
        "stringData": {
            "DB_PASSWORD": secrets.token_urlsafe(36),
            "TRACKER_USERNAME": username,
            "TRACKER_PASSWORD_HASH": encoded_password,
        },
    }
    run_tool(
        ["kubectl", "--context", context, "create", "-f", "-"],
        json.dumps(manifest),
    )


def main() -> None:
    """Validate the target and create one runtime Secret interactively."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--context", required=True, help="Full EKS cluster ARN"
    )
    parser.add_argument(
        "--image", default="job-application-tracker-modular:local"
    )
    args = parser.parse_args()
    context = str(args.context)
    image = str(args.image)
    context_pattern = r"arn:aws:eks:[a-z0-9-]+:\d{12}:cluster/[a-z][a-z0-9-]+"
    if not re.fullmatch(context_pattern, context):
        parser.error("Use the exact EKS context ARN.")

    try:
        ensure_secret_absent(context)
        username, password = read_credentials()
        encoded_password = encode_password(password, image)
        create_secret(context, username, encoded_password)
    except ValueError as error:
        parser.exit(1, f"{error}\n")
    except FileNotFoundError:
        parser.exit(
            1, "Required tool unavailable; check Docker and kubectl.\n"
        )
    except subprocess.CalledProcessError:
        parser.exit(
            1,
            "Tool failed; check Docker, cluster access and Secrets.\n",
        )

    print("Runtime Secret created without writing a credentials file.")


if __name__ == "__main__":
    main()
