package by.slava_borisov.nodehealthtracker.dto;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;

public record NotificationSettingResponse(

        Long id,

        Long userId,

        NotificationChannel channel,

        Boolean isEnabled,

        String destination,

        Boolean notifyOnIncidentOpen,

        Boolean notifyOnIncidentResolved
) {
}