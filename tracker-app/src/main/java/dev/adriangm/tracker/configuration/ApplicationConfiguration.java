package dev.adriangm.tracker.configuration;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.application.UnitOfWork;
import dev.adriangm.tracker.applications.application.VersionedUpdate;
import dev.adriangm.tracker.applications.application.activities.AddNote;
import dev.adriangm.tracker.applications.application.activities.AddTask;
import dev.adriangm.tracker.applications.application.activities.CompleteTask;
import dev.adriangm.tracker.applications.application.activities.RecordInterviewOutcome;
import dev.adriangm.tracker.applications.application.activities.ScheduleInterview;
import dev.adriangm.tracker.applications.application.details.ArchiveApplication;
import dev.adriangm.tracker.applications.application.details.EditDetails;
import dev.adriangm.tracker.applications.application.details.RegisterApplication;
import dev.adriangm.tracker.applications.application.details.RestoreApplication;
import dev.adriangm.tracker.applications.application.status.ChangeStatus;
import java.time.Clock;
import java.time.ZoneId;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Composition root: builds the use cases and connects them to their collaborators. */
@Configuration
public class ApplicationConfiguration {
  @Bean
  Clock clock(@Value("${tracker.zone}") String zone) {
    return Clock.system(ZoneId.of(zone));
  }

  @Bean
  UnitOfWork unitOfWork(PlatformTransactionManager manager) {
    var transaction = new TransactionTemplate(manager);
    return new UnitOfWork() {
      @Override
      public <T> T required(Supplier<T> work) {
        return transaction.execute(status -> work.get());
      }
    };
  }

  @Bean
  VersionedUpdate versionedUpdate(ApplicationStore store, UnitOfWork work) {
    return new VersionedUpdate(store, work);
  }

  @Bean
  RegisterApplication registerApplication(ApplicationStore store, UnitOfWork work, Clock clock) {
    return new RegisterApplication(store, work, clock);
  }

  @Bean
  EditDetails editDetails(VersionedUpdate update, Clock clock) {
    return new EditDetails(update, clock);
  }

  @Bean
  ArchiveApplication archiveApplication(VersionedUpdate update, Clock clock) {
    return new ArchiveApplication(update, clock);
  }

  @Bean
  RestoreApplication restoreApplication(VersionedUpdate update, Clock clock) {
    return new RestoreApplication(update, clock);
  }

  @Bean
  ChangeStatus changeStatus(VersionedUpdate update, Clock clock) {
    return new ChangeStatus(update, clock);
  }

  @Bean
  AddNote addNote(VersionedUpdate update, Clock clock) {
    return new AddNote(update, clock);
  }

  @Bean
  AddTask addTask(VersionedUpdate update, Clock clock) {
    return new AddTask(update, clock);
  }

  @Bean
  CompleteTask completeTask(VersionedUpdate update, Clock clock) {
    return new CompleteTask(update, clock);
  }

  @Bean
  ScheduleInterview scheduleInterview(VersionedUpdate update, Clock clock) {
    return new ScheduleInterview(update, clock);
  }

  @Bean
  RecordInterviewOutcome recordInterviewOutcome(VersionedUpdate update, Clock clock) {
    return new RecordInterviewOutcome(update, clock);
  }
}
