package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.user.*;
import by.slava_borisov.nodehealthtracker.service.AuthService;
import by.slava_borisov.nodehealthtracker.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/me")
    public UserProfileResponse getCurrentUserProfile() {
        return authService.getCurrentUserProfile();
    }

    @PatchMapping("/me")
    public UserProfileResponse updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return authService.updateCurrentUserProfile(request);
    }

    @PatchMapping("/me/password")
    public void changeCurrentUserPassword(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        authService.changeCurrentUserPassword(request);
    }

    @PostMapping("/register")
    public UserProfileResponse register(
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        passwordResetService.requestPasswordReset(request);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmPasswordReset(request);

        return ResponseEntity.noContent().build();
    }
}