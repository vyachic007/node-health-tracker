package by.slava_borisov.nodehealthtracker.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Информация об ошибке обработки HTTP-запроса")
public record ApiErrorResponse(

        @Schema(description = "Дата и время возникновения ошибки")
        LocalDateTime timestamp,

        @Schema(description = "HTTP-код ошибки")
        int status,

        @Schema(description = "Стандартное название HTTP-ошибки")
        String error,

        @Schema(description = "Описание причины ошибки")
        String message,

        @Schema(description = "Адрес API-endpoint, при обработке которого возникла ошибка")
        String path
) {
}