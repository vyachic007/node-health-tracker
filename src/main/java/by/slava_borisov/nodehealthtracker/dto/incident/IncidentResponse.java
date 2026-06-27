package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Инцидент, связанный с недоступностью или деградацией сервиса")
public record IncidentResponse(

        @Schema(description = "Уникальный идентификатор инцидента", example = "1")
        Long id,

        @Schema(description = "Идентификатор сервиса, по которому открыт инцидент", example = "5")
        Long serviceId,

        @Schema(description = "Название сервиса", example = "Основной API")
        String serviceName,

        @Schema(
                description = "Текущий статус инцидента",
                example = "OPEN",
                allowableValues = {"OPEN", "RESOLVED"}
        )
        IncidentStatus status,

        @Schema(
                description = "Уровень серьёзности инцидента",
                example = "HIGH",
                allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"}
        )
        IncidentSeverity severity,

        @Schema(description = "Дата и время открытия инцидента", example = "2026-06-26T16:00:00")
        LocalDateTime openedAt,

        @Schema(
                description = "Дата и время закрытия инцидента",
                example = "2026-06-26T16:25:00",
                nullable = true
        )
        LocalDateTime closedAt,

        @Schema(
                description = "Причина открытия инцидента",
                example = "Сервис недоступен после выполнения проверки"
        )
        String reason,

        @Schema(
                description = "Идентификатор результата проверки, из-за которого инцидент был открыт",
                example = "101",
                nullable = true
        )
        Long openedByCheckResultId,

        @Schema(
                description = "Идентификатор результата проверки, из-за которого инцидент был закрыт",
                example = "118",
                nullable = true
        )
        Long closedByCheckResultId
) {
}