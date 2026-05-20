package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecoveryChecklistResponse;

public interface IncidentRecoveryChecklistService {

    IncidentRecoveryChecklistResponse getRecoveryChecklist(Long incidentId);
}