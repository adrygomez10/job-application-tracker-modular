package dev.adriangm.tracker.imports.application;

import java.util.Optional;

public interface ImportLedger {
    Optional<String> fingerprint(String source, String externalId);
}
