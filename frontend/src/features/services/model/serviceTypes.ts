export type CheckType =
    | 'PING'
    | 'TCP'
    | 'HTTP'
    | 'HTTPS'
    | 'DNS'
    | 'SSL'
    | 'HEARTBEAT';

export type ServiceStatus = 'UP' | 'DOWN';

export type FailureLayer =
    | 'DNS'
    | 'NETWORK'
    | 'PORT'
    | 'APPLICATION'
    | 'SSL'
    | 'PERFORMANCE'
    | 'HEARTBEAT'
    | 'UNKNOWN';

export type HealthLevel = 'HEALTHY' | 'DEGRADED' | 'UNSTABLE' | 'CRITICAL';

export type RecurrenceLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface NetworkService {
    id: number;
    nodeId: number;
    checkType: CheckType;
    heartbeatToken: string | null;
    lastHeartbeatAt: string | null;
    lastCheckedAt: string | null;
    name: string;
    targetHost: string;
    port: number | null;
    path: string | null;
    intervalSeconds: number;
    isEnabled: boolean;
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
}

export interface UpdateNetworkServiceRequest {
    checkType: CheckType;
    name: string;
    targetHost: string;
    port: number | null;
    path: string | null;
    intervalSeconds: number;
    isEnabled: boolean;
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