package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.user.*;

public interface AuthService {

    UserProfileResponse register(UserRegistrationRequest request);

    AuthResponse login(UserLoginRequest request);

    UserProfileResponse getCurrentUserProfile();

    void changeCurrentUserPassword(PasswordChangeRequest request);
}