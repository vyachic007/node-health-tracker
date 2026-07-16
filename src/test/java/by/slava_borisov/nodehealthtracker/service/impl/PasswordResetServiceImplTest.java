package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.config.PasswordResetProperties;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetConfirmRequest;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetRequest;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.model.entity.PasswordResetToken;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.PasswordResetTokenRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.PasswordResetMailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты PasswordResetServiceImpl")
class PasswordResetServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    @Mock
    private PasswordResetProperties passwordResetProperties;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User activeUser;
    private User blockedUser;
    private PasswordResetToken validToken;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("testuser");
        activeUser.setEmail("test@example.com");
        activeUser.setPasswordHash("encodedOldPassword");
        activeUser.setStatus(UserStatus.ACTIVE);

        blockedUser = new User();
        blockedUser.setId(2L);
        blockedUser.setUsername("blockeduser");
        blockedUser.setEmail("blocked@example.com");
        blockedUser.setStatus(UserStatus.BLOCKED);

        validToken = new PasswordResetToken();
        validToken.setId(100L);
        validToken.setUser(activeUser);
        validToken.setTokenHash("hashed-token");
        validToken.setCreatedAt(LocalDateTime.now());
        validToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        validToken.setUsedAt(null);
    }

    @Test
    @DisplayName("Запросить сброс пароля - успешно")
    void requestPasswordReset_success() {
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        when(passwordResetProperties.isLogToken()).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(validToken);

        passwordResetService.requestPasswordReset(request);

        verify(passwordResetTokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(passwordResetMailService, times(1)).sendPasswordResetToken(
                eq(activeUser),
                anyString(),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Запросить сброс пароля - пользователь не найден (безопасный фолбэк)")
    void requestPasswordReset_userNotFound_doesNothing() {
        PasswordResetRequest request = new PasswordResetRequest("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(request);

        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetMailService, never()).sendPasswordResetToken(any(), any(), any());
    }

    @Test
    @DisplayName("Запросить сброс пароля - пользователь заблокирован")
    void requestPasswordReset_userBlocked_doesNothing() {
        PasswordResetRequest request = new PasswordResetRequest("blocked@example.com");
        when(userRepository.findByEmail("blocked@example.com")).thenReturn(Optional.of(blockedUser));

        passwordResetService.requestPasswordReset(request);

        verify(passwordResetTokenRepository, never()).save(any());
        verify(passwordResetMailService, never()).sendPasswordResetToken(any(), any(), any());
    }

    @Test
    @DisplayName("Подтвердить сброс пароля - успешно")
    void confirmPasswordReset_success() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("raw-token", "newPassword");

        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));
        when(passwordEncoder.matches("newPassword", "encodedOldPassword")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenReturn(validToken);

        passwordResetService.confirmPasswordReset(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertEquals("encodedNewPassword", userCaptor.getValue().getPasswordHash());
        assertNotNull(userCaptor.getValue().getPasswordChangedAt());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository, times(1)).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getUsedAt());
    }

    @Test
    @DisplayName("Подтвердить сброс пароля - токен не найден")
    void confirmPasswordReset_tokenNotFound_throwsException() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("invalid-token", "newPassword");
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(
                InvalidOperationException.class,
                () -> passwordResetService.confirmPasswordReset(request)
        );
    }

    @Test
    @DisplayName("Подтвердить сброс пароля - токен уже использован")
    void confirmPasswordReset_tokenAlreadyUsed_throwsException() {
        validToken.setUsedAt(LocalDateTime.now().minusMinutes(10));
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("raw-token", "newPassword");

        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));

        assertThrows(
                InvalidOperationException.class,
                () -> passwordResetService.confirmPasswordReset(request)
        );
    }

    @Test
    @DisplayName("Подтвердить сброс пароля - токен просрочен")
    void confirmPasswordReset_tokenExpired_throwsException() {
        validToken.setExpiresAt(LocalDateTime.now().minusMinutes(10));
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("raw-token", "newPassword");

        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));

        assertThrows(
                InvalidOperationException.class,
                () -> passwordResetService.confirmPasswordReset(request)
        );
    }

    @Test
    @DisplayName("Подтвердить сброс пароля - пользователь заблокирован")
    void confirmPasswordReset_userBlocked_throwsException() {
        validToken.setUser(blockedUser);
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("raw-token", "newPassword");

        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));

        assertThrows(
                InvalidOperationException.class,
                () -> passwordResetService.confirmPasswordReset(request)
        );
    }

    @Test
    @DisplayName("Подтвердить сброс пароля - новый пароль совпадает со старым")
    void confirmPasswordReset_samePassword_throwsException() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("raw-token", "newPassword");

        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(validToken));
        when(passwordEncoder.matches("newPassword", "encodedOldPassword")).thenReturn(true);

        assertThrows(
                InvalidOperationException.class,
                () -> passwordResetService.confirmPasswordReset(request)
        );
    }
}