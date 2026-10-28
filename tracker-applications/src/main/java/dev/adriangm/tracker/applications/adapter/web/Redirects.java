package dev.adriangm.tracker.applications.adapter.web;

import java.util.UUID;

/** Post/Redirect/Get: after a successful change, reloading the page does not repeat it. */
final class Redirects {
    private Redirects()
    {
    }

    static String toDetail(UUID id)
    {
        return "redirect:/applications/" + id;
    }
}
