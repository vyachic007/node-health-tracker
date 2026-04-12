package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;

import java.time.LocalDateTime;

public record IncidentResponse(

        Long id,

        Long serviceId,

        IncidentStatus status,

        LocalDateTime openedAt,

        LocalDateTime closedAt,

        String reason,

        Long openedByCheckResultId,

        Long closedByCheckResultId
) {
}