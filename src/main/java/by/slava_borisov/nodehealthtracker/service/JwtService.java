package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.model.entity.User;

import java.time.LocalDateTime;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    LocalDateTime extractIssuedAt(String token);

    boolean isTokenValid(String token, User user);

    Long extractUserId(String token);
}