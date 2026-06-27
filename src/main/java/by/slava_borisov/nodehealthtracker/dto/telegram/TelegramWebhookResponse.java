package by.slava_borisov.nodehealthtracker.dto.telegram;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на обработку Telegram webhook")
public record TelegramWebhookResponse(

        @Schema(
                description = "Результат обработки webhook-запроса",
                example = "true"
        )
        boolean success,

        @Schema(
                description = "Сообщение с результатом обработки",
                example = "Telegram успешно привязан"
        )
        String message
) {
}