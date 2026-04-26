package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.user.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse getUserProfileById(Long userId);
}