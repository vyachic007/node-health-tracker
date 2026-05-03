package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NetworkServiceRepository extends JpaRepository<NetworkService, Long> {

    List<NetworkService> findAllByIsEnabledTrue();

    List<NetworkService> findAllByNodeIdOrderByCreatedAtDesc(Long nodeId);

    List<NetworkService> findAllByNodeOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<NetworkService> findByHeartbeatToken(String heartbeatToken);

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