package by.slava_borisov.nodehealthtracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные для авторизации пользователя")
public record UserLoginRequest(

        @Schema(
                description = "Имя пользователя",
                example = "network_admin"
        )
        @NotBlank
        String username,

        @Schema(
                description = "Пароль пользователя",
                example = "StrongPassword123",
                format = "password"
        )
        @NotBlank
        String password
) {
}