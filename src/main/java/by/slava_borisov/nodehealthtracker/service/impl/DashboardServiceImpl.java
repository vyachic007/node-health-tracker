package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.dashboard.DashboardSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.DashboardService;
import by.slava_borisov.nodehealthtracker.service.ServiceHealthScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CurrentUserService currentUserService;
    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final IncidentRepository incidentRepository;
    private final CheckResultRepository checkResultRepository;
    private final ServiceHealthScoreService serviceHealthScoreService;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getCurrentUserSummary() {
        User currentUser = currentUserService.getCurrentUser();
        Long ownerId = currentUser.getId();

        long totalNodes = networkNodeRepository.countByOwnerId(ownerId);
        long activeNodes = networkNodeRepository.countByOwnerIdAndIsActiveTrue(ownerId);
        long inactiveNodes = networkNodeRepository.countByOwnerIdAndIsActiveFalse(ownerId);

        long totalServices = networkServiceRepository.countByNodeOwnerId(ownerId);
        long enabledServices = networkServiceRepository.countByNodeOwnerIdAndIsEnabledTrue(ownerId);
        long disabledServices = networkServiceRepository.countByNodeOwnerIdAndIsEnabledFalse(ownerId);

        long upServices = networkServiceRepository.countCurrentServicesByStatus(
                ownerId,
                ServiceStatus.UP.name()
        );

        long downServices = networkServiceRepository.countCurrentServicesByStatus(
                ownerId,
                ServiceStatus.DOWN.name()
        );

        long unknownServices = calculateUnknownServices(
                enabledServices,
                upServices,
                downServices
        );

        long openIncidents = incidentRepository.countByServiceNodeOwnerIdAndStatus(
                ownerId,
                IncidentStatus.OPEN
        );

        long resolvedIncidents = incidentRepository.countByServiceNodeOwnerIdAndStatus(
                ownerId,
                IncidentStatus.RESOLVED
        );

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        long checksLast24Hours = checkResultRepository.countByServiceNodeOwnerIdAndCheckedAtAfter(
                ownerId,
                last24Hours
        );

        List<ServiceHealthScoreResponse> serviceHealthScores = networkServiceRepository
                .findAllByNodeOwnerId(ownerId)
                .stream()
                .filter(NetworkService::getIsEnabled)
                .map(service -> serviceHealthScoreService.calculateHealthScore(service.getId()))
                .toList();

        Integer averageHealthScore = calculateAverageHealthScore(serviceHealthScores);
        HealthLevel averageHealthLevel = determineHealthLevel(averageHealthScore);

        long healthyServices = countServicesByHealthLevel(
                serviceHealthScores,
                HealthLevel.HEALTHY
        );

        long degradedServices = countServicesByHealthLevel(
                serviceHealthScores,
                HealthLevel.DEGRADED
        );

        long unstableServices = countServicesByHealthLevel(
                serviceHealthScores,
                HealthLevel.UNSTABLE
        );

        long criticalServices = countServicesByHealthLevel(
                serviceHealthScores,
                HealthLevel.CRITICAL
        );

        Double availabilityPercent24h = calculateAvailabilityPercent24h(
                ownerId,
                last24Hours
        );

        Double averageResponseTimeMs24h = roundToTwoDecimals(
                checkResultRepository.findAverageResponseTimeByOwnerIdAndStatusAfter(
                        ownerId,
                        ServiceStatus.UP,
                        last24Hours
                )
        );

        return new DashboardSummaryResponse(
                totalNodes,
                activeNodes,
                inactiveNodes,

                totalServices,
                enabledServices,
                disabledServices,

                upServices,
                downServices,
                unknownServices,

                openIncidents,
                resolvedIncidents,

                checksLast24Hours,

                averageHealthScore,
                averageHealthLevel,

                healthyServices,
                degradedServices,
                unstableServices,
                criticalServices,

                availabilityPercent24h,
                averageResponseTimeMs24h
        );
    }

    private long calculateUnknownServices(
            long enabledServices,
            long upServices,
            long downServices
    ) {
        long unknownServices = enabledServices - upServices - downServices;

        return Math.max(unknownServices, 0);
    }

    private Integer calculateAverageHealthScore(List<ServiceHealthScoreResponse> serviceHealthScores) {
        if (serviceHealthScores.isEmpty()) {
            return null;
        }

        double averageScore = serviceHealthScores.stream()
                .map(ServiceHealthScoreResponse::healthScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        return (int) Math.round(averageScore);
    }

    private HealthLevel determineHealthLevel(Integer healthScore) {
        if (healthScore == null) {
            return null;
        }

        if (healthScore >= 90) {
            return HealthLevel.HEALTHY;
        }

        if (healthScore >= 70) {
            return HealthLevel.DEGRADED;
        }

        if (healthScore >= 40) {
            return HealthLevel.UNSTABLE;
        }

        return HealthLevel.CRITICAL;
    }

    private long countServicesByHealthLevel(
            List<ServiceHealthScoreResponse> serviceHealthScores,
            HealthLevel healthLevel
    ) {
        return serviceHealthScores.stream()
                .filter(serviceHealthScore -> serviceHealthScore.healthLevel() == healthLevel)
                .count();
    }

    private Double calculateAvailabilityPercent24h(Long ownerId, LocalDateTime checkedAtAfter) {
        long totalChecks = checkResultRepository.countByServiceNodeOwnerIdAndCheckedAtAfter(
                ownerId,
                checkedAtAfter
        );

        if (totalChecks == 0) {
            return null;
        }

        long successfulChecks = checkResultRepository.countByServiceNodeOwnerIdAndStatusAndCheckedAtAfter(
                ownerId,
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
}