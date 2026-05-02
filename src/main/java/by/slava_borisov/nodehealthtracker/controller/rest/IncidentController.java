package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("{incidentId}")
    public IncidentResponse getIncidentById(@PathVariable Long incidentId) {
        return incidentService.getIncidentById(incidentId);
    }

    @GetMapping("/my")
    public List<IncidentResponse> getCurrentUserIncidents() {
        return incidentService.getCurrentUserIncidents();
    }

    @GetMapping("{serviceId}")
    public List<IncidentResponse> getServiceIncidents(@PathVariable Long serviceId) {
        return incidentService.getServiceIncidents(serviceId);
    }

    @PostMapping("{incidentId}")
    public IncidentResponse closeIncident(@PathVariable Long incidentId) {
        return incidentService.closeIncident(incidentId);
    }
}
