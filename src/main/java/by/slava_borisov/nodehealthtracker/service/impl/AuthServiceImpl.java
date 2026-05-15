package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.user.*;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.InvalidCredentialsException;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.UserAlreadyExistsException;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.AuthService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.JwtService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public UserProfileResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(Messages.USER_EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException(Messages.USERNAME_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(RoleName.ROLE_USER);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setPasswordChangedAt(now);

        User savedUser = userRepository.save(user);

        return buildUserProfileResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new InvalidCredentialsException(Messages.INVALID_USERNAME_OR_PASSWORD));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(Messages.INVALID_USERNAME_OR_PASSWORD);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AccessDeniedException(Messages.USER_BLOCKED);
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = currentUserService.getCurrentUser();

        return buildUserProfileResponse(currentUser);
    }

    @Override
    @Transactional
    public void changeCurrentUserPassword(PasswordChangeRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            throw new InvalidCredentialsException(Messages.CURRENT_PASSWORD_INVALID);
        }

        if (passwordEncoder.matches(request.newPassword(), currentUser.getPasswordHash())) {
            throw new InvalidOperationException(Messages.NEW_PASSWORD_MUST_BE_DIFFERENT);
        }

        LocalDateTime now = LocalDateTime.now();

        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        currentUser.setUpdatedAt(now);
        currentUser.setPasswordChangedAt(now);

        userRepository.save(currentUser);
    }


    private UserProfileResponse buildUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                user.getRole(),
                token,
                TOKEN_TYPE
        );
    }
}