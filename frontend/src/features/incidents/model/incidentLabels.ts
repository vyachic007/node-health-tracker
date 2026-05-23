import type { FailureLayer } from '../../services/model/serviceTypes';
import type {
    IncidentSeverity,
    IncidentStatus,
    IncidentTimelineEventType,
    RecurrenceLevel,
} from './incidentTypes';

export const incidentStatusLabels: Record<IncidentStatus, string> = {
    OPEN: 'Открыт',
    RESOLVED: 'Закрыт',
};

export const severityLabels: Record<IncidentSeverity, string> = {
    LOW: 'Низкая',
    MEDIUM: 'Средняя',
    HIGH: 'Высокая',
    CRITICAL: 'Критическая',
};

export const failureLayerLabels: Record<FailureLayer, string> = {
    DNS: 'DNS',
    NETWORK: 'Сеть',
    PORT: 'Порт',
    APPLICATION: 'Приложение',
    SSL: 'SSL',
    PERFORMANCE: 'Производительность',
    HEARTBEAT: 'Heartbeat',
    UNKNOWN: 'Не определён',
};

export const recurrenceLevelLabels: Record<RecurrenceLevel, string> = {
    LOW: 'Низкая',
    MEDIUM: 'Средняя',
    HIGH: 'Высокая',
};

export function getIncidentStatusColor(status: IncidentStatus) {
    switch (status) {
        case 'OPEN':
            return 'error';
        case 'RESOLVED':
            return 'success';
        default:
            return 'default';
    }
}

export function getSeverityColor(severity: IncidentSeverity) {
    switch (severity) {
        case 'LOW':
            return 'success';
        case 'MEDIUM':
            return 'warning';
        case 'HIGH':
        case 'CRITICAL':
            return 'error';
        default:
            return 'default';
    }
}

export function getTimelineEventLabel(eventType: IncidentTimelineEventType) {
    switch (eventType) {
        case 'CHECK_FAILED':
            return 'Проверка завершилась ошибкой';
        case 'CHECK_SUCCESS':
            return 'Проверка успешна';
        case 'SEVERITY_ASSIGNED':
            return 'Назначена критичность';
        case 'INCIDENT_OPENED':
            return 'Инцидент открыт';
        case 'INCIDENT_RESOLVED':
            return 'Инцидент закрыт';
        case 'NOTIFICATION_SENT':
            return 'Уведомление отправлено';
        case 'NOTIFICATION_FAILED':
            return 'Ошибка уведомления';
        case 'MANUAL_ACTION':
            return 'Ручное действие';
        default:
            return eventType;
    }
}