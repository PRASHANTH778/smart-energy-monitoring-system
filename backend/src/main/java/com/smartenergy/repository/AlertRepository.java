package com.smartenergy.repository;

import com.smartenergy.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findTop10ByOrderByCreatedAtDesc();

    List<Alert> findByIsResolvedFalseOrderByCreatedAtDesc();

    long countByIsResolvedFalse();

    Page<Alert> findByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE (:severity IS NULL OR a.severity = :severity) " +
           "AND (:isResolved IS NULL OR a.isResolved = :isResolved) " +
           "ORDER BY a.createdAt DESC")
    Page<Alert> findFilteredAlerts(@Param("severity") String severity,
                                   @Param("isResolved") Boolean isResolved,
                                   Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.alertType = :alertType " +
           "AND (:deviceId IS NULL OR a.device.id = :deviceId) " +
           "AND (:zoneId IS NULL OR a.zone.id = :zoneId) " +
           "AND a.isResolved = false " +
           "AND a.createdAt >= :since")
    List<Alert> findRecentDuplicateAlerts(@Param("alertType") String alertType,
                                          @Param("deviceId") Long deviceId,
                                          @Param("zoneId") Long zoneId,
                                          @Param("since") LocalDateTime since);
}

