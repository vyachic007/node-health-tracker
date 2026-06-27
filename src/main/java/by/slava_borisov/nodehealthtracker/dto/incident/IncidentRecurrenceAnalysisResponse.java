package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Анализ повторяемости однотипных инцидентов")
public record IncidentRecurrenceAnalysisResponse(

        @Schema(description = "Уникальный идентификатор инцидента", example = "10")
        Long incidentId,

        @Schema(description = "Идентификатор сервиса", example = "5")
        Long serviceId,

        @Schema(description = "Название сервиса", example = "Основной API")
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
                description = "Количество похожих инцидентов за последние 24 часа",
                example = "1"
        )
        Long similarIncidentsLast24h,

        @Schema(
                description = "Количество похожих инцидентов за последние 7 дней",
                example = "3"
        )
        Long similarIncidentsLast7d,

        @Schema(
                description = "Количество похожих инцидентов за последние 30 дней",
                example = "7"
        )
        Long similarIncidentsLast30d,

        @Schema(
                description = "Признак повторяющейся проблемы",
                example = "true"
        )
        Boolean isRecurring,

        @Schema(
                description = "Уровень повторяемости проблемы",
                example = "MEDIUM",
                allowableValues = {
                        "LOW",
                        "MEDIUM",
                        "HIGH"
                }
        )
        RecurrenceLevel recurrenceLevel,

        @Schema(
                description = "Рекомендация по работе с повторяющейся проблемой",
                example = "Проблема повторяется. Рекомендуется провести анализ конфигурации сервиса и инфраструктуры."
        )
        String recommendation
) {
}