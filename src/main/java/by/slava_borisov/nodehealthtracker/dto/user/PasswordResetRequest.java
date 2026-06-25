package by.slava_borisov.nodehealthtracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для запроса восстановления пароля")
public record PasswordResetRequest(

        @Schema(
                description = "Адрес электронной почты учётной записи",
                example = "user@example.com",
                maxLength = 255
        )
        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {
}