package dev.adriangm.tracker.applications.adapter.module;

import dev.adriangm.tracker.applications.api.ApplicationsApi;
import dev.adriangm.tracker.applications.application.details.RegisterApplication;
import dev.adriangm.tracker.applications.application.query.ApplicationQueries;
import dev.adriangm.tracker.applications.domain.Status;
import dev.adriangm.tracker.applications.domain.WorkMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationsFacade implements ApplicationsApi {
    private final RegisterApplication register;
    private final ApplicationQueries queries;
    private final ModuleReadQueries reads;

    public ApplicationsFacade(RegisterApplication register, ApplicationQueries queries, ModuleReadQueries reads)
    {
        this.register = register;
        this.queries = queries;
        this.reads = reads;
    }

    @Override
    public void validate(Offer offer)
    {
        OfferMapping.details(offer);
    }

    @Override
    public UUID register(UUID id, Offer offer)
    {
        return register.execute(id, OfferMapping.details(offer), Status.SAVED, null).id();
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<ApplicationView> applications(Filter filter)
    {
        List<ApplicationView> result = new ArrayList<>();
        int page = 0;
        ApplicationQueries.Results found;
        do {
            found = queries.search(criteria(filter, page));
            found
                .items()
                .forEach(
                    row -> result
                        .add(
                            new ApplicationView(
                                row.id(),
                                row.company(),
                                row.role(),
                                row.status().name(),
                                row.workMode().name(),
                                row.appliedOn(),
                                row.updatedAt()
                            )
                        )
                );
            page++;
        } while (page < found.totalPages());
        return List.copyOf(result);
    }

    private static ApplicationQueries.Criteria criteria(Filter filter, int page)
    {
        Status status = filter.status().isBlank() ? null : Status.valueOf(filter.status());
        WorkMode mode = filter.workMode().isBlank() ? null : WorkMode.valueOf(filter.workMode());
        return new ApplicationQueries.Criteria(
            filter.query(),
            status,
            mode,
            filter.from(),
            filter.to(),
            filter.archived(),
            page
        );
    }

    @Override
    public List<InterviewView> interviews(Instant from, Instant until)
    {
        if (from == null || until == null || !from.isBefore(until)) {
            throw new IllegalArgumentException("Invalid interview interval");
        }
        return reads.interviews(from, until);
    }

    @Override
    public List<ActivityView> openActivities()
    {
        return reads.openActivities();
    }

    @Override
    public Statistics statistics()
    {
        return reads.statistics();
    }
}
