package by.slava_borisov.nodehealthtracker.dto;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationSettingCreateRequest(

        @NotNull
        NotificationChannel channel,

        @NotNull
        Boolean isEnabled,

        @Size(max = 255)
        String destination,

        @NotNull
        Boolean notifyOnIncidentOpen,

        @NotNull
        Boolean notifyOnIncidentResolved
) {
}