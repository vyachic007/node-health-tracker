package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты ServiceHealthScoreServiceImpl")
class ServiceHealthScoreServiceImplTest {

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ServiceHealthScoreServiceImpl serviceHealthScoreService;

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
    @DisplayName("Рассчитать здоровье сервиса - сервис не найден")
    void calculateHealthScore_serviceNotFound_throwsException() {
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> serviceHealthScoreService.calculateHealthScore(10L)
        );
    }

    @Test
    @DisplayName("Рассчитать здоровье сервиса - отказ в доступе")
    void calculateHealthScore_accessDenied_throwsException() {
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);

        assertThrows(
                AccessDeniedException.class,
                () -> serviceHealthScoreService.calculateHealthScore(10L)
        );
    }

    @Test
    @DisplayName("Рассчитать здоровье сервиса - идеально здоров (100 баллов)")
    void calculateHealthScore_perfectHealth_returns100() {
        CheckResult latestCheck = mock(CheckResult.class);
        when(latestCheck.getStatus()).thenReturn(ServiceStatus.UP);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(checkResultRepository.findTopByServiceIdOrderByCheckedAtDesc(10L))
                .thenReturn(Optional.of(latestCheck));
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.empty());
        when(incidentRepository.findTopByServiceIdOrderByOpenedAtDesc(10L))
                .thenReturn(Optional.empty());
        when(checkResultRepository.countByServiceIdAndCheckedAtAfter(eq(10L), any(LocalDateTime.class)))
                .thenReturn(100L);
        when(checkResultRepository.countByServiceIdAndStatusAndCheckedAtAfter(
                eq(10L), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(100L);
        when(checkResultRepository.findAverageResponseTimeByServiceIdAndStatusAfter(
                eq(10L), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(500.0);

        ServiceHealthScoreResponse result = serviceHealthScoreService.calculateHealthScore(10L);

        assertNotNull(result);
        assertEquals(100, result.healthScore());
        assertEquals(HealthLevel.HEALTHY, result.healthLevel());
        assertEquals(100.0, result.availabilityPercent24h());
        assertEquals(500.0, result.averageResponseTimeMs24h());
        assertFalse(result.hasOpenIncident());
        assertEquals(RecurrenceLevel.LOW, result.recurrenceLevel());
    }

    @Test
    @DisplayName("Рассчитать здоровье сервиса - применение всех штрафов (0 баллов)")
    void calculateHealthScore_allPenalties_returns0() {
        CheckResult latestCheck = mock(CheckResult.class);
        when(latestCheck.getStatus()).thenReturn(ServiceStatus.DOWN);

        Incident openIncident = mock(Incident.class);
        when(openIncident.getSeverity()).thenReturn(IncidentSeverity.MEDIUM);

        Incident latestIncident = mock(Incident.class);
        CheckResult incidentCheckResult = mock(CheckResult.class);
        when(latestIncident.getOpenedByCheckResult()).thenReturn(incidentCheckResult);
        when(incidentCheckResult.getFailureLayer()).thenReturn(by.slava_borisov.nodehealthtracker.model.enums.FailureLayer.NETWORK);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(checkResultRepository.findTopByServiceIdOrderByCheckedAtDesc(10L))
                .thenReturn(Optional.of(latestCheck));
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.of(openIncident));
        when(incidentRepository.findTopByServiceIdOrderByOpenedAtDesc(10L))
                .thenReturn(Optional.of(latestIncident));
        when(checkResultRepository.countByServiceIdAndCheckedAtAfter(eq(10L), any(LocalDateTime.class)))
                .thenReturn(100L);
        when(checkResultRepository.countByServiceIdAndStatusAndCheckedAtAfter(
                eq(10L), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(80L);
        when(checkResultRepository.findAverageResponseTimeByServiceIdAndStatusAfter(
                eq(10L), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(1500.0);
        when(incidentRepository.countSimilarIncidentsByServiceIdAndFailureLayerAfter(
                eq(10L), any(), any(LocalDateTime.class)
        )).thenReturn(5L);

        ServiceHealthScoreResponse result = serviceHealthScoreService.calculateHealthScore(10L);

        assertNotNull(result);
        assertEquals(0, result.healthScore());
        assertEquals(HealthLevel.CRITICAL, result.healthLevel());
        assertEquals(80.0, result.availabilityPercent24h());
        assertEquals(1500.0, result.averageResponseTimeMs24h());
        assertTrue(result.hasOpenIncident());
        assertEquals(RecurrenceLevel.HIGH, result.recurrenceLevel());
    }

    @Test
    @DisplayName("Рассчитать здоровье сервиса - отсутствие инцидентов дает LOW recurrence")
    void calculateHealthScore_noIncidents_returnsLowRecurrence() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(checkResultRepository.findTopByServiceIdOrderByCheckedAtDesc(10L))
                .thenReturn(Optional.empty());
        when(incidentRepository.findByServiceIdAndStatus(10L, IncidentStatus.OPEN))
                .thenReturn(Optional.empty());
        when(incidentRepository.findTopByServiceIdOrderByOpenedAtDesc(10L))
                .thenReturn(Optional.empty());
        when(checkResultRepository.countByServiceIdAndCheckedAtAfter(eq(10L), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(checkResultRepository.findAverageResponseTimeByServiceIdAndStatusAfter(
                eq(10L), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(null);

        ServiceHealthScoreResponse result = serviceHealthScoreService.calculateHealthScore(10L);

        assertNotNull(result);
        assertEquals(100, result.healthScore());
        assertEquals(RecurrenceLevel.LOW, result.recurrenceLevel());
        assertNull(result.availabilityPercent24h());
        assertNull(result.averageResponseTimeMs24h());
    }
}