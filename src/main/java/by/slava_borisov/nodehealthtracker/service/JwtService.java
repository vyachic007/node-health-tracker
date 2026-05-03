package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.model.entity.User;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, User user);
}