package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import by.slava_borisov.nodehealthtracker.service.IncidentLifecycleService;
import by.slava_borisov.nodehealthtracker.service.IncidentSeverityService;
import by.slava_borisov.nodehealthtracker.service.IncidentTimelineService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentLifecycleServiceImpl implements IncidentLifecycleService {

    private static final String INCIDENT_ENTITY_TYPE = "Incident";

    private final IncidentRepository incidentRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final NotificationService notificationService;
    private final IncidentSeverityService incidentSeverityService;
    private final IncidentTimelineService incidentTimelineService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public void processCheckResult(CheckResult checkResult) {
        NetworkService networkService = checkResult.getService();

        if (checkResult.getStatus() == ServiceStatus.DOWN) {
            processFailedCheck(checkResult, networkService);
            return;
        }

        if (checkResult.getStatus() == ServiceStatus.UP) {
            processSuccessfulCheck(checkResult, networkService);
        }
    }

    private void processFailedCheck(CheckResult checkResult, NetworkService networkService) {
        networkService.setConsecutiveFailures(networkService.getConsecutiveFailures() + 1);
        networkService.setConsecutiveSuccesses(0);
        networkServiceRepository.save(networkService);

        if (networkService.getConsecutiveFailures() < networkService.getFailureThreshold()) {
            return;
        }

        openIncidentIfNotExists(checkResult);
    }

    private void processSuccessfulCheck(CheckResult checkResult, NetworkService networkService) {
        networkService.setConsecutiveSuccesses(networkService.getConsecutiveSuccesses() + 1);
        networkService.setConsecutiveFailures(0);
        networkServiceRepository.save(networkService);

        if (networkService.getConsecutiveSuccesses() < networkService.getRecoveryThreshold()) {
            return;
        }

        closeIncidentIfExists(checkResult);
    }

    private void openIncidentIfNotExists(CheckResult checkResult) {
        incidentRepository.findByServiceIdAndStatus(
                checkResult.getService().getId(),
                IncidentStatus.OPEN
        ).ifPresentOrElse(
                incident -> {
                },
                () -> openIncident(checkResult)
        );
    }

    private void openIncident(CheckResult checkResult) {
        Incident incident = new Incident();
        incident.setStatus(IncidentStatus.OPEN);
        incident.setSeverity(incidentSeverityService.determineSeverity(checkResult));
        incident.setOpenedAt(LocalDateTime.now());
        incident.setReason(checkResult.getDiagnosticMessage());
        incident.setService(checkResult.getService());
        incident.setOpenedByCheckResult(checkResult);

        Incident savedIncident = incidentRepository.save(incident);

        incidentTimelineService.createEvent(
                savedIncident,
                checkResult,
                IncidentTimelineEventType.CHECK_FAILED,
                Messages.INCIDENT_TIMELINE_CHECK_FAILED + checkResult.getDiagnosticMessage()
        );

        incidentTimelineService.createEvent(
                savedIncident,
                checkResult,
                IncidentTimelineEventType.SEVERITY_ASSIGNED,
                Messages.INCIDENT_TIMELINE_SEVERITY_ASSIGNED + savedIncident.getSeverity()
        );

        incidentTimelineService.createEvent(
                savedIncident,
                checkResult,
                IncidentTimelineEventType.INCIDENT_OPENED,
                Messages.INCIDENT_TIMELINE_INCIDENT_OPENED + savedIncident.getService().getName()
        );

        auditLogService.logSystemAction(
                AuditActionType.INCIDENT_OPENED,
                Messages.AUDIT_INCIDENT_OPENED + savedIncident.getService().getName(),
                INCIDENT_ENTITY_TYPE,
                savedIncident.getId()
        );

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

        incidentTimelineService.createEvent(
                savedIncident,
                checkResult,
                IncidentTimelineEventType.CHECK_RECOVERED,
                Messages.INCIDENT_TIMELINE_CHECK_RECOVERED
        );

        incidentTimelineService.createEvent(
                savedIncident,
                checkResult,
                IncidentTimelineEventType.INCIDENT_RESOLVED,
                Messages.INCIDENT_TIMELINE_INCIDENT_RESOLVED
        );

        auditLogService.logSystemAction(
                AuditActionType.INCIDENT_RESOLVED,
                Messages.AUDIT_INCIDENT_RESOLVED + savedIncident.getService().getName(),
                INCIDENT_ENTITY_TYPE,
                savedIncident.getId()
        );

        notificationService.notifyIncidentResolved(savedIncident);
    }
}