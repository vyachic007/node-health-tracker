package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentReportResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentTimelineEventRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты IncidentReportServiceImpl")
class IncidentReportServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentTimelineEventRepository incidentTimelineEventRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private IncidentReportServiceImpl incidentReportService;

    private User ownerUser;
    private User differentUser;
    private NetworkNode networkNode;
    private NetworkService networkService;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner");

        differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("other");

        networkNode = mock(NetworkNode.class);
        lenient().when(networkNode.getOwner()).thenReturn(ownerUser);

        networkService = new NetworkService();
        networkService.setId(10L);
        networkService.setName("Test Service");
        networkService.setNode(networkNode);
    }

    @Test
    @DisplayName("Получить отчет по инциденту - инцидент не найден")
    void getIncidentReport_incidentNotFound_throwsException() {
        when(incidentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentReportService.getIncidentReport(100L)
        );
    }

    @Test
    @DisplayName("Получить отчет по инциденту - отказ в доступе")
    void getIncidentReport_accessDenied_throwsException() {
        Incident incident = createIncident(100L, IncidentStatus.OPEN);

        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        assertThrows(
                AccessDeniedException.class,
                () -> incidentReportService.getIncidentReport(100L)
        );
    }

    @Test
    @DisplayName("Получить отчет по инциденту - успешно (OPEN, checkResult null)")
    void getIncidentReport_success_openIncident_nullCheckResult() {
        Incident incident = createIncident(100L, IncidentStatus.OPEN);
        incident.setOpenedAt(LocalDateTime.now().minusHours(2));
        incident.setOpenedByCheckResult(null);
        incident.setSeverity(IncidentSeverity.HIGH);
        incident.setReason("Test reason");

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentTimelineEventRepository.countByIncidentId(100L)).thenReturn(5L);

        IncidentReportResponse response = incidentReportService.getIncidentReport(100L);

        assertNotNull(response);
        assertEquals(100L, response.incidentId());
        assertEquals(10L, response.serviceId());
        assertEquals("Test Service", response.serviceName());
        assertEquals(IncidentStatus.OPEN, response.status());
        assertEquals(IncidentSeverity.HIGH, response.severity());
        assertEquals(FailureLayer.UNKNOWN, response.failureLayer());
        assertNull(response.recommendation());
        assertNull(response.openedByCheckResultId());
        assertNull(response.closedByCheckResultId());
        assertEquals(5, response.timelineEventsCount());
        assertNotNull(response.durationSeconds());
        assertNotNull(response.durationMinutes());
        assertTrue(response.durationSeconds() >= 7199);
        assertTrue(response.durationSeconds() <= 7201);
        assertEquals(120L, response.durationMinutes());
        assertNotNull(response.summary());
    }

    @Test
    @DisplayName("Получить отчет по инциденту - успешно (RESOLVED, с checkResult)")
    void getIncidentReport_success_resolvedIncident_withCheckResult() {
        Incident incident = createIncident(100L, IncidentStatus.RESOLVED);
        LocalDateTime openedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 1, 12, 30);
        incident.setOpenedAt(openedAt);
        incident.setClosedAt(closedAt);
        incident.setSeverity(IncidentSeverity.MEDIUM);
        incident.setReason("DNS failure");

        CheckResult openedCheckResult = mock(CheckResult.class);
        when(openedCheckResult.getFailureLayer()).thenReturn(FailureLayer.DNS);
        when(openedCheckResult.getRecommendation()).thenReturn("Check DNS settings");
        when(openedCheckResult.getId()).thenReturn(500L);
        incident.setOpenedByCheckResult(openedCheckResult);

        CheckResult closedCheckResult = mock(CheckResult.class);
        when(closedCheckResult.getId()).thenReturn(501L);
        incident.setClosedByCheckResult(closedCheckResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentTimelineEventRepository.countByIncidentId(100L)).thenReturn(8L);

        IncidentReportResponse response = incidentReportService.getIncidentReport(100L);

        assertNotNull(response);
        assertEquals(IncidentStatus.RESOLVED, response.status());
        assertEquals(FailureLayer.DNS, response.failureLayer());
        assertEquals("Check DNS settings", response.recommendation());
        assertEquals(500L, response.openedByCheckResultId());
        assertEquals(501L, response.closedByCheckResultId());
        assertEquals(8, response.timelineEventsCount());
        assertEquals(9000L, response.durationSeconds());
        assertEquals(150L, response.durationMinutes());
        assertNotNull(response.summary());
    }

    @Test
    @DisplayName("Получить отчет по инциденту - успешно (OPEN, failureLayer APPLICATION)")
    void getIncidentReport_success_openIncident_applicationLayer() {
        Incident incident = createIncident(100L, IncidentStatus.OPEN);
        incident.setOpenedAt(LocalDateTime.now().minusMinutes(30));
        incident.setSeverity(IncidentSeverity.LOW);

        CheckResult checkResult = mock(CheckResult.class);
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.APPLICATION);
        when(checkResult.getRecommendation()).thenReturn("Restart service");
        when(checkResult.getId()).thenReturn(600L);
        incident.setOpenedByCheckResult(checkResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentTimelineEventRepository.countByIncidentId(100L)).thenReturn(3L);

        IncidentReportResponse response = incidentReportService.getIncidentReport(100L);

        assertNotNull(response);
        assertEquals(FailureLayer.APPLICATION, response.failureLayer());
        assertEquals("Restart service", response.recommendation());
        assertEquals(600L, response.openedByCheckResultId());
        assertNull(response.closedByCheckResultId());
        assertEquals(3, response.timelineEventsCount());
        assertNotNull(response.summary());
    }

    private Incident createIncident(Long id, IncidentStatus status) {
        Incident incident = new Incident();
        incident.setId(id);
        incident.setService(networkService);
        incident.setStatus(status);
        return incident;
    }
}