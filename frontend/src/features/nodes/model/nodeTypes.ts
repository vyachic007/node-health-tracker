export type NodeHealthStatus =
    | 'HEALTHY'
    | 'DEGRADED'
    | 'UNSTABLE'
    | 'CRITICAL'
    | 'UNKNOWN';

export type HealthLevel =
    | 'HEALTHY'
    | 'DEGRADED'
    | 'UNSTABLE'
    | 'CRITICAL';

export interface NetworkNode {
    id: number;
    ownerId: number;
    name: string;
    host: string;
    description: string | null;
    isActive: boolean;

    healthStatus: NodeHealthStatus;

    totalServices: number;
    enabledServices: number;
    disabledServices: number;
    upServices: number;
    downServices: number;
    unknownServices: number;
    openIncidents: number;

    lastCheckedAt: string | null;
    availabilityPercent24h: number | null;
    averageResponseTimeMs24h: number | null;

    healthScore: number;
    healthLevel: HealthLevel;

    healthyServicesCount: number;
    degradedServicesCount: number;
    unstableServicesCount: number;
    criticalServicesCount: number;

    createdAt: string;
    updatedAt: string;
}

export interface CreateNetworkNodeRequest {
    name: string;
    host: string;
    description: string | null;
}

export interface UpdateNetworkNodeRequest {
    name: string;
    host: string;
    description: string | null;
    isActive: boolean;
}