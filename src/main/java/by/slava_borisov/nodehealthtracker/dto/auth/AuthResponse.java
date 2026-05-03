package by.slava_borisov.nodehealthtracker.dto.auth;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;

public record AuthResponse(

        Long id,

        String email,

        String username,

        UserStatus status,

        RoleName role,

        String token,

        String tokenType
) {
}