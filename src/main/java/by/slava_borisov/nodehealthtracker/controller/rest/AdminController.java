package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.admin.*;
import by.slava_borisov.nodehealthtracker.dto.common.PageResponse;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public PageResponse<UserAdminResponse> getAllUsers(
            @RequestParam(required = false) UserStatus status,
            @RequestParam(name = "role", required = false) RoleName role,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminService.getAllUsers(status, role, query, page, size);
    }

    @GetMapping("/users/summary")
    public UserAdminSummaryResponse getUserSummary() {
        return adminService.getUserSummary();
    }

    @GetMapping("/summary")
    public AdminPlatformSummaryResponse getPlatformSummary() {
        return adminService.getPlatformSummary();
    }

    @GetMapping("/users/{userId}")
    public UserAdminResponse getUserById(@PathVariable Long userId) {
        return adminService.getUserById(userId);
    }

    @PatchMapping("/users/{userId}/status")
    public UserAdminResponse updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserBlockRequest request
    ) {
        return adminService.updateUserStatus(userId, request);
    }

    @PatchMapping("/users/{userId}/role")
    public UserAdminResponse updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return adminService.updateUserRole(userId, request);
    }

    @DeleteMapping("/users/{userId}")
    public UserAdminResponse deleteUser(@PathVariable Long userId) {
        return adminService.deleteUser(userId);
    }
}