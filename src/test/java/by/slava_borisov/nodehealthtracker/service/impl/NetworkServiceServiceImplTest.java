package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.NetworkServiceMapper;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты NetworkServiceServiceImpl")
class NetworkServiceServiceImplTest {

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private NetworkNodeRepository networkNodeRepository;

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private NetworkServiceMapper networkServiceMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ServiceHealthScoreService serviceHealthScoreService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private NetworkServiceServiceImpl networkServiceService;

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

        networkNode = new NetworkNode();
        networkNode.setId(10L);
        networkNode.setName("Test Node");
        networkNode.setOwner(ownerUser);

        networkService = new NetworkService();
        networkService.setId(20L);
        networkService.setName("Test Service");
        networkService.setNode(networkNode);
        networkService.setCheckType(CheckType.HTTP);
    }

    @Test
    @DisplayName("Создать сервис - успешно")
    void createService_success() {
        ServiceCreateRequest createRequest = mock(ServiceCreateRequest.class);
        when(createRequest.nodeId()).thenReturn(10L);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.of(networkNode));
        when(networkServiceMapper.toEntity(createRequest)).thenReturn(networkService);
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);

        mockBuildServiceResponseDependencies(20L);

        networkServiceService.createService(createRequest);

        verify(auditLogService, times(1)).log(
                eq(AuditActionType.SERVICE_CREATED),
                any(),
                eq("NetworkService"),
                eq(20L)
        );
    }

    @Test
    @DisplayName("Обновить сервис - успешно")
    void updateService_success() {
        ServiceUpdateRequest updateRequest = mock(ServiceUpdateRequest.class);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.of(networkService));
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);

        mockBuildServiceResponseDependencies(20L);

        networkServiceService.updateService(20L, updateRequest);

        verify(networkServiceMapper, times(1)).updateEntityFromDto(updateRequest, networkService);
        verify(auditLogService, times(1)).log(
                eq(AuditActionType.SERVICE_UPDATED),
                any(),
                eq("NetworkService"),
                eq(20L)
        );
    }

    @Test
    @DisplayName("Обновить сервис - сервис не найден")
    void updateService_notFound_throwsException() {
        ServiceUpdateRequest updateRequest = mock(ServiceUpdateRequest.class);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> networkServiceService.updateService(20L, updateRequest)
        );
    }

    @Test
    @DisplayName("Обновить сервис - отказ в доступе")
    void updateService_accessDenied_throwsException() {
        ServiceUpdateRequest updateRequest = mock(ServiceUpdateRequest.class);

        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.of(networkService));

        assertThrows(
                AccessDeniedException.class,
                () -> networkServiceService.updateService(20L, updateRequest)
        );
    }

    @Test
    @DisplayName("Удалить сервис - успешно")
    void deleteService_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.of(networkService));

        networkServiceService.deleteService(20L);

        verify(networkServiceRepository, times(1)).delete(networkService);
        verify(auditLogService, times(1)).log(
                eq(AuditActionType.SERVICE_DELETED),
                any(),
                eq("NetworkService"),
                eq(20L)
        );
    }

    @Test
    @DisplayName("Получить сервис по ID - успешно")
    void getServiceById_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.of(networkService));

        mockBuildServiceResponseDependencies(20L);

        networkServiceService.getServiceById(20L);
    }

    @Test
    @DisplayName("Получить список сервисов узла - успешно")
    void getServicesByNodeId_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.of(networkNode));
        when(networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(networkService));

        mockBuildServiceResponseDependencies(20L);

        List<ServiceResponse> result = networkServiceService.getServicesByNodeId(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Получить список сервисов текущего пользователя - успешно")
    void getCurrentUserServices_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findAllByNodeOwnerIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(networkService));

        mockBuildServiceResponseDependencies(20L);

        List<ServiceResponse> result = networkServiceService.getCurrentUserServices();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Включить сервис - успешно")
    void enableService_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.of(networkService));
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);

        mockBuildServiceResponseDependencies(20L);

        networkServiceService.enableService(20L);

        verify(auditLogService, times(1)).log(
                eq(AuditActionType.SERVICE_UPDATED),
                any(),
                eq("NetworkService"),
                eq(20L)
        );
    }

    @Test
    @DisplayName("Отключить сервис - успешно")
    void disableService_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(20L)).thenReturn(Optional.of(networkService));
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);

        mockBuildServiceResponseDependencies(20L);

        networkServiceService.disableService(20L);

        verify(auditLogService, times(1)).log(
                eq(AuditActionType.SERVICE_UPDATED),
                any(),
                eq("NetworkService"),
                eq(20L)
        );
    }

    private void mockBuildServiceResponseDependencies(Long serviceId) {
        ServiceHealthScoreResponse healthScoreResponse = mock(ServiceHealthScoreResponse.class);
        when(healthScoreResponse.healthScore()).thenReturn(95);
        when(healthScoreResponse.healthLevel()).thenReturn(HealthLevel.HEALTHY);
        when(healthScoreResponse.recurrenceLevel()).thenReturn(RecurrenceLevel.LOW);

        when(checkResultRepository.findTopByServiceIdOrderByCheckedAtDesc(serviceId))
                .thenReturn(Optional.empty());
        when(incidentRepository.findByServiceIdAndStatus(serviceId, IncidentStatus.OPEN))
                .thenReturn(Optional.empty());
        when(checkResultRepository.countByServiceIdAndCheckedAtAfter(eq(serviceId), any(LocalDateTime.class)))
                .thenReturn(100L);
        when(checkResultRepository.countByServiceIdAndStatusAndCheckedAtAfter(
                eq(serviceId), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(90L);
        when(checkResultRepository.findAverageResponseTimeByServiceIdAndStatusAfter(
                eq(serviceId), eq(ServiceStatus.UP), any(LocalDateTime.class)
        )).thenReturn(150.5);
        when(serviceHealthScoreService.calculateHealthScore(serviceId))
                .thenReturn(healthScoreResponse);
    }
}