package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.NetworkNodeMapper;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты NetworkNodeServiceImpl")
class NetworkNodeServiceImplTest {

    @Mock
    private NetworkNodeRepository networkNodeRepository;

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private NetworkNodeMapper networkNodeMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private NetworkNodeServiceImpl networkNodeService;

    private User ownerUser;
    private User differentUser;
    private NetworkNode networkNode;
    private NodeCreateRequest createRequest;
    private NodeUpdateRequest updateRequest;

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
        networkNode.setHost("192.168.1.1");
        networkNode.setOwner(ownerUser);

        createRequest = mock(NodeCreateRequest.class);
        updateRequest = mock(NodeUpdateRequest.class);
    }

    @Test
    @DisplayName("Создать ноду - успешно")
    void createNode_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeMapper.toEntity(createRequest)).thenReturn(networkNode);
        when(networkNodeRepository.save(any(NetworkNode.class))).thenReturn(networkNode);

        mockBuildNodeResponseDependencies(10L);

        NodeResponse result = networkNodeService.createNode(createRequest);

        assertNotNull(result);
        verify(auditLogService, times(1)).log(
                eq(AuditActionType.NODE_CREATED),
                anyString(),
                eq("NetworkNode"),
                eq(10L)
        );
    }

    @Test
    @DisplayName("Обновить ноду - успешно")
    void updateNode_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.of(networkNode));
        when(networkNodeRepository.save(any(NetworkNode.class))).thenReturn(networkNode);

        mockBuildNodeResponseDependencies(10L);

        NodeResponse result = networkNodeService.updateNode(10L, updateRequest);

        assertNotNull(result);
        verify(networkNodeMapper, times(1)).updateEntityFromDto(updateRequest, networkNode);
        verify(auditLogService, times(1)).log(
                eq(AuditActionType.NODE_UPDATED),
                anyString(),
                eq("NetworkNode"),
                eq(10L)
        );
    }

    @Test
    @DisplayName("Обновить ноду - нода не найдена")
    void updateNode_notFound_throwsException() {
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> networkNodeService.updateNode(10L, updateRequest)
        );
    }

    @Test
    @DisplayName("Обновить ноду - отказ в доступе")
    void updateNode_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.of(networkNode));

        assertThrows(
                AccessDeniedException.class,
                () -> networkNodeService.updateNode(10L, updateRequest)
        );
    }

    @Test
    @DisplayName("Удалить ноду - успешно")
    void deleteNode_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.of(networkNode));

        networkNodeService.deleteNode(10L);

        verify(networkNodeRepository, times(1)).delete(networkNode);
        verify(auditLogService, times(1)).log(
                eq(AuditActionType.NODE_DELETED),
                anyString(),
                eq("NetworkNode"),
                eq(10L)
        );
    }

    @Test
    @DisplayName("Получить ноду по ID - успешно")
    void getNodeById_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeRepository.findById(10L)).thenReturn(Optional.of(networkNode));

        mockBuildNodeResponseDependencies(10L);

        NodeResponse result = networkNodeService.getNodeById(10L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Получить список нод текущего пользователя - успешно")
    void getCurrentUserNodes_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkNodeRepository.findAllByOwnerIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(networkNode));

        mockBuildNodeResponseDependencies(10L);

        List<NodeResponse> result = networkNodeService.getCurrentUserNodes();

        assertNotNull(result);
        assertEquals(1, result.size());
    }


    private void mockBuildNodeResponseDependencies(Long nodeId) {
        when(networkServiceRepository.countByNodeId(nodeId)).thenReturn(5L);
        when(networkServiceRepository.countByNodeIdAndIsEnabledTrue(nodeId)).thenReturn(4L);
        when(networkServiceRepository.countByNodeIdAndIsEnabledFalse(nodeId)).thenReturn(1L);
        when(networkServiceRepository.countCurrentServicesByNodeIdAndStatus(nodeId, ServiceStatus.UP.name())).thenReturn(3L);
        when(networkServiceRepository.countCurrentServicesByNodeIdAndStatus(nodeId, ServiceStatus.DOWN.name())).thenReturn(1L);
        when(networkServiceRepository.countEnabledServicesWithoutChecksByNodeId(nodeId)).thenReturn(0L);

        when(incidentRepository.countByServiceNodeIdAndStatus(nodeId, IncidentStatus.OPEN)).thenReturn(1L);

        when(checkResultRepository.findTopByServiceNodeIdOrderByCheckedAtDesc(nodeId))
                .thenReturn(Optional.empty());
        when(checkResultRepository.countByServiceNodeIdAndCheckedAtAfter(eq(nodeId), any(LocalDateTime.class)))
                .thenReturn(100L);
        when(checkResultRepository.countByServiceNodeIdAndStatusAndCheckedAtAfter(eq(nodeId), eq(ServiceStatus.UP), any(LocalDateTime.class)))
                .thenReturn(90L);
        when(checkResultRepository.findAverageResponseTimeByNodeIdAndStatusAfter(eq(nodeId), eq(ServiceStatus.UP), any(LocalDateTime.class)))
                .thenReturn(150.5);

        when(networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(nodeId))
                .thenReturn(List.of());
    }
}