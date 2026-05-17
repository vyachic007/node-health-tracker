package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {

    List<NetworkNode> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    long countByOwnerId(Long ownerId);
}