package by.slava_borisov.nodehealthtracker.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Административная платформенная сводка")
public record AdminPlatformSummaryResponse(

        @Schema(
                description = "Общее количество пользователей",
                example = "12"
        )
        long totalUsers,

        @Schema(
                description = "Количество активных пользователей",
                example = "10"
        )
        long activeUsers,

        @Schema(
                description = "Количество заблокированных пользователей",
                example = "2"
        )
        long blockedUsers,

        @Schema(
                description = "Количество пользователей с ролью администратора",
                example = "1"
        )
        long adminUsers,

        @Schema(
                description = "Количество обычных пользователей",
                example = "11"
        )
        long regularUsers,

        @Schema(
                description = "Общее количество сетевых узлов",
                example = "20"
        )
        long totalNodes,

        @Schema(
                description = "Общее количество сервисов мониторинга",
                example = "54"
        )
        long totalServices,

        @Schema(
                description = "Количество включённых сервисов",
                example = "47"
        )
        long enabledServices,

        @Schema(
                description = "Количество отключённых сервисов",
                example = "7"
        )
        long disabledServices,

        @Schema(
                description = "Количество доступных сервисов",
                example = "42"
        )
        long upServices,

        @Schema(
                description = "Количество недоступных сервисов",
                example = "3"
        )
        long downServices,

        @Schema(
                description = "Количество открытых инцидентов",
                example = "3"
        )
        long openIncidents,

        @Schema(
                description = "Количество закрытых инцидентов",
                example = "25"
        )
        long resolvedIncidents,

        @Schema(
                description = "Количество проверок за последние 24 часа",
                example = "860"
        )
        long checksLast24Hours
) {
}