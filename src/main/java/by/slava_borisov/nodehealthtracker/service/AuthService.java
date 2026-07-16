package by.slava_borisov.nodehealthtracker.service;


import by.slava_borisov.nodehealthtracker.dto.user.AuthResponse;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordChangeRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserLoginRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserRegistrationRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileUpdateRequest;

public interface AuthService {

    UserProfileResponse register(UserRegistrationRequest request);

    AuthResponse login(UserLoginRequest request);

    UserProfileResponse getCurrentUserProfile();

    void changeCurrentUserPassword(PasswordChangeRequest request);

    UserProfileResponse updateCurrentUserProfile(UserProfileUpdateRequest request);
}