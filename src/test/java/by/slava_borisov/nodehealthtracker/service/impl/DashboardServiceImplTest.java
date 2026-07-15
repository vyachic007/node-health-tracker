package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.dashboard.AdminDashboardSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.dashboard.DashboardSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.ServiceHealthScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты DashboardServiceImpl")
class DashboardServiceImplTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NetworkNodeRepository networkNodeRepository;

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private ServiceHealthScoreService serviceHealthScoreService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User currentUser;
    private Long ownerId;
    private NetworkService networkService;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("owner");
        ownerId = currentUser.getId();

        networkService = new NetworkService();
        networkService.setId(10L);
        networkService.setIsEnabled(true);
    }

    @Test
    @DisplayName("Получить сводку текущего пользователя - успешно")
    void getCurrentUserSummary_success() {
        ServiceHealthScoreResponse healthScoreResponse = mock(ServiceHealthScoreResponse.class);
        when(healthScoreResponse.healthScore()).thenReturn(95);
        when(healthScoreResponse.healthLevel()).thenReturn(HealthLevel.HEALTHY);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        when(networkNodeRepository.countByOwnerId(ownerId)).thenReturn(5L);
        when(networkNodeRepository.countByOwnerIdAndIsActiveTrue(ownerId)).thenReturn(4L);
        when(networkNodeRepository.countByOwnerIdAndIsActiveFalse(ownerId)).thenReturn(1L);

        when(networkServiceRepository.countByNodeOwnerId(ownerId)).thenReturn(10L);
        when(networkServiceRepository.countByNodeOwnerIdAndIsEnabledTrue(ownerId)).thenReturn(8L);
        when(networkServiceRepository.countByNodeOwnerIdAndIsEnabledFalse(ownerId)).thenReturn(2L);
        when(networkServiceRepository.countCurrentServicesByStatus(ownerId, ServiceStatus.UP.name())).thenReturn(5L);
        when(networkServiceRepository.countCurrentServicesByStatus(ownerId, ServiceStatus.DOWN.name())).thenReturn(2L);
        when(networkServiceRepository.findAllByNodeOwnerId(ownerId)).thenReturn(List.of(networkService));

        when(incidentRepository.countByServiceNodeOwnerIdAndStatus(ownerId, IncidentStatus.OPEN)).thenReturn(1L);
        when(incidentRepository.countByServiceNodeOwnerIdAndStatus(ownerId, IncidentStatus.RESOLVED)).thenReturn(3L);

        when(checkResultRepository.countByServiceNodeOwnerIdAndCheckedAtAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(100L);
        when(checkResultRepository.countByServiceNodeOwnerIdAndStatusAndCheckedAtAfter(
                anyLong(), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(90L);
        when(checkResultRepository.findAverageResponseTimeByOwnerIdAndStatusAfter(
                anyLong(), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(150.5);

        when(serviceHealthScoreService.calculateHealthScore(10L)).thenReturn(healthScoreResponse);

        DashboardSummaryResponse result = dashboardService.getCurrentUserSummary();

        assertNotNull(result);
        assertEquals(5L, result.totalNodes());
        assertEquals(8L, result.enabledServices());
        assertEquals(1L, result.openIncidents());
        assertEquals(95, result.averageHealthScore());
        assertEquals(HealthLevel.HEALTHY, result.averageHealthLevel());
        assertEquals(90.0, result.availabilityPercent24h());
        assertEquals(150.5, result.averageResponseTimeMs24h());
    }

    @Test
    @DisplayName("Получить сводку текущего пользователя - нет сервисов (средний балл null)")
    void getCurrentUserSummary_noServices_averageScoreNull() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        when(networkNodeRepository.countByOwnerId(ownerId)).thenReturn(0L);
        when(networkNodeRepository.countByOwnerIdAndIsActiveTrue(ownerId)).thenReturn(0L);
        when(networkNodeRepository.countByOwnerIdAndIsActiveFalse(ownerId)).thenReturn(0L);

        when(networkServiceRepository.countByNodeOwnerId(ownerId)).thenReturn(0L);
        when(networkServiceRepository.countByNodeOwnerIdAndIsEnabledTrue(ownerId)).thenReturn(0L);
        when(networkServiceRepository.countByNodeOwnerIdAndIsEnabledFalse(ownerId)).thenReturn(0L);
        when(networkServiceRepository.countCurrentServicesByStatus(ownerId, ServiceStatus.UP.name())).thenReturn(0L);
        when(networkServiceRepository.countCurrentServicesByStatus(ownerId, ServiceStatus.DOWN.name())).thenReturn(0L);
        when(networkServiceRepository.findAllByNodeOwnerId(ownerId)).thenReturn(List.of());

        when(incidentRepository.countByServiceNodeOwnerIdAndStatus(ownerId, IncidentStatus.OPEN)).thenReturn(0L);
        when(incidentRepository.countByServiceNodeOwnerIdAndStatus(ownerId, IncidentStatus.RESOLVED)).thenReturn(0L);

        when(checkResultRepository.countByServiceNodeOwnerIdAndCheckedAtAfter(anyLong(), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(checkResultRepository.findAverageResponseTimeByOwnerIdAndStatusAfter(
                anyLong(), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(null);

        DashboardSummaryResponse result = dashboardService.getCurrentUserSummary();

        assertNotNull(result);
        assertNull(result.averageHealthScore());
        assertNull(result.averageHealthLevel());
        assertNull(result.availabilityPercent24h());
    }

    @Test
    @DisplayName("Получить админскую сводку - успешно")
    void getAdminSummary_success() {
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(45L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(5L);

        when(networkNodeRepository.count()).thenReturn(100L);
        when(networkNodeRepository.countByIsActiveTrue()).thenReturn(90L);
        when(networkNodeRepository.countByIsActiveFalse()).thenReturn(10L);

        when(networkServiceRepository.count()).thenReturn(200L);
        when(networkServiceRepository.countByIsEnabledTrue()).thenReturn(150L);
        when(networkServiceRepository.countByIsEnabledFalse()).thenReturn(50L);
        when(networkServiceRepository.countCurrentServicesByStatus(ServiceStatus.UP.name())).thenReturn(100L);
        when(networkServiceRepository.countCurrentServicesByStatus(ServiceStatus.DOWN.name())).thenReturn(30L);

        when(incidentRepository.countByStatus(IncidentStatus.OPEN)).thenReturn(5L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(20L);

        when(checkResultRepository.countByCheckedAtAfter(any(LocalDateTime.class))).thenReturn(1000L);
        when(checkResultRepository.countByStatusAndCheckedAtAfter(
                eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(900L);
        when(checkResultRepository.findAverageResponseTimeByStatusAfter(
                eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(120.75);

        AdminDashboardSummaryResponse result = dashboardService.getAdminSummary();

        assertNotNull(result);
        assertEquals(50L, result.totalUsers());
        assertEquals(150L, result.enabledServices());
        assertEquals(5L, result.openIncidents());

        long unknown = 150L - 100L - 30L;
        long expectedScore = Math.round((100L * 100 + unknown * 60 + 30L * 30) * 1.0 / 150L);
        assertEquals((int) expectedScore, result.averageHealthScore());
        assertEquals(90.0, result.availabilityPercent24h());
        assertEquals(120.75, result.averageResponseTimeMs24h());
    }

    @Test
    @DisplayName("Получить админскую сводку - нет включенных сервисов (средний балл null)")
    void getAdminSummary_noEnabledServices_averageScoreNull() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(10L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(0L);

        when(networkNodeRepository.count()).thenReturn(5L);
        when(networkNodeRepository.countByIsActiveTrue()).thenReturn(5L);
        when(networkNodeRepository.countByIsActiveFalse()).thenReturn(0L);

        when(networkServiceRepository.count()).thenReturn(0L);
        when(networkServiceRepository.countByIsEnabledTrue()).thenReturn(0L);
        when(networkServiceRepository.countByIsEnabledFalse()).thenReturn(0L);
        when(networkServiceRepository.countCurrentServicesByStatus(ServiceStatus.UP.name())).thenReturn(0L);
        when(networkServiceRepository.countCurrentServicesByStatus(ServiceStatus.DOWN.name())).thenReturn(0L);

        when(incidentRepository.countByStatus(IncidentStatus.OPEN)).thenReturn(0L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(0L);

        when(checkResultRepository.countByCheckedAtAfter(any(LocalDateTime.class))).thenReturn(0L);
        when(checkResultRepository.findAverageResponseTimeByStatusAfter(
                eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(null);

        AdminDashboardSummaryResponse result = dashboardService.getAdminSummary();

        assertNotNull(result);
        assertNull(result.averageHealthScore());
        assertNull(result.averageHealthLevel());
        assertNull(result.availabilityPercent24h());
    }
}