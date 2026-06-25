package by.slava_borisov.nodehealthtracker.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для изменения сетевого узла")
public record NodeUpdateRequest(

        @Schema(
                description = "Название сетевого узла",
                example = "Основной сервер",
                maxLength = 150
        )
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(
                description = "Доменное имя или IP-адрес сетевого узла",
                example = "example.com",
                maxLength = 255
        )
        @NotBlank
        @Size(max = 255)
        String host,

        @Schema(
                description = "Дополнительное описание сетевого узла",
                example = "Основной сервер веб-приложения",
                nullable = true
        )
        String description,

        @Schema(
                description = "Признак активности сетевого узла",
                example = "true"
        )
        @NotNull
        Boolean isActive
) {
}