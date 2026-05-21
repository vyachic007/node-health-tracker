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
    private final UserRepository userRepository;
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

        Double availabilityPercent24h = calculateUserAvailabilityPercent24h(
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

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getAdminSummary() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedUsers = userRepository.countByStatus(UserStatus.BLOCKED);

        long totalNodes = networkNodeRepository.count();
        long activeNodes = networkNodeRepository.countByIsActiveTrue();
        long inactiveNodes = networkNodeRepository.countByIsActiveFalse();

        long totalServices = networkServiceRepository.count();
        long enabledServices = networkServiceRepository.countByIsEnabledTrue();
        long disabledServices = networkServiceRepository.countByIsEnabledFalse();

        long upServices = networkServiceRepository.countCurrentServicesByStatus(
                ServiceStatus.UP.name()
        );

        long downServices = networkServiceRepository.countCurrentServicesByStatus(
                ServiceStatus.DOWN.name()
        );

        long unknownServices = calculateUnknownServices(
                enabledServices,
                upServices,
                downServices
        );

        long openIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long resolvedIncidents = incidentRepository.countByStatus(IncidentStatus.RESOLVED);

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);

        long checksLast24Hours = checkResultRepository.countByCheckedAtAfter(last24Hours);

        Double availabilityPercent24h = calculateAdminAvailabilityPercent24h(last24Hours);

        Double averageResponseTimeMs24h = roundToTwoDecimals(
                checkResultRepository.findAverageResponseTimeByStatusAfter(
                        ServiceStatus.UP,
                        last24Hours
                )
        );

        Integer averageHealthScore = calculateAdminAverageHealthScore(
                enabledServices,
                upServices,
                downServices,
                unknownServices
        );

        HealthLevel averageHealthLevel = determineHealthLevel(averageHealthScore);

        long healthyServices = averageHealthLevel == HealthLevel.HEALTHY ? enabledServices : 0;
        long degradedServices = averageHealthLevel == HealthLevel.DEGRADED ? enabledServices : 0;
        long unstableServices = averageHealthLevel == HealthLevel.UNSTABLE ? enabledServices : 0;
        long criticalServices = averageHealthLevel == HealthLevel.CRITICAL ? enabledServices : 0;

        return new AdminDashboardSummaryResponse(
                totalUsers,
                activeUsers,
                blockedUsers,

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

    private Integer calculateAdminAverageHealthScore(
            long enabledServices,
            long upServices,
            long downServices,
            long unknownServices
    ) {
        if (enabledServices == 0) {
            return null;
        }

        long totalScore =
                upServices * 100
                        + unknownServices * 60
                        + downServices * 30;

        return (int) Math.round(totalScore * 1.0 / enabledServices);
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

    private Double calculateUserAvailabilityPercent24h(Long ownerId, LocalDateTime checkedAtAfter) {
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

    private Double calculateAdminAvailabilityPercent24h(LocalDateTime checkedAtAfter) {
        long totalChecks = checkResultRepository.countByCheckedAtAfter(checkedAtAfter);

        if (totalChecks == 0) {
            return null;
        }

        long successfulChecks = checkResultRepository.countByStatusAndCheckedAtAfter(
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