package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.IncidentMapper;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты IncidentServiceImpl")
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentMapper incidentMapper;

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private User ownerUser;
    private User differentUser;
    private NetworkNode networkNode;
    private NetworkService networkService;
    private Incident openIncident;
    private IncidentResponse incidentResponse;

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

        openIncident = new Incident();
        openIncident.setId(100L);
        openIncident.setService(networkService);
        openIncident.setStatus(IncidentStatus.OPEN);

        incidentResponse = mock(IncidentResponse.class);
    }

    @Test
    @DisplayName("Получить инцидент по ID - успешно")
    void getIncidentById_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(openIncident));
        when(incidentMapper.toIncidentResponse(openIncident)).thenReturn(incidentResponse);

        IncidentResponse result = incidentService.getIncidentById(100L);

        assertNotNull(result);
        verify(incidentMapper, times(1)).toIncidentResponse(openIncident);
    }

    @Test
    @DisplayName("Получить инцидент по ID - инцидент не найден")
    void getIncidentById_notFound_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.getIncidentById(100L)
        );
    }

    @Test
    @DisplayName("Получить инцидент по ID - отказ в доступе")
    void getIncidentById_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(openIncident));

        assertThrows(
                AccessDeniedException.class,
                () -> incidentService.getIncidentById(100L)
        );
    }

    @Test
    @DisplayName("Получить инциденты текущего пользователя - успешно")
    void getCurrentUserIncidents_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findAllByServiceNodeOwnerIdOrderByOpenedAtDesc(1L))
                .thenReturn(List.of(openIncident));
        when(incidentMapper.toIncidentResponse(openIncident)).thenReturn(incidentResponse);

        List<IncidentResponse> result = incidentService.getCurrentUserIncidents();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(incidentRepository, times(1))
                .findAllByServiceNodeOwnerIdOrderByOpenedAtDesc(1L);
    }

    @Test
    @DisplayName("Получить инциденты сервиса - успешно")
    void getServiceIncidents_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(incidentRepository.findAllByServiceIdOrderByOpenedAtDesc(10L))
                .thenReturn(List.of(openIncident));
        when(incidentMapper.toIncidentResponse(openIncident)).thenReturn(incidentResponse);

        List<IncidentResponse> result = incidentService.getServiceIncidents(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Получить инциденты сервиса - сервис не найден")
    void getServiceIncidents_serviceNotFound_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.getServiceIncidents(10L)
        );
    }

    @Test
    @DisplayName("Получить инциденты сервиса - отказ в доступе")
    void getServiceIncidents_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));

        assertThrows(
                AccessDeniedException.class,
                () -> incidentService.getServiceIncidents(10L)
        );
    }

    @Test
    @DisplayName("Закрыть инцидент - успешно")
    void closeIncident_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(openIncident));
        when(incidentRepository.save(any(Incident.class))).thenAnswer(invocation -> {
            Incident saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setService(networkService);
            return saved;
        });
        when(incidentMapper.toIncidentResponse(any(Incident.class))).thenReturn(incidentResponse);

        IncidentResponse result = incidentService.closeIncident(100L);

        assertNotNull(result);
        verify(incidentRepository, times(1)).save(any(Incident.class));
        verify(auditLogService, times(1)).log(
                eq(AuditActionType.INCIDENT_RESOLVED),
                anyString(),
                eq("Incident"),
                eq(100L)
        );
        verify(notificationService, times(1)).notifyIncidentResolved(any(Incident.class));
    }

    @Test
    @DisplayName("Закрыть инцидент - инцидент уже закрыт")
    void closeIncident_alreadyResolved_throwsException() {
        Incident resolvedIncident = new Incident();
        resolvedIncident.setId(100L);
        resolvedIncident.setService(networkService);
        resolvedIncident.setStatus(IncidentStatus.RESOLVED);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(resolvedIncident));

        assertThrows(
                InvalidOperationException.class,
                () -> incidentService.closeIncident(100L)
        );
    }

    @Test
    @DisplayName("Закрыть инцидент - инцидент не найден")
    void closeIncident_notFound_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentService.closeIncident(100L)
        );
    }

    @Test
    @DisplayName("Закрыть инцидент - отказ в доступе")
    void closeIncident_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(openIncident));

        assertThrows(
                AccessDeniedException.class,
                () -> incidentService.closeIncident(100L)
        );
    }
}