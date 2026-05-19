package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message, UserNotificationSetting setting) {
        log.info(
                "EMAIL notification sent to {}. Event: {}, incidentId: {}, serviceName: {}, reason: {}",
                setting.getDestination(),
                message.event(),
                message.incidentId(),
                message.serviceName(),
                message.reason()
        );
    }
}