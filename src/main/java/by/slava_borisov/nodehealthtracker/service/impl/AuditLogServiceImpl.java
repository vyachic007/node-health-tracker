package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.audit.AuditLogResponse;
import by.slava_borisov.nodehealthtracker.model.entity.AuditLog;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.repository.AuditLogRepository;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final String SYSTEM_USERNAME = "SYSTEM";

    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            AuditActionType actionType,
            String description,
            String entityType,
            Long entityId
    ) {
        User currentUser = currentUserService.getCurrentUser();

        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setDescription(description);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setUserId(currentUser.getId());
        auditLog.setUsername(currentUser.getUsername());
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystemAction(
            AuditActionType actionType,
            String description,
            String entityType,
            Long entityId
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActionType(actionType);
        auditLog.setDescription(description);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setUserId(null);
        auditLog.setUsername(SYSTEM_USERNAME);
        auditLog.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getCurrentUserAuditLogs() {
        User currentUser = currentUserService.getCurrentUser();

        return auditLogRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActionType(),
                auditLog.getDescription(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getUserId(),
                auditLog.getUsername(),
                auditLog.getCreatedAt()
        );
    }
}