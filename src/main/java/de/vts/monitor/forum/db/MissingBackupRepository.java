package de.vts.monitor.forum.db;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MissingBackupRepository extends JpaRepository<MissingBackupRecord, Long> {
    List<MissingBackupRecord> findByStatus(String status);
    List<MissingBackupRecord> findByStatusAndDaysOldIsNotNull(String status);
    Optional<MissingBackupRecord> findByMandantAndInstance(String mandant, String instance);
}
