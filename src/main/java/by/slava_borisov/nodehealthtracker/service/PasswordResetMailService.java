package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.model.entity.User;

import java.time.LocalDateTime;

public interface PasswordResetMailService {

    void sendPasswordResetToken(
            User user,
            String rawToken,
            LocalDateTime expiresAt
    );
}