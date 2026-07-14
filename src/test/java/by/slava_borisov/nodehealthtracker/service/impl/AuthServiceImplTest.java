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
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты AuthServiceImpl")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User activeUser;
    private User blockedUser;

    @BeforeEach
    void setUp() {
        activeUser = createUser(
                1L,
                "testuser",
                "test@example.com",
                "encodedPassword",
                UserStatus.ACTIVE,
                RoleName.ROLE_USER
        );

        blockedUser = createUser(
                2L,
                "blockeduser",
                "blocked@example.com",
                "encodedPassword",
                UserStatus.BLOCKED,
                RoleName.ROLE_USER
        );
    }

    @Test
    @DisplayName("Регистрация пользователя - успешно")
    void register_success() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "new@example.com",
                "newuser",
                "password123"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        UserProfileResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.username());
        assertEquals("new@example.com", result.email());
        assertEquals(UserStatus.ACTIVE, result.status());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация - email уже существует")
    void register_emailExists_throwsException() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "existing@example.com",
                "newuser",
                "password123"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Регистрация - username уже существует")
    void register_usernameExists_throwsException() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "new@example.com",
                "existinguser",
                "password123"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Вход в систему - успешно")
    void login_success() {
        UserLoginRequest request = new UserLoginRequest(
                "testuser",
                "password123"
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("mocked-jwt-token");

        AuthResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("mocked-jwt-token", result.token());
        assertEquals("Bearer", result.tokenType());
    }

    @Test
    @DisplayName("Вход в систему - пользователь не найден")
    void login_userNotFound_throwsException() {
        UserLoginRequest request = new UserLoginRequest(
                "unknown",
                "password123"
        );

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Вход в систему - неверный пароль")
    void login_wrongPassword_throwsException() {
        UserLoginRequest request = new UserLoginRequest(
                "testuser",
                "wrongpassword"
        );

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(
                "wrongpassword",
                "encodedPassword")
        ).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Вход в систему - пользователь заблокирован")
    void login_blockedUser_throwsException() {
        UserLoginRequest request = new UserLoginRequest(
                "blockeduser",
                "password123"
        );

        when(userRepository.findByUsername("blockeduser")).thenReturn(Optional.of(blockedUser));
        when(passwordEncoder.matches(
                "password123",
                "encodedPassword")
        ).thenReturn(true);

        assertThrows(AccessDeniedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Получить профиль текущего пользователя - успешно")
    void getCurrentUserProfile_success() {
        when(currentUserService.getCurrentUser()).thenReturn(activeUser);

        UserProfileResponse result = authService.getCurrentUserProfile();

        assertNotNull(result);
        assertEquals("testuser", result.username());
        verify(currentUserService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("Смена пароля - успешно")
    void changeCurrentUserPassword_success() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "oldPassword",
                "newPassword"
        );

        when(currentUserService.getCurrentUser()).thenReturn(activeUser);

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedPassword"))
                .thenReturn(true);

        when(passwordEncoder.matches(
                "newPassword",
                "encodedPassword"))
                .thenReturn(false);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.changeCurrentUserPassword(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Смена пароля - неверный текущий пароль")
    void changeCurrentUserPassword_wrongCurrentPassword_throwsException() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "wrongOldPassword",
                "newPassword"
        );

        when(currentUserService.getCurrentUser()).thenReturn(activeUser);
        when(passwordEncoder.matches(
                "wrongOldPassword",
                "encodedPassword"))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.changeCurrentUserPassword(request));
    }

    @Test
    @DisplayName("Смена пароля - новый пароль совпадает с текущим")
    void changeCurrentUserPassword_sameAsNew_throwsException() {
        PasswordChangeRequest request = new PasswordChangeRequest(
                "oldPassword",
                "oldPassword"
        );

        when(currentUserService.getCurrentUser()).thenReturn(activeUser);

        when(passwordEncoder.matches(
                "oldPassword",
                "encodedPassword"))
                .thenReturn(true);

        assertThrows(
                InvalidOperationException.class,
                () -> authService.changeCurrentUserPassword(request)
        );
    }

    @Test
    @DisplayName("Обновление профиля - успешно")
    void updateCurrentUserProfile_success() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "new@example.com",
                "newuser");

        when(currentUserService.getCurrentUser()).thenReturn(activeUser);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse result = authService.updateCurrentUserProfile(request);

        assertNotNull(result);
        assertEquals("newuser", result.username());
        assertEquals("new@example.com", result.email());
    }

    @Test
    @DisplayName("Обновление профиля - новый email уже существует")
    void updateCurrentUserProfile_emailExists_throwsException() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "existing@example.com",
                "newuser"
        );

        when(currentUserService.getCurrentUser()).thenReturn(activeUser);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.updateCurrentUserProfile(request)
        );
    }

    @Test
    @DisplayName("Обновление профиля - новый username уже существует")
    void updateCurrentUserProfile_usernameExists_throwsException() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "new@example.com",
                "existinguser"
        );

        when(currentUserService.getCurrentUser()).thenReturn(activeUser);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.updateCurrentUserProfile(request)
        );
    }

    private User createUser(
            Long id,
            String username,
            String email,
            String passwordHash,
            UserStatus status,
            RoleName role
    ) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setStatus(status);
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}