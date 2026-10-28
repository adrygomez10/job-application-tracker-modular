package dev.adriangm.tracker.exports.api;

import java.io.IOException;
import java.io.OutputStream;

public interface ExportPlugin {
    ExportDescriptor descriptor();

    void write(ExportData data, OutputStream output) throws IOException;
}
