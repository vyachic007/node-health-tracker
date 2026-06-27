package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Событие timeline инцидента")
public record IncidentTimelineEventResponse(

        @Schema(
                description = "Уникальный идентификатор события timeline",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Идентификатор инцидента",
                example = "10"
        )
        Long incidentId,

        @Schema(
                description = "Идентификатор результата проверки, связанного с событием",
                example = "125",
                nullable = true
        )
        Long checkResultId,

        @Schema(
                description = "Тип события timeline",
                example = "INCIDENT_OPENED"
        )
        IncidentTimelineEventType eventType,

        @Schema(
                description = "Текстовое описание события",
                example = "Инцидент открыт после неуспешной проверки сервиса"
        )
        String message,

        @Schema(
                description = "Дата и время создания события timeline",
                example = "2026-06-26T16:00:00"
        )
        LocalDateTime createdAt
) {
}