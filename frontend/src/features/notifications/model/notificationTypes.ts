export type NotificationChannel = 'EMAIL' | 'TELEGRAM' | 'VK';

export type NotificationEvent =
    | 'INCIDENT_OPENED'
    | 'INCIDENT_RESOLVED'
    | string;

export type SentNotificationStatus = 'SENT' | 'FAILED' | string;

export interface NotificationSetting {
    id: number;
    userId: number;
    channel: NotificationChannel;
    isEnabled: boolean;
    destination: string;
    notifyOnIncidentOpen: boolean;
    notifyOnIncidentResolved: boolean;
}

export interface CreateNotificationSettingRequest {
    channel: NotificationChannel;
    isEnabled: boolean;
    destination: string;
    notifyOnIncidentOpen: boolean;
    notifyOnIncidentResolved: boolean;
}

export interface UpdateNotificationSettingRequest {
    isEnabled: boolean;
    destination: string;
    notifyOnIncidentOpen: boolean;
    notifyOnIncidentResolved: boolean;
}

export interface TelegramBindLinkResponse {
    bindToken: string;
    botUsername: string;
    telegramLink: string;
    expiresAt: string;
}

export interface SentNotification {
    id: number;
    userId: number;
    incidentId: number;
    channel: NotificationChannel;
    event: NotificationEvent;
    sentAt: string;
    status: SentNotificationStatus;
    errorMessage: string | null;
}