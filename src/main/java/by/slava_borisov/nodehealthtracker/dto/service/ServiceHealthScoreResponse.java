package by.slava_borisov.nodehealthtracker.dto.service;

import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.RecurrenceLevel;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;

public record ServiceHealthScoreResponse(

        Long serviceId,

        String serviceName,

        ServiceStatus lastStatus,

        Integer healthScore,

        HealthLevel healthLevel,

        Double availabilityPercent24h,

        Double averageResponseTimeMs24h,

        Boolean hasOpenIncident,

        IncidentSeverity openIncidentSeverity,

        RecurrenceLevel recurrenceLevel,

        String summary
) {
}