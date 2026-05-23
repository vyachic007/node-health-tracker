export type AuditEventType =
    | 'USER_LOGIN'
    | 'USER_LOGOUT'
    | 'USER_REGISTERED'
    | 'NODE_CREATED'
    | 'NODE_UPDATED'
    | 'NODE_DELETED'
    | 'SERVICE_CREATED'
    | 'SERVICE_UPDATED'
    | 'SERVICE_DELETED'
    | 'CHECK_STARTED'
    | 'CHECK_FINISHED'
    | 'INCIDENT_OPENED'
    | 'INCIDENT_RESOLVED'
    | 'NOTIFICATION_SENT'
    | 'NOTIFICATION_FAILED'
    | 'TELEGRAM_CONNECTED'
    | 'TELEGRAM_DISCONNECTED'
    | 'PASSWORD_RESET_REQUESTED'
    | 'PASSWORD_RESET_CONFIRMED'
    | 'UNKNOWN';

export type AuditSeverity = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR';

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