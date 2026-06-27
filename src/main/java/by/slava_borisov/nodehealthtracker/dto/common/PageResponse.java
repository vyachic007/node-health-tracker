package by.slava_borisov.nodehealthtracker.dto.common;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Страница данных с информацией о пагинации")
public record PageResponse<T>(

        @ArraySchema(
                schema = @Schema(description = "Элемент страницы")
        )
        List<T> content,

        @Schema(
                description = "Номер текущей страницы, начиная с 0",
                example = "0"
        )
        int page,

        @Schema(
                description = "Размер страницы",
                example = "20"
        )
        int size,

        @Schema(
                description = "Общее количество элементов",
                example = "125"
        )
        long totalElements,

        @Schema(
                description = "Общее количество страниц",
                example = "7"
        )
        int totalPages
) {
}