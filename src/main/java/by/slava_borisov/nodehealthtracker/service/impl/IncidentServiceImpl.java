package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.IncidentMapper;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.IncidentService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentMapper incidentMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public IncidentResponse getIncidentById(Long incidentId) {
        User currentUser = currentUserService.getCurrentUser();
        Incident incident = findIncidentById(incidentId);

        validateIncidentAccess(incident, currentUser);

        return incidentMapper.toIncidentResponse(incident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getCurrentUserIncidents() {
        User currentUser = currentUserService.getCurrentUser();

        return incidentRepository.findAllByServiceNodeOwnerIdOrderByOpenedAtDesc(currentUser.getId())
                .stream()
                .map(incidentMapper::toIncidentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentResponse> getServiceIncidents(Long serviceId) {
        User currentUser = currentUserService.getCurrentUser();

        return incidentRepository.findAllByServiceIdOrderByOpenedAtDesc(serviceId)
                .stream()
                .filter(incident -> isIncidentOwner(incident, currentUser))
                .map(incidentMapper::toIncidentResponse)
                .toList();
    }

    @Override
    @Transactional
    public IncidentResponse closeIncident(Long incidentId) {
        User currentUser = currentUserService.getCurrentUser();
        Incident incident = findIncidentById(incidentId);

        validateIncidentAccess(incident, currentUser);

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            throw new InvalidOperationException(Messages.INCIDENT_ALREADY_CLOSED);
        }

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setClosedAt(LocalDateTime.now());

        Incident savedIncident = incidentRepository.save(incident);

        return incidentMapper.toIncidentResponse(savedIncident);
    }

    private Incident findIncidentById(Long incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.INCIDENT_NOT_FOUND));
    }

    private void validateIncidentAccess(Incident incident, User currentUser) {
        if (!isIncidentOwner(incident, currentUser)) {
            throw new AccessDeniedException(Messages.INCIDENT_ACCESS_DENIED);
        }
    }

    private boolean isIncidentOwner(Incident incident, User currentUser) {
        return incident.getService()
                .getNode()
                .getOwner()
                .getId()
                .equals(currentUser.getId());
    }
}