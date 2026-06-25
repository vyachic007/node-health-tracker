package by.slava_borisov.nodehealthtracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для регистрации нового пользователя")
public record UserRegistrationRequest(

        @Schema(
                description = "Адрес электронной почты пользователя",
                example = "user@example.com",
                maxLength = 255
        )
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(
                description = "Уникальное имя пользователя",
                example = "network_admin",
                maxLength = 100
        )
        @NotBlank
        @Size(max = 100)
        String username,

        @Schema(
                description = "Пароль пользователя",
                example = "StrongPassword123",
                minLength = 6,
                maxLength = 255,
                format = "password"
        )
        @NotBlank
        @Size(min = 6, max = 255)
        String password
) {
}