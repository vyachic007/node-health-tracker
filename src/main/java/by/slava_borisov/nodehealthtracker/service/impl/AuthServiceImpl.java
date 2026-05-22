package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.user.AuthResponse;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordChangeRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserLoginRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserRegistrationRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
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
        log.info(
                "Попытка регистрации пользователя: username={}, email={}",
                request.username(),
                request.email()
        );

        if (userRepository.existsByEmail(request.email())) {
            log.warn(
                    "Регистрация отклонена: email уже используется, username={}, email={}",
                    request.username(),
                    request.email()
            );

            throw new UserAlreadyExistsException(Messages.USER_EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByUsername(request.username())) {
            log.warn(
                    "Регистрация отклонена: username уже используется, username={}, email={}",
                    request.username(),
                    request.email()
            );

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
        user.setCredentialsChangedAt(now);

        User savedUser = userRepository.save(user);

        log.info(
                "Пользователь успешно зарегистрирован: userId={}, username={}, email={}, role={}, status={}",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getStatus()
        );

        return buildUserProfileResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(UserLoginRequest request) {
        log.info(
                "Попытка входа пользователя: username={}",
                request.username()
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn(
                            "Вход отклонён: пользователь не найден, username={}",
                            request.username()
                    );

                    return new InvalidCredentialsException(Messages.INVALID_USERNAME_OR_PASSWORD);
                });

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn(
                    "Вход отклонён: неверный пароль, userId={}, username={}",
                    user.getId(),
                    user.getUsername()
            );

            throw new InvalidCredentialsException(Messages.INVALID_USERNAME_OR_PASSWORD);
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            log.warn(
                    "Вход отклонён: пользователь заблокирован, userId={}, username={}",
                    user.getId(),
                    user.getUsername()
            );

            throw new AccessDeniedException(Messages.USER_BLOCKED);
        }

        AuthResponse authResponse = buildAuthResponse(user);

        log.info(
                "Пользователь успешно вошёл в систему: userId={}, username={}, role={}, status={}",
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus()
        );

        return authResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Запрошен профиль текущего пользователя: userId={}, username={}",
                currentUser.getId(),
                currentUser.getUsername()
        );

        return buildUserProfileResponse(currentUser);
    }

    @Override
    @Transactional
    public void changeCurrentUserPassword(PasswordChangeRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Попытка смены пароля текущего пользователя: userId={}, username={}",
                currentUser.getId(),
                currentUser.getUsername()
        );

        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPasswordHash())) {
            log.warn(
                    "Смена пароля отклонена: неверный текущий пароль, userId={}, username={}",
                    currentUser.getId(),
                    currentUser.getUsername()
            );

            throw new InvalidCredentialsException(Messages.CURRENT_PASSWORD_INVALID);
        }

        if (passwordEncoder.matches(request.newPassword(), currentUser.getPasswordHash())) {
            log.warn(
                    "Смена пароля отклонена: новый пароль совпадает с текущим, userId={}, username={}",
                    currentUser.getId(),
                    currentUser.getUsername()
            );

            throw new InvalidOperationException(Messages.NEW_PASSWORD_MUST_BE_DIFFERENT);
        }

        LocalDateTime now = LocalDateTime.now();

        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        currentUser.setUpdatedAt(now);
        currentUser.setPasswordChangedAt(now);
        currentUser.setCredentialsChangedAt(now);

        userRepository.save(currentUser);

        log.info(
                "Пароль пользователя успешно изменён: userId={}, username={}",
                currentUser.getId(),
                currentUser.getUsername()
        );
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UserProfileUpdateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Попытка обновления профиля пользователя: userId={}, currentUsername={}, newUsername={}, currentEmail={}, newEmail={}",
                currentUser.getId(),
                currentUser.getUsername(),
                request.username(),
                currentUser.getEmail(),
                request.email()
        );

        if (!currentUser.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            log.warn(
                    "Обновление профиля отклонено: email уже используется, userId={}, username={}, requestedEmail={}",
                    currentUser.getId(),
                    currentUser.getUsername(),
                    request.email()
            );

            throw new UserAlreadyExistsException(Messages.USER_EMAIL_ALREADY_EXISTS);
        }

        if (!currentUser.getUsername().equals(request.username())
                && userRepository.existsByUsername(request.username())) {
            log.warn(
                    "Обновление профиля отклонено: username уже используется, userId={}, currentUsername={}, requestedUsername={}",
                    currentUser.getId(),
                    currentUser.getUsername(),
                    request.username()
            );

            throw new UserAlreadyExistsException(Messages.USERNAME_ALREADY_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();

        currentUser.setEmail(request.email());
        currentUser.setUsername(request.username());
        currentUser.setUpdatedAt(now);
        currentUser.setCredentialsChangedAt(now);

        User savedUser = userRepository.save(currentUser);

        log.info(
                "Профиль пользователя успешно обновлён: userId={}, username={}, email={}",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );

        return buildUserProfileResponse(savedUser);
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