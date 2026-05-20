package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecurrenceAnalysisResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.IncidentRecurrenceAnalysisService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentRecurrenceAnalysisServiceImpl implements IncidentRecurrenceAnalysisService {

    private static final long MEDIUM_RECURRENCE_THRESHOLD_7D = 3;
    private static final long HIGH_RECURRENCE_THRESHOLD_7D = 5;

    private final IncidentRepository incidentRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public IncidentRecurrenceAnalysisResponse analyzeRecurrence(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.INCIDENT_NOT_FOUND));

        validateIncidentOwner(incident);

        FailureLayer failureLayer = resolveFailureLayer(incident.getOpenedByCheckResult());

        Long similarIncidentsLast24h = countSimilarIncidents(
                incident,
                failureLayer,
                LocalDateTime.now().minusHours(24)
        );

        Long similarIncidentsLast7d = countSimilarIncidents(
                incident,
                failureLayer,
                LocalDateTime.now().minusDays(7)
        );

        Long similarIncidentsLast30d = countSimilarIncidents(
                incident,
                failureLayer,
                LocalDateTime.now().minusDays(30)
        );

        RecurrenceLevel recurrenceLevel = determineRecurrenceLevel(similarIncidentsLast7d);
        Boolean isRecurring = recurrenceLevel != RecurrenceLevel.LOW;

        return new IncidentRecurrenceAnalysisResponse(
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName(),
                failureLayer,
                incident.getSeverity(),
                similarIncidentsLast24h,
                similarIncidentsLast7d,
                similarIncidentsLast30d,
                isRecurring,
                recurrenceLevel,
                buildRecommendation(recurrenceLevel)
        );
    }

    private FailureLayer resolveFailureLayer(CheckResult checkResult) {
        if (checkResult == null || checkResult.getFailureLayer() == null) {
            return FailureLayer.UNKNOWN;
        }

        return checkResult.getFailureLayer();
    }

    private Long countSimilarIncidents(
            Incident incident,
            FailureLayer failureLayer,
            LocalDateTime openedAtAfter
    ) {
        return incidentRepository.countSimilarIncidentsByServiceIdAndFailureLayerAfter(
                incident.getService().getId(),
                failureLayer,
                openedAtAfter
        );
    }

    private RecurrenceLevel determineRecurrenceLevel(Long similarIncidentsLast7d) {
        if (similarIncidentsLast7d >= HIGH_RECURRENCE_THRESHOLD_7D) {
            return RecurrenceLevel.HIGH;
        }

        if (similarIncidentsLast7d >= MEDIUM_RECURRENCE_THRESHOLD_7D) {
            return RecurrenceLevel.MEDIUM;
        }

        return RecurrenceLevel.LOW;
    }

    private String buildRecommendation(RecurrenceLevel recurrenceLevel) {
        return switch (recurrenceLevel) {
            case LOW -> Messages.RECURRENCE_RECOMMENDATION_LOW;
            case MEDIUM -> Messages.RECURRENCE_RECOMMENDATION_MEDIUM;
            case HIGH -> Messages.RECURRENCE_RECOMMENDATION_HIGH;
        };
    }

    private void validateIncidentOwner(Incident incident) {
        User currentUser = currentUserService.getCurrentUser();

        if (!incident.getService().getNode().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.INCIDENT_ACCESS_DENIED);
        }
    }
}