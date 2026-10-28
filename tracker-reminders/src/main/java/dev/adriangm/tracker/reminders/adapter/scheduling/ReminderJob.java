package dev.adriangm.tracker.reminders.adapter.scheduling;

import dev.adriangm.tracker.reminders.application.RefreshReminders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderJob {
    private static final Logger LOG = LoggerFactory.getLogger(ReminderJob.class);
    private final RefreshReminders refresh;
    private volatile boolean failed;

    public ReminderJob(RefreshReminders refresh)
    {
        this.refresh = refresh;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(
        fixedDelayString = "${tracker.reminders.interval-ms:60000}",
        initialDelayString = "${tracker.reminders.interval-ms:60000}"
    )
    public synchronized void run()
    {
        try {
            refresh.execute();
            failed = false;
        } catch (DataAccessException unavailable) {
            failed = true;
            LOG.warn("Reminder refresh failed; previous snapshot retained: {}", unavailable.getClass().getSimpleName());
        }
    }

    public boolean failed()
    {
        return failed;
    }
}
