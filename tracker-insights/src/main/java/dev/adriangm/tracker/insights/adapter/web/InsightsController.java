package dev.adriangm.tracker.insights.adapter.web;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.insights.application.Insights;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InsightsController {
    private final ApplicationsApi applications;

    public InsightsController(ApplicationsApi applications)
    {
        this.applications = applications;
    }

    @GetMapping("/insights")
    String show(Model model)
    {
        var statistics = applications.statistics();
        model.addAttribute("statistics", statistics);
        model.addAttribute("rate", Insights.interviewRate(statistics));
        return "insights";
    }
}
