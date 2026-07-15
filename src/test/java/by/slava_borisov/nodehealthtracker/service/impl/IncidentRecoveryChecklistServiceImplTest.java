package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecoveryChecklistResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты IncidentRecoveryChecklistServiceImpl")
class IncidentRecoveryChecklistServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private IncidentRecoveryChecklistServiceImpl checklistService;

    private User ownerUser;
    private User differentUser;
    private NetworkNode networkNode;
    private NetworkService networkService;
    private Incident incident;

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

        incident = new Incident();
        incident.setId(100L);
        incident.setService(networkService);
    }

    @Test
    @DisplayName("Получить чек-лист восстановления - инцидент не найден")
    void getRecoveryChecklist_incidentNotFound_throwsException() {
        when(incidentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> checklistService.getRecoveryChecklist(100L)
        );
    }

    @Test
    @DisplayName("Получить чек-лист восстановления - отказ в доступе")
    void getRecoveryChecklist_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        assertThrows(
                AccessDeniedException.class,
                () -> checklistService.getRecoveryChecklist(100L)
        );
    }

    @Test
    @DisplayName("Получить чек-лист восстановления - успешно (слой DNS)")
    void getRecoveryChecklist_success_dnsLayer() {
        CheckResult checkResult = mock(CheckResult.class);
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.DNS);
        incident.setOpenedByCheckResult(checkResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        IncidentRecoveryChecklistResponse response = checklistService.getRecoveryChecklist(100L);

        assertNotNull(response);
        assertEquals(100L, response.incidentId());
        assertEquals(10L, response.serviceId());
        assertEquals("Test Service", response.serviceName());
        assertEquals(FailureLayer.DNS, response.failureLayer());
        assertEquals(4, response.items().size());
        assertTrue(response.items().get(0).isCritical());
        assertFalse(response.items().get(3).isCritical());
    }

    @Test
    @DisplayName("Получить чек-лист восстановления - успешно (неизвестный слой, checkResult null)")
    void getRecoveryChecklist_success_unknownLayer_nullCheckResult() {
        incident.setOpenedByCheckResult(null);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        IncidentRecoveryChecklistResponse response = checklistService.getRecoveryChecklist(100L);

        assertNotNull(response);
        assertEquals(FailureLayer.UNKNOWN, response.failureLayer());
        assertEquals(4, response.items().size());
    }

    @Test
    @DisplayName("Получить чек-лист восстановления - успешно (слой APPLICATION)")
    void getRecoveryChecklist_success_applicationLayer() {
        CheckResult checkResult = mock(CheckResult.class);
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.APPLICATION);
        incident.setOpenedByCheckResult(checkResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        IncidentRecoveryChecklistResponse response = checklistService.getRecoveryChecklist(100L);

        assertNotNull(response);
        assertEquals(FailureLayer.APPLICATION, response.failureLayer());
        assertEquals(4, response.items().size());
    }

    @Test
    @DisplayName("Получить чек-лист восстановления - успешно (слой PERFORMANCE)")
    void getRecoveryChecklist_success_performanceLayer() {
        CheckResult checkResult = mock(CheckResult.class);
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.PERFORMANCE);
        incident.setOpenedByCheckResult(checkResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        IncidentRecoveryChecklistResponse response = checklistService.getRecoveryChecklist(100L);

        assertNotNull(response);
        assertEquals(FailureLayer.PERFORMANCE, response.failureLayer());
        assertEquals(4, response.items().size());
    }
}