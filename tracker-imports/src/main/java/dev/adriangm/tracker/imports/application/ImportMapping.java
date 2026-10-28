package dev.adriangm.tracker.imports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Offer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public final class ImportMapping {
    private ImportMapping()
    {
    }

    public static Offer offer(ImportRow row)
    {
        return new Offer(
            row.field("company"),
            row.field("role"),
            row.field("url"),
            row.field("source"),
            row.field("location"),
            row.field("work_mode"),
            row.field("technologies")
        );
    }

    public static ImportEntry entry(ImportRow row)
    {
        return new ImportEntry(row.field("source"), row.field("external_id"), offer(row), hash(row));
    }

    private static String hash(ImportRow row)
    {
        var canonical = new StringBuilder();
        for (
            String name : List
                .of("source", "external_id", "company", "role", "url", "location", "work_mode", "technologies")
        ) {
            String value = row.field(name);
            canonical.append(value.length()).append(':').append(value);
        }
        try {
            return HexFormat
                .of()
                .formatHex(
                    MessageDigest.getInstance("SHA-256").digest(canonical.toString().getBytes(StandardCharsets.UTF_8))
                );
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 is required", unavailable);
        }
    }
}
