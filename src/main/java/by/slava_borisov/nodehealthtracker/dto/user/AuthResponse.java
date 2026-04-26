package by.slava_borisov.nodehealthtracker.dto.user;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;

public record AuthResponse(
        Long userId,
        String username,
        RoleName role,
        String token
) { }