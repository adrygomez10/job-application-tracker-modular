package dev.adriangm.tracker.insights.application;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Statistics;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InsightsTest {
  @Test
  void noDenominatorMeansNoData() {
    assertThat(Insights.interviewRate(new Statistics(Map.of(), 0, 0))).isEqualTo("Sin datos");
  }

  @Test
  void rateUsesDecimalRounding() {
    assertThat(Insights.interviewRate(new Statistics(Map.of(), 3, 1))).isEqualTo("33.3%");
  }
}
