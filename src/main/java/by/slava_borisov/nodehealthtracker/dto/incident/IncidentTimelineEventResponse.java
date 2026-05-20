package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;

import java.time.LocalDateTime;

public record IncidentTimelineEventResponse(

        Long id,

        Long incidentId,

        Long checkResultId,

        IncidentTimelineEventType eventType,

        String message,

        LocalDateTime createdAt
) {
}