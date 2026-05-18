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
import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;
import by.slava_borisov.nodehealthtracker.repository.SentNotificationRepository;
import by.slava_borisov.nodehealthtracker.repository.UserNotificationSettingRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String NOTIFICATION_STATUS_SENT = "SENT";

    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final SentNotificationRepository sentNotificationRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public NotificationSettingResponse createNotificationSetting(NotificationSettingCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        userNotificationSettingRepository.findByUserIdAndChannel(
                currentUser.getId(),
                request.channel()
        ).ifPresent(setting -> {
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

        setting.setIsEnabled(request.isEnabled());
        setting.setDestination(request.destination());
        setting.setNotifyOnIncidentOpen(request.notifyOnIncidentOpen());
        setting.setNotifyOnIncidentResolved(request.notifyOnIncidentResolved());

        UserNotificationSetting savedSetting = userNotificationSettingRepository.save(setting);

        return toNotificationSettingResponse(savedSetting);
    }

    @Override
    @Transactional
    public void deleteNotificationSetting(Long settingId) {
        User currentUser = currentUserService.getCurrentUser();

        UserNotificationSetting setting = findSettingById(settingId);

        validateSettingOwner(currentUser, setting);

        userNotificationSettingRepository.delete(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationSettingResponse> getCurrentUserNotificationSettings() {
        User currentUser = currentUserService.getCurrentUser();

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

        return sentNotificationRepository
                .findAllByUserIdOrderBySentAtDesc(currentUser.getId())
                .stream()
                .map(this::toSentNotificationResponse)
                .toList();
    }

    @Override
    @Transactional
    public void notifyIncidentOpened(Incident incident) {
        sendIncidentNotification(incident, NotificationEvent.INCIDENT_OPENED);
    }

    @Override
    @Transactional
    public void notifyIncidentResolved(Incident incident) {
        sendIncidentNotification(incident, NotificationEvent.INCIDENT_RESOLVED);
    }

    private void sendIncidentNotification(Incident incident, NotificationEvent event) {
        User user = incident.getService()
                .getNode()
                .getOwner();

        List<UserNotificationSetting> enabledSettings = userNotificationSettingRepository
                .findAllByUserIdAndIsEnabledTrue(user.getId());

        enabledSettings.stream()
                .filter(setting -> shouldNotify(setting, event))
                .forEach(setting -> saveSentNotification(user, incident, setting, event));
    }

    private boolean shouldNotify(UserNotificationSetting setting, NotificationEvent event) {
        return switch (event) {
            case INCIDENT_OPENED -> Boolean.TRUE.equals(setting.getNotifyOnIncidentOpen());
            case INCIDENT_RESOLVED -> Boolean.TRUE.equals(setting.getNotifyOnIncidentResolved());
        };
    }

    private void saveSentNotification(
            User user,
            Incident incident,
            UserNotificationSetting setting,
            NotificationEvent event
    ) {
        SentNotification notification = new SentNotification();
        notification.setUser(user);
        notification.setIncident(incident);
        notification.setChannel(setting.getChannel());
        notification.setEvent(event);
        notification.setSentAt(LocalDateTime.now());
        notification.setStatus(NOTIFICATION_STATUS_SENT);
        notification.setErrorMessage(null);

        sentNotificationRepository.save(notification);
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
            throw new AccessDeniedException(Messages.ACCESS_DENIED);
        }
    }
}