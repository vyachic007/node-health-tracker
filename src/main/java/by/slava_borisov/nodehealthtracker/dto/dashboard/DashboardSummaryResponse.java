package by.slava_borisov.nodehealthtracker.dto.dashboard;

import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сводка dashboard текущего пользователя")
public record DashboardSummaryResponse(

        @Schema(description = "Общее количество сетевых узлов пользователя", example = "3")
        long totalNodes,

        @Schema(description = "Количество активных сетевых узлов", example = "2")
        long activeNodes,

        @Schema(description = "Количество неактивных сетевых узлов", example = "1")
        long inactiveNodes,

        @Schema(description = "Общее количество сервисов мониторинга", example = "8")
        long totalServices,

        @Schema(description = "Количество включённых сервисов", example = "6")
        long enabledServices,

        @Schema(description = "Количество отключённых сервисов", example = "2")
        long disabledServices,

        @Schema(description = "Количество доступных сервисов", example = "5")
        long upServices,

        @Schema(description = "Количество недоступных сервисов", example = "1")
        long downServices,

        @Schema(description = "Количество сервисов с неизвестным состоянием", example = "0")
        long unknownServices,

        @Schema(description = "Количество открытых инцидентов", example = "1")
        long openIncidents,

        @Schema(description = "Количество закрытых инцидентов", example = "4")
        long resolvedIncidents,

        @Schema(description = "Количество проверок за последние 24 часа", example = "120")
        long checksLast24Hours,

        @Schema(
                description = "Средняя оценка здоровья включённых сервисов от 0 до 100",
                example = "87",
                minimum = "0",
                maximum = "100",
                nullable = true
        )
        Integer averageHealthScore,

        @Schema(
                description = "Средний уровень здоровья сервисов",
                example = "DEGRADED",
                allowableValues = {
                        "HEALTHY",
                        "DEGRADED",
                        "UNSTABLE",
                        "CRITICAL"
                },
                nullable = true
        )
        HealthLevel averageHealthLevel,

        @Schema(description = "Количество сервисов с уровнем HEALTHY", example = "4")
        long healthyServices,

        @Schema(description = "Количество сервисов с уровнем DEGRADED", example = "1")
        long degradedServices,

        @Schema(description = "Количество сервисов с уровнем UNSTABLE", example = "1")
        long unstableServices,

        @Schema(description = "Количество сервисов с уровнем CRITICAL", example = "0")
        long criticalServices,

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
        Double averageResponseTimeMs24h
) {
}