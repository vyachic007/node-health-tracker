import type {
    NotificationChannel,
    NotificationEvent,
    SentNotificationStatus,
} from './notificationTypes';

export const notificationChannelLabels: Record<NotificationChannel, string> = {
    EMAIL: 'Email',
    TELEGRAM: 'Telegram',
    VK: 'VK',
};

export function getNotificationEventLabel(event: NotificationEvent): string {
    switch (event) {
        case 'INCIDENT_OPENED':
            return 'Инцидент открыт';
        case 'INCIDENT_RESOLVED':
            return 'Инцидент закрыт';
        default:
            return event;
    }
}

export function getSentStatusLabel(status: SentNotificationStatus): string {
    switch (status) {
        case 'SENT':
            return 'Отправлено';
        case 'FAILED':
            return 'Ошибка';
        default:
            return status;
    }
}

export function getSentStatusColor(status: SentNotificationStatus) {
    switch (status) {
        case 'SENT':
            return 'success';
        case 'FAILED':
            return 'error';
        default:
            return 'default';
    }
}