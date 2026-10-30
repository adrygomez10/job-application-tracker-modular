"""Check credential boundaries without contacting Docker or Kubernetes."""

import json
import subprocess
import unittest
from unittest.mock import patch

from scripts.cloud.create_secret import (
    create_secret,
    encode_password,
    ensure_secret_absent,
    main,
    validate_password,
)


class TestCreateSecret(unittest.TestCase):
    """Keep passwords off arguments and preserve existing credentials."""

    def test_password_uses_utf8_byte_limit(self) -> None:
        """Reject values whose UTF-8 representation exceeds bcrypt's limit."""
        validate_password("a" * 72)
        validate_password("é" * 36)
        for password in ("a" * 11, "a" * 73, "é" * 37):
            with self.subTest(password_length=len(password)):
                with self.assertRaises(ValueError):
                    validate_password(password)

    def test_existing_secret_stops_before_password_prompt(self) -> None:
        """An existing Secret must never trigger credential regeneration."""
        context = "arn:aws:eks:eu-west-1:123456789012:cluster/tracker-workshop"
        with (
            patch("sys.argv", ["create_secret.py", "--context", context]),
            patch(
                "scripts.cloud.create_secret.run_tool",
                return_value="secret/tracker-runtime",
            ),
            patch("builtins.input") as prompt,
            self.assertRaises(SystemExit) as failure,
        ):
            main()
        self.assertEqual(failure.exception.code, 1)
        prompt.assert_not_called()

    def test_cluster_failure_does_not_look_like_an_absent_secret(self) -> None:
        """An authentication error must prevent subsequent Secret creation."""
        with patch(
            "scripts.cloud.create_secret.run_tool",
            side_effect=subprocess.CalledProcessError(1, ["kubectl"]),
        ):
            with self.assertRaises(subprocess.CalledProcessError):
                ensure_secret_absent("unused-context")

    def test_password_reaches_docker_only_through_stdin(self) -> None:
        """Do not expose the plaintext password in process arguments."""
        password = "test-only-password"
        encoded = "{bcrypt}$2a$12$" + "a" * 53
        completed = subprocess.CompletedProcess(
            ["docker"], 0, stdout=encoded + "\n"
        )
        with patch(
            "scripts.cloud.create_secret.subprocess.run",
            return_value=completed,
        ) as run:
            self.assertEqual(encode_password(password, "test-image"), encoded)
        self.assertNotIn(password, run.call_args.args[0])
        self.assertEqual(run.call_args.kwargs["input"], password + "\n")
        self.assertTrue(run.call_args.kwargs["capture_output"])

    def test_secret_uses_atomic_create_and_stdin(self) -> None:
        """Send all Secret fields as input without using apply or a file."""
        with (
            patch(
                "scripts.cloud.create_secret.secrets.token_urlsafe",
                return_value="test-database-password",
            ),
            patch("scripts.cloud.create_secret.run_tool") as run,
        ):
            create_secret("test-context", "test-user", "test-hash")
        command, payload = run.call_args.args
        self.assertEqual(
            command,
            ["kubectl", "--context", "test-context", "create", "-f", "-"],
        )
        self.assertEqual(
            json.loads(payload)["stringData"],
            {
                "DB_PASSWORD": "test-database-password",
                "TRACKER_USERNAME": "test-user",
                "TRACKER_PASSWORD_HASH": "test-hash",
            },
        )
