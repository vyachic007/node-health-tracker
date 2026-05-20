package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;

import java.util.List;

public interface IncidentTimelineService {

    void createEvent(
            Incident incident,
            CheckResult checkResult,
            IncidentTimelineEventType eventType,
            String message
    );

    List<IncidentTimelineEventResponse> getIncidentTimeline(Long incidentId);
}