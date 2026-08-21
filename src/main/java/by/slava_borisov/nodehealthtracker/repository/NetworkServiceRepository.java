package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NetworkServiceRepository extends JpaRepository<NetworkService, Long> {

    List<NetworkService> findAllByIsEnabledTrue();

    List<NetworkService> findAllByNodeIdOrderByCreatedAtDesc(Long nodeId);

    List<NetworkService> findAllByNodeOwnerIdOrderByCreatedAtDesc(Long ownerId);

    long countByNodeOwnerId(Long ownerId);

    long countByNodeOwnerIdAndIsEnabledTrue(Long ownerId);

    long countByNodeOwnerIdAndIsEnabledFalse(Long ownerId);

    long countByIsEnabledTrue();

    long countByIsEnabledFalse();

    long countByNodeId(Long nodeId);

    long countByNodeIdAndIsEnabledTrue(Long nodeId);

    long countByNodeIdAndIsEnabledFalse(Long nodeId);

    List<NetworkService> findAllByNodeOwnerId(Long ownerId);

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM network_services ns
                    JOIN network_nodes nn ON ns.node_id = nn.id
                    JOIN check_results cr ON cr.service_id = ns.id
                    WHERE nn.owner_id = :ownerId
                      AND cr.checked_at = (
                            SELECT MAX(cr2.checked_at)
                            FROM check_results cr2
                            WHERE cr2.service_id = ns.id
                      )
                      AND cr.status = :status
                    """,
            nativeQuery = true
    )
    long countCurrentServicesByStatus(
            @Param("ownerId") Long ownerId,
            @Param("status") String status
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM network_services ns
                    JOIN check_results cr ON cr.service_id = ns.id
                    WHERE cr.checked_at = (
                          SELECT MAX(cr2.checked_at)
                          FROM check_results cr2
                          WHERE cr2.service_id = ns.id
                    )
                      AND cr.status = :status
                    """,
            nativeQuery = true
    )
    long countCurrentServicesByStatus(
            @Param("status") String status
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM network_services ns
                    JOIN check_results cr ON cr.service_id = ns.id
                    WHERE ns.node_id = :nodeId
                      AND ns.is_enabled = true
                      AND cr.checked_at = (
                            SELECT MAX(cr2.checked_at)
                            FROM check_results cr2
                            WHERE cr2.service_id = ns.id
                      )
                      AND cr.status = :status
                    """,
            nativeQuery = true
    )
    long countCurrentServicesByNodeIdAndStatus(
            @Param("nodeId") Long nodeId,
            @Param("status") String status
    );

    @Query(
            value = """
                    SELECT COUNT(*)
                    FROM network_services ns
                    WHERE ns.node_id = :nodeId
                      AND ns.is_enabled = true
                      AND NOT EXISTS (
                            SELECT 1
                            FROM check_results cr
                            WHERE cr.service_id = ns.id
                      )
                    """,
            nativeQuery = true
    )
    long countEnabledServicesWithoutChecksByNodeId(@Param("nodeId") Long nodeId);

    @Query(
            value = """
                    SELECT *
                    FROM network_services
                    WHERE is_enabled = true
                      AND (
                            last_checked_at IS NULL
                            OR last_checked_at + (interval_seconds * INTERVAL '1 second') <= NOW()
                      )
                    ORDER BY last_checked_at NULLS FIRST, created_at
                    """,
            nativeQuery = true
    )
    List<NetworkService> findServicesDueForCheck();
}