package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecurrenceAnalysisResponse;

public interface IncidentRecurrenceAnalysisService {

    IncidentRecurrenceAnalysisResponse analyzeRecurrence(Long incidentId);
}