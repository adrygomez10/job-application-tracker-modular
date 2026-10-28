package dev.adriangm.tracker.applications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ChecksTest {
    @ParameterizedTest
    @ValueSource(strings = {"http://example.com/jobs", "https://example.com/jobs"})
    void httpLinksAreNormalizedWithoutChangingTheirContents(String url)
    {
        assertThat(Checks.url("  " + url + "  ")).isEqualTo(url);
    }

    @Test
    void malformedUrlKeepsItsParsingCause()
    {
        assertThatThrownBy(() -> Checks.url("https://exa mple.com"))
            .isInstanceOf(RuleViolation.class)
            .hasMessageContaining("url")
            .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void optionalTextStillEnforcesItsMaximumLength()
    {
        assertThat(Checks.optionalText("  abc  ", 3)).isEqualTo("abc");
        assertThatThrownBy(() -> Checks.optionalText("abcd", 3))
            .isInstanceOf(RuleViolation.class)
            .hasMessageContaining("tooLong");
    }
}
