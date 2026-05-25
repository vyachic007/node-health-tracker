import { apiClient } from '../../../shared/api/apiClient';
import type {
    AuditEvent,
    AuditLogResponse,
    AuditSeverity,
} from '../model/auditTypes';

function getSeverityByActionType(actionType: string): AuditSeverity {
    if (
        actionType.includes('DELETED') ||
        actionType.includes('BLOCKED') ||
        actionType.includes('FAILED')
    ) {
        return 'ERROR';
    }

    if (
        actionType.includes('INCIDENT_OPENED') ||
        actionType.includes('UPDATED') ||
        actionType.includes('CHECK_STARTED')
    ) {
        return 'WARNING';
    }

    if (
        actionType.includes('CREATED') ||
        actionType.includes('RESOLVED') ||
        actionType.includes('UNBLOCKED') ||
        actionType.includes('ROLE_UPDATED')
    ) {
        return 'SUCCESS';
    }

    return 'INFO';
}

function mapAuditLogToEvent(log: AuditLogResponse): AuditEvent {
    return {
        id: log.id,
        eventType: log.actionType,
        severity: getSeverityByActionType(log.actionType),
        username: log.username,
        userId: log.userId,
        entityType: log.entityType,
        entityId: log.entityId,
        message: log.description,
        ipAddress: null,
        userAgent: null,
        createdAt: log.createdAt,
    };
}

export const auditApi = {
    async getMyEvents(): Promise<AuditEvent[]> {
        const { data } = await apiClient.get<AuditLogResponse[]>(
            '/api/audit/my',
        );

        return data.map(mapAuditLogToEvent);
    },

    async getAdminEvents(): Promise<AuditEvent[]> {
        const { data } = await apiClient.get<AuditLogResponse[]>(
            '/api/admin/audit',
        );

        return data.map(mapAuditLogToEvent);
    },

    async getEvents(isAdmin: boolean): Promise<AuditEvent[]> {
        if (isAdmin) {
            return auditApi.getAdminEvents();
        }

        return auditApi.getMyEvents();
    },
};