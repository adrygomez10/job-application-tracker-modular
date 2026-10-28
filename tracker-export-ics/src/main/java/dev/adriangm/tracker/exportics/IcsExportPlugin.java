package dev.adriangm.tracker.exportics;

import dev.adriangm.tracker.exports.api.ExportData;
import dev.adriangm.tracker.exports.api.ExportDescriptor;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class IcsExportPlugin implements ExportPlugin {
    private static final DateTimeFormatter DATE = DateTimeFormatter
        .ofPattern("uuuuMMdd'T'HHmmss'Z'")
        .withZone(ZoneOffset.UTC);

    @Override
    public ExportDescriptor descriptor()
    {
        return new ExportDescriptor(
            "interviews-ics",
            "interviews",
            "ICS",
            "text/calendar;charset=UTF-8",
            "interviews.ics"
        );
    }

    @Override
    public void write(ExportData data, OutputStream output) throws IOException
    {
        IcsLines.write(output, "BEGIN:VCALENDAR");
        IcsLines.write(output, "VERSION:2.0");
        IcsLines.write(output, "PRODID:-//AdrianGM//Tracker Workshops//EN");
        for (var row : data.rows()) {
            event(output, row, data.generatedAt());
        }
        IcsLines.write(output, "END:VCALENDAR");
    }

    private static void event(OutputStream output, List<String> row, Instant generatedAt) throws IOException
    {
        IcsLines.write(output, "BEGIN:VEVENT");
        IcsLines.write(output, "UID:" + row.get(0) + "@tracker.local");
        IcsLines.write(output, "DTSTAMP:" + DATE.format(generatedAt));
        IcsLines.write(output, "DTSTART:" + DATE.format(Instant.parse(row.get(3))));
        IcsLines.write(output, "SUMMARY:" + IcsLines.text(row.get(1) + " - " + row.get(2)));
        IcsLines.write(output, "LOCATION:" + IcsLines.text(row.get(4)));
        IcsLines.write(output, "DESCRIPTION:" + IcsLines.text(row.get(5)));
        IcsLines.write(output, "END:VEVENT");
    }
}
