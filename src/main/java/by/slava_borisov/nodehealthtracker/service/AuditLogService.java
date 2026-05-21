package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.audit.AuditLogResponse;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;

import java.util.List;

public interface AuditLogService {

    void log(
            AuditActionType actionType,
            String description,
            String entityType,
            Long entityId
    );

    void logSystemAction(
            AuditActionType actionType,
            String description,
            String entityType,
            Long entityId
    );

    List<AuditLogResponse> getCurrentUserAuditLogs();

    List<AuditLogResponse> getAllAuditLogs();
}