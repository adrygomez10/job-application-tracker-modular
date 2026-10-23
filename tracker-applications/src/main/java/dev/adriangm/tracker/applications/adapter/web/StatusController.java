package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.application.details.ArchiveApplication;
import dev.adriangm.tracker.applications.application.details.RestoreApplication;
import dev.adriangm.tracker.applications.application.status.ChangeStatus;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/** Status, archive and restore of an application. */
@Controller
public class StatusController {
  private final ChangeStatus changeStatus;
  private final ArchiveApplication archive;
  private final RestoreApplication restore;

  public StatusController(
      ChangeStatus changeStatus, ArchiveApplication archive, RestoreApplication restore) {
    this.changeStatus = changeStatus;
    this.archive = archive;
    this.restore = restore;
  }

  @PostMapping("/applications/{id}/status")
  String status(@PathVariable UUID id, @Valid @ModelAttribute TrackingForms.StatusForm form) {
    changeStatus.execute(id, form.version(), form.change());
    return Redirects.toDetail(id);
  }

  @PostMapping("/applications/{id}/archive")
  String archive(@PathVariable UUID id, @Valid @ModelAttribute TrackingForms.VersionForm form) {
    archive.execute(id, form.version());
    return Redirects.toDetail(id);
  }

  @PostMapping("/applications/{id}/restore")
  String restore(@PathVariable UUID id, @Valid @ModelAttribute TrackingForms.VersionForm form) {
    restore.execute(id, form.version());
    return Redirects.toDetail(id);
  }
}
