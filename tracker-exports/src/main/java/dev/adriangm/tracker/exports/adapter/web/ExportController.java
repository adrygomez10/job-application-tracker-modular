package dev.adriangm.tracker.exports.adapter.web;

import dev.adriangm.tracker.applications.api.ApplicationsApi.Filter;
import dev.adriangm.tracker.exports.application.ExportService;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExportController {
    private final ExportService exports;
    private final Clock clock;

    public ExportController(ExportService exports, Clock clock)
    {
        this.exports = exports;
        this.clock = clock;
    }

    @GetMapping("/exports")
    String form(Model model)
    {
        model.addAttribute("formats", exports.formats());
        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("nextMonth", LocalDate.now(clock).plusMonths(1));
        return "exports";
    }

    @GetMapping("/exports/download")
    ResponseEntity<byte[]> download(ExportFilter form) throws IOException
    {
        var filter = new Filter(form.q(), form.status(), form.mode(), form.from(), form.to(), form.archived());
        LocalDate first = form.from() == null ? LocalDate.now(clock) : form.from();
        LocalDate last = form.to() == null ? first.plusMonths(1) : form.to();
        var download = exports
            .export(
                form.plugin(),
                filter,
                first.atStartOfDay(clock.getZone()).toInstant(),
                last.plusDays(1).atStartOfDay(clock.getZone()).toInstant()
            );
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(download.descriptor().mediaType()))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename(download.descriptor().fileName()).build().toString()
            )
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(download.contents());
    }

    public record ExportFilter(
        String plugin,
        String q,
        String status,
        String mode,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        Boolean archived
    ) {
        public ExportFilter
        {
            archived = Boolean.TRUE.equals(archived);
            q = q == null ? "" : q;
            status = status == null ? "" : status;
            mode = mode == null ? "" : mode;
        }
    }
}
