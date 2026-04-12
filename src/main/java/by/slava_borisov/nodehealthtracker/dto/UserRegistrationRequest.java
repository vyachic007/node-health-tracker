package by.slava_borisov.nodehealthtracker.dto;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Size(min = 6, max = 255)
        String password,

        @NotNull
        RoleName role
) {
}