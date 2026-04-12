package by.slava_borisov.nodehealthtracker.dto.notification;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;

import java.time.LocalDateTime;

public record SentNotificationResponse(

        Long id,

        Long userId,

        Long incidentId,

        NotificationChannel channel,

        NotificationEvent event,

        LocalDateTime sentAt,

        String status,

        String errorMessage
) {
}