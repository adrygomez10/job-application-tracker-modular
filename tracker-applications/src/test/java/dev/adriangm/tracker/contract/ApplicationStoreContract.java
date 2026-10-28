package dev.adriangm.tracker.contract;

import static dev.adriangm.tracker.support.Fixtures.NOW;
import static dev.adriangm.tracker.support.Fixtures.saved;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adriangm.tracker.applications.application.ApplicationStore;
import dev.adriangm.tracker.applications.application.EditConflict;
import dev.adriangm.tracker.applications.application.UnitOfWork;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Behaviour every {@link ApplicationStore} adapter must honour. Each adapter test implements this
 * interface and inherits the tests.
 */
public interface ApplicationStoreContract {
    ApplicationStore store();

    UnitOfWork work();

    @Test
    default void missingRecordIsAnEmptyOptional()
    {
        work().required(() -> {
            assertThat(store().find(UUID.randomUUID())).isEmpty();
            return null;
        });
    }

    @Test
    default void createdRecordRoundTripsWithoutLeakingMutation()
    {
        work().required(() -> {
            var application = store().create(saved());
            assertThat(store().find(application.id())).contains(application);
            return null;
        });
    }

    @Test
    default void saveIncrementsVersionAndKeepsHistory()
    {
        work().required(() -> {
            var application = store().create(saved());
            var next = store().save(application.addNote("Contract note", NOW), application.version());
            assertThat(next.version()).isEqualTo(application.version() + 1);
            assertThat(store().find(application.id()).orElseThrow().timeline()).hasSize(2);
            return null;
        });
    }

    @Test
    default void staleVersionIsRejected()
    {
        work().required(() -> {
            var application = store().create(saved());
            store().save(application.addNote("First", NOW), 0);
            assertThatThrownBy(() -> store().save(application.addNote("Stale", NOW), 0))
                .isInstanceOf(EditConflict.class);
            return null;
        });
    }

    @Test
    default void duplicateIdentityIsRejected()
    {
        work().required(() -> {
            var application = store().create(saved());
            assertThatThrownBy(() -> store().create(application)).isInstanceOf(EditConflict.class);
            return null;
        });
    }
}
