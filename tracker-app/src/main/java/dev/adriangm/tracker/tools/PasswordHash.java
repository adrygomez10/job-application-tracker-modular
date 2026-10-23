package dev.adriangm.tracker.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Reads a password from stdin; only the encoded hash is written to stdout. */
public final class PasswordHash {
  private PasswordHash() {}

  public static void main(String[] args) throws Exception {
    String password =
        new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
    if (password == null
        || password.length() < 12
        || password.getBytes(StandardCharsets.UTF_8).length > 72) {
      throw new IllegalArgumentException(
          "Password must contain at least 12 characters and at most 72 UTF-8 bytes.");
    }
    System.out.println("{bcrypt}" + new BCryptPasswordEncoder(12).encode(password));
  }
}
