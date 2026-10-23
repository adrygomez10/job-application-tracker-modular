package dev.adriangm.tracker.applications.domain;

import java.net.URI;

final class Checks {
  private static final int MAXIMUM_URL_LENGTH = 2000;

  private Checks() {}

  static String requiredText(String value, int maximumLength) {
    String normalizedText = optionalText(value, maximumLength);
    if (normalizedText.isEmpty()) {
      throw new RuleViolation("required");
    }
    return normalizedText;
  }

  static String optionalText(String value, int maximumLength) {
    String normalizedText = value == null ? "" : value.strip();
    if (normalizedText.length() > maximumLength) {
      throw new RuleViolation("tooLong");
    }
    return normalizedText;
  }

  static String url(String value) {
    String normalizedUrl = optionalText(value, MAXIMUM_URL_LENGTH);
    if (normalizedUrl.isEmpty()) {
      return normalizedUrl;
    }
    URI parsedUrl = parseUrl(normalizedUrl);
    if (!hasHttpScheme(parsedUrl)
        || parsedUrl.getHost() == null
        || parsedUrl.getUserInfo() != null) {
      throw new RuleViolation("url");
    }
    return normalizedUrl;
  }

  private static URI parseUrl(String value) {
    try {
      return URI.create(value);
    } catch (IllegalArgumentException invalidUrl) {
      throw new RuleViolation("url", invalidUrl);
    }
  }

  private static boolean hasHttpScheme(URI url) {
    return "https".equalsIgnoreCase(url.getScheme()) || "http".equalsIgnoreCase(url.getScheme());
  }
}
