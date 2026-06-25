package by.slava_borisov.nodehealthtracker.dto.node;

import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.NodeHealthStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Сетевой узел и агрегированные показатели его состояния")
public record NodeResponse(

        @Schema(description = "Уникальный идентификатор узла", example = "1")
        Long id,

        @Schema(description = "Идентификатор владельца узла", example = "3")
        Long ownerId,

        @Schema(description = "Название узла", example = "Основной сервер")
        String name,

        @Schema(
                description = "Доменное имя или IP-адрес узла",
                example = "example.com"
        )
        String host,

        @Schema(
                description = "Описание узла",
                example = "Основной сервер веб-приложения",
                nullable = true
        )
        String description,

        @Schema(description = "Признак активности узла", example = "true")
        Boolean isActive,

        @Schema(
                description = "Обобщённый статус доступности узла",
                example = "UP",
                allowableValues = {"UP", "DOWN", "DEGRADED", "UNKNOWN"}
        )
        NodeHealthStatus healthStatus,

        @Schema(description = "Общее количество сервисов узла", example = "5")
        Long totalServices,

        @Schema(description = "Количество включённых сервисов", example = "4")
        Long enabledServices,

        @Schema(description = "Количество отключённых сервисов", example = "1")
        Long disabledServices,

        @Schema(description = "Количество доступных сервисов", example = "3")
        Long upServices,

        @Schema(description = "Количество недоступных сервисов", example = "1")
        Long downServices,

        @Schema(
                description = "Количество сервисов с неопределённым статусом",
                example = "0"
        )
        Long unknownServices,

        @Schema(description = "Количество открытых инцидентов", example = "1")
        Long openIncidents,

        @Schema(
                description = "Дата и время последней проверки сервисов узла",
                example = "2026-06-25T20:30:00",
                nullable = true
        )
        LocalDateTime lastCheckedAt,

        @Schema(
                description = "Процент успешных проверок сервисов узла за последние 24 часа",
                example = "98.75",
                minimum = "0",
                maximum = "100",
                nullable = true
        )
        Double availabilityPercent24h,

        @Schema(
                description = "Среднее время ответа успешных проверок за последние 24 часа в миллисекундах",
                example = "145.62",
                nullable = true
        )
        Double averageResponseTimeMs24h,

        @Schema(
                description = "Обобщённая оценка здоровья узла от 0 до 100",
                example = "87",
                minimum = "0",
                maximum = "100",
                nullable = true
        )
        Integer healthScore,

        @Schema(
                description = "Уровень здоровья узла",
                example = "DEGRADED",
                allowableValues = {
                        "HEALTHY",
                        "DEGRADED",
                        "UNSTABLE",
                        "CRITICAL"
                },
                nullable = true
        )
        HealthLevel healthLevel,

        @Schema(
                description = "Количество сервисов с уровнем HEALTHY",
                example = "2"
        )
        Long healthyServicesCount,

        @Schema(
                description = "Количество сервисов с уровнем DEGRADED",
                example = "1"
        )
        Long degradedServicesCount,

        @Schema(
                description = "Количество сервисов с уровнем UNSTABLE",
                example = "1"
        )
        Long unstableServicesCount,

        @Schema(
                description = "Количество сервисов с уровнем CRITICAL",
                example = "0"
        )
        Long criticalServicesCount,

        @Schema(
                description = "Дата и время создания узла",
                example = "2026-06-20T12:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Дата и время последнего изменения узла",
                example = "2026-06-25T18:45:00"
        )
        LocalDateTime updatedAt
) {
}