package by.slava_borisov.nodehealthtracker.dto.user;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Результат успешной авторизации пользователя")
public record AuthResponse(

        @Schema(
                description = "Уникальный идентификатор пользователя",
                example = "1"
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
                allowableValues = {"ACTIVE", "BLOCKED"}
        )
        UserStatus status,

        @Schema(
                description = "Роль пользователя в системе",
                example = "ROLE_USER",
                allowableValues = {"ROLE_USER", "ROLE_ADMIN"}
        )
        RoleName role,

        @Schema(
                description = "JWT-токен для доступа к защищённым endpoint",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXR3b3JrX2FkbWluIn0.signature"
        )
        String token,

        @Schema(
                description = "Тип токена авторизации",
                example = "Bearer"
        )
        String tokenType
) {
}