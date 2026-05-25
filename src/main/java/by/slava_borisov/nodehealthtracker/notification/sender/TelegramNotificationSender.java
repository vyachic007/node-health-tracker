package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.config.TelegramProperties;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import by.slava_borisov.nodehealthtracker.util.DateTimeUtils;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramNotificationSender implements NotificationSender {

    private static final String SEND_MESSAGE_PATH_TEMPLATE = "/bot%s/sendMessage";

    private final TelegramProperties telegramProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.TELEGRAM;
    }

    @Override
    public void send(
            NotificationMessage message,
            UserNotificationSetting setting
    ) {
        validateConfiguration(setting);

        String text = buildText(message);

        log.info(
                "Отправка Telegram-уведомления: incidentId={}, serviceId={}, event={}, chatId={}",
                message.incidentId(),
                message.serviceId(),
                message.event(),
                setting.getDestination()
        );

        RestClient restClient = RestClient.create(telegramProperties.getApiUrl());

        restClient.post()
                .uri(String.format(SEND_MESSAGE_PATH_TEMPLATE, telegramProperties.getBotToken()))
                .body(Map.of(
                        "chat_id", setting.getDestination(),
                        "text", text
                ))
                .retrieve()
                .body(String.class);

        log.info(
                "Telegram-уведомление отправлено: incidentId={}, serviceId={}, event={}, chatId={}",
                message.incidentId(),
                message.serviceId(),
                message.event(),
                setting.getDestination()
        );
    }

    private void validateConfiguration(UserNotificationSetting setting) {
        if (telegramProperties.getBotToken() == null
                || telegramProperties.getBotToken().isBlank()) {
            throw new InvalidOperationException(Messages.TELEGRAM_BOT_TOKEN_NOT_CONFIGURED);
        }

        if (setting.getDestination() == null
                || setting.getDestination().isBlank()) {
            throw new InvalidOperationException(Messages.TELEGRAM_DESTINATION_NOT_CONFIGURED);
        }
    }

    private String buildText(NotificationMessage message) {
        String title = switch (message.event()) {
            case INCIDENT_OPENED -> Messages.TELEGRAM_INCIDENT_OPENED_TITLE;
            case INCIDENT_RESOLVED -> Messages.TELEGRAM_INCIDENT_RESOLVED_TITLE;
        };

        return Messages.TELEGRAM_NOTIFICATION_TEXT.formatted(
                title,
                message.serviceName(),
                message.checkType(),
                formatNullable(message.targetHost()),
                formatNullable(message.port()),
                formatNullable(message.path()),
                message.serviceId(),
                message.incidentId(),
                message.reason(),
                DateTimeUtils.formatMoscowDateTime(message.eventTime())
        );
    }

    private String formatNullable(Object value) {
        if (value == null) {
            return "-";
        }

        return value.toString();
    }
}