package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Recovery checklist для восстановления после инцидента")
public record IncidentRecoveryChecklistResponse(

        @Schema(
                description = "Уникальный идентификатор инцидента",
                example = "10"
        )
        Long incidentId,

        @Schema(
                description = "Идентификатор сервиса, по которому открыт инцидент",
                example = "5"
        )
        Long serviceId,

        @Schema(
                description = "Название сервиса",
                example = "Основной API"
        )
        String serviceName,

        @Schema(
                description = "Уровень системы, на котором обнаружена проблема",
                example = "APPLICATION",
                allowableValues = {
                        "DNS",
                        "NETWORK",
                        "PORT",
                        "SSL",
                        "APPLICATION",
                        "PERFORMANCE",
                        "HEARTBEAT",
                        "UNKNOWN"
                }
        )
        FailureLayer failureLayer,

        @Schema(
                description = "Уровень серьёзности инцидента",
                example = "HIGH",
                allowableValues = {
                        "LOW",
                        "MEDIUM",
                        "HIGH",
                        "CRITICAL"
                }
        )
        IncidentSeverity severity,

        @Schema(
                description = "Краткое описание направления восстановления",
                example = "Проблема связана с уровнем приложения. Проверьте состояние сервиса, логи и конфигурацию."
        )
        String summary,

        @ArraySchema(
                schema = @Schema(implementation = RecoveryChecklistItemResponse.class)
        )
        List<RecoveryChecklistItemResponse> items
) {
}