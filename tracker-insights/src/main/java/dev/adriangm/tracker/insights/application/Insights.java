package dev.adriangm.tracker.insights.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Statistics;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Insights {
  private Insights() {}

  public static String interviewRate(Statistics statistics) {
    if (statistics.submitted() == 0) {
      return "Sin datos";
    }
    return BigDecimal.valueOf(statistics.interviewed())
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(statistics.submitted()), 1, RoundingMode.HALF_UP)
            .toPlainString()
        + "%";
  }
}
