package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.notification.TelegramBindLinkResponse;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookRequest;
import by.slava_borisov.nodehealthtracker.dto.telegram.TelegramWebhookResponse;
import by.slava_borisov.nodehealthtracker.model.entity.TelegramBindingToken;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.repository.TelegramBindingTokenRepository;
import by.slava_borisov.nodehealthtracker.repository.UserNotificationSettingRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
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
    private static final String START_COMMAND_PREFIX = "/start";

    private final TelegramBindingTokenRepository telegramBindingTokenRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final UserRepository userRepository;
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

    @Override
    @Transactional
    public TelegramWebhookResponse handleTelegramWebhook(TelegramWebhookRequest request) {
        if (request == null || request.message() == null || request.message().chat() == null) {
            log.warn("Получен некорректный Telegram webhook: request={}", request);
            return new TelegramWebhookResponse(false, "Некорректный webhook Telegram.");
        }

        Long chatId = request.message().chat().id();
        String text = request.message().text();

        log.info(
                "Получен Telegram webhook: updateId={}, chatId={}, text={}",
                request.updateId(),
                chatId,
                text
        );

        if (chatId == null) {
            log.warn("Telegram webhook не содержит chatId: updateId={}", request.updateId());
            return new TelegramWebhookResponse(false, "Не удалось определить Telegram chatId.");
        }

        String token = extractStartToken(text);

        if (token == null || token.isBlank()) {
            log.info(
                    "Telegram webhook без bind-token: chatId={}, text={}",
                    chatId,
                    text
            );

            return new TelegramWebhookResponse(
                    false,
                    "Сначала войдите в Node Health Tracker и нажмите “Подключить Telegram” на странице уведомлений."
            );
        }

        TelegramBindingToken bindingToken = telegramBindingTokenRepository.findByToken(token)
                .orElse(null);

        if (bindingToken == null) {
            log.warn(
                    "Telegram bind-token не найден: chatId={}, token={}",
                    chatId,
                    token
            );

            return new TelegramWebhookResponse(
                    false,
                    "Ссылка подключения Telegram недействительна. Создайте новую ссылку на сайте."
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (bindingToken.getUsedAt() != null) {
            log.warn(
                    "Повторное использование Telegram bind-token: tokenId={}, userId={}, chatId={}",
                    bindingToken.getId(),
                    bindingToken.getUserId(),
                    chatId
            );

            return new TelegramWebhookResponse(
                    false,
                    "Эта ссылка подключения Telegram уже была использована."
            );
        }

        if (bindingToken.getExpiresAt().isBefore(now)) {
            log.warn(
                    "Истёк срок действия Telegram bind-token: tokenId={}, userId={}, chatId={}, expiresAt={}",
                    bindingToken.getId(),
                    bindingToken.getUserId(),
                    chatId,
                    bindingToken.getExpiresAt()
            );

            return new TelegramWebhookResponse(
                    false,
                    "Срок действия ссылки подключения Telegram истёк. Создайте новую ссылку на сайте."
            );
        }

        User user = userRepository.findById(bindingToken.getUserId())
                .orElse(null);

        if (user == null) {
            log.error(
                    "Пользователь для Telegram bind-token не найден: tokenId={}, userId={}",
                    bindingToken.getId(),
                    bindingToken.getUserId()
            );

            return new TelegramWebhookResponse(
                    false,
                    "Пользователь для привязки Telegram не найден."
            );
        }

        UserNotificationSetting setting = userNotificationSettingRepository
                .findByUserIdAndChannel(user.getId(), NotificationChannel.TELEGRAM)
                .orElseGet(() -> createTelegramSetting(user));

        setting.setIsEnabled(true);
        setting.setDestination(String.valueOf(chatId));
        setting.setNotifyOnIncidentOpen(true);
        setting.setNotifyOnIncidentResolved(true);

        userNotificationSettingRepository.save(setting);

        bindingToken.setUsedAt(now);
        telegramBindingTokenRepository.save(bindingToken);

        log.info(
                "Telegram успешно привязан к пользователю: userId={}, username={}, chatId={}, settingId={}",
                user.getId(),
                user.getUsername(),
                chatId,
                setting.getId()
        );

        return new TelegramWebhookResponse(
                true,
                "Telegram успешно подключён к Node Health Tracker."
        );
    }

    private UserNotificationSetting createTelegramSetting(User user) {
        UserNotificationSetting setting = new UserNotificationSetting();
        setting.setUser(user);
        setting.setChannel(NotificationChannel.TELEGRAM);
        setting.setIsEnabled(true);
        setting.setNotifyOnIncidentOpen(true);
        setting.setNotifyOnIncidentResolved(true);

        return setting;
    }

    private String extractStartToken(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmedText = text.trim();

        if (!trimmedText.startsWith(START_COMMAND_PREFIX)) {
            return null;
        }

        String[] parts = trimmedText.split("\\s+", 2);

        if (parts.length < 2) {
            return null;
        }

        return parts[1].trim();
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