package by.slava_borisov.nodehealthtracker.dto.incident;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;

public record IncidentRecurrenceAnalysisResponse(

        Long incidentId,

        Long serviceId,

        String serviceName,

        FailureLayer failureLayer,

        IncidentSeverity severity,

        Long similarIncidentsLast24h,

        Long similarIncidentsLast7d,

        Long similarIncidentsLast30d,

        Boolean isRecurring,

        RecurrenceLevel recurrenceLevel,

        String recommendation
) {
}