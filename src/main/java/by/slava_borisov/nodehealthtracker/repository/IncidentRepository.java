package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByServiceIdAndStatus(Long serviceId, IncidentStatus status);

    List<Incident> findAllByServiceIdOrderByOpenedAtDesc(Long serviceId);

    List<Incident> findAllByServiceNodeOwnerIdOrderByOpenedAtDesc(Long ownerId);

    long countByServiceNodeOwnerIdAndStatus(Long ownerId, IncidentStatus status);

    long countByServiceNodeIdAndStatus(Long nodeId, IncidentStatus status);

    long countByStatus(IncidentStatus status);
}