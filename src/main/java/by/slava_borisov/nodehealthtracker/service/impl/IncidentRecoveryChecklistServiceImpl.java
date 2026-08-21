package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecoveryChecklistResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.RecoveryChecklistItemResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.IncidentRecoveryChecklistService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentRecoveryChecklistServiceImpl implements IncidentRecoveryChecklistService {

    private final IncidentRepository incidentRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public IncidentRecoveryChecklistResponse getRecoveryChecklist(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.INCIDENT_NOT_FOUND));

        validateIncidentOwner(incident);

        FailureLayer failureLayer = resolveFailureLayer(incident);

        return new IncidentRecoveryChecklistResponse(
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName(),
                failureLayer,
                incident.getSeverity(),
                buildSummary(failureLayer),
                buildItems(failureLayer)
        );
    }

    private FailureLayer resolveFailureLayer(Incident incident) {
        CheckResult openedByCheckResult = incident.getOpenedByCheckResult();

        if (openedByCheckResult == null || openedByCheckResult.getFailureLayer() == null) {
            return FailureLayer.UNKNOWN;
        }

        return openedByCheckResult.getFailureLayer();
    }

    private String buildSummary(FailureLayer failureLayer) {
        return switch (failureLayer) {
            case DNS -> Messages.RECOVERY_CHECKLIST_SUMMARY_DNS;
            case NETWORK -> Messages.RECOVERY_CHECKLIST_SUMMARY_NETWORK;
            case PORT -> Messages.RECOVERY_CHECKLIST_SUMMARY_PORT;
            case SSL -> Messages.RECOVERY_CHECKLIST_SUMMARY_SSL;
            case APPLICATION -> Messages.RECOVERY_CHECKLIST_SUMMARY_APPLICATION;
            case PERFORMANCE -> Messages.RECOVERY_CHECKLIST_SUMMARY_PERFORMANCE;
            case UNKNOWN -> Messages.RECOVERY_CHECKLIST_SUMMARY_UNKNOWN;
        };
    }

    private List<RecoveryChecklistItemResponse> buildItems(FailureLayer failureLayer) {
        return switch (failureLayer) {
            case DNS -> buildDnsChecklist();
            case NETWORK -> buildNetworkChecklist();
            case PORT -> buildPortChecklist();
            case SSL -> buildSslChecklist();
            case APPLICATION -> buildApplicationChecklist();
            case PERFORMANCE -> buildPerformanceChecklist();
            case UNKNOWN -> buildUnknownChecklist();
        };
    }

    private List<RecoveryChecklistItemResponse> buildDnsChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_DNS_STEP_1_TITLE, Messages.RECOVERY_DNS_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_DNS_STEP_2_TITLE, Messages.RECOVERY_DNS_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_DNS_STEP_3_TITLE, Messages.RECOVERY_DNS_STEP_3_DESCRIPTION, true),
                item(4, Messages.RECOVERY_DNS_STEP_4_TITLE, Messages.RECOVERY_DNS_STEP_4_DESCRIPTION, false)
        );
    }

    private List<RecoveryChecklistItemResponse> buildNetworkChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_NETWORK_STEP_1_TITLE, Messages.RECOVERY_NETWORK_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_NETWORK_STEP_2_TITLE, Messages.RECOVERY_NETWORK_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_NETWORK_STEP_3_TITLE, Messages.RECOVERY_NETWORK_STEP_3_DESCRIPTION, true),
                item(4, Messages.RECOVERY_NETWORK_STEP_4_TITLE, Messages.RECOVERY_NETWORK_STEP_4_DESCRIPTION, false)
        );
    }

    private List<RecoveryChecklistItemResponse> buildPortChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_PORT_STEP_1_TITLE, Messages.RECOVERY_PORT_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_PORT_STEP_2_TITLE, Messages.RECOVERY_PORT_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_PORT_STEP_3_TITLE, Messages.RECOVERY_PORT_STEP_3_DESCRIPTION, true),
                item(4, Messages.RECOVERY_PORT_STEP_4_TITLE, Messages.RECOVERY_PORT_STEP_4_DESCRIPTION, false),
                item(5, Messages.RECOVERY_PORT_STEP_5_TITLE, Messages.RECOVERY_PORT_STEP_5_DESCRIPTION, false)
        );
    }

    private List<RecoveryChecklistItemResponse> buildSslChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_SSL_STEP_1_TITLE, Messages.RECOVERY_SSL_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_SSL_STEP_2_TITLE, Messages.RECOVERY_SSL_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_SSL_STEP_3_TITLE, Messages.RECOVERY_SSL_STEP_3_DESCRIPTION, true),
                item(4, Messages.RECOVERY_SSL_STEP_4_TITLE, Messages.RECOVERY_SSL_STEP_4_DESCRIPTION, false)
        );
    }

    private List<RecoveryChecklistItemResponse> buildApplicationChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_APPLICATION_STEP_1_TITLE, Messages.RECOVERY_APPLICATION_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_APPLICATION_STEP_2_TITLE, Messages.RECOVERY_APPLICATION_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_APPLICATION_STEP_3_TITLE, Messages.RECOVERY_APPLICATION_STEP_3_DESCRIPTION, false),
                item(4, Messages.RECOVERY_APPLICATION_STEP_4_TITLE, Messages.RECOVERY_APPLICATION_STEP_4_DESCRIPTION, false)
        );
    }

    private List<RecoveryChecklistItemResponse> buildPerformanceChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_PERFORMANCE_STEP_1_TITLE, Messages.RECOVERY_PERFORMANCE_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_PERFORMANCE_STEP_2_TITLE, Messages.RECOVERY_PERFORMANCE_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_PERFORMANCE_STEP_3_TITLE, Messages.RECOVERY_PERFORMANCE_STEP_3_DESCRIPTION, false),
                item(4, Messages.RECOVERY_PERFORMANCE_STEP_4_TITLE, Messages.RECOVERY_PERFORMANCE_STEP_4_DESCRIPTION, false)
        );
    }

    private List<RecoveryChecklistItemResponse> buildUnknownChecklist() {
        return List.of(
                item(1, Messages.RECOVERY_UNKNOWN_STEP_1_TITLE, Messages.RECOVERY_UNKNOWN_STEP_1_DESCRIPTION, true),
                item(2, Messages.RECOVERY_UNKNOWN_STEP_2_TITLE, Messages.RECOVERY_UNKNOWN_STEP_2_DESCRIPTION, true),
                item(3, Messages.RECOVERY_UNKNOWN_STEP_3_TITLE, Messages.RECOVERY_UNKNOWN_STEP_3_DESCRIPTION, true),
                item(4, Messages.RECOVERY_UNKNOWN_STEP_4_TITLE, Messages.RECOVERY_UNKNOWN_STEP_4_DESCRIPTION, false)
        );
    }

    private RecoveryChecklistItemResponse item(
            Integer stepNumber,
            String title,
            String description,
            Boolean isCritical
    ) {
        return new RecoveryChecklistItemResponse(
                stepNumber,
                title,
                description,
                isCritical
        );
    }

    private void validateIncidentOwner(Incident incident) {
        User currentUser = currentUserService.getCurrentUser();

        if (!incident.getService().getNode().getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(Messages.INCIDENT_ACCESS_DENIED);
        }
    }
}