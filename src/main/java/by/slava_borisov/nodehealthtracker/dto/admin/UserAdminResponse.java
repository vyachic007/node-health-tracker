package by.slava_borisov.nodehealthtracker.dto.admin;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;

import java.time.LocalDateTime;

public record UserAdminResponse(

        Long id,

        String email,

        String username,

        UserStatus status,

        RoleName role,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}