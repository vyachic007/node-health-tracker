package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkServiceRepository extends JpaRepository<NetworkService, Long> {

    List<NetworkService> findAllByIsEnabledTrue();

    List<NetworkService> findAllByNodeIdOrderByCreatedAtDesc(Long nodeId);

    List<NetworkService> findAllByNodeOwnerIdOrderByCreatedAtDesc(Long ownerId);
}