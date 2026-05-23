package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.SentNotificationResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.TelegramBindLinkResponse;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import by.slava_borisov.nodehealthtracker.service.TelegramBindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final TelegramBindingService telegramBindingService;

    @GetMapping("/settings")
    public List<NotificationSettingResponse> getCurrentUserNotificationSettings() {
        return notificationService.getCurrentUserNotificationSettings();
    }

    @PostMapping("/settings")
    public NotificationSettingResponse createNotificationSetting(
            @Valid @RequestBody NotificationSettingCreateRequest request
    ) {
        return notificationService.createNotificationSetting(request);
    }

    @PatchMapping("/settings/{settingId}")
    public NotificationSettingResponse updateNotificationSetting(
            @PathVariable Long settingId,
            @Valid @RequestBody NotificationSettingUpdateRequest request
    ) {
        return notificationService.updateNotificationSetting(settingId, request);
    }

    @DeleteMapping("/settings/{settingId}")
    public void deleteNotificationSetting(@PathVariable Long settingId) {
        notificationService.deleteNotificationSetting(settingId);
    }

    @GetMapping("/sent")
    public List<SentNotificationResponse> getCurrentUserSentNotifications() {
        return notificationService.getCurrentUserSentNotifications();
    }

    @PostMapping("/telegram/bind-link")
    public TelegramBindLinkResponse createTelegramBindLink() {
        return telegramBindingService.createCurrentUserBindLink();
    }
}