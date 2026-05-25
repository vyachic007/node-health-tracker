export type AuditActionType =
    | 'NODE_CREATED'
    | 'NODE_UPDATED'
    | 'NODE_DELETED'
    | 'SERVICE_CREATED'
    | 'SERVICE_UPDATED'
    | 'SERVICE_DELETED'
    | 'CHECK_STARTED'
    | 'INCIDENT_OPENED'
    | 'INCIDENT_RESOLVED'
    | 'USER_BLOCKED'
    | 'USER_UNBLOCKED'
    | 'USER_ROLE_UPDATED'
    | string;

export type AuditEventType =
    | AuditActionType
    | 'USER_LOGIN'
    | 'USER_LOGOUT'
    | 'USER_REGISTERED'
    | 'CHECK_FINISHED'
    | 'NOTIFICATION_SENT'
    | 'NOTIFICATION_FAILED'
    | 'TELEGRAM_CONNECTED'
    | 'TELEGRAM_DISCONNECTED'
    | 'PASSWORD_RESET_REQUESTED'
    | 'PASSWORD_RESET_CONFIRMED'
    | 'UNKNOWN';

export type AuditSeverity = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';

export interface AuditLogResponse {
    id: number;
    actionType: AuditActionType;
    description: string;
    entityType: string;
    entityId: number | null;
    userId: number | null;
    username: string | null;
    createdAt: string;
}

export interface AuditEvent {
    id: number;
    eventType: AuditEventType;
    severity: AuditSeverity;
    username: string | null;
    userId: number | null;
    entityType: string | null;
    entityId: number | null;
    message: string;
    ipAddress: string | null;
    userAgent: string | null;
    createdAt: string;
}