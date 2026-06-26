package by.slava_borisov.nodehealthtracker.dto.service;

import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Интегральная оценка здоровья сервиса мониторинга")
public record ServiceHealthScoreResponse(

        @Schema(
                description = "Уникальный идентификатор сервиса",
                example = "1"
        )
        Long serviceId,

        @Schema(
                description = "Название сервиса",
                example = "Основной API"
        )
        String serviceName,

        @Schema(
                description = "Статус последней проверки сервиса",
                example = "UP",
                nullable = true
        )
        ServiceStatus lastStatus,

        @Schema(
                description = "Интегральная оценка здоровья сервиса от 0 до 100",
                example = "92",
                minimum = "0",
                maximum = "100"
        )
        Integer healthScore,

        @Schema(
                description = "Уровень здоровья сервиса",
                example = "HEALTHY",
                allowableValues = {
                        "HEALTHY",
                        "DEGRADED",
                        "UNSTABLE",
                        "CRITICAL"
                }
        )
        HealthLevel healthLevel,

        @Schema(
                description = "Процент успешных проверок за последние 24 часа",
                example = "98.75",
                minimum = "0",
                maximum = "100",
                nullable = true
        )
        Double availabilityPercent24h,

        @Schema(
                description = "Среднее время ответа успешных проверок за последние 24 часа в миллисекундах",
                example = "152.48",
                nullable = true
        )
        Double averageResponseTimeMs24h,

        @Schema(
                description = "Наличие открытого инцидента",
                example = "false"
        )
        Boolean hasOpenIncident,

        @Schema(
                description = "Уровень серьёзности открытого инцидента",
                example = "HIGH",
                allowableValues = {
                        "LOW",
                        "MEDIUM",
                        "HIGH",
                        "CRITICAL"
                },
                nullable = true
        )
        IncidentSeverity openIncidentSeverity,

        @Schema(
                description = "Уровень повторяемости однотипных инцидентов за последние 7 дней",
                example = "LOW",
                allowableValues = {
                        "LOW",
                        "MEDIUM",
                        "HIGH"
                }
        )
        RecurrenceLevel recurrenceLevel,

        @Schema(
                description = "Краткое текстовое объяснение состояния здоровья сервиса",
                example = "Сервис работает стабильно, критических проблем не обнаружено"
        )
        String summary
) {
}