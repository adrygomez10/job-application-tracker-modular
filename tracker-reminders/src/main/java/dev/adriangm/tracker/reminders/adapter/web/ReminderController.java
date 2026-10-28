package dev.adriangm.tracker.reminders.adapter.web;

import dev.adriangm.tracker.reminders.adapter.scheduling.ReminderJob;
import dev.adriangm.tracker.reminders.application.ReminderStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReminderController {
    private final ReminderStore store;
    private final ReminderJob job;

    public ReminderController(ReminderStore store, ReminderJob job)
    {
        this.store = store;
        this.job = job;
    }

    @GetMapping("/reminders")
    String list(Model model)
    {
        model.addAttribute("snapshot", store.snapshot());
        model.addAttribute("failed", job.failed());
        return "reminders";
    }
}
