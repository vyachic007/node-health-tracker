package by.slava_borisov.nodehealthtracker.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(

        @NotBlank
        String username,

        @NotBlank
        String password
) {
}