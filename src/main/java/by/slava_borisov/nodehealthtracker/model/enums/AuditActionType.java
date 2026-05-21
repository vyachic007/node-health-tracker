package by.slava_borisov.nodehealthtracker.model.enums;

public enum AuditActionType {

    NODE_CREATED,
    NODE_UPDATED,
    NODE_DELETED,

    SERVICE_CREATED,
    SERVICE_UPDATED,
    SERVICE_DELETED,

    CHECK_STARTED,

    INCIDENT_OPENED,
    INCIDENT_RESOLVED,

    USER_BLOCKED,
    USER_UNBLOCKED,
    USER_ROLE_UPDATED
}