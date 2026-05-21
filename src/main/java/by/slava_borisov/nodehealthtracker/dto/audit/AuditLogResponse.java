package by.slava_borisov.nodehealthtracker.dto.audit;

import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;

import java.time.LocalDateTime;

public record AuditLogResponse(

        Long id,

        AuditActionType actionType,

        String description,

        String entityType,

        Long entityId,

        Long userId,

        String username,

        LocalDateTime createdAt
) {
}