package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecoveryChecklistResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecurrenceAnalysisResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentReportResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
import by.slava_borisov.nodehealthtracker.service.IncidentRecoveryChecklistService;
import by.slava_borisov.nodehealthtracker.service.IncidentRecurrenceAnalysisService;
import by.slava_borisov.nodehealthtracker.service.IncidentReportService;
import by.slava_borisov.nodehealthtracker.service.IncidentService;
import by.slava_borisov.nodehealthtracker.service.IncidentTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentTimelineService incidentTimelineService;
    private final IncidentRecoveryChecklistService incidentRecoveryChecklistService;
    private final IncidentReportService incidentReportService;
    private final IncidentRecurrenceAnalysisService incidentRecurrenceAnalysisService;

    @GetMapping("/{incidentId}")
    public IncidentResponse getIncidentById(@PathVariable Long incidentId) {
        return incidentService.getIncidentById(incidentId);
    }

    @GetMapping("/{incidentId}/timeline")
    public List<IncidentTimelineEventResponse> getIncidentTimeline(@PathVariable Long incidentId) {
        return incidentTimelineService.getIncidentTimeline(incidentId);
    }

    @GetMapping("/{incidentId}/recovery-checklist")
    public IncidentRecoveryChecklistResponse getRecoveryChecklist(@PathVariable Long incidentId) {
        return incidentRecoveryChecklistService.getRecoveryChecklist(incidentId);
    }

    @GetMapping("/{incidentId}/report")
    public IncidentReportResponse getIncidentReport(@PathVariable Long incidentId) {
        return incidentReportService.getIncidentReport(incidentId);
    }

    @GetMapping("/{incidentId}/recurrence-analysis")
    public IncidentRecurrenceAnalysisResponse getIncidentRecurrenceAnalysis(@PathVariable Long incidentId) {
        return incidentRecurrenceAnalysisService.analyzeRecurrence(incidentId);
    }

    @GetMapping("/my")
    public List<IncidentResponse> getCurrentUserIncidents() {
        return incidentService.getCurrentUserIncidents();
    }

    @GetMapping("/services/{serviceId}")
    public List<IncidentResponse> getServiceIncidents(@PathVariable Long serviceId) {
        return incidentService.getServiceIncidents(serviceId);
    }

    @PostMapping("/{incidentId}/close")
    public IncidentResponse closeIncident(@PathVariable Long incidentId) {
        return incidentService.closeIncident(incidentId);
    }
}