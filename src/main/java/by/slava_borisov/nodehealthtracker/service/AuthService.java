package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.user.AuthResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserLoginRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserRegistrationRequest;

public interface AuthService {

    UserProfileResponse register(UserRegistrationRequest request);

    AuthResponse login(UserLoginRequest request);

    UserProfileResponse getCurrentUserProfile();
}