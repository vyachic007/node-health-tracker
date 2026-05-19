package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;

public interface NotificationSender {

    NotificationChannel getChannel();

    void send(NotificationMessage message, UserNotificationSetting setting);
}