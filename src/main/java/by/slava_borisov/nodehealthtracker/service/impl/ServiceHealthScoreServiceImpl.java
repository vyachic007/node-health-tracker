package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.ServiceHealthScoreService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceHealthScoreServiceImpl implements ServiceHealthScoreService {

    private static final int MAX_HEALTH_SCORE = 100;
    private static final int MIN_HEALTH_SCORE = 0;

    private static final double AVAILABILITY_WARNING_THRESHOLD = 95.0;
    private static final double AVAILABILITY_CRITICAL_THRESHOLD = 90.0;

    private static final double RESPONSE_TIME_WARNING_MS = 1000.0;

    private static final long MEDIUM_RECURRENCE_THRESHOLD_7D = 3;
    private static final long HIGH_RECURRENCE_THRESHOLD_7D = 5;

    private final NetworkServiceRepository networkServiceRepository;
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public ServiceHealthScoreResponse calculateHealthScore(Long serviceId) {
        NetworkService networkService = networkServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_SERVICE_NOT_FOUND));

        validateServiceOwner(networkService);

        Optional<CheckResult> latestCheckResult = checkResultRepository
                .findTopByServiceIdOrderByCheckedAtDesc(serviceId);

        Optional<Incident> openIncident = incidentRepository.findByServiceIdAndStatus(
                serviceId,
                IncidentStatus.OPEN
        );

        Optional<Incident> latestIncident = incidentRepository.findTopByServiceIdOrderByOpenedAtDesc(
                serviceId
        );

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        LocalDateTime last7Days = LocalDateTime.now().minusDays(7);

        Double availabilityPercent24h = calculateAvailabilityPercent24h(serviceId, last24Hours);

        Double averageResponseTimeMs24h = checkResultRepository.findAverageResponseTimeByServiceIdAndStatusAfter(
                serviceId,
                ServiceStatus.UP,
                last24Hours
        );

        ServiceStatus lastStatus = latestCheckResult
                .map(CheckResult::getStatus)
                .orElse(null);

        IncidentSeverity openIncidentSeverity = openIncident
                .map(Incident::getSeverity)
                .orElse(null);

        RecurrenceLevel recurrenceLevel = determineRecurrenceLevel(
                networkService,
                latestIncident,
                last7Days
        );

        Integer healthScore = calculateScore(
                lastStatus,
                openIncident.isPresent(),
                openIncidentSeverity,
                availabilityPercent24h,
                averageResponseTimeMs24h,
                recurrenceLevel
        );

        HealthLevel healthLevel = determineHealthLevel(healthScore);

        return new ServiceHealthScoreResponse(
                networkService.getId(),
                networkService.getName(),
                lastStatus,
                healthScore,
                healthLevel,
                availabilityPercent24h,
                averageResponseTimeMs24h,
                openIncident.isPresent(),
                openIncidentSeverity,
                recurrenceLevel,
                buildSummary(healthLevel)
        );
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

        return Math.round(availability * 100.0) / 100.0;
    }

    private RecurrenceLevel determineRecurrenceLevel(
            NetworkService networkService,
            Optional<Incident> latestIncident,
            LocalDateTime openedAtAfter
    ) {
        if (latestIncident.isEmpty()
                || latestIncident.get().getOpenedByCheckResult() == null
                || latestIncident.get().getOpenedByCheckResult().getFailureLayer() == null) {
            return RecurrenceLevel.LOW;
        }

        long similarIncidentsLast7d = incidentRepository.countSimilarIncidentsByServiceIdAndFailureLayerAfter(
                networkService.getId(),
                latestIncident.get().getOpenedByCheckResult().getFailureLayer(),
                openedAtAfter
        );

        if (similarIncidentsLast7d >= HIGH_RECURRENCE_THRESHOLD_7D) {
            return RecurrenceLevel.HIGH;
        }

        if (similarIncidentsLast7d >= MEDIUM_RECURRENCE_THRESHOLD_7D) {
            return RecurrenceLevel.MEDIUM;
        }

        return RecurrenceLevel.LOW;
    }

    private Integer calculateScore(
            ServiceStatus lastStatus,
            Boolean hasOpenIncident,
            IncidentSeverity openIncidentSeverity,
            Double availabilityPercent24h,
            Double averageResponseTimeMs24h,
            RecurrenceLevel recurrenceLevel
    ) {
        int score = MAX_HEALTH_SCORE;

        if (lastStatus == ServiceStatus.DOWN) {
            score -= 30;
        }

        if (Boolean.TRUE.equals(hasOpenIncident)) {
            score -= 25;
        }

        score -= calculateSeverityPenalty(openIncidentSeverity);
        score -= calculateAvailabilityPenalty(availabilityPercent24h);
        score -= calculateResponseTimePenalty(averageResponseTimeMs24h);
        score -= calculateRecurrencePenalty(recurrenceLevel);

        return Math.max(MIN_HEALTH_SCORE, Math.min(MAX_HEALTH_SCORE, score));
    }

    private Integer calculateSeverityPenalty(IncidentSeverity severity) {
        if (severity == null) {
            return 0;
        }

        return switch (severity) {
            case CRITICAL -> 25;
            case HIGH -> 18;
            case MEDIUM -> 10;
            case LOW -> 5;
        };
    }

    private Integer calculateAvailabilityPenalty(Double availabilityPercent24h) {
        if (availabilityPercent24h == null) {
            return 0;
        }

        if (availabilityPercent24h < AVAILABILITY_CRITICAL_THRESHOLD) {
            return 25;
        }

        if (availabilityPercent24h < AVAILABILITY_WARNING_THRESHOLD) {
            return 15;
        }

        return 0;
    }

    private Integer calculateResponseTimePenalty(Double averageResponseTimeMs24h) {
        if (averageResponseTimeMs24h == null) {
            return 0;
        }

        if (averageResponseTimeMs24h > RESPONSE_TIME_WARNING_MS) {
            return 10;
        }

        return 0;
    }

    private Integer calculateRecurrencePenalty(RecurrenceLevel recurrenceLevel) {
        if (recurrenceLevel == null) {
            return 0;
        }

        return switch (recurrenceLevel) {
            case HIGH -> 20;
            case MEDIUM -> 10;
            case LOW -> 0;
        };
    }

    private HealthLevel determineHealthLevel(Integer healthScore) {
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

    private String buildSummary(HealthLevel healthLevel) {
        return switch (healthLevel) {
            case HEALTHY -> Messages.HEALTH_SCORE_SUMMARY_HEALTHY;
            case DEGRADED -> Messages.HEALTH_SCORE_SUMMARY_DEGRADED;
            case UNSTABLE -> Messages.HEALTH_SCORE_SUMMARY_UNSTABLE;
            case CRITICAL -> Messages.HEALTH_SCORE_SUMMARY_CRITICAL;
        };
    }

    private void validateServiceOwner(NetworkService networkService) {
        User currentUser = currentUserService.getCurrentUser();

        if (!networkService.getNode().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.NETWORK_SERVICE_ACCESS_DENIED);
        }
    }
}