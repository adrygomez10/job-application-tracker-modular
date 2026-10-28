package dev.adriangm.tracker.imports.adapter.web;

import dev.adriangm.tracker.imports.adapter.csv.CsvOfferSource;
import dev.adriangm.tracker.imports.application.ImportService;
import dev.adriangm.tracker.imports.application.InvalidCsv;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImportController {
    private static final String KEY = "offer-preview";
    private final ImportService imports;
    private final Clock clock;

    public ImportController(ImportService imports, Clock clock)
    {
        this.imports = imports;
        this.clock = clock;
    }

    @GetMapping("/imports")
    String form()
    {
        return "imports";
    }

    @GetMapping("/imports/template")
    ResponseEntity<Resource> template()
    {
        Resource file = new ClassPathResource("import-templates/offers-template.csv");
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment().filename("offers-template.csv").build().toString()
            )
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(file);
    }

    @PostMapping("/imports/preview")
    String preview(@RequestParam MultipartFile file, HttpSession session, Model model, HttpServletResponse response)
        throws IOException
    {
        session.removeAttribute(KEY);
        try {
            if (file.getSize() > CsvOfferSource.MAX_BYTES) {
                throw new InvalidCsv("File exceeds 1 MiB");
            }
            byte[] contents = file.getBytes();
            model.addAttribute("report", imports.preview(contents));
            session.setAttribute(KEY, new Preview(contents, clock.instant()));
            model.addAttribute("confirmable", true);
        } catch (InvalidCsv invalid) {
            return invalid(invalid.getMessage(), model, response);
        }
        return "imports";
    }

    @PostMapping("/imports/confirm")
    String confirm(HttpSession session, Model model, HttpServletResponse response)
    {
        Preview preview = (Preview) session.getAttribute(KEY);
        session.removeAttribute(KEY);
        if (preview == null || !clock.instant().isBefore(preview.createdAt().plus(Duration.ofMinutes(15)))) {
            return invalid("Preview expired; upload the file again", model, response);
        }
        model.addAttribute("report", imports.confirm(preview.contents()));
        model.addAttribute("confirmable", false);
        return "imports";
    }

    private static String invalid(String message, Model model, HttpServletResponse response)
    {
        response.setStatus(400);
        model.addAttribute("importError", message);
        return "imports";
    }

    private record Preview(byte[] contents, Instant createdAt) implements Serializable {
        private Preview
        {
            contents = contents.clone();
        }

        @Override
        public byte[] contents()
        {
            return contents.clone();
        }
    }
}
