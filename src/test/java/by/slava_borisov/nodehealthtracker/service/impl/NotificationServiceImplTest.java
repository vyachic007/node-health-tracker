package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.config.VkProperties;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.VkBindLinkResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.SentNotification;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationStatus;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import by.slava_borisov.nodehealthtracker.notification.sender.NotificationSender;
import by.slava_borisov.nodehealthtracker.repository.SentNotificationRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты NotificationServiceImpl")
class NotificationServiceImplTest {

    @Mock
    private UserNotificationSettingRepository userNotificationSettingRepository;

    @Mock
    private SentNotificationRepository sentNotificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NotificationSender notificationSender;

    @Mock
    private VkProperties vkProperties;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User ownerUser;
    private User differentUser;
    private NetworkNode networkNode;
    private NetworkService networkService;
    private Incident incident;
    private UserNotificationSetting setting;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner");

        differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("other");

        networkNode = new NetworkNode();
        networkNode.setId(10L);
        networkNode.setOwner(ownerUser);

        networkService = new NetworkService();
        networkService.setId(20L);
        networkService.setName("Test Service");
        networkService.setNode(networkNode);
        networkService.setNotifyEmail(true);

        incident = new Incident();
        incident.setId(100L);
        incident.setService(networkService);

        setting = new UserNotificationSetting();
        setting.setId(30L);
        setting.setUser(ownerUser);
        setting.setChannel(NotificationChannel.EMAIL);
        setting.setIsEnabled(true);
        setting.setNotifyOnIncidentOpen(true);
        setting.setNotifyOnIncidentResolved(true);

        when(notificationSender.getChannel()).thenReturn(NotificationChannel.EMAIL);

        // Явно внедряем список с моком, чтобы initSenders() не падал с NPE
        ReflectionTestUtils.setField(notificationService, "notificationSenders", List.of(notificationSender));
        notificationService.initSenders();

