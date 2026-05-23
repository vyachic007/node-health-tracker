import type { FailureLayer } from '../../services/model/serviceTypes';

export type IncidentStatus = 'OPEN' | 'RESOLVED';

export type IncidentSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type IncidentTimelineEventType =
    | 'CHECK_FAILED'
    | 'CHECK_SUCCESS'
    | 'SEVERITY_ASSIGNED'
    | 'INCIDENT_OPENED'
    | 'INCIDENT_RESOLVED'
    | 'NOTIFICATION_SENT'
    | 'NOTIFICATION_FAILED'
    | 'MANUAL_ACTION'
    | string;

export type RecurrenceLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Incident {
    id: number;
    serviceId: number;
    serviceName: string;
    status: IncidentStatus;
    severity: IncidentSeverity;
    openedAt: string;
    closedAt: string | null;
    reason: string;
    openedByCheckResultId: number | null;
    closedByCheckResultId: number | null;
}

export interface IncidentTimelineEvent {
    id: number;
    incidentId: number;
    checkResultId: number | null;
    eventType: IncidentTimelineEventType;
    message: string;
    createdAt: string;
}

export interface RecoveryChecklistItem {
    stepNumber: number;
    title: string;
    description: string;
    isCritical: boolean;
}

export interface IncidentRecoveryChecklist {
    incidentId: number;
    serviceId: number;
    serviceName: string;
    failureLayer: FailureLayer;
    severity: IncidentSeverity;
    summary: string;
    items: RecoveryChecklistItem[];
}

export interface IncidentReport {
    incidentId: number;
    serviceId: number;
    serviceName: string;
    status: IncidentStatus;
    severity: IncidentSeverity;
    failureLayer: FailureLayer;
    reason: string;
    recommendation: string;
    openedAt: string;
    closedAt: string | null;
    durationSeconds: number;
    durationMinutes: number;
    openedByCheckResultId: number | null;
    closedByCheckResultId: number | null;
    timelineEventsCount: number;
    summary: string;
}

export interface IncidentRecurrenceAnalysis {
    incidentId: number;
    serviceId: number;
    serviceName: string;
    failureLayer: FailureLayer;
    severity: IncidentSeverity;
    similarIncidentsLast24h: number;
    similarIncidentsLast7d: number;
    similarIncidentsLast30d: number;
    isRecurring: boolean;
    recurrenceLevel: RecurrenceLevel;
    recommendation: string;
}