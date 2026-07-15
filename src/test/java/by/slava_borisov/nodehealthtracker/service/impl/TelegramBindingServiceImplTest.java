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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты TelegramBindingServiceImpl")
class TelegramBindingServiceImplTest {

    @Mock
    private TelegramBindingTokenRepository telegramBindingTokenRepository;

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TelegramBindingServiceImpl telegramBindingService;

    private User ownerUser;
    private TelegramBindingToken validToken;
    private UserNotificationSetting existingSetting;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner");

        validToken = new TelegramBindingToken();
        validToken.setId(100L);
        validToken.setUserId(1L);
        validToken.setToken("valid-token");
        validToken.setCreatedAt(LocalDateTime.now());
        validToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        validToken.setUsedAt(null);

        existingSetting = new UserNotificationSetting();
        existingSetting.setId(200L);
        existingSetting.setUser(ownerUser);
        existingSetting.setChannel(NotificationChannel.TELEGRAM);

        ReflectionTestUtils.setField(telegramBindingService, "botUsername", "@test_bot");
        ReflectionTestUtils.setField(telegramBindingService, "bindingTokenExpirationMinutes", 15L);
    }

    @Test
    @DisplayName("Создать ссылку привязки Telegram - успешно")
    void createCurrentUserBindLink_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(telegramBindingTokenRepository.save(any(TelegramBindingToken.class))).thenReturn(validToken);

        TelegramBindLinkResponse result = telegramBindingService.createCurrentUserBindLink();

        assertNotNull(result);
        assertEquals("test_bot", result.botUsername());
        assertTrue(result.telegramLink().contains("test_bot"));
        assertTrue(result.telegramLink().contains("start="));
        verify(telegramBindingTokenRepository, times(1)).save(any(TelegramBindingToken.class));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - некорректный запрос (null)")
    void handleTelegramWebhook_nullRequest_returnsFalse() {
        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(null);

        assertFalse(result.success());
        assertTrue(result.message().contains("Некорректный"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - отсутствует chatId")
    void handleTelegramWebhook_missingChatId_returnsFalse() {
        TelegramWebhookRequest request = createRequest(null, "/start token", 1L);

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertFalse(result.success());
        assertTrue(result.message().contains("chatId"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - отсутствует токен в тексте")
    void handleTelegramWebhook_missingToken_returnsFalse() {
        TelegramWebhookRequest request = createRequest(12345L, "Hello without token", 1L);

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertFalse(result.success());
        assertTrue(result.message().contains("Сначала войдите"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - токен не найден")
    void handleTelegramWebhook_tokenNotFound_returnsFalse() {
        TelegramWebhookRequest request = createRequest(12345L, "/start invalid-token", 1L);
        when(telegramBindingTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertFalse(result.success());
        assertTrue(result.message().contains("недействительна"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - токен уже использован")
    void handleTelegramWebhook_tokenAlreadyUsed_returnsFalse() {
        validToken.setUsedAt(LocalDateTime.now().minusMinutes(1));
        TelegramWebhookRequest request = createRequest(12345L, "/start valid-token", 1L);

        when(telegramBindingTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertFalse(result.success());
        assertTrue(result.message().contains("уже была использована"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - токен просрочен")
    void handleTelegramWebhook_tokenExpired_returnsFalse() {
        validToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        TelegramWebhookRequest request = createRequest(12345L, "/start valid-token", 1L);

        when(telegramBindingTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertFalse(result.success());
        assertTrue(result.message().contains("истёк"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - пользователь не найден")
    void handleTelegramWebhook_userNotFound_returnsFalse() {
        TelegramWebhookRequest request = createRequest(12345L, "/start valid-token", 1L);

        when(telegramBindingTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertFalse(result.success());
        assertTrue(result.message().contains("не найден"));
    }

    @Test
    @DisplayName("Обработать webhook Telegram - успешно (новая настройка)")
    void handleTelegramWebhook_success_newSetting() {
        TelegramWebhookRequest request = createRequest(12345L, "/start valid-token", 1L);

        when(telegramBindingTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(userNotificationSettingRepository.findByUserIdAndChannel(1L, NotificationChannel.TELEGRAM))
                .thenReturn(Optional.empty());
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class))).thenReturn(existingSetting);
        when(telegramBindingTokenRepository.save(any(TelegramBindingToken.class))).thenReturn(validToken);

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertTrue(result.success());
        assertTrue(result.message().contains("успешно подключён"));

        ArgumentCaptor<UserNotificationSetting> settingCaptor = ArgumentCaptor.forClass(UserNotificationSetting.class);
        verify(userNotificationSettingRepository, times(1)).save(settingCaptor.capture());
        assertEquals("12345", settingCaptor.getValue().getDestination());
        assertTrue(settingCaptor.getValue().getIsEnabled());

        ArgumentCaptor<TelegramBindingToken> tokenCaptor = ArgumentCaptor.forClass(TelegramBindingToken.class);
        verify(telegramBindingTokenRepository, times(1)).save(tokenCaptor.capture());
        assertNotNull(tokenCaptor.getValue().getUsedAt());
    }

    @Test
    @DisplayName("Обработать webhook Telegram - успешно (существующая настройка)")
    void handleTelegramWebhook_success_existingSetting() {
        existingSetting.setDestination("old-chat-id");
        existingSetting.setIsEnabled(false);

        TelegramWebhookRequest request = createRequest(12345L, "/start valid-token", 1L);

        when(telegramBindingTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(userNotificationSettingRepository.findByUserIdAndChannel(1L, NotificationChannel.TELEGRAM))
                .thenReturn(Optional.of(existingSetting));
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class))).thenReturn(existingSetting);
        when(telegramBindingTokenRepository.save(any(TelegramBindingToken.class))).thenReturn(validToken);

        TelegramWebhookResponse result = telegramBindingService.handleTelegramWebhook(request);

        assertTrue(result.success());

        ArgumentCaptor<UserNotificationSetting> settingCaptor = ArgumentCaptor.forClass(UserNotificationSetting.class);
        verify(userNotificationSettingRepository, times(1)).save(settingCaptor.capture());
        assertEquals("12345", settingCaptor.getValue().getDestination());
        assertTrue(settingCaptor.getValue().getIsEnabled());
    }

    private TelegramWebhookRequest createRequest(Long chatId, String text, Long updateId) {
        TelegramWebhookRequest.TelegramChat chat = mock(TelegramWebhookRequest.TelegramChat.class);
        when(chat.id()).thenReturn(chatId);

        TelegramWebhookRequest.TelegramMessage message = mock(TelegramWebhookRequest.TelegramMessage.class);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(text);

        TelegramWebhookRequest request = mock(TelegramWebhookRequest.class);
        when(request.message()).thenReturn(message);
        when(request.updateId()).thenReturn(updateId);

        return request;
    }
}