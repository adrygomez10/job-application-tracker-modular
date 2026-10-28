package dev.adriangm.tracker.applications.application;

public final class RecordMissing extends RuntimeException {
    public RecordMissing()
    {
        super("Application not found.");
    }
}
