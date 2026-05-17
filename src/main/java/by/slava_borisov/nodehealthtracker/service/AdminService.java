package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.admin.AdminPlatformSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.dto.admin.UserRoleUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.common.PageResponse;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;

public interface AdminService {

    PageResponse<UserAdminResponse> getAllUsers(
            UserStatus status,
            RoleName role,
            String query,
            int page,
            int size
    );

    UserAdminSummaryResponse getUserSummary();

    AdminPlatformSummaryResponse getPlatformSummary();

    UserAdminResponse getUserById(Long userId);

    UserAdminResponse updateUserStatus(Long userId, UserBlockRequest request);

    UserAdminResponse updateUserRole(Long userId, UserRoleUpdateRequest request);

    UserAdminResponse deleteUser(Long userId);
}