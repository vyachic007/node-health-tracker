package by.slava_borisov.nodehealthtracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для подтверждения восстановления пароля")
public record PasswordResetConfirmRequest(

        @Schema(
                description = "Одноразовый токен восстановления пароля",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotBlank
        String token,

        @Schema(
                description = "Новый пароль пользователя",
                example = "NewStrongPassword123",
                minLength = 8,
                maxLength = 100,
                format = "password"
        )
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {
}