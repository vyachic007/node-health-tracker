package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NetworkServiceRepository extends JpaRepository<NetworkService, Long> {

    List<NetworkService> findAllByIsEnabledTrue();

    List<NetworkService> findAllByNodeIdOrderByCreatedAtDesc(Long nodeId);

    List<NetworkService> findAllByNodeOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<NetworkService> findByHeartbeatToken(String heartbeatToken);
}