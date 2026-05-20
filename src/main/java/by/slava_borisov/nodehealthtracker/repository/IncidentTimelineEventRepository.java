package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.IncidentTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentTimelineEventRepository extends JpaRepository<IncidentTimelineEvent, Long> {

    List<IncidentTimelineEvent> findAllByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}