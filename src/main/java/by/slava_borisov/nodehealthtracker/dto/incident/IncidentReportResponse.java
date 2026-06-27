package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Автоматический отчёт по инциденту")
public record IncidentReportResponse(

        @Schema(description = "Уникальный идентификатор инцидента", example = "10")
        Long incidentId,

        @Schema(description = "Идентификатор сервиса", example = "5")
        Long serviceId,

        @Schema(description = "Название сервиса", example = "Основной API")
        String serviceName,

        @Schema(
                description = "Текущий статус инцидента",
                example = "RESOLVED",
                allowableValues = {"OPEN", "RESOLVED"}
        )
        IncidentStatus status,

        @Schema(
                description = "Уровень серьёзности инцидента",
                example = "HIGH",
                allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"}
        )
        IncidentSeverity severity,

        @Schema(
                description = "Уровень системы, на котором была обнаружена проблема",
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
                description = "Причина открытия инцидента",
                example = "Сервис недоступен после выполнения проверки"
        )
        String reason,

        @Schema(
                description = "Рекомендация по устранению проблемы",
                example = "Проверьте состояние приложения и журналы сервера",
                nullable = true
        )
        String recommendation,

        @Schema(description = "Дата и время открытия инцидента", example = "2026-06-26T16:00:00")
        LocalDateTime openedAt,

        @Schema(
                description = "Дата и время закрытия инцидента",
                example = "2026-06-26T16:25:00",
                nullable = true
        )
        LocalDateTime closedAt,

        @Schema(
                description = "Длительность инцидента в секундах",
                example = "1500"
        )
        Long durationSeconds,

        @Schema(
                description = "Длительность инцидента в минутах",
                example = "25"
        )
        Long durationMinutes,

        @Schema(
                description = "Идентификатор проверки, открывшей инцидент",
                example = "101",
                nullable = true
        )
        Long openedByCheckResultId,

        @Schema(
                description = "Идентификатор проверки, закрывшей инцидент",
                example = "118",
                nullable = true
        )
        Long closedByCheckResultId,

        @Schema(
                description = "Количество событий в timeline инцидента",
                example = "4"
        )
        Integer timelineEventsCount,

        @Schema(
                description = "Краткое резюме отчёта по инциденту",
                example = "Инцидент закрыт. Основная проблема была связана с уровнем приложения."
        )
        String summary
) {
}