package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.NetworkServiceMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.NetworkServiceService;
import by.slava_borisov.nodehealthtracker.service.ServiceHealthScoreService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NetworkServiceServiceImpl implements NetworkServiceService {

    private final NetworkServiceRepository networkServiceRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final NetworkServiceMapper networkServiceMapper;
    private final CurrentUserService currentUserService;
    private final ServiceHealthScoreService serviceHealthScoreService;

    @Override
    @Transactional
    public ServiceResponse createService(ServiceCreateRequest request) {
        NetworkNode node = findNodeById(request.nodeId());
        validateNodeOwner(node);

        LocalDateTime now = LocalDateTime.now();

        NetworkService networkService = networkServiceMapper.toEntity(request);
        networkService.setNode(node);
        networkService.setIsEnabled(true);
        networkService.setFailureThreshold(2);
        networkService.setRecoveryThreshold(2);
        networkService.setConsecutiveFailures(0);
        networkService.setConsecutiveSuccesses(0);
        networkService.setCreatedAt(now);
        networkService.setUpdatedAt(now);

        if (networkService.getCheckType() == CheckType.HEARTBEAT) {
            networkService.setHeartbeatToken(generateHeartbeatToken());
        }

        NetworkService savedService = networkServiceRepository.save(networkService);

        return buildServiceResponse(savedService);
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long serviceId, ServiceUpdateRequest request) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkServiceMapper.updateEntityFromDto(request, networkService);
        networkService.setUpdatedAt(LocalDateTime.now());

        if (networkService.getCheckType() == CheckType.HEARTBEAT
                && networkService.getHeartbeatToken() == null) {
            networkService.setHeartbeatToken(generateHeartbeatToken());
        }

        if (networkService.getCheckType() != CheckType.HEARTBEAT) {
            networkService.setHeartbeatToken(null);
            networkService.setLastHeartbeatAt(null);
        }

        NetworkService savedService = networkServiceRepository.save(networkService);

        return buildServiceResponse(savedService);
    }

    @Override
    @Transactional
    public void deleteService(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkServiceRepository.delete(networkService);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        return buildServiceResponse(networkService);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByNodeId(Long nodeId) {
        NetworkNode node = findNodeById(nodeId);
        validateNodeOwner(node);

        return networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(nodeId)
                .stream()
                .map(this::buildServiceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponse> getCurrentUserServices() {
        User currentUser = currentUserService.getCurrentUser();

        return networkServiceRepository.findAllByNodeOwnerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::buildServiceResponse)
                .toList();
    }

    @Override
    @Transactional
    public ServiceResponse enableService(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkService.setIsEnabled(true);
        networkService.setUpdatedAt(LocalDateTime.now());

        NetworkService savedService = networkServiceRepository.save(networkService);

        return buildServiceResponse(savedService);
    }

    @Override
    @Transactional
    public ServiceResponse disableService(Long serviceId) {
        NetworkService networkService = findServiceById(serviceId);
        validateServiceOwner(networkService);

        networkService.setIsEnabled(false);
        networkService.setUpdatedAt(LocalDateTime.now());

        NetworkService savedService = networkServiceRepository.save(networkService);

        return buildServiceResponse(savedService);
    }

    private ServiceResponse buildServiceResponse(NetworkService networkService) {
        Optional<CheckResult> latestCheckResult = checkResultRepository
                .findTopByServiceIdOrderByCheckedAtDesc(networkService.getId());

        Optional<Incident> openIncident = incidentRepository.findByServiceIdAndStatus(
                networkService.getId(),
                IncidentStatus.OPEN
        );

        LocalDateTime nextCheckAt = calculateNextCheckAt(networkService);
        Long secondsUntilNextCheck = calculateSecondsUntilNextCheck(nextCheckAt);
        Long currentDowntimeSeconds = calculateCurrentDowntimeSeconds(openIncident);

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        Double availabilityPercent24h = calculateAvailabilityPercent24h(
                networkService.getId(),
                last24Hours
        );

        Double averageResponseTimeMs24h = roundToTwoDecimals(
                checkResultRepository.findAverageResponseTimeByServiceIdAndStatusAfter(
                        networkService.getId(),
                        ServiceStatus.UP,
                        last24Hours
                )
        );

        ServiceHealthScoreResponse healthScoreResponse = serviceHealthScoreService.calculateHealthScore(
                networkService.getId()
        );

        return new ServiceResponse(
                networkService.getId(),
                networkService.getNode().getId(),
                networkService.getCheckType(),
                networkService.getHeartbeatToken(),
                networkService.getLastHeartbeatAt(),
                networkService.getLastCheckedAt(),
                networkService.getName(),
                networkService.getTargetHost(),
                networkService.getPort(),
                networkService.getPath(),
                networkService.getIntervalSeconds(),
                networkService.getIsEnabled(),

                latestCheckResult.map(CheckResult::getStatus).orElse(null),
                latestCheckResult.map(CheckResult::getResponseTimeMs).orElse(null),
                latestCheckResult.map(CheckResult::getHttpStatusCode).orElse(null),
                latestCheckResult.map(CheckResult::getFailureLayer).orElse(null),
                latestCheckResult.map(CheckResult::getDiagnosticMessage).orElse(null),
                latestCheckResult.map(CheckResult::getRecommendation).orElse(null),

                nextCheckAt,
                secondsUntilNextCheck,

                openIncident.isPresent(),
                openIncident.map(Incident::getId).orElse(null),
                currentDowntimeSeconds,

                availabilityPercent24h,
                averageResponseTimeMs24h,

                healthScoreResponse.healthScore(),
                healthScoreResponse.healthLevel(),
                healthScoreResponse.recurrenceLevel(),

                networkService.getCreatedAt(),
                networkService.getUpdatedAt()
        );
    }

    private LocalDateTime calculateNextCheckAt(NetworkService networkService) {
        if (!Boolean.TRUE.equals(networkService.getIsEnabled())
                || networkService.getLastCheckedAt() == null
                || networkService.getIntervalSeconds() == null) {
            return null;
        }

        return networkService.getLastCheckedAt()
                .plusSeconds(networkService.getIntervalSeconds());
    }

    private Long calculateSecondsUntilNextCheck(LocalDateTime nextCheckAt) {
        if (nextCheckAt == null) {
            return null;
        }

        long seconds = Duration.between(LocalDateTime.now(), nextCheckAt).getSeconds();

        return Math.max(seconds, 0);
    }

    private Long calculateCurrentDowntimeSeconds(Optional<Incident> openIncident) {
        return openIncident
                .map(Incident::getOpenedAt)
                .map(openedAt -> Duration.between(openedAt, LocalDateTime.now()).getSeconds())
                .orElse(0L);
    }

    private Double calculateAvailabilityPercent24h(Long serviceId, LocalDateTime checkedAtAfter) {
        long totalChecks = checkResultRepository.countByServiceIdAndCheckedAtAfter(
                serviceId,
                checkedAtAfter
        );

        if (totalChecks == 0) {
            return null;
        }

        long successfulChecks = checkResultRepository.countByServiceIdAndStatusAndCheckedAtAfter(
                serviceId,
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

    private NetworkService findServiceById(Long serviceId) {
        return networkServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_SERVICE_NOT_FOUND));
    }

    private NetworkNode findNodeById(Long nodeId) {
        return networkNodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_NODE_NOT_FOUND));
    }

    private void validateServiceOwner(NetworkService networkService) {
        validateNodeOwner(networkService.getNode());
    }

    private void validateNodeOwner(NetworkNode networkNode) {
        User currentUser = currentUserService.getCurrentUser();

        if (!networkNode.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.NETWORK_NODE_ACCESS_DENIED);
        }
    }

    private String generateHeartbeatToken() {
        return UUID.randomUUID().toString();
    }
}