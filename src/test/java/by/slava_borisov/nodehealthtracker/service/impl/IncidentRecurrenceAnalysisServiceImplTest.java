package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecurrenceAnalysisResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
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
@DisplayName("Тесты IncidentRecurrenceAnalysisServiceImpl")
class IncidentRecurrenceAnalysisServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private IncidentRecurrenceAnalysisServiceImpl recurrenceAnalysisService;

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
    @DisplayName("Анализ повторяемости - инцидент не найден")
    void analyzeRecurrence_incidentNotFound_throwsException() {
        when(incidentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> recurrenceAnalysisService.analyzeRecurrence(100L)
        );
    }

    @Test
    @DisplayName("Анализ повторяемости - отказ в доступе")
    void analyzeRecurrence_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        assertThrows(
                AccessDeniedException.class,
                () -> recurrenceAnalysisService.analyzeRecurrence(100L)
        );
    }

    @Test
    @DisplayName("Анализ повторяемости - успешно, уровень LOW (checkResult null)")
    void analyzeRecurrence_success_lowRecurrence() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentRepository.countSimilarIncidentsByServiceIdAndFailureLayerAfter(
                eq(10L), eq(FailureLayer.UNKNOWN), any(LocalDateTime.class)
        )).thenReturn(0L)
                .thenReturn(2L)
                .thenReturn(5L);

        IncidentRecurrenceAnalysisResponse response = recurrenceAnalysisService.analyzeRecurrence(100L);

        assertNotNull(response);
        assertEquals(100L, response.incidentId());
        assertEquals(FailureLayer.UNKNOWN, response.failureLayer());
        assertEquals(RecurrenceLevel.LOW, response.recurrenceLevel());
        assertFalse(response.isRecurring());
    }

    @Test
    @DisplayName("Анализ повторяемости - успешно, уровень MEDIUM")
    void analyzeRecurrence_success_mediumRecurrence() {
        CheckResult checkResult = mock(CheckResult.class);
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.NETWORK);
        incident.setOpenedByCheckResult(checkResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentRepository.countSimilarIncidentsByServiceIdAndFailureLayerAfter(
                eq(10L), eq(FailureLayer.NETWORK), any(LocalDateTime.class)
        )).thenReturn(1L)
                .thenReturn(3L)
                .thenReturn(10L);

        IncidentRecurrenceAnalysisResponse response = recurrenceAnalysisService.analyzeRecurrence(100L);

        assertNotNull(response);
        assertEquals(FailureLayer.NETWORK, response.failureLayer());
        assertEquals(RecurrenceLevel.MEDIUM, response.recurrenceLevel());
        assertTrue(response.isRecurring());
    }

    @Test
    @DisplayName("Анализ повторяемости - успешно, уровень HIGH")
    void analyzeRecurrence_success_highRecurrence() {
        CheckResult checkResult = mock(CheckResult.class);
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.APPLICATION);
        incident.setOpenedByCheckResult(checkResult);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentRepository.countSimilarIncidentsByServiceIdAndFailureLayerAfter(
                eq(10L), eq(FailureLayer.APPLICATION), any(LocalDateTime.class)
        )).thenReturn(2L)
                .thenReturn(5L)
                .thenReturn(15L);

        IncidentRecurrenceAnalysisResponse response = recurrenceAnalysisService.analyzeRecurrence(100L);

        assertNotNull(response);
        assertEquals(FailureLayer.APPLICATION, response.failureLayer());
        assertEquals(RecurrenceLevel.HIGH, response.recurrenceLevel());
        assertTrue(response.isRecurring());
    }
}