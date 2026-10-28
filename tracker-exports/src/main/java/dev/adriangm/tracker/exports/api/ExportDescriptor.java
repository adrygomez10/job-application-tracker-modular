package dev.adriangm.tracker.exports.api;

public record ExportDescriptor(String id, String dataset, String format, String mediaType, String fileName) {
}
