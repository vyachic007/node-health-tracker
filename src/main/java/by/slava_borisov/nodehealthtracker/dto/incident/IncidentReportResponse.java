package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentReportResponse(

        Long incidentId,

        Long serviceId,

        String serviceName,

        IncidentStatus status,

        IncidentSeverity severity,

        FailureLayer failureLayer,

        String reason,

        String recommendation,

        LocalDateTime openedAt,

        LocalDateTime closedAt,

        Long durationSeconds,

        Long durationMinutes,

        Long openedByCheckResultId,

        Long closedByCheckResultId,

        Integer timelineEventsCount,

        String summary
) {
}