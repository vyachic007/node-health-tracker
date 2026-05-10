package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserAdminResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public UserAdminResponse getUserById(Long userId) {
        return adminService.getUserById(userId);
    }

    @PatchMapping("/users/{userId}/status")
    public UserAdminResponse updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserBlockRequest request
    ) {
        return adminService.updateUserStatus(userId, request);
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
    }
}