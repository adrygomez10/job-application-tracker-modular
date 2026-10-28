package dev.adriangm.tracker.applications.adapter.persistence;

import dev.adriangm.tracker.applications.application.query.ApplicationQueries;
import dev.adriangm.tracker.applications.domain.Interview;
import dev.adriangm.tracker.applications.domain.JobApplication;
import dev.adriangm.tracker.applications.domain.Status;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements the read contract. Lists select only the columns they show, so they never load the
 * notes, history or activities of each application.
 */
@Repository
@Transactional(readOnly = true)
public class JpaApplicationQueries implements ApplicationQueries {
    private final ApplicationJpaRepository repository;
    private final EntityManager entities;

    public JpaApplicationQueries(ApplicationJpaRepository repository, EntityManager entities)
    {
        this.repository = repository;
        this.entities = entities;
    }

    @Override
    public Results search(Criteria criteria)
    {
        var builder = entities.getCriteriaBuilder();
        var query = builder.createQuery(Summary.class);
        var root = query.from(ApplicationRow.class);
        query
            .select(
                builder
                    .construct(
                        Summary.class,
                        root.get("id"),
                        root.get("company"),
                        root.get("role"),
                        root.get("status"),
                        root.get("workMode"),
                        root.get("appliedOn"),
                        root.get("updatedAt")
                    )
            )
            .where(matching(criteria, root, builder))
            .orderBy(builder.desc(root.get("updatedAt")), builder.asc(root.get("id")));
        var items = entities
            .createQuery(query)
            .setFirstResult(criteria.page() * PAGE_SIZE)
            .setMaxResults(PAGE_SIZE)
            .getResultList();
        long total = count(criteria);
        int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
        return new Results(items, total, criteria.page(), totalPages);
    }

    private long count(Criteria criteria)
    {
        var builder = entities.getCriteriaBuilder();
        var query = builder.createQuery(Long.class);
        var root = query.from(ApplicationRow.class);
        query.select(builder.count(root)).where(matching(criteria, root, builder));
        return entities.createQuery(query).getSingleResult();
    }

    private static Predicate matching(Criteria criteria, Root<ApplicationRow> root, CriteriaBuilder builder)
    {
        List<Predicate> conditions = new ArrayList<>();
        conditions.add(builder.equal(root.get("archived"), criteria.archived()));
        if (!criteria.query().isEmpty()) {
            String pattern = "%" + escapeLike(criteria.query().toLowerCase(Locale.ROOT)) + "%";
            conditions
                .add(
                    builder
                        .or(
                            builder.like(builder.lower(root.get("company")), pattern, '!'),
                            builder.like(builder.lower(root.get("role")), pattern, '!')
                        )
                );
        }
        if (criteria.status() != null) {
            conditions.add(builder.equal(root.get("status"), criteria.status()));
        }
        if (criteria.workMode() != null) {
            conditions.add(builder.equal(root.get("workMode"), criteria.workMode()));
        }
        if (criteria.from() != null) {
            conditions.add(builder.greaterThanOrEqualTo(root.get("appliedOn"), criteria.from()));
        }
        if (criteria.to() != null) {
            conditions.add(builder.lessThanOrEqualTo(root.get("appliedOn"), criteria.to()));
        }
        return builder.and(conditions.toArray(Predicate[]::new));
    }

    private static String escapeLike(String text)
    {
        return text.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    @Override
    public Optional<JobApplication> detail(UUID id)
    {
        return repository.findById(id).map(ApplicationRow::domain);
    }

    @Override
    public StatusCounts counts()
    {
        var byStatus = new EnumMap<Status, Long>(Status.class);
        entities
            .createQuery(
                "select a.status, count(a) from ApplicationRow a" + " where a.archived = false group by a.status",
                Object[].class
            )
            .getResultList()
            .forEach(row -> byStatus.put((Status) row[0], (Long) row[1]));
        return new StatusCounts(byStatus);
    }

    @Override
    public List<UpcomingInterview> upcomingInterviews(Instant from, int limit)
    {
        return scheduledInterviews("i.startsAt >= :moment", from, limit);
    }

    @Override
    public List<UpcomingInterview> interviewsAwaitingResult(Instant before, int limit)
    {
        return scheduledInterviews("i.startsAt < :moment", before, limit);
    }

    private List<UpcomingInterview> scheduledInterviews(String timeCondition, Instant moment, int limit)
    {
        return entities
            .createQuery(
                "select new " + UpcomingInterview.class.getName() + "(a.id, a.company, a.role,"
                    + " i.title, i.startsAt) from ApplicationRow a join a.interviews i"
                    + " where a.archived = false and i.outcome = :outcome and " + timeCondition
                    + " order by i.startsAt, a.id",
                UpcomingInterview.class
            )
            .setParameter("outcome", Interview.Outcome.SCHEDULED)
            .setParameter("moment", moment)
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public List<PendingTask> pendingTasks(int limit)
    {
        return entities
            .createQuery(
                "select new " + PendingTask.class.getName() + "(a.id, a.company, a.role,"
                    + " t.title, t.dueOn) from ApplicationRow a join a.tasks t"
                    + " where a.archived = false and t.completed = false order by t.dueOn, a.id",
                PendingTask.class
            )
            .setMaxResults(limit)
            .getResultList();
    }
}
