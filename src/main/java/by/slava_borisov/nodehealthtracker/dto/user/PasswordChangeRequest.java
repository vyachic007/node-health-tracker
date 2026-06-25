package by.slava_borisov.nodehealthtracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для изменения пароля текущего пользователя")
public record PasswordChangeRequest(

        @Schema(
                description = "Текущий пароль пользователя",
                example = "CurrentPassword123",
                minLength = 6,
                maxLength = 255,
                format = "password"
        )
        @NotBlank
        @Size(min = 6, max = 255)
        String currentPassword,

        @Schema(
                description = "Новый пароль пользователя, который должен отличаться от текущего",
                example = "NewPassword456",
                minLength = 6,
                maxLength = 255,
                format = "password"
        )
        @NotBlank
        @Size(min = 6, max = 255)
        String newPassword
) {
}