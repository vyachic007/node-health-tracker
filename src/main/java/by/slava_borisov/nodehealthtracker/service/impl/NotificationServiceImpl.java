package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.SentNotificationResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.SentNotification;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationStatus;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import by.slava_borisov.nodehealthtracker.notification.sender.NotificationSender;
import by.slava_borisov.nodehealthtracker.repository.SentNotificationRepository;
import by.slava_borisov.nodehealthtracker.repository.UserNotificationSettingRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final SentNotificationRepository sentNotificationRepository;
    private final CurrentUserService currentUserService;
    private final List<NotificationSender> notificationSenders;

    private final Map<NotificationChannel, NotificationSender> senderByChannel =
            new EnumMap<>(NotificationChannel.class);

    @PostConstruct
    public void initSenders() {
        notificationSenders.forEach(sender -> {
            senderByChannel.put(sender.getChannel(), sender);

            log.info(
                    "Зарегистрирован отправитель уведомлений: channel={}, senderClass={}",
                    sender.getChannel(),
                    sender.getClass().getSimpleName()
            );
        });

        log.info(
                "Инициализация отправителей уведомлений завершена: sendersCount={}",
                senderByChannel.size()
        );
    }

    @Override
    @Transactional
    public NotificationSettingResponse createNotificationSetting(NotificationSettingCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Создание настройки уведомлений: userId={}, username={}, channel={}",
                currentUser.getId(),
                currentUser.getUsername(),
                request.channel()
        );

        userNotificationSettingRepository.findByUserIdAndChannel(
                currentUser.getId(),
                request.channel()
        ).ifPresent(setting -> {
            log.warn(
                    "Попытка создать дублирующую настройку уведомлений: userId={}, username={}, channel={}",
                    currentUser.getId(),
                    currentUser.getUsername(),
                    request.channel()
            );

            throw new InvalidOperationException(Messages.NOTIFICATION_SETTING_ALREADY_EXISTS);
        });

        UserNotificationSetting setting = new UserNotificationSetting();
        setting.setUser(currentUser);
        setting.setChannel(request.channel());
        setting.setIsEnabled(request.isEnabled());
        setting.setDestination(request.destination());
        setting.setNotifyOnIncidentOpen(request.notifyOnIncidentOpen());
        setting.setNotifyOnIncidentResolved(request.notifyOnIncidentResolved());

        UserNotificationSetting savedSetting = userNotificationSettingRepository.save(setting);

        log.info(
                "Настройка уведомлений создана: settingId={}, userId={}, channel={}, isEnabled={}",
                savedSetting.getId(),
                currentUser.getId(),
                savedSetting.getChannel(),
                savedSetting.getIsEnabled()
        );

        return toNotificationSettingResponse(savedSetting);
    }

    @Override
    @Transactional
    public NotificationSettingResponse updateNotificationSetting(
            Long settingId,
            NotificationSettingUpdateRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();

        UserNotificationSetting setting = findSettingById(settingId);

        validateSettingOwner(currentUser, setting);

        log.info(
                "Обновление настройки уведомлений: settingId={}, userId={}, username={}, channel={}",
                setting.getId(),
                currentUser.getId(),
                currentUser.getUsername(),
                setting.getChannel()
        );

        setting.setIsEnabled(request.isEnabled());
        setting.setDestination(request.destination());
        setting.setNotifyOnIncidentOpen(request.notifyOnIncidentOpen());
        setting.setNotifyOnIncidentResolved(request.notifyOnIncidentResolved());

        UserNotificationSetting savedSetting = userNotificationSettingRepository.save(setting);

        log.info(
                "Настройка уведомлений обновлена: settingId={}, userId={}, channel={}, isEnabled={}",
                savedSetting.getId(),
                currentUser.getId(),
                savedSetting.getChannel(),
                savedSetting.getIsEnabled()
        );

        return toNotificationSettingResponse(savedSetting);
    }

    @Override
    @Transactional
    public void deleteNotificationSetting(Long settingId) {
        User currentUser = currentUserService.getCurrentUser();

        UserNotificationSetting setting = findSettingById(settingId);

        validateSettingOwner(currentUser, setting);

        log.info(
                "Удаление настройки уведомлений: settingId={}, userId={}, username={}, channel={}",
                setting.getId(),
                currentUser.getId(),
                currentUser.getUsername(),
                setting.getChannel()
        );

        userNotificationSettingRepository.delete(setting);

        log.info(
                "Настройка уведомлений удалена: settingId={}, userId={}, channel={}",
                settingId,
                currentUser.getId(),
                setting.getChannel()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSettingResponse> getCurrentUserNotificationSettings() {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Запрошен список настроек уведомлений пользователя: userId={}, username={}",
                currentUser.getId(),
                currentUser.getUsername()
        );

        return userNotificationSettingRepository
                .findAllByUserIdOrderByChannelAsc(currentUser.getId())
                .stream()
                .map(this::toNotificationSettingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SentNotificationResponse> getCurrentUserSentNotifications() {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Запрошена история отправленных уведомлений пользователя: userId={}, username={}",
                currentUser.getId(),
                currentUser.getUsername()
        );

        return sentNotificationRepository
                .findAllByUserIdOrderBySentAtDesc(currentUser.getId())
                .stream()
                .map(this::toSentNotificationResponse)
                .toList();
    }

    @Override
    @Transactional
    public void notifyIncidentOpened(Incident incident) {
        log.info(
                "Запуск отправки уведомлений об открытии инцидента: incidentId={}, serviceId={}, serviceName={}",
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName()
        );

        sendIncidentNotification(incident, NotificationEvent.INCIDENT_OPENED);
    }

    @Override
    @Transactional
    public void notifyIncidentResolved(Incident incident) {
        log.info(
                "Запуск отправки уведомлений о закрытии инцидента: incidentId={}, serviceId={}, serviceName={}",
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName()
        );

        sendIncidentNotification(incident, NotificationEvent.INCIDENT_RESOLVED);
    }

    private void sendIncidentNotification(Incident incident, NotificationEvent event) {
        User user = incident.getService()
                .getNode()
                .getOwner();

        List<UserNotificationSetting> enabledSettings = userNotificationSettingRepository
                .findAllByUserIdAndIsEnabledTrue(user.getId());

        log.info(
                "Найдены включённые настройки уведомлений: incidentId={}, userId={}, event={}, enabledSettingsCount={}",
                incident.getId(),
                user.getId(),
                event,
                enabledSettings.size()
        );

        List<UserNotificationSetting> suitableSettings = enabledSettings.stream()
                .filter(setting -> shouldNotify(setting, event))
                .toList();

        log.info(
                "Отобраны подходящие настройки уведомлений: incidentId={}, userId={}, event={}, suitableSettingsCount={}",
                incident.getId(),
                user.getId(),
                event,
                suitableSettings.size()
        );

        suitableSettings.forEach(setting -> sendAndSaveNotification(user, incident, setting, event));
    }

    private boolean shouldNotify(UserNotificationSetting setting, NotificationEvent event) {
        return switch (event) {
            case INCIDENT_OPENED -> Boolean.TRUE.equals(setting.getNotifyOnIncidentOpen());
            case INCIDENT_RESOLVED -> Boolean.TRUE.equals(setting.getNotifyOnIncidentResolved());
        };
    }

    private void sendAndSaveNotification(
            User user,
            Incident incident,
            UserNotificationSetting setting,
            NotificationEvent event
    ) {
        NotificationMessage message = buildNotificationMessage(incident, event);

        log.info(
                "Попытка отправки уведомления: incidentId={}, userId={}, channel={}, event={}",
                incident.getId(),
                user.getId(),
                setting.getChannel(),
                event
        );

        try {
            NotificationSender sender = findSender(setting.getChannel());
            sender.send(message, setting);

            SentNotification savedNotification = saveSentNotification(
                    user,
                    incident,
                    setting,
                    event,
                    NotificationStatus.SENT.name(),
                    null
            );

            log.info(
                    "Уведомление успешно отправлено: notificationId={}, incidentId={}, userId={}, channel={}, event={}",
                    savedNotification.getId(),
                    incident.getId(),
                    user.getId(),
                    setting.getChannel(),
                    event
            );
        } catch (Exception exception) {
            SentNotification savedNotification = saveSentNotification(
                    user,
                    incident,
                    setting,
                    event,
                    NotificationStatus.FAILED.name(),
                    exception.getMessage()
            );

            log.error(
                    "Ошибка отправки уведомления: notificationId={}, incidentId={}, userId={}, channel={}, event={}",
                    savedNotification.getId(),
                    incident.getId(),
                    user.getId(),
                    setting.getChannel(),
                    event,
                    exception
            );
        }
    }

    private NotificationMessage buildNotificationMessage(Incident incident, NotificationEvent event) {
        LocalDateTime eventTime = switch (event) {
            case INCIDENT_OPENED -> incident.getOpenedAt();
            case INCIDENT_RESOLVED -> incident.getClosedAt();
        };

        return new NotificationMessage(
                event,
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName(),
                incident.getService().getCheckType(),
                incident.getService().getTargetHost(),
                incident.getService().getPort(),
                incident.getService().getPath(),
                incident.getReason(),
                eventTime
        );
    }

    private NotificationSender findSender(NotificationChannel channel) {
        NotificationSender sender = senderByChannel.get(channel);

        if (sender == null) {
            log.error(
                    "Отправитель уведомлений не найден: channel={}",
                    channel
            );

            throw new InvalidOperationException(
                    String.format(Messages.NOTIFICATION_SENDER_NOT_FOUND, channel)
            );
        }

        return sender;
    }

    private SentNotification saveSentNotification(
            User user,
            Incident incident,
            UserNotificationSetting setting,
            NotificationEvent event,
            String status,
            String errorMessage
    ) {
        SentNotification notification = new SentNotification();
        notification.setUser(user);
        notification.setIncident(incident);
        notification.setChannel(setting.getChannel());
        notification.setEvent(event);
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus(status);
        notification.setErrorMessage(errorMessage);

        return sentNotificationRepository.save(notification);
    }

    private NotificationSettingResponse toNotificationSettingResponse(UserNotificationSetting setting) {
        return new NotificationSettingResponse(
                setting.getId(),
                setting.getUser().getId(),
                setting.getChannel(),
                setting.getIsEnabled(),
                setting.getDestination(),
                setting.getNotifyOnIncidentOpen(),
                setting.getNotifyOnIncidentResolved()
        );
    }

    private SentNotificationResponse toSentNotificationResponse(SentNotification notification) {
        return new SentNotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getIncident().getId(),
                notification.getChannel(),
                notification.getEvent(),
                notification.getSentAt(),
                notification.getStatus(),
                notification.getErrorMessage()
        );
    }

    private UserNotificationSetting findSettingById(Long settingId) {
        return userNotificationSettingRepository.findById(settingId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NOTIFICATION_SETTING_NOT_FOUND));
    }

    private void validateSettingOwner(User currentUser, UserNotificationSetting setting) {
        if (!Objects.equals(currentUser.getId(), setting.getUser().getId())) {
            log.warn(
                    "Отказано в доступе к настройке уведомлений: settingId={}, currentUserId={}, ownerUserId={}",
                    setting.getId(),
                    currentUser.getId(),
                    setting.getUser().getId()
            );

            throw new AccessDeniedException(Messages.ACCESS_DENIED);
        }
    }
}