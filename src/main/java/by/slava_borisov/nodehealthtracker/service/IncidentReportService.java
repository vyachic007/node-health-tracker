package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentReportResponse;

public interface IncidentReportService {

    IncidentReportResponse getIncidentReport(Long incidentId);
}