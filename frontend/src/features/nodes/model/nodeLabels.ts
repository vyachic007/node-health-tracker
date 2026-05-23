import type { HealthLevel, NodeHealthStatus } from './nodeTypes';

export const nodeHealthStatusLabels: Record<NodeHealthStatus, string> = {
    HEALTHY: 'Стабильный',
    DEGRADED: 'Снижен',
    UNSTABLE: 'Нестабильный',
    CRITICAL: 'Критический',
    UNKNOWN: 'Неизвестно',
};

export const healthLevelLabels: Record<HealthLevel, string> = {
    HEALTHY: 'Стабильный',
    DEGRADED: 'Снижен',
    UNSTABLE: 'Нестабильный',
    CRITICAL: 'Критический',
};

export function getHealthColor(level: HealthLevel | NodeHealthStatus) {
    switch (level) {
        case 'HEALTHY':
            return 'success';
        case 'DEGRADED':
        case 'UNSTABLE':
            return 'warning';
        case 'CRITICAL':
            return 'error';
        case 'UNKNOWN':
        default:
            return 'default';
    }
}