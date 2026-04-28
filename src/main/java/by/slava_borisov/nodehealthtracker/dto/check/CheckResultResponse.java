package by.slava_borisov.nodehealthtracker.dto.check;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;

import java.time.LocalDateTime;

public record CheckResultResponse(

        Long id,

        Long serviceId,

        ServiceStatus status,

        FailureLayer failureLayer,

        String diagnosticMessage,

        String recommendation,

        LocalDateTime startedAt,

        LocalDateTime finishedAt,

        Integer responseTimeMs,

        Integer httpStatusCode,

        String errorMessage,

        LocalDateTime checkedAt
) {
}