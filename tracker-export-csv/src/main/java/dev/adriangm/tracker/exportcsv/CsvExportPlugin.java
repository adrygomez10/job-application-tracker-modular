package dev.adriangm.tracker.exportcsv;

import dev.adriangm.tracker.exports.api.ExportData;
import dev.adriangm.tracker.exports.api.ExportDescriptor;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class CsvExportPlugin implements ExportPlugin {
    @Override
    public ExportDescriptor descriptor()
    {
        return new ExportDescriptor(
            "applications-csv",
            "applications",
            "CSV",
            "text/csv;charset=UTF-8",
            "applications.csv"
        );
    }

    @Override
    public void write(ExportData data, OutputStream output) throws IOException
    {
        try (var csv = new CSVPrinter(new OutputStreamWriter(output, StandardCharsets.UTF_8), CSVFormat.RFC4180)) {
            csv.printRecord(data.columns());
            for (var row : data.rows()) {
                csv.printRecord(row.stream().map(CsvExportPlugin::safeCell).toList());
            }
        }
    }

    static String safeCell(String text)
    {
        String stripped = text.stripLeading();
        if (!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0) {
            return "'" + text;
        }
        if (text.startsWith("\t") || text.startsWith("\r") || text.startsWith("\n")) {
            return "'" + text;
        }
        return text;
    }
}
