package by.slava_borisov.nodehealthtracker.dto.admin;

import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserBlockRequest(

        @NotNull
        UserStatus status
) {
}