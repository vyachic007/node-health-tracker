package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;

import java.util.List;

public record IncidentRecoveryChecklistResponse(

        Long incidentId,

        Long serviceId,

        String serviceName,

        FailureLayer failureLayer,

        IncidentSeverity severity,

        String summary,

        List<RecoveryChecklistItemResponse> items
) {
}