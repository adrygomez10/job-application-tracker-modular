package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.application.RecordMissing;
import dev.adriangm.tracker.applications.application.query.ApplicationQueries;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Read-only pages. They use the read contract directly because reads apply no rules. */
@Controller
public class OverviewController {
    private static final int DASHBOARD_ITEMS = 5;
    private static final int AGENDA_ITEMS = 100;

    private final ApplicationQueries queries;
    private final Clock clock;

    public OverviewController(ApplicationQueries queries, Clock clock)
    {
        this.queries = queries;
        this.clock = clock;
    }

    @GetMapping("/")
    String dashboard(Model model)
    {
        model.addAttribute("counts", queries.counts());
        model.addAttribute("interviews", queries.upcomingInterviews(clock.instant(), DASHBOARD_ITEMS));
        model.addAttribute("tasks", queries.pendingTasks(DASHBOARD_ITEMS));
        return "dashboard";
    }

    @GetMapping("/agenda")
    String agenda(Model model)
    {
        model.addAttribute("interviews", queries.upcomingInterviews(clock.instant(), AGENDA_ITEMS));
        model.addAttribute("awaitingResult", queries.interviewsAwaitingResult(clock.instant(), AGENDA_ITEMS));
        model.addAttribute("tasks", queries.pendingTasks(AGENDA_ITEMS));
        model.addAttribute("agendaLimit", AGENDA_ITEMS);
        return "agenda";
    }

    @GetMapping("/applications")
    String list(
        @RequestParam(name = "q", defaultValue = "") String query,
        @RequestParam(required = false) Status status,
        @RequestParam(required = false) WorkMode mode,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "false") boolean archived,
        @RequestParam(defaultValue = "0") int page,
        Model model
    )
    {
        var criteria = new ApplicationQueries.Criteria(query, status, mode, from, to, archived, page);
        model.addAttribute("criteria", criteria);
        model.addAttribute("results", queries.search(criteria));
        return "applications";
    }

    @GetMapping("/applications/{id}")
    String detail(@PathVariable UUID id, Model model)
    {
        model.addAttribute("job", queries.detail(id).orElseThrow(RecordMissing::new));
        return "application-detail";
    }
}
