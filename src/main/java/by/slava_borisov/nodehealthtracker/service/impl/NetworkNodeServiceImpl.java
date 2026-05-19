package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.NetworkNodeMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.NodeHealthStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NetworkNodeService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NetworkNodeServiceImpl implements NetworkNodeService {

    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final IncidentRepository incidentRepository;
    private final CheckResultRepository checkResultRepository;
    private final NetworkNodeMapper networkNodeMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public NodeResponse createNode(NodeCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        LocalDateTime now = LocalDateTime.now();

        NetworkNode networkNode = networkNodeMapper.toEntity(request);
        networkNode.setOwner(currentUser);
        networkNode.setIsActive(true);
        networkNode.setCreatedAt(now);
        networkNode.setUpdatedAt(now);

        NetworkNode savedNode = networkNodeRepository.save(networkNode);

        return buildNodeResponse(savedNode);
    }

    @Override
    @Transactional
    public NodeResponse updateNode(Long nodeId, NodeUpdateRequest request) {
        NetworkNode networkNode = findNodeById(nodeId);
        validateNodeOwner(networkNode);

        networkNodeMapper.updateEntityFromDto(request, networkNode);
        networkNode.setUpdatedAt(LocalDateTime.now());

        NetworkNode savedNode = networkNodeRepository.save(networkNode);

        return buildNodeResponse(savedNode);
    }

    @Override
    @Transactional
    public void deleteNode(Long nodeId) {
        NetworkNode networkNode = findNodeById(nodeId);
        validateNodeOwner(networkNode);

        networkNodeRepository.delete(networkNode);
    }

    @Override
    @Transactional(readOnly = true)
    public NodeResponse getNodeById(Long nodeId) {
        NetworkNode networkNode = findNodeById(nodeId);
        validateNodeOwner(networkNode);

        return buildNodeResponse(networkNode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeResponse> getCurrentUserNodes() {
        User currentUser = currentUserService.getCurrentUser();

        return networkNodeRepository.findAllByOwnerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::buildNodeResponse)
                .toList();
    }

    private NodeResponse buildNodeResponse(NetworkNode node) {
        Long nodeId = node.getId();

        long totalServices = networkServiceRepository.countByNodeId(nodeId);
        long enabledServices = networkServiceRepository.countByNodeIdAndIsEnabledTrue(nodeId);
        long disabledServices = networkServiceRepository.countByNodeIdAndIsEnabledFalse(nodeId);

        long upServices = networkServiceRepository.countCurrentServicesByNodeIdAndStatus(
                nodeId,
                ServiceStatus.UP.name()
        );

        long downServices = networkServiceRepository.countCurrentServicesByNodeIdAndStatus(
                nodeId,
                ServiceStatus.DOWN.name()
        );

        long unknownServices = calculateUnknownServices(
                enabledServices,
                upServices,
                downServices,
                nodeId
        );

        long openIncidents = incidentRepository.countByServiceNodeIdAndStatus(
                nodeId,
                IncidentStatus.OPEN
        );

        Optional<CheckResult> latestCheckResult = checkResultRepository
                .findTopByServiceNodeIdOrderByCheckedAtDesc(nodeId);

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        Double availabilityPercent24h = calculateNodeAvailabilityPercent24h(
                nodeId,
                last24Hours
        );

        Double averageResponseTimeMs24h = roundToTwoDecimals(
                checkResultRepository.findAverageResponseTimeByNodeIdAndStatusAfter(
                        nodeId,
                        ServiceStatus.UP,
                        last24Hours
                )
        );

        NodeHealthStatus healthStatus = calculateNodeHealthStatus(
                enabledServices,
                upServices,
                downServices,
                unknownServices
        );

        return new NodeResponse(
                node.getId(),
                node.getOwner().getId(),
                node.getName(),
                node.getHost(),
                node.getDescription(),
                node.getIsActive(),

                healthStatus,
                totalServices,
                enabledServices,
                disabledServices,
                upServices,
                downServices,
                unknownServices,
                openIncidents,
                latestCheckResult.map(CheckResult::getCheckedAt).orElse(null),
                availabilityPercent24h,
                averageResponseTimeMs24h,

                node.getCreatedAt(),
                node.getUpdatedAt()
        );
    }

    private long calculateUnknownServices(
            long enabledServices,
            long upServices,
            long downServices,
            Long nodeId
    ) {
        long servicesWithoutChecks = networkServiceRepository
                .countEnabledServicesWithoutChecksByNodeId(nodeId);

        long calculatedUnknown = enabledServices - upServices - downServices;

        return Math.max(calculatedUnknown, servicesWithoutChecks);
    }

    private NodeHealthStatus calculateNodeHealthStatus(
            long enabledServices,
            long upServices,
            long downServices,
            long unknownServices
    ) {
        if (enabledServices == 0) {
            return NodeHealthStatus.UNKNOWN;
        }

        if (upServices == enabledServices) {
            return NodeHealthStatus.UP;
        }

        if (downServices == enabledServices) {
            return NodeHealthStatus.DOWN;
        }

        if (upServices == 0 && downServices == 0 && unknownServices > 0) {
            return NodeHealthStatus.UNKNOWN;
        }

        return NodeHealthStatus.DEGRADED;
    }

    private Double calculateNodeAvailabilityPercent24h(Long nodeId, LocalDateTime checkedAtAfter) {
        long totalChecks = checkResultRepository.countByServiceNodeIdAndCheckedAtAfter(
                nodeId,
                checkedAtAfter
        );

        if (totalChecks == 0) {
            return null;
        }

        long successfulChecks = checkResultRepository.countByServiceNodeIdAndStatusAndCheckedAtAfter(
                nodeId,
                ServiceStatus.UP,
                checkedAtAfter
        );

        double availability = successfulChecks * 100.0 / totalChecks;

        return roundToTwoDecimals(availability);
    }

    private Double roundToTwoDecimals(Double value) {
        if (value == null) {
            return null;
        }

        return Math.round(value * 100.0) / 100.0;
    }

    private NetworkNode findNodeById(Long nodeId) {
        return networkNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_NODE_NOT_FOUND));
    }

    private void validateNodeOwner(NetworkNode networkNode) {
        User currentUser = currentUserService.getCurrentUser();

        if (!networkNode.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.NETWORK_NODE_ACCESS_DENIED);
        }
    }
}