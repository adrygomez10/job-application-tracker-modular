package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Options and reference dates that every page can show. */
@ControllerAdvice
public class ViewChoices {
    private final Clock clock;

    public ViewChoices(Clock clock)
    {
        this.clock = clock;
    }

    @ModelAttribute
    void choices(Model model)
    {
        model.addAttribute("statuses", Status.values());
        model.addAttribute("workModes", WorkMode.values());
        model.addAttribute("interviewKinds", Interview.Kind.values());
        model.addAttribute("interviewResults", List.of(Interview.Outcome.COMPLETED, Interview.Outcome.CANCELLED));
        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("zone", clock.getZone().getId());
    }
}
