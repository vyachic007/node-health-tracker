package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    List<CheckResult> findAllByServiceIdOrderByCheckedAtDesc(Long serviceId);

    Optional<CheckResult> findTopByServiceIdOrderByCheckedAtDesc(Long serviceId);

    Optional<CheckResult> findTopByServiceNodeIdOrderByCheckedAtDesc(Long nodeId);

    long countByCheckedAtAfter(LocalDateTime checkedAt);

    long countByServiceNodeOwnerIdAndCheckedAtAfter(Long ownerId, LocalDateTime checkedAt);

    long countByServiceIdAndCheckedAtAfter(Long serviceId, LocalDateTime checkedAt);

    long countByServiceIdAndStatusAndCheckedAtAfter(
            Long serviceId,
            ServiceStatus status,
            LocalDateTime checkedAt
    );

    long countByServiceNodeIdAndCheckedAtAfter(Long nodeId, LocalDateTime checkedAt);

    long countByServiceNodeIdAndStatusAndCheckedAtAfter(
            Long nodeId,
            ServiceStatus status,
            LocalDateTime checkedAt
    );

    long countByServiceNodeOwnerIdAndStatusAndCheckedAtAfter(
            Long ownerId,
            ServiceStatus status,
            LocalDateTime checkedAt
    );

    @Query("""
        SELECT AVG(c.responseTimeMs)
        FROM CheckResult c
        WHERE c.service.node.owner.id = :ownerId
          AND c.status = :status
          AND c.checkedAt >= :checkedAt
          AND c.responseTimeMs IS NOT NULL
        """)
    Double findAverageResponseTimeByOwnerIdAndStatusAfter(
            @Param("ownerId") Long ownerId,
            @Param("status") ServiceStatus status,
            @Param("checkedAt") LocalDateTime checkedAt
    );

    @Query("""
            SELECT AVG(c.responseTimeMs)
            FROM CheckResult c
            WHERE c.service.id = :serviceId
              AND c.status = :status
              AND c.checkedAt >= :checkedAt
              AND c.responseTimeMs IS NOT NULL
            """)
    Double findAverageResponseTimeByServiceIdAndStatusAfter(
            @Param("serviceId") Long serviceId,
            @Param("status") ServiceStatus status,
            @Param("checkedAt") LocalDateTime checkedAt
    );

    @Query("""
            SELECT AVG(c.responseTimeMs)
            FROM CheckResult c
            WHERE c.service.node.id = :nodeId
              AND c.status = :status
              AND c.checkedAt >= :checkedAt
              AND c.responseTimeMs IS NOT NULL
            """)
    Double findAverageResponseTimeByNodeIdAndStatusAfter(
            @Param("nodeId") Long nodeId,
            @Param("status") ServiceStatus status,
            @Param("checkedAt") LocalDateTime checkedAt
    );
}