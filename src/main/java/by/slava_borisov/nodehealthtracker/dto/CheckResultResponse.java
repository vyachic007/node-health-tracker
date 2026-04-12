package by.slava_borisov.nodehealthtracker.dto;

import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;

import java.time.LocalDateTime;

public record CheckResultResponse(

        Long id,

        Long serviceId,

        ServiceStatus status,

        LocalDateTime startedAt,

        LocalDateTime finishedAt,

        Integer responseTimeMs,

        Integer httpStatusCode,

        String errorMessage,

        LocalDateTime checkedAt
) {
}