package by.slava_borisov.nodehealthtracker.controller.webhook;

import by.slava_borisov.nodehealthtracker.config.VkProperties;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/vk")
@RequiredArgsConstructor
public class VkWebhookController {

    private static final String CALLBACK_CONFIRMATION_TYPE = "confirmation";
    private static final String CALLBACK_MESSAGE_NEW_TYPE = "message_new";
    private static final String START_COMMAND_PREFIX = "/start ";

    private final VkProperties vkProperties;
    private final NotificationService notificationService;

    @PostMapping("/webhook")
    public String handleVkCallback(@RequestBody JsonNode body) {
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