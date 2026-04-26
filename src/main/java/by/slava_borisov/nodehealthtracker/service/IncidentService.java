package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;

import java.util.List;

public interface IncidentService {

    IncidentResponse getIncidentById(Long incidentId);

    List<IncidentResponse> getCurrentUserIncidents();

    List<IncidentResponse> getServiceIncidents(Long serviceId);

    IncidentResponse closeIncident(Long incidentId);
}