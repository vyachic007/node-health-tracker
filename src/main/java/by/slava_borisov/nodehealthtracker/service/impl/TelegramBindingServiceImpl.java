package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.notification.TelegramBindLinkResponse;
import by.slava_borisov.nodehealthtracker.model.entity.TelegramBindingToken;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.repository.TelegramBindingTokenRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.TelegramBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBindingServiceImpl implements TelegramBindingService {

    private static final int TOKEN_RANDOM_BYTES = 32;

    private final TelegramBindingTokenRepository telegramBindingTokenRepository;
    private final CurrentUserService currentUserService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.telegram.bot-username:}")
    private String botUsername;

    @Value("${app.telegram.binding-token-expiration-minutes:15}")
    private long bindingTokenExpirationMinutes;

    @Override
    @Transactional
    public TelegramBindLinkResponse createCurrentUserBindLink() {
        User currentUser = currentUserService.getCurrentUser();

        String token = generateToken();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(bindingTokenExpirationMinutes);

        TelegramBindingToken bindingToken = new TelegramBindingToken();
        bindingToken.setUserId(currentUser.getId());
        bindingToken.setToken(token);
        bindingToken.setCreatedAt(now);
        bindingToken.setExpiresAt(expiresAt);
        bindingToken.setUsedAt(null);

        telegramBindingTokenRepository.save(bindingToken);

        String normalizedBotUsername = normalizeBotUsername(botUsername);
        String telegramLink = buildTelegramLink(normalizedBotUsername, token);

        log.info(
                "Создана ссылка привязки Telegram: userId={}, username={}, expiresAt={}",
                currentUser.getId(),
                currentUser.getUsername(),
                expiresAt
        );

        return new TelegramBindLinkResponse(
                token,
                normalizedBotUsername,
                telegramLink,
                expiresAt
        );
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_RANDOM_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String normalizeBotUsername(String value) {
        if (value == null || value.isBlank()) {
            return "your_bot_username";
        }

        return value.startsWith("@")
                ? value.substring(1)
                : value;
    }

    private String buildTelegramLink(String botUsername, String token) {
        return "https://t.me/" + botUsername + "?start=" + token;
    }
}