package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.IncidentLifecycleService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentLifecycleServiceImpl implements IncidentLifecycleService {

    private static final int DEFAULT_FAILURE_THRESHOLD = 2;
    private static final int DEFAULT_RECOVERY_THRESHOLD = 2;
    private static final int CRITICAL_FAILURE_COUNT = 5;

    private final IncidentRepository incidentRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void processCheckResult(CheckResult checkResult) {
        NetworkService service = checkResult.getService();

        initializeAntiFlappingFieldsIfNeeded(service);

        if (checkResult.getStatus() == ServiceStatus.DOWN) {
            processDownResult(checkResult, service);
            return;
        }

        if (checkResult.getStatus() == ServiceStatus.UP) {
            processUpResult(checkResult, service);
        }
    }

    private void processDownResult(CheckResult checkResult, NetworkService service) {
        service.setConsecutiveFailures(service.getConsecutiveFailures() + 1);
        service.setConsecutiveSuccesses(0);

        NetworkService savedService = networkServiceRepository.save(service);

        if (savedService.getConsecutiveFailures() < savedService.getFailureThreshold()) {
            return;
        }

        openIncidentIfNotExists(checkResult, savedService);
    }

    private void processUpResult(CheckResult checkResult, NetworkService service) {
        service.setConsecutiveSuccesses(service.getConsecutiveSuccesses() + 1);
        service.setConsecutiveFailures(0);

        NetworkService savedService = networkServiceRepository.save(service);

        if (savedService.getConsecutiveSuccesses() < savedService.getRecoveryThreshold()) {
            return;
        }

        closeIncidentIfExists(checkResult);
    }

    private void openIncidentIfNotExists(CheckResult checkResult, NetworkService service) {
        incidentRepository.findByServiceIdAndStatus(
                checkResult.getService().getId(),
                IncidentStatus.OPEN
        ).ifPresentOrElse(
                incident -> {
                },
                () -> openIncident(checkResult, service)
        );
    }

    private void openIncident(CheckResult checkResult, NetworkService service) {
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.OPEN);
        incident.setSeverity(determineSeverity(checkResult, service));
        incident.setOpenedAt(LocalDateTime.now());
        incident.setReason(checkResult.getDiagnosticMessage());
        incident.setService(checkResult.getService());
        incident.setOpenedByCheckResult(checkResult);

        Incident savedIncident = incidentRepository.save(incident);

        notificationService.notifyIncidentOpened(savedIncident);
    }

    private void closeIncidentIfExists(CheckResult checkResult) {
        incidentRepository.findByServiceIdAndStatus(
                checkResult.getService().getId(),
                IncidentStatus.OPEN
        ).ifPresent(incident -> closeIncident(incident, checkResult));
    }

    private void closeIncident(Incident incident, CheckResult checkResult) {
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setClosedAt(LocalDateTime.now());
        incident.setClosedByCheckResult(checkResult);

        Incident savedIncident = incidentRepository.save(incident);

        notificationService.notifyIncidentResolved(savedIncident);
    }

    private IncidentSeverity determineSeverity(CheckResult checkResult, NetworkService service) {
        if (service.getConsecutiveFailures() != null
                && service.getConsecutiveFailures() >= CRITICAL_FAILURE_COUNT) {
            return IncidentSeverity.CRITICAL;
        }

        FailureLayer failureLayer = checkResult.getFailureLayer();

        if (failureLayer == null) {
            return IncidentSeverity.MEDIUM;
        }

        return switch (failureLayer) {
            case PERFORMANCE -> IncidentSeverity.LOW;
            case APPLICATION, UNKNOWN -> IncidentSeverity.MEDIUM;
            case DNS, PORT, SSL, HEARTBEAT -> IncidentSeverity.HIGH;
            case NETWORK -> IncidentSeverity.CRITICAL;
        };
    }

    private void initializeAntiFlappingFieldsIfNeeded(NetworkService service) {
        if (service.getFailureThreshold() == null) {
            service.setFailureThreshold(DEFAULT_FAILURE_THRESHOLD);
        }

        if (service.getRecoveryThreshold() == null) {
            service.setRecoveryThreshold(DEFAULT_RECOVERY_THRESHOLD);
        }

        if (service.getConsecutiveFailures() == null) {
            service.setConsecutiveFailures(0);
        }

        if (service.getConsecutiveSuccesses() == null) {
            service.setConsecutiveSuccesses(0);
        }
    }
}