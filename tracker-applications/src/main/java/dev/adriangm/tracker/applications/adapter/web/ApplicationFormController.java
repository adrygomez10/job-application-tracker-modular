package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.application.RecordMissing;
import dev.adriangm.tracker.applications.application.details.EditDetails;
import dev.adriangm.tracker.applications.application.details.RegisterApplication;
import dev.adriangm.tracker.applications.application.query.ApplicationQueries;
import dev.adriangm.tracker.applications.domain.RuleViolation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Creating and editing the descriptive data. Errors return the form with what was typed. */
@Controller
public class ApplicationFormController {
    private static final String FORM = "application-form";

    private final RegisterApplication register;
    private final EditDetails editDetails;
    private final ApplicationQueries queries;

    public ApplicationFormController(RegisterApplication register, EditDetails editDetails, ApplicationQueries queries)
    {
        this.register = register;
        this.editDetails = editDetails;
        this.queries = queries;
    }

    @GetMapping("/applications/new")
    String create(Model model)
    {
        model.addAttribute("form", ApplicationForm.empty());
        model.addAttribute("editing", false);
        return FORM;
    }

    @PostMapping("/applications")
    String create(
        @Valid @ModelAttribute("form") ApplicationForm form,
        BindingResult errors,
        Model model,
        HttpServletResponse response
    )
    {
        model.addAttribute("editing", false);
        if (errors.hasErrors()) {
            return rejected(response);
        }
        try {
            register.execute(form.id(), form.details(), form.status(), form.appliedOn());
        } catch (RuleViolation invalid) {
            model.addAttribute("rule", invalid.code());
            return rejected(response);
        }
        return Redirects.toDetail(form.id());
    }

    @GetMapping("/applications/{id}/edit")
    String edit(@PathVariable UUID id, Model model)
    {
        var application = queries.detail(id).orElseThrow(RecordMissing::new);
        model.addAttribute("form", ApplicationForm.from(application));
        model.addAttribute("editing", true);
        return FORM;
    }

    @PostMapping("/applications/{id}/edit")
    String edit(
        @PathVariable UUID id,
        @Valid @ModelAttribute("form") ApplicationForm form,
        BindingResult errors,
        Model model,
        HttpServletResponse response
    )
    {
        model.addAttribute("editing", true);
        if (!id.equals(form.id())) {
            errors.reject("invalid", "Invalid application identifier.");
        }
        if (errors.hasErrors()) {
            return rejected(response);
        }
        try {
            editDetails.execute(id, form.version(), form.details());
        } catch (RuleViolation invalid) {
            model.addAttribute("rule", invalid.code());
            return rejected(response);
        }
        return Redirects.toDetail(id);
    }

    private static String rejected(HttpServletResponse response)
    {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return FORM;
    }
}
