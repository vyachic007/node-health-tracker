package by.slava_borisov.nodehealthtracker.dto.notification;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationSettingRequest(

        @NotNull
        NotificationChannel channel,

        @NotNull
        Boolean isEnabled,

        @Size(max = 255)
        String destination,

        @NotNull
        Boolean notificationOnIncidentOpen,

        @NotNull
        Boolean notifyOnIncidentResolved
) {
}