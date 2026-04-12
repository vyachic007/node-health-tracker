package by.slava_borisov.nodehealthtracker.dto.node;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NodeCreateRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 255)
        String host,

        String description
) {
}