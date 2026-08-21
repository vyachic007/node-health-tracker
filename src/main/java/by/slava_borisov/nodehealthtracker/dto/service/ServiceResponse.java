package by.slava_borisov.nodehealthtracker.dto.service;

import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сервис мониторинга и его текущее диагностическое состояние")
public record ServiceResponse(

        @Schema(
                description = "Уникальный идентификатор сервиса",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Идентификатор сетевого узла, к которому относится сервис",
                example = "3"
        )
        Long nodeId,

        @Schema(
                description = "Тип проверки сервиса",
                example = "HTTPS"
        )
        CheckType checkType,

        @Schema(
                description = "Дата и время последней выполненной проверки",
                example = "2026-06-26T14:30:00",
                nullable = true
        )
        LocalDateTime lastCheckedAt,

        @Schema(
                description = "Название сервиса",
                example = "Основной API"
        )
        String name,

        @Schema(
                description = "Доменное имя или IP-адрес проверяемого сервиса",
                example = "api.example.com"
        )
        String targetHost,

        @Schema(
                description = "Сетевой порт сервиса",
                example = "443",
                nullable = true
        )
        Integer port,

        @Schema(
                description = "Путь HTTP- или HTTPS-запроса",
                example = "/actuator/health",
                nullable = true
        )
        String path,

        @Schema(
                description = "Интервал между автоматическими проверками в секундах",
                example = "60"
        )
        Integer intervalSeconds,

        @Schema(
                description = "Признак активности автоматических проверок",
                example = "true"
        )
        Boolean isEnabled,

        @Schema(
                description = "Порог времени ответа в миллисекундах",
                example = "1000",
                nullable = true
        )
        Integer responseTimeThresholdMs,

        @Schema(
                description = "Количество последовательных превышений порога для фиксации деградации",
                example = "3",
                nullable = true
        )
        Integer degradationThreshold,

        @Schema(
                description = "Текущее количество последовательных превышений порога времени ответа",
                example = "2"
        )
        Integer consecutiveDegradations,

        @Schema(
                description = "Признак деградированного состояния сервиса",
                example = "true"
        )
        Boolean degraded,

        @Schema(
                description = "Включены ли уведомления по электронной почте",
                example = "true"
        )
        Boolean notifyEmail,

        @Schema(
                description = "Включены ли уведомления в Telegram",
                example = "true"
        )
        Boolean notifyTelegram,

        @Schema(
                description = "Включены ли уведомления во ВКонтакте",
                example = "false"
        )
        Boolean notifyVk,

        @Schema(
                description = "Статус последней проверки",
                example = "UP",
                nullable = true
        )
        ServiceStatus lastStatus,

        @Schema(
                description = "Время ответа последней проверки в миллисекундах",
                example = "145",
                nullable = true
        )
        Integer lastResponseTimeMs,

        @Schema(
                description = "HTTP-код ответа последней проверки",
                example = "200",
                nullable = true
        )
        Integer lastHttpStatusCode,

        @Schema(
                description = "Уровень системы, на котором обнаружена ошибка",
                example = "APPLICATION",
                nullable = true
        )
        FailureLayer lastFailureLayer,

        @Schema(
                description = "Диагностическое описание результата последней проверки",
                example = "Сервис успешно ответил на запрос",
                nullable = true
        )
        String lastDiagnosticMessage,

        @Schema(
                description = "Рекомендация по устранению обнаруженной проблемы",
                example = "Проверьте состояние приложения и журналы сервера",
                nullable = true
        )
        String lastRecommendation,

        @Schema(
                description = "Расчётная дата и время следующей проверки",
                example = "2026-06-26T14:31:00",
                nullable = true
        )
        LocalDateTime nextCheckAt,

        @Schema(
                description = "Количество секунд до следующей проверки",
                example = "42",
                nullable = true
        )
        Long secondsUntilNextCheck,

        @Schema(
                description = "Наличие открытого инцидента",
                example = "false"
        )
        Boolean hasOpenIncident,

        @Schema(
                description = "Идентификатор открытого инцидента",
                example = "15",
                nullable = true
        )
        Long openIncidentId,

        @Schema(
                description = "Продолжительность текущего простоя в секундах",
                example = "0"
        )
        Long currentDowntimeSeconds,

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
                description = "Дата и время создания сервиса",
                example = "2026-06-20T12:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Дата и время последнего изменения сервиса",
                example = "2026-06-26T14:20:00"
        )
        LocalDateTime updatedAt
) {
}