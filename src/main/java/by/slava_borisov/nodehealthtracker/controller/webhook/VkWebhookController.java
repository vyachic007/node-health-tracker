package by.slava_borisov.nodehealthtracker.controller.webhook;

import by.slava_borisov.nodehealthtracker.config.VkProperties;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@Slf4j
@Tag(
        name = "VK webhook",
        description = """
                Публичный endpoint для обработки callback-событий от VK. \
                Используется для подтверждения callback-сервера и привязки VK peerId \
                к пользователю через bind-token.
                """
)
@RestController
@RequestMapping("/api/vk")
@RequiredArgsConstructor
public class VkWebhookController {

    private static final String CALLBACK_CONFIRMATION_TYPE = "confirmation";
    private static final String CALLBACK_MESSAGE_NEW_TYPE = "message_new";
    private static final String START_COMMAND_PREFIX = "/start ";

    private final VkProperties vkProperties;
    private final NotificationService notificationService;

    @Operation(
            summary = "Обработать VK callback",
            description = """
                    Принимает callback-событие от VK.
                    
                    Если type = confirmation, endpoint возвращает confirmation code \
                    для подтверждения callback-сервера.
                    
                    Если type = message_new, endpoint проверяет secret, извлекает текст \
                    сообщения и peer_id. При команде /start {bindToken} выполняется \
                    привязка VK к пользователю.
                    
                    Endpoint публичный и не требует JWT-токена, потому что вызывается \
                    внешней системой VK.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = """
                            Callback успешно обработан. Для confirmation возвращается confirmation code, \
                            для остальных корректных событий возвращается ok.
                            """,
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(
                                    implementation = String.class,
                                    example = "ok"
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "Обычный callback",
                                            value = "ok"
                                    ),
                                    @ExampleObject(
                                            name = "Confirmation callback",
                                            value = "vk_confirmation_code"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректное тело VK callback-запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке VK callback",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/webhook")
    public String handleVkCallback(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Callback-запрос от VK",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Подтверждение callback-сервера",
                                            value = """
                                                    {
                                                      "type": "confirmation",
                                                      "group_id": 123456
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Новое сообщение с bind-token",
                                            value = """
                                                    {
                                                      "type": "message_new",
                                                      "secret": "callback-secret",
                                                      "object": {
                                                        "message": {
                                                          "peer_id": 123456789,
                                                          "text": "/start vk_bind_abc123"
                                                        }
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @RequestBody JsonNode body
    ) {
        String type = body.path("type").asText();

        log.info("Получен VK callback: type={}", type);

        if (CALLBACK_CONFIRMATION_TYPE.equals(type)) {
            return vkProperties.getConfirmationCode();
        }

        if (CALLBACK_MESSAGE_NEW_TYPE.equals(type)) {
            handleMessageNew(body);
            return "ok";
        }

        return "ok";
    }

    private void handleMessageNew(JsonNode body) {
        String secret = body.path("secret").asText(null);

        if (vkProperties.getCallbackSecret() != null
                && !vkProperties.getCallbackSecret().isBlank()
                && !vkProperties.getCallbackSecret().equals(secret)) {
            log.warn("VK callback отклонён: неверный secret");
            return;
        }

        JsonNode message = body.path("object").path("message");

        String text = message.path("text").asText("").trim();
        long peerId = message.path("peer_id").asLong(0);

        log.info(
                "Получено VK-сообщение: peerId={}, text={}",
                peerId,
                text
        );

        if (peerId <= 0) {
            log.warn("VK-сообщение не обработано: peer_id отсутствует");
            return;
        }

        if (!text.startsWith(START_COMMAND_PREFIX)) {
            log.info("VK-сообщение не является командой привязки");
            return;
        }

        String bindToken = text.substring(START_COMMAND_PREFIX.length()).trim();

        if (bindToken.isBlank()) {
            log.warn("VK-команда привязки без токена");
            return;
        }

        notificationService.connectVkByBindToken(bindToken, String.valueOf(peerId));
    }
}