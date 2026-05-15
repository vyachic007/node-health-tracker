package by.slava_borisov.nodehealthtracker.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
        @NotBlank
        @Size(min = 6, max = 255)
        String currentPassword,

        @NotBlank
        @Size(min = 6, max = 255)
        String newPassword
) {
}
