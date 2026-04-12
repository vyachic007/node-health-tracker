package by.slava_borisov.nodehealthtracker.dto.service;

import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceCreateRequest(

        @NotNull
        Long nodeId,

        @NotNull
        CheckType checkType,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotBlank
        @Size(max = 255)
        String targetHost,

        Integer port,

        @Size(max = 500)
        String path,

        @NotNull
        Integer intervalSeconds
) {
}