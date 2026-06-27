package by.slava_borisov.nodehealthtracker.dto.admin;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Административная информация о пользователе")
public record UserAdminResponse(

        @Schema(
                description = "Уникальный идентификатор пользователя",
                example = "3"
        )
        Long id,

        @Schema(
                description = "Адрес электронной почты пользователя",
                example = "user@example.com"
        )
        String email,

        @Schema(
                description = "Имя пользователя",
                example = "network_admin"
        )
        String username,

        @Schema(
                description = "Текущий статус учётной записи",
                example = "ACTIVE",
                allowableValues = {
                        "ACTIVE",
                        "BLOCKED"
                }
        )
        UserStatus status,

        @Schema(
                description = "Роль пользователя в системе",
                example = "ROLE_USER",
                allowableValues = {
                        "ROLE_USER",
                        "ROLE_ADMIN"
                }
        )
        RoleName role,

        @Schema(
                description = "Дата и время создания учётной записи",
                example = "2026-06-20T12:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Дата и время последнего изменения учётной записи",
                example = "2026-06-26T17:30:00"
        )
        LocalDateTime updatedAt
) {
}