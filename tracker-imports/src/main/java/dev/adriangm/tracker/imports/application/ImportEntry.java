package dev.adriangm.tracker.imports.application;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Offer;

public record ImportEntry(String source, String externalId, Offer offer, String fingerprint) {}
