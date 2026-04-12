package by.slava_borisov.nodehealthtracker.dto;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;

import java.time.LocalDateTime;

public record UserProfileResponse(

        Long id,

        String email,

        String username,

        UserStatus status,

        RoleName role,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}