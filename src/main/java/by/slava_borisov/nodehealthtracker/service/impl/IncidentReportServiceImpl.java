package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentReportResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentTimelineEventRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.IncidentReportService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentReportServiceImpl implements IncidentReportService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineEventRepository incidentTimelineEventRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public IncidentReportResponse getIncidentReport(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.INCIDENT_NOT_FOUND));

        validateIncidentOwner(incident);

        CheckResult openedByCheckResult = incident.getOpenedByCheckResult();
        CheckResult closedByCheckResult = incident.getClosedByCheckResult();

        FailureLayer failureLayer = resolveFailureLayer(openedByCheckResult);
        Long durationSeconds = calculateDurationSeconds(incident);
        Long durationMinutes = calculateDurationMinutes(durationSeconds);
        Integer timelineEventsCount = calculateTimelineEventsCount(incident.getId());

        return new IncidentReportResponse(
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName(),
                incident.getStatus(),
                incident.getSeverity(),
                failureLayer,
                incident.getReason(),
                resolveRecommendation(openedByCheckResult),
                incident.getOpenedAt(),
                incident.getClosedAt(),
                durationSeconds,
                durationMinutes,
                resolveCheckResultId(openedByCheckResult),
                resolveCheckResultId(closedByCheckResult),
                timelineEventsCount,
                buildSummary(incident.getStatus(), failureLayer)
        );
    }

    private FailureLayer resolveFailureLayer(CheckResult checkResult) {
        if (checkResult == null || checkResult.getFailureLayer() == null) {
            return FailureLayer.UNKNOWN;
        }

        return checkResult.getFailureLayer();
    }

    private String resolveRecommendation(CheckResult checkResult) {
        if (checkResult == null) {
            return null;
        }

        return checkResult.getRecommendation();
    }

    private Long resolveCheckResultId(CheckResult checkResult) {
        if (checkResult == null) {
            return null;
        }

        return checkResult.getId();
    }

    private Long calculateDurationSeconds(Incident incident) {
        LocalDateTime endTime = incident.getClosedAt();

        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return Duration.between(incident.getOpenedAt(), endTime).getSeconds();
    }

    private Long calculateDurationMinutes(Long durationSeconds) {
        if (durationSeconds == null) {
            return null;
        }

        return durationSeconds / 60;
    }

    private Integer calculateTimelineEventsCount(Long incidentId) {
        return Math.toIntExact(
                incidentTimelineEventRepository.countByIncidentId(incidentId)
        );
    }

    private String buildSummary(
            IncidentStatus status,
            FailureLayer failureLayer
    ) {
        String statusSummary = status == IncidentStatus.RESOLVED
                ? Messages.INCIDENT_REPORT_SUMMARY_RESOLVED
                : Messages.INCIDENT_REPORT_SUMMARY_OPEN;

        return statusSummary + " " + buildFailureLayerSummary(failureLayer);
    }

    private String buildFailureLayerSummary(FailureLayer failureLayer) {
        return switch (failureLayer) {
            case DNS -> Messages.INCIDENT_REPORT_SUMMARY_DNS;
            case NETWORK -> Messages.INCIDENT_REPORT_SUMMARY_NETWORK;
            case PORT -> Messages.INCIDENT_REPORT_SUMMARY_PORT;
            case SSL -> Messages.INCIDENT_REPORT_SUMMARY_SSL;
            case APPLICATION -> Messages.INCIDENT_REPORT_SUMMARY_APPLICATION;
            case PERFORMANCE -> Messages.INCIDENT_REPORT_SUMMARY_PERFORMANCE;
            case UNKNOWN -> Messages.INCIDENT_REPORT_SUMMARY_UNKNOWN;
        };
    }

    private void validateIncidentOwner(Incident incident) {
        User currentUser = currentUserService.getCurrentUser();

        if (!incident.getService()
                .getNode()
                .getOwner()
                .getId()
                .equals(currentUser.getId())) {

            throw new AccessDeniedException(
                    Messages.INCIDENT_ACCESS_DENIED
            );
        }
    }
}