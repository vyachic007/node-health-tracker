import { apiClient } from '../../../shared/api/apiClient';
import type {
    CreateNotificationSettingRequest,
    NotificationSetting,
    SentNotification,
    TelegramBindLinkResponse,
    UpdateNotificationSettingRequest,
} from '../model/notificationTypes';

export const notificationsApi = {
    async getSettings(): Promise<NotificationSetting[]> {
        const response = await apiClient.get<NotificationSetting[]>(
            '/api/notifications/settings',
        );

        return response.data as NotificationSetting[];
    },

    async createSetting(
        payload: CreateNotificationSettingRequest,
    ): Promise<NotificationSetting> {
        const response = await apiClient.post<NotificationSetting>(
            '/api/notifications/settings',
            payload,
        );

        return response.data as NotificationSetting;
    },

    async updateSetting(
        settingId: number,
        payload: UpdateNotificationSettingRequest,
    ): Promise<NotificationSetting> {
        const response = await apiClient.patch<NotificationSetting>(
            `/api/notifications/settings/${settingId}`,
            payload,
        );

        return response.data as NotificationSetting;
    },

    async deleteSetting(settingId: number): Promise<void> {
        await apiClient.delete(`/api/notifications/settings/${settingId}`);
    },

    async createTelegramBindLink(): Promise<TelegramBindLinkResponse> {
        const response = await apiClient.post<TelegramBindLinkResponse>(
            '/api/notifications/telegram/bind-link',
        );

        return response.data as TelegramBindLinkResponse;
    },

    async getSentNotifications(): Promise<SentNotification[]> {
        const response = await apiClient.get<SentNotification[]>(
            '/api/notifications/sent',
        );

        return response.data as SentNotification[];
    },
};