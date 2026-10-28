package dev.adriangm.tracker.applications.adapter.module;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Offer;
import dev.adriangm.tracker.applications.api.InvalidOffer;
import dev.adriangm.tracker.applications.domain.ApplicationDetails;
import dev.adriangm.tracker.applications.domain.RuleViolation;
import dev.adriangm.tracker.applications.domain.WorkMode;

final class OfferMapping {
    private OfferMapping()
    {
    }

    static ApplicationDetails details(Offer offer)
    {
        try {
            return new ApplicationDetails(
                offer.company(),
                offer.role(),
                offer.url(),
                offer.source(),
                offer.location(),
                WorkMode.valueOf(offer.workMode()),
                offer.technologies(),
                "",
                ""
            );
        } catch (IllegalArgumentException | RuleViolation invalid) {
            throw new InvalidOffer("Invalid offer fields or work mode", invalid);
        }
    }
}
