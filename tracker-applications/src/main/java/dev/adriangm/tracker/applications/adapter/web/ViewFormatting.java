package dev.adriangm.tracker.applications.adapter.web;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/** Formats instants in the configured zone for the templates. */
@Component("viewFormatting")
public class ViewFormatting {
    private final DateTimeFormatter formatter;

    public ViewFormatting(Clock clock)
    {
        formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm").withZone(clock.getZone());
    }

    public String dateTime(Instant instant)
    {
        return formatter.format(instant);
    }
}
