package by.slava_borisov.nodehealthtracker.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Административная сводка по пользователям")
public record UserAdminSummaryResponse(

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
        long regularUsers
) {
}