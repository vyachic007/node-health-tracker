package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.IncidentTimelineEventMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.IncidentTimelineEvent;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentTimelineEventRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.IncidentTimelineService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentTimelineServiceImpl implements IncidentTimelineService {

    private final IncidentTimelineEventRepository incidentTimelineEventRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentTimelineEventMapper incidentTimelineEventMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public void createEvent(
            Incident incident,
            CheckResult checkResult,
            IncidentTimelineEventType eventType,
            String message
    ) {
        IncidentTimelineEvent event = new IncidentTimelineEvent();
        event.setIncident(incident);
        event.setCheckResult(checkResult);
        event.setEventType(eventType);
        event.setMessage(message);
        event.setCreatedAt(LocalDateTime.now());

        incidentTimelineEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentTimelineEventResponse> getIncidentTimeline(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.INCIDENT_NOT_FOUND));

        validateIncidentOwner(incident);

        return incidentTimelineEventRepository.findAllByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(incidentTimelineEventMapper::toResponse)
                .toList();
    }

    private void validateIncidentOwner(Incident incident) {
        User currentUser = currentUserService.getCurrentUser();

        if (!incident.getService().getNode().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.INCIDENT_ACCESS_DENIED);
        }
    }
}