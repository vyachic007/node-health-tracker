package by.slava_borisov.nodehealthtracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NotificationSettingUpdateRequest(

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