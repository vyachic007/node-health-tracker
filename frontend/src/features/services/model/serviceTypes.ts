export type CheckType =
    | 'PING'
    | 'TCP'
    | 'HTTP'
    | 'HTTPS'
    | 'DNS'
    | 'SSL';

export type ServiceStatus = 'UP' | 'DOWN';

export type FailureLayer =
    | 'DNS'
    | 'NETWORK'
    | 'PORT'
    | 'APPLICATION'
    | 'SSL'
    | 'PERFORMANCE'
    | 'UNKNOWN';

export type HealthLevel = 'HEALTHY' | 'DEGRADED' | 'UNSTABLE' | 'CRITICAL';

export type RecurrenceLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface NetworkService {
    id: number;
    nodeId: number;
    checkType: CheckType;
    lastCheckedAt: string | null;
    name: string;
    targetHost: string;
    port: number | null;
    path: string | null;
    intervalSeconds: number;
    isEnabled: boolean;

    responseTimeThresholdMs: number;
    degradationThreshold: number;
    consecutiveDegradations: number;
    degraded: boolean;

    notifyEmail: boolean;
    notifyTelegram: boolean;
    notifyVk: boolean;

    lastStatus: ServiceStatus | null;
    lastResponseTimeMs: number | null;
    lastHttpStatusCode: number | null;
    lastFailureLayer: FailureLayer | null;
    lastDiagnosticMessage: string | null;
    lastRecommendation: string | null;

    nextCheckAt: string | null;
    secondsUntilNextCheck: number | null;

    hasOpenIncident: boolean;
    openIncidentId: number | null;
    currentDowntimeSeconds: number;

    availabilityPercent24h: number | null;
    averageResponseTimeMs24h: number | null;

    healthScore: number;
    healthLevel: HealthLevel;
    recurrenceLevel: RecurrenceLevel;

    createdAt: string;
    updatedAt: string;
}

export interface CreateNetworkServiceRequest {
    nodeId: number;
    checkType: CheckType;
    name: string;
    targetHost: string;
    port: number | null;
    path: string | null;
    intervalSeconds: number;

    responseTimeThresholdMs: number;
    degradationThreshold: number;

    notifyEmail: boolean;
    notifyTelegram: boolean;
    notifyVk: boolean;
}

export interface UpdateNetworkServiceRequest {
    checkType: CheckType;
    name: string;
    targetHost: string;
    port: number | null;
    path: string | null;
    intervalSeconds: number;
    isEnabled: boolean;

    responseTimeThresholdMs: number;
    degradationThreshold: number;

    notifyEmail: boolean;
    notifyTelegram: boolean;
    notifyVk: boolean;
}

export interface CheckResult {
    id: number;
    serviceId: number;
    status: ServiceStatus;
    failureLayer: FailureLayer;
    diagnosticMessage: string;
    recommendation: string;
    startedAt: string;
    finishedAt: string;
    responseTimeMs: number | null;
    httpStatusCode: number | null;
    errorMessage: string | null;
    checkedAt: string;
}

export function checkTypeNeedsPort(checkType: CheckType): boolean {
    return checkType === 'TCP'
        || checkType === 'HTTP'
        || checkType === 'HTTPS'
        || checkType === 'SSL';
}

export function checkTypeNeedsPath(checkType: CheckType): boolean {
    return checkType === 'HTTP' || checkType === 'HTTPS';
}

export function checkTypeSupportsDegradation(checkType: CheckType): boolean {
    return checkType === 'HTTP' || checkType === 'HTTPS';
}

export function getDefaultPortByCheckType(checkType: CheckType): number | null {
    switch (checkType) {
        case 'HTTP':
            return 80;

        case 'HTTPS':
        case 'SSL':
            return 443;

        default:
            return null;
    }
}

export function getDefaultPathByCheckType(checkType: CheckType): string {
    return checkTypeNeedsPath(checkType) ? '/' : '';
}

export function getServiceTargetLabel(service: NetworkService): string {
    const port = checkTypeNeedsPort(service.checkType) && service.port
        ? `:${service.port}`
        : '';

    const path = checkTypeNeedsPath(service.checkType) && service.path
        ? service.path
        : '';

    return `${service.targetHost}${port}${path}`;
}