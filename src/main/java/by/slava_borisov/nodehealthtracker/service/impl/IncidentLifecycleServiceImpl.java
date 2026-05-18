package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.service.IncidentLifecycleService;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentLifecycleServiceImpl implements IncidentLifecycleService {

    private final IncidentRepository incidentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void processCheckResult(CheckResult checkResult) {
        if (checkResult.getStatus() == ServiceStatus.DOWN) {
            openIncidentIfNotExists(checkResult);
            return;
        }

        if (checkResult.getStatus() == ServiceStatus.UP) {
            closeIncidentIfExists(checkResult);
        }
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
}