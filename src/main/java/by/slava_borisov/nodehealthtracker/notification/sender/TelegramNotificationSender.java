package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TelegramNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.TELEGRAM;
    }

    @Override
    public void send(NotificationMessage message, UserNotificationSetting setting) {
        log.info(
                "TELEGRAM notification sent to chatId {}. Event: {}, incidentId: {}, serviceName: {}, reason: {}",
                setting.getDestination(),
                message.event(),
                message.incidentId(),
                message.serviceName(),
                message.reason()
        );
    }
}