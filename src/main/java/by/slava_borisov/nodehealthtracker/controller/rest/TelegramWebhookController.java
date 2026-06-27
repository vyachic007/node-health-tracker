package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookRequest;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookResponse;
import by.slava_borisov.nodehealthtracker.service.TelegramBindingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Telegram webhook",
        description = """
                Публичный endpoint для обработки сообщений от Telegram-бота. \
                Используется для привязки Telegram-чата к пользователю через bind-token.
                """
)
@RestController
@RequestMapping("/api/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final TelegramBindingService telegramBindingService;

    @Operation(
            summary = "Обработать Telegram webhook",
            description = """
                    Принимает входящее событие от Telegram. Если пользователь отправил \
                    bind-token, система пытается привязать Telegram chatId к его аккаунту.
                    
                    Endpoint публичный и не требует JWT-токена, потому что вызывается \
                    внешней системой Telegram.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Webhook успешно обработан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TelegramWebhookResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "ok": true,
                                              "message": "Telegram успешно привязан"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректное тело webhook-запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке Telegram webhook",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/webhook")
    public TelegramWebhookResponse handleWebhook(@RequestBody TelegramWebhookRequest request) {
        return telegramBindingService.handleTelegramWebhook(request);
    }
}