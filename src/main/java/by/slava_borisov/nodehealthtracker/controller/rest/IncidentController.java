package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
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

    @GetMapping("/{incidentId}")
    public IncidentResponse getIncidentById(@PathVariable Long incidentId) {
        return incidentService.getIncidentById(incidentId);
    }

    @GetMapping("/{incidentId}/timeline")
    public List<IncidentTimelineEventResponse> getIncidentTimeline(@PathVariable Long incidentId) {
        return incidentTimelineService.getIncidentTimeline(incidentId);
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