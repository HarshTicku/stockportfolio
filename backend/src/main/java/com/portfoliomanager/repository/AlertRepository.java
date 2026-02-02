package com.portfoliomanager.repository;

import com.portfoliomanager.entity.Alert;
import com.portfoliomanager.entity.AlertStatus;
import com.portfoliomanager.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findByAlertId(String alertId);

    Page<Alert> findByStatus(AlertStatus status, Pageable pageable);

    Page<Alert> findBySeverity(Severity severity, Pageable pageable);

    Page<Alert> findByStatusAndSeverity(AlertStatus status, Severity severity, Pageable pageable);

    List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status = :status")
    long countByStatus(@Param("status") AlertStatus status);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = :severity")
    long countBySeverity(@Param("severity") Severity severity);

    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<Alert> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
