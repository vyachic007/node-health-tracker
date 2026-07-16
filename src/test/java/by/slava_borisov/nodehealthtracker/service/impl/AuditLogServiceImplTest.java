package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.audit.AuditLogResponse;
import by.slava_borisov.nodehealthtracker.model.entity.AuditLog;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.repository.AuditLogRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты AuditLogServiceImpl")
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test_admin");
    }


    @Test
    @DisplayName("Записать действие пользователя в аудит - успешно")
    void log_success() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);

        AuditActionType action = AuditActionType.USER_ROLE_UPDATED;
        String description = "User role was updated to ADMIN";
        String entityType = "User";
        Long entityId = 42L;

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);

        auditLogService.log(action, description, entityType, entityId);

        verify(auditLogRepository, times(1)).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();

        assertEquals(action, savedLog.getActionType());
        assertEquals(description, savedLog.getDescription());
        assertEquals(entityType, savedLog.getEntityType());
        assertEquals(entityId, savedLog.getEntityId());
        assertEquals(1L, savedLog.getUserId());
        assertEquals("test_admin", savedLog.getUsername());
        assertNotNull(savedLog.getCreatedAt());
    }


    @Test
    @DisplayName("Записать системное действие в аудит - успешно")
    void logSystemAction_success() {
        AuditActionType action = AuditActionType.CHECK_STARTED;
        String description = "Scheduled health check started";
        String entityType = "System";
        Long entityId = 0L;

        ArgumentCaptor<AuditLog> logCaptor = ArgumentCaptor.forClass(AuditLog.class);

        auditLogService.logSystemAction(action, description, entityType, entityId);

        verify(auditLogRepository, times(1)).save(logCaptor.capture());
        AuditLog savedLog = logCaptor.getValue();

        assertEquals(action, savedLog.getActionType());
        assertEquals(description, savedLog.getDescription());
        assertEquals(entityType, savedLog.getEntityType());
        assertEquals(entityId, savedLog.getEntityId());
        assertNull(savedLog.getUserId());
        assertEquals("SYSTEM", savedLog.getUsername());
        assertNotNull(savedLog.getCreatedAt());

        verify(currentUserService, never()).getCurrentUser();
    }


    @Test
    @DisplayName("Получить логи аудита текущего пользователя - успешно")
    void getCurrentUserAuditLogs_success() {
        when(currentUserService.getCurrentUser()).thenReturn(mockUser);

        AuditLog log1 = createMockAuditLog(10L, AuditActionType.USER_BLOCKED, "test_admin");
        AuditLog log2 = createMockAuditLog(11L, AuditActionType.USER_UNBLOCKED, "test_admin");

        when(auditLogRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(log1, log2));

        List<AuditLogResponse> result = auditLogService.getCurrentUserAuditLogs();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(AuditActionType.USER_BLOCKED, result.get(0).actionType());
        assertEquals("test_admin", result.get(0).username());

        verify(auditLogRepository, times(1)).findAllByUserIdOrderByCreatedAtDesc(1L);
    }


    @Test
    @DisplayName("Получить все логи аудита системы - успешно")
    void getAllAuditLogs_success() {
        AuditLog log1 = createMockAuditLog(20L, AuditActionType.NODE_CREATED, "admin1");
        AuditLog log2 = createMockAuditLog(21L, AuditActionType.SERVICE_DELETED, "admin2");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(log1, log2));

        List<AuditLogResponse> result = auditLogService.getAllAuditLogs();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(AuditActionType.NODE_CREATED, result.get(0).actionType());
        assertEquals("admin1", result.get(0).username());

        verify(auditLogRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }


    private AuditLog createMockAuditLog(Long id, AuditActionType actionType, String username) {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setActionType(actionType);
        log.setDescription("Test description");
        log.setEntityType("User");
        log.setEntityId(1L);
        log.setUserId(1L);
        log.setUsername(username);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}