package by.slava_borisov.nodehealthtracker.dto.check;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Результат выполнения проверки сервиса мониторинга")
public record CheckResultResponse(

        @Schema(
                description = "Уникальный идентификатор результата проверки",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Идентификатор проверенного сервиса",
                example = "5"
        )
        Long serviceId,

        @Schema(
                description = "Итоговый статус проверки сервиса",
                example = "UP",
                allowableValues = {
                        "UP",
                        "DOWN"
                }
        )
        ServiceStatus status,

        @Schema(
                description = "Уровень системы, на котором обнаружена проблема",
                example = "APPLICATION",
                nullable = true
        )
        FailureLayer failureLayer,

        @Schema(
                description = "Диагностическое описание результата проверки",
                example = "Сервис успешно ответил на запрос"
        )
        String diagnosticMessage,

        @Schema(
                description = "Рекомендация по устранению обнаруженной проблемы",
                example = "Проверьте состояние приложения и журналы сервера",
                nullable = true
        )
        String recommendation,

        @Schema(
                description = "Дата и время начала проверки",
                example = "2026-06-26T15:20:00"
        )
        LocalDateTime startedAt,

        @Schema(
                description = "Дата и время завершения проверки",
                example = "2026-06-26T15:20:01"
        )
        LocalDateTime finishedAt,

        @Schema(
                description = "Время выполнения проверки в миллисекундах",
                example = "145"
        )
        Integer responseTimeMs,

        @Schema(
                description = "HTTP-код ответа, если выполнялась HTTP- или HTTPS-проверка",
                example = "200",
                nullable = true
        )
        Integer httpStatusCode,

        @Schema(
                description = "Текст технической ошибки, если проверка завершилась с ошибкой",
                example = "Connection timed out",
                nullable = true
        )
        String errorMessage,

        @Schema(
                description = "Дата и время фиксации результата проверки",
                example = "2026-06-26T15:20:01"
        )
        LocalDateTime checkedAt
) {
}