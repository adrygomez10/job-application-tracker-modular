package dev.adriangm.tracker.applications.adapter.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface ApplicationJpaRepository
    extends
        JpaRepository<ApplicationRow, UUID>,
        JpaSpecificationExecutor<ApplicationRow> {
}
