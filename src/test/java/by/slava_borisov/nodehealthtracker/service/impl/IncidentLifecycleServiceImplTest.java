package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import by.slava_borisov.nodehealthtracker.service.IncidentSeverityService;
import by.slava_borisov.nodehealthtracker.service.IncidentTimelineService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты IncidentLifecycleServiceImpl")
class IncidentLifecycleServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private IncidentSeverityService incidentSeverityService;

    @Mock
    private IncidentTimelineService incidentTimelineService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private IncidentLifecycleServiceImpl incidentLifecycleService;

    private NetworkService networkService;
    private CheckResult checkResult;

    @BeforeEach
    void setUp() {
        networkService = new NetworkService();
        networkService.setId(10L);
        networkService.setName("Test Service");
        networkService.setFailureThreshold(3);
        networkService.setRecoveryThreshold(2);
        networkService.setConsecutiveFailures(0);
        networkService.setConsecutiveSuccesses(0);

        checkResult = new CheckResult();
        checkResult.setId(100L);
        checkResult.setService(networkService);
        checkResult.setDiagnosticMessage("Test failure reason");
    }

    @Test
    @DisplayName("Обработка DOWN проверки - порог не достигнут, инцидент не создается")
    void processCheckResult_downBelowThreshold_noIncidentCreated() {
        networkService.setConsecutiveFailures(1);
        checkResult.setStatus(ServiceStatus.DOWN);

        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);

        incidentLifecycleService.processCheckResult(checkResult);

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).notifyIncidentOpened(any(Incident.class));
    }

    @Test
    @DisplayName("Обработка DOWN проверки - порог достигнут, инцидент успешно создается")
    void processCheckResult_downThresholdReached_opensIncident() {
        networkService.setConsecutiveFailures(2);
        checkResult.setStatus(ServiceStatus.DOWN);

        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.empty());
        when(incidentSeverityService.determineSeverity(any(CheckResult.class)))
                .thenReturn(IncidentSeverity.HIGH);
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident saved = invocation.getArgument(0);
            saved.setId(500L);
            saved.setService(networkService);
            return saved;
        });

        incidentLifecycleService.processCheckResult(checkResult);

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));
        verify(incidentRepository, times(1)).save(any(Incident.class));
        verify(incidentTimelineService, times(3)).createEvent(any(Incident.class), any(CheckResult.class), any(IncidentTimelineEventType.class), any(String.class));
        verify(auditLogService, times(1)).logSystemAction(eq(AuditActionType.INCIDENT_OPENED), anyString(), eq("Incident"), eq(500L));
        verify(notificationService, times(1)).notifyIncidentOpened(any(Incident.class));
    }

    @Test
    @DisplayName("Обработка DOWN проверки - инцидент уже открыт, дубликат не создается")
    void processCheckResult_downThresholdReached_incidentAlreadyOpen_noDuplicate() {
        networkService.setConsecutiveFailures(2);
        checkResult.setStatus(ServiceStatus.DOWN);

        Incident existingIncident = new Incident();
        existingIncident.setId(500L);
        existingIncident.setService(networkService);

        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.of(existingIncident));

        incidentLifecycleService.processCheckResult(checkResult);

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).notifyIncidentOpened(any(Incident.class));
    }

    @Test
    @DisplayName("Обработка UP проверки - порог восстановления не достигнут, инцидент не закрывается")
    void processCheckResult_upBelowRecoveryThreshold_noIncidentClosed() {
        networkService.setConsecutiveSuccesses(1);
        checkResult.setStatus(ServiceStatus.UP);

        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);

        incidentLifecycleService.processCheckResult(checkResult);

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).notifyIncidentResolved(any(Incident.class));
    }

    @Test
    @DisplayName("Обработка UP проверки - порог восстановления достигнут, инцидент успешно закрывается")
    void processCheckResult_upRecoveryThresholdReached_closesIncident() {
        networkService.setConsecutiveSuccesses(1);
        checkResult.setStatus(ServiceStatus.UP);

        Incident openIncident = new Incident();
        openIncident.setId(500L);
        openIncident.setService(networkService);
        openIncident.setStatus(IncidentStatus.OPEN);

        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.of(openIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident saved = invocation.getArgument(0);
            saved.setId(500L);
            saved.setService(networkService);
            return saved;
        });

        incidentLifecycleService.processCheckResult(checkResult);

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));

        ArgumentCaptor<Incident> incidentCaptor = ArgumentCaptor.forClass(Incident.class);
        verify(incidentRepository, times(1)).save(incidentCaptor.capture());
        assertEquals(IncidentStatus.RESOLVED, incidentCaptor.getValue().getStatus());
        assertNotNull(incidentCaptor.getValue().getClosedAt());

        verify(incidentTimelineService, times(2)).createEvent(any(Incident.class), any(CheckResult.class), any(IncidentTimelineEventType.class), any(String.class));
        verify(auditLogService, times(1)).logSystemAction(eq(AuditActionType.INCIDENT_RESOLVED), anyString(), eq("Incident"), eq(500L));
        verify(notificationService, times(1)).notifyIncidentResolved(any(Incident.class));
    }

    @Test
    @DisplayName("Обработка UP проверки - порог восстановления достигнут, но открытого инцидента нет")
    void processCheckResult_upRecoveryThresholdReached_noOpenIncident_doesNothing() {
        networkService.setConsecutiveSuccesses(1);
        checkResult.setStatus(ServiceStatus.UP);

        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.empty());

        incidentLifecycleService.processCheckResult(checkResult);

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));
        verify(incidentRepository, never()).save(any(Incident.class));
        verify(notificationService, never()).notifyIncidentResolved(any(Incident.class));
    }
}