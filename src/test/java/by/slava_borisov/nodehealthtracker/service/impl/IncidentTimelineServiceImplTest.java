package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.IncidentTimelineEventMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.IncidentTimelineEvent;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentTimelineEventRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты IncidentTimelineServiceImpl")
class IncidentTimelineServiceImplTest {

    @Mock
    private IncidentTimelineEventRepository incidentTimelineEventRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentTimelineEventMapper incidentTimelineEventMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private IncidentTimelineServiceImpl incidentTimelineService;

    private User ownerUser;
    private User differentUser;
    private NetworkNode networkNode;
    private NetworkService networkService;
    private Incident incident;
    private CheckResult checkResult;

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

        checkResult = new CheckResult();
        checkResult.setId(500L);
    }

    @Test
    @DisplayName("Создать событие timeline - успешно")
    void createEvent_success() {
        ArgumentCaptor<IncidentTimelineEvent> eventCaptor =
                ArgumentCaptor.forClass(IncidentTimelineEvent.class);

        incidentTimelineService.createEvent(
                incident,
                checkResult,
                IncidentTimelineEventType.INCIDENT_OPENED,
                "Test message"
        );

        verify(incidentTimelineEventRepository, times(1)).save(eventCaptor.capture());

        IncidentTimelineEvent savedEvent = eventCaptor.getValue();
        assertEquals(incident, savedEvent.getIncident());
        assertEquals(checkResult, savedEvent.getCheckResult());
        assertEquals(IncidentTimelineEventType.INCIDENT_OPENED, savedEvent.getEventType());
        assertEquals("Test message", savedEvent.getMessage());
        assertNotNull(savedEvent.getCreatedAt());
    }

    @Test
    @DisplayName("Получить timeline инцидента - успешно")
    void getIncidentTimeline_success() {
        IncidentTimelineEvent event = new IncidentTimelineEvent();
        event.setId(1L);
        event.setIncident(incident);
        event.setEventType(IncidentTimelineEventType.INCIDENT_OPENED);
        event.setMessage("Test event");

        IncidentTimelineEventResponse response = mock(IncidentTimelineEventResponse.class);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));
        when(incidentTimelineEventRepository.findAllByIncidentIdOrderByCreatedAtAsc(100L))
                .thenReturn(List.of(event));
        when(incidentTimelineEventMapper.toResponse(event)).thenReturn(response);

        List<IncidentTimelineEventResponse> result =
                incidentTimelineService.getIncidentTimeline(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(incidentTimelineEventMapper, times(1)).toResponse(event);
    }

    @Test
    @DisplayName("Получить timeline инцидента - инцидент не найден")
    void getIncidentTimeline_incidentNotFound_throwsException() {
        when(incidentRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> incidentTimelineService.getIncidentTimeline(100L)
        );
    }

    @Test
    @DisplayName("Получить timeline инцидента - отказ в доступе")
    void getIncidentTimeline_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(incidentRepository.findById(100L)).thenReturn(Optional.of(incident));

        assertThrows(
                AccessDeniedException.class,
                () -> incidentTimelineService.getIncidentTimeline(100L)
        );
    }
}