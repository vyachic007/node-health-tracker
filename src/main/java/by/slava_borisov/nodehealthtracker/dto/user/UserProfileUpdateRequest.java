package by.slava_borisov.nodehealthtracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для изменения профиля текущего пользователя")
public record UserProfileUpdateRequest(

        @Schema(
                description = "Новый адрес электронной почты пользователя",
                example = "updated.user@example.com",
                maxLength = 255
        )
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(
                description = "Новое уникальное имя пользователя",
                example = "updated_network_user",
                maxLength = 100
        )
        @NotBlank
        @Size(max = 100)
        String username
) {
}