package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.config.VkProperties;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class VkNotificationSender implements NotificationSender {

    private static final String SEND_MESSAGE_PATH = "/messages.send";

    private final VkProperties vkProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.VK;
    }

    @Override
    public void send(
            NotificationMessage message,
            UserNotificationSetting setting
    ) {
        validateConfiguration(setting);

        String text = buildText(message);

        log.info(
                "Отправка VK-уведомления: incidentId={}, serviceId={}, event={}, peerId={}",
                message.incidentId(),
                message.serviceId(),
                message.event(),
                setting.getDestination()
        );

        RestClient restClient = RestClient.create(vkProperties.getApiUrl());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("peer_id", setting.getDestination());
        formData.add("message", text);
        formData.add("random_id", String.valueOf(generateRandomId()));
        formData.add("access_token", vkProperties.getAccessToken());
        formData.add("v", vkProperties.getApiVersion());

        String response = restClient.post()
                .uri(SEND_MESSAGE_PATH)
                .body(formData)
                .retrieve()
                .body(String.class);

        log.info(
                "VK-уведомление отправлено: incidentId={}, serviceId={}, event={}, peerId={}, response={}",
                message.incidentId(),
                message.serviceId(),
                message.event(),
                setting.getDestination(),
                response
        );
    }

    private void validateConfiguration(UserNotificationSetting setting) {
        if (vkProperties.getAccessToken() == null
                || vkProperties.getAccessToken().isBlank()) {
            throw new InvalidOperationException(Messages.VK_ACCESS_TOKEN_NOT_CONFIGURED);
        }

        if (setting.getDestination() == null
                || setting.getDestination().isBlank()) {
            throw new InvalidOperationException(Messages.VK_DESTINATION_NOT_CONFIGURED);
        }
    }

    private String buildText(NotificationMessage message) {
        String title = switch (message.event()) {
            case INCIDENT_OPENED -> Messages.VK_INCIDENT_OPENED_TITLE;
            case INCIDENT_RESOLVED -> Messages.VK_INCIDENT_RESOLVED_TITLE;
        };

        return Messages.VK_NOTIFICATION_TEXT.formatted(
                title,
                message.serviceName(),
                message.checkType(),
                formatNullable(message.targetHost()),
                formatNullable(message.port()),
                formatNullable(message.path()),
                message.serviceId(),
                message.incidentId(),
                message.reason(),
                formatEventDate(message),
                formatEventTime(message)
        );
    }

    private long generateRandomId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }


    private String formatEventDate(NotificationMessage message) {
        if (message.eventTime() == null) {
            return "-";
        }

        return message.eventTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
        );
    }

    private String formatEventTime(NotificationMessage message) {
        if (message.eventTime() == null) {
            return "-";
        }

        return message.eventTime().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
    }

    private String formatNullable(Object value) {
        if (value == null) {
            return "-";
        }

        return value.toString();
    }
}