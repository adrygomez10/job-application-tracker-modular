package dev.adriangm.tracker.exportics;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class IcsLines {
  private IcsLines() {}

  static void write(OutputStream output, String line) throws IOException {
    int octets = 0;
    for (int offset = 0; offset < line.length(); ) {
      int codePoint = line.codePointAt(offset);
      byte[] encoded = new String(Character.toChars(codePoint)).getBytes(StandardCharsets.UTF_8);
      if (octets + encoded.length > 75) {
        output.write("\r\n ".getBytes(StandardCharsets.UTF_8));
        octets = 1;
      }
      output.write(encoded);
      octets += encoded.length;
      offset += Character.charCount(codePoint);
    }
    output.write("\r\n".getBytes(StandardCharsets.UTF_8));
  }

  static String text(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "\\n")
        .replace(";", "\\;")
        .replace(",", "\\,");
  }
}
