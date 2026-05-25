package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.VkProperties;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/vk")
@RequiredArgsConstructor
public class VkWebhookController {

    private static final String TYPE_CONFIRMATION = "confirmation";
    private static final String TYPE_MESSAGE_NEW = "message_new";
    private static final String OK_RESPONSE = "ok";
    private static final String START_COMMAND_PREFIX = "/start ";

    private final VkProperties vkProperties;
    private final NotificationService notificationService;

    @PostMapping("/webhook")
    public String handleWebhook(@RequestBody Map<String, Object> payload) {
        String type = Objects.toString(payload.get("type"), "");

        log.info("Получен VK webhook: type={}", type);

        if (TYPE_CONFIRMATION.equals(type)) {
            return handleConfirmation();
        }

        if (!isSecretValid(payload)) {
            log.warn("VK webhook отклонён: неверный secret");
            return OK_RESPONSE;
        }

        if (TYPE_MESSAGE_NEW.equals(type)) {
            handleMessageNew(payload);
            return OK_RESPONSE;
        }

        log.info("VK webhook проигнорирован: unsupportedType={}", type);

        return OK_RESPONSE;
    }

    private String handleConfirmation() {
        log.info("VK Callback API confirmation запрошен");

        return vkProperties.getConfirmationCode();
    }

    private boolean isSecretValid(Map<String, Object> payload) {
        String configuredSecret = vkProperties.getCallbackSecret();

        if (configuredSecret == null || configuredSecret.isBlank()) {
            return true;
        }

        String receivedSecret = Objects.toString(payload.get("secret"), "");

        return configuredSecret.equals(receivedSecret);
    }

    @SuppressWarnings("unchecked")
    private void handleMessageNew(Map<String, Object> payload) {
        Object objectPayload = payload.get("object");

        if (!(objectPayload instanceof Map<?, ?> rawObject)) {
            log.warn("VK message_new без object");
            return;
        }

        Object messagePayload = rawObject.get("message");

        if (!(messagePayload instanceof Map<?, ?> rawMessage)) {
            log.warn("VK message_new без message");
            return;
        }

        Map<String, Object> message = (Map<String, Object>) rawMessage;

        String text = Objects.toString(message.get("text"), "").trim();
        String peerId = Objects.toString(message.get("peer_id"), "");

        log.info(
                "Получено VK-сообщение: peerId={}, text={}",
                peerId,
                text
        );

        if (!text.startsWith(START_COMMAND_PREFIX)) {
            log.info("VK-сообщение проигнорировано: команда не является /start");
            return;
        }

        String bindToken = text.substring(START_COMMAND_PREFIX.length()).trim();

        if (bindToken.isBlank()) {
            log.warn("VK /start получен без bindToken");
            return;
        }

        notificationService.connectVkByBindToken(bindToken, peerId);

        log.info("VK успешно привязан через webhook: peerId={}", peerId);
    }
}
