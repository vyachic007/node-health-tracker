package by.slava_borisov.nodehealthtracker.notification.dto;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;

import java.time.LocalDateTime;

public record NotificationMessage(

        NotificationEvent event,

        Long incidentId,

        Long serviceId,

        String serviceName,

        String reason,

        LocalDateTime eventTime
) {
}