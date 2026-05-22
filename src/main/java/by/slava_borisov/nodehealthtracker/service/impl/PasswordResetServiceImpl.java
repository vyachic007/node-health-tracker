package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetConfirmRequest;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetRequest;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.model.entity.PasswordResetToken;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.PasswordResetTokenRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.PasswordResetMailService;
import by.slava_borisov.nodehealthtracker.service.PasswordResetService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import static by.slava_borisov.nodehealthtracker.util.Messages.TOKEN_HASH_ALGORITHM;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetMailService passwordResetMailService;

    @Override
    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        log.info(
                "Запрошено восстановление пароля: email={}",
                normalizedEmail
        );

        userRepository.findByEmail(normalizedEmail)
                .ifPresentOrElse(
                        this::createPasswordResetToken,
                        () -> log.warn(
                                "Восстановление пароля запрошено для несуществующего email: email={}",
                                normalizedEmail
                        )
                );
    }

    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String tokenHash = hashToken(request.token());

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Попытка восстановления пароля с несуществующим tokenHash");
                    return new InvalidOperationException(Messages.PASSWORD_RESET_TOKEN_INVALID);
                });

        validatePasswordResetToken(passwordResetToken);

        User user = passwordResetToken.getUser();

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            log.warn(
                    "Восстановление пароля отклонено: новый пароль совпадает со старым, userId={}, username={}",
                    user.getId(),
                    user.getUsername()
            );

            throw new InvalidOperationException(Messages.NEW_PASSWORD_MUST_BE_DIFFERENT);
        }

        LocalDateTime now = LocalDateTime.now();

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(now);
        user.setPasswordChangedAt(now);
        user.setCredentialsChangedAt(now);

        passwordResetToken.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);

        log.info(
                "Пароль успешно восстановлен: userId={}, username={}",
                user.getId(),
                user.getUsername()
        );
    }

    private void createPasswordResetToken(User user) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            log.warn(
                    "Восстановление пароля отклонено: пользователь заблокирован, userId={}, username={}",
                    user.getId(),
                    user.getUsername()
            );

            return;
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setTokenHash(tokenHash);
        passwordResetToken.setCreatedAt(now);
        passwordResetToken.setExpiresAt(now.plusMinutes(RESET_TOKEN_TTL_MINUTES));
        passwordResetToken.setUsedAt(null);

        PasswordResetToken savedToken = passwordResetTokenRepository.save(passwordResetToken);

        log.info(
                "Создан токен восстановления пароля: tokenId={}, userId={}, username={}, expiresAt={}",
                savedToken.getId(),
                user.getId(),
                user.getUsername(),
                savedToken.getExpiresAt()
        );

        passwordResetMailService.sendPasswordResetToken(
                user,
                rawToken,
                savedToken.getExpiresAt()
        );

        log.info(
                "DEV password reset token for username={}: {}",
                user.getUsername(),
                rawToken
        );
    }

    private void validatePasswordResetToken(PasswordResetToken passwordResetToken) {
        LocalDateTime now = LocalDateTime.now();

        if (passwordResetToken.getUsedAt() != null) {
            log.warn(
                    "Попытка использовать уже использованный токен восстановления: tokenId={}, userId={}",
                    passwordResetToken.getId(),
                    passwordResetToken.getUser().getId()
            );

            throw new InvalidOperationException(Messages.PASSWORD_RESET_TOKEN_ALREADY_USED);
        }

        if (passwordResetToken.getExpiresAt().isBefore(now)) {
            log.warn(
                    "Попытка использовать просроченный токен восстановления: tokenId={}, userId={}, expiresAt={}",
                    passwordResetToken.getId(),
                    passwordResetToken.getUser().getId(),
                    passwordResetToken.getExpiresAt()
            );

            throw new InvalidOperationException(Messages.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        if (passwordResetToken.getUser().getStatus() == UserStatus.BLOCKED) {
            log.warn(
                    "Попытка восстановить пароль заблокированного пользователя: userId={}, username={}",
                    passwordResetToken.getUser().getId(),
                    passwordResetToken.getUser().getUsername()
            );

            throw new InvalidOperationException(Messages.USER_BLOCKED);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(Messages.TOKEN_HASH_ALGORITHM);
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    Messages.PASSWORD_RESET_HASH_ALGORITHM_NOT_AVAILABLE,
                    exception
            );
        }
    }
}