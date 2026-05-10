package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.dto.admin.UserRoleUpdateRequest;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;

import java.util.List;

public interface AdminService {

    List<UserAdminResponse> getAllUsers(UserStatus status, RoleName role, String query);

    UserAdminResponse getUserById(Long userId);

    UserAdminResponse updateUserStatus(Long userId, UserBlockRequest request);

    UserAdminResponse updateUserRole(Long userId, UserRoleUpdateRequest request);

    UserAdminResponse deleteUser(Long userId);
}