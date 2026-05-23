import type {
    CheckType,
    FailureLayer,
    HealthLevel,
    RecurrenceLevel,
    ServiceStatus,
} from './serviceTypes';

export const checkTypeLabels: Record<CheckType, string> = {
    PING: 'Пинг',
    TCP: 'TCP-порт',
    HTTP: 'HTTP',
    HTTPS: 'HTTPS',
    DNS: 'DNS',
    SSL: 'SSL-сертификат',
    HEARTBEAT: 'Heartbeat',
};

export const serviceStatusLabels: Record<ServiceStatus, string> = {
    UP: 'Работает',
    DOWN: 'Недоступен',
};

export const healthLevelLabels: Record<HealthLevel, string> = {
    HEALTHY: 'Стабильный',
    DEGRADED: 'Есть ухудшения',
    UNSTABLE: 'Нестабильный',
    CRITICAL: 'Критический',
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

export function getServiceStatusLabel(status: ServiceStatus | null): string {
    if (!status) {
        return 'Не проверялся';
    }

    return serviceStatusLabels[status];
}

export function getHealthLevelLabel(level: HealthLevel): string {
    return healthLevelLabels[level];
}

export function getCheckTypeLabel(type: CheckType): string {
    return checkTypeLabels[type];
}