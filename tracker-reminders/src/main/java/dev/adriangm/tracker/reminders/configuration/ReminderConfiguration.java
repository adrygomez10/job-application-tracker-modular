package dev.adriangm.tracker.reminders.configuration;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.reminders.application.RefreshReminders;
import dev.adriangm.tracker.reminders.application.ReminderPolicy;
import dev.adriangm.tracker.reminders.application.ReminderStore;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ReminderConfiguration {
    @Bean
    RefreshReminders refreshReminders(ApplicationsApi applications, ReminderStore store, Clock clock)
    {
        return new RefreshReminders(applications, new ReminderPolicy(clock), store, clock);
    }
}