        // Очищаем карту токенов для изоляции тестов
        ReflectionTestUtils.setField(
                notificationService,
                "vkBindTokens",
                new ConcurrentHashMap<>()
        );
    }

    @Test
    @DisplayName("Создать настройку уведомлений - успешно")
    void createNotificationSetting_success() {
        NotificationSettingCreateRequest request = mock(NotificationSettingCreateRequest.class);
        when(request.channel()).thenReturn(NotificationChannel.EMAIL);
        when(request.isEnabled()).thenReturn(true);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(userNotificationSettingRepository.findByUserIdAndChannel(1L, NotificationChannel.EMAIL))
                .thenReturn(Optional.empty());
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class))).thenReturn(setting);

        NotificationSettingResponse result = notificationService.createNotificationSetting(request);

        assertNotNull(result);
        verify(userNotificationSettingRepository, times(1)).save(any(UserNotificationSetting.class));
    }

    @Test
    @DisplayName("Создать настройку уведомлений - уже существует")
    void createNotificationSetting_alreadyExists_throwsException() {
        NotificationSettingCreateRequest request = mock(NotificationSettingCreateRequest.class);
        when(request.channel()).thenReturn(NotificationChannel.EMAIL);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(userNotificationSettingRepository.findByUserIdAndChannel(1L, NotificationChannel.EMAIL))
                .thenReturn(Optional.of(setting));

        assertThrows(
                InvalidOperationException.class,
                () -> notificationService.createNotificationSetting(request)
        );
    }

    @Test
    @DisplayName("Обновить настройку уведомлений - успешно")
    void updateNotificationSetting_success() {
        NotificationSettingUpdateRequest request = mock(NotificationSettingUpdateRequest.class);
        when(request.isEnabled()).thenReturn(false);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(userNotificationSettingRepository.findById(30L)).thenReturn(Optional.of(setting));
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class))).thenReturn(setting);

        NotificationSettingResponse result = notificationService.updateNotificationSetting(30L, request);

        assertNotNull(result);
        verify(userNotificationSettingRepository, times(1)).save(any(UserNotificationSetting.class));
    }

    @Test
    @DisplayName("Обновить настройку уведомлений - не найдена")
    void updateNotificationSetting_notFound_throwsException() {
        NotificationSettingUpdateRequest request = mock(NotificationSettingUpdateRequest.class);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(userNotificationSettingRepository.findById(30L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.updateNotificationSetting(30L, request)
        );
    }

    @Test
    @DisplayName("Обновить настройку уведомлений - отказ в доступе")
    void updateNotificationSetting_accessDenied_throwsException() {
        NotificationSettingUpdateRequest request = mock(NotificationSettingUpdateRequest.class);
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(userNotificationSettingRepository.findById(30L)).thenReturn(Optional.of(setting));

        assertThrows(
                AccessDeniedException.class,
                () -> notificationService.updateNotificationSetting(30L, request)
        );
    }

    @Test
    @DisplayName("Удалить настройку уведомлений - успешно")
    void deleteNotificationSetting_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(userNotificationSettingRepository.findById(30L)).thenReturn(Optional.of(setting));

        notificationService.deleteNotificationSetting(30L);

        verify(userNotificationSettingRepository, times(1)).delete(setting);
    }

    @Test
    @DisplayName("Получить настройки текущего пользователя - успешно")
    void getCurrentUserNotificationSettings_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(userNotificationSettingRepository.findAllByUserIdOrderByChannelAsc(1L))
                .thenReturn(List.of(setting));

        List<NotificationSettingResponse> result = notificationService.getCurrentUserNotificationSettings();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Получить историю отправленных уведомлений - успешно")
    void getCurrentUserSentNotifications_success() {
        SentNotification sentNotification = new SentNotification();
        sentNotification.setId(1L);
        sentNotification.setUser(ownerUser);
        sentNotification.setIncident(incident);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(sentNotificationRepository.findAllByUserIdOrderBySentAtDesc(1L))
                .thenReturn(List.of(sentNotification));

        var result = notificationService.getCurrentUserSentNotifications();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Создать ссылку привязки VK - успешно")
    void createVkBindLink_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(vkProperties.getGroupId()).thenReturn("123456");

        VkBindLinkResponse result = notificationService.createVkBindLink();

        assertNotNull(result);
        assertTrue(result.vkLink().contains("123456"));
        assertNotNull(result.bindToken());
    }

    @Test
    @DisplayName("Создать ссылку привязки VK - VK не настроен")
    void createVkBindLink_unconfigured_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(vkProperties.getGroupId()).thenReturn("");

        assertThrows(
                InvalidOperationException.class,
                () -> notificationService.createVkBindLink()
        );
    }

    @Test
    @DisplayName("Подключить VK по токену - успешно")
    void connectVkByBindToken_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(vkProperties.getGroupId()).thenReturn("123456");

        VkBindLinkResponse link = notificationService.createVkBindLink();

        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(userNotificationSettingRepository.findByUserIdAndChannel(1L, NotificationChannel.VK))
                .thenReturn(Optional.empty());
        when(userNotificationSettingRepository.save(any(UserNotificationSetting.class))).thenReturn(setting);

        notificationService.connectVkByBindToken(link.bindToken(), "peer_123");

        verify(userNotificationSettingRepository, times(1)).save(any(UserNotificationSetting.class));
    }

    @Test
    @DisplayName("Подключить VK по токену - недействительный токен")
    void connectVkByBindToken_invalidToken_throwsException() {
        assertThrows(
                InvalidOperationException.class,
                () -> notificationService.connectVkByBindToken("invalid-token", "peer_123")
        );
    }

    @Test
    @DisplayName("Отправить уведомление об открытии инцидента - успешно")
    void notifyIncidentOpened_success() {
        when(userNotificationSettingRepository.findAllByUserIdAndIsEnabledTrue(1L))
                .thenReturn(List.of(setting));
        when(sentNotificationRepository.save(any(SentNotification.class))).thenAnswer(invocation -> {
            SentNotification saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        notificationService.notifyIncidentOpened(incident);

        verify(notificationSender, times(1)).send(any(NotificationMessage.class), eq(setting));

        ArgumentCaptor<SentNotification> captor = ArgumentCaptor.forClass(SentNotification.class);
        verify(sentNotificationRepository, times(1)).save(captor.capture());
        assertEquals(NotificationStatus.SENT.name(), captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Отправить уведомление об открытии инцидента - ошибка отправителя сохраняется как FAILED")
    void notifyIncidentOpened_senderThrowsException_savesFailed() {
        when(userNotificationSettingRepository.findAllByUserIdAndIsEnabledTrue(1L))
                .thenReturn(List.of(setting));
        doThrow(new RuntimeException("Network error"))
                .when(notificationSender).send(any(NotificationMessage.class), eq(setting));
        when(sentNotificationRepository.save(any(SentNotification.class))).thenAnswer(invocation -> {
            SentNotification saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        notificationService.notifyIncidentOpened(incident);

        ArgumentCaptor<SentNotification> captor = ArgumentCaptor.forClass(SentNotification.class);
        verify(sentNotificationRepository, times(1)).save(captor.capture());
        assertEquals(NotificationStatus.FAILED.name(), captor.getValue().getStatus());
        assertEquals("Network error", captor.getValue().getErrorMessage());
    }
}