package by.slava_borisov.nodehealthtracker.dto.dashboard;

import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Административная сводка dashboard по всей системе")
public record AdminDashboardSummaryResponse(

        @Schema(description = "Общее количество пользователей в системе", example = "12")
        long totalUsers,

        @Schema(description = "Количество активных пользователей", example = "10")
        long activeUsers,

        @Schema(description = "Количество заблокированных пользователей", example = "2")
        long blockedUsers,

        @Schema(description = "Общее количество сетевых узлов в системе", example = "20")
        long totalNodes,

        @Schema(description = "Количество активных сетевых узлов", example = "17")
        long activeNodes,

        @Schema(description = "Количество неактивных сетевых узлов", example = "3")
        long inactiveNodes,

        @Schema(description = "Общее количество сервисов мониторинга", example = "54")
        long totalServices,

        @Schema(description = "Количество включённых сервисов", example = "47")
        long enabledServices,

        @Schema(description = "Количество отключённых сервисов", example = "7")
        long disabledServices,

        @Schema(description = "Количество доступных сервисов", example = "42")
        long upServices,

        @Schema(description = "Количество недоступных сервисов", example = "3")
        long downServices,

        @Schema(description = "Количество сервисов с неизвестным состоянием", example = "2")
        long unknownServices,

        @Schema(description = "Количество открытых инцидентов", example = "3")
        long openIncidents,

        @Schema(description = "Количество закрытых инцидентов", example = "25")
        long resolvedIncidents,

        @Schema(description = "Количество проверок за последние 24 часа", example = "860")
        long checksLast24Hours,

        @Schema(
                description = "Средняя системная оценка здоровья сервисов от 0 до 100",
                example = "91",
                minimum = "0",
                maximum = "100",
                nullable = true
        )
        Integer averageHealthScore,

        @Schema(
                description = "Средний системный уровень здоровья сервисов",
                example = "HEALTHY",
                allowableValues = {
                        "HEALTHY",
                        "DEGRADED",
                        "UNSTABLE",
                        "CRITICAL"
                },
                nullable = true
        )
        HealthLevel averageHealthLevel,

        @Schema(description = "Количество сервисов с уровнем HEALTHY", example = "47")
        long healthyServices,

        @Schema(description = "Количество сервисов с уровнем DEGRADED", example = "0")
        long degradedServices,

        @Schema(description = "Количество сервисов с уровнем UNSTABLE", example = "0")
        long unstableServices,

        @Schema(description = "Количество сервисов с уровнем CRITICAL", example = "0")
        long criticalServices,

        @Schema(
                description = "Процент успешных проверок за последние 24 часа по всей системе",
                example = "97.35",
                minimum = "0",
                maximum = "100",
                nullable = true
        )
        Double availabilityPercent24h,

        @Schema(
                description = "Среднее время ответа успешных проверок за последние 24 часа по всей системе в миллисекундах",
                example = "188.24",
                nullable = true
        )
        Double averageResponseTimeMs24h
) {
}