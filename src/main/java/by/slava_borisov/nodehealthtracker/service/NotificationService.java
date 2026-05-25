package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.SentNotificationResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.VkBindLinkResponse;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;

import java.util.List;

public interface NotificationService {

    NotificationSettingResponse createNotificationSetting(NotificationSettingCreateRequest request);

    NotificationSettingResponse updateNotificationSetting(Long settingId, NotificationSettingUpdateRequest request);

    void deleteNotificationSetting(Long settingId);

    List<NotificationSettingResponse> getCurrentUserNotificationSettings();

    List<SentNotificationResponse> getCurrentUserSentNotifications();

    VkBindLinkResponse createVkBindLink();

    void connectVkByBindToken(String bindToken, String vkPeerId);

    void notifyIncidentOpened(Incident incident);

    void notifyIncidentResolved(Incident incident);
}
