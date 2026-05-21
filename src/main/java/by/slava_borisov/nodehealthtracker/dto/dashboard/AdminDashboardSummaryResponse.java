package by.slava_borisov.nodehealthtracker.dto.dashboard;

import by.slava_borisov.nodehealthtracker.model.enums.HealthLevel;

public record AdminDashboardSummaryResponse(

        long totalUsers,
        long activeUsers,
        long blockedUsers,

        long totalNodes,
        long activeNodes,
        long inactiveNodes,

        long totalServices,
        long enabledServices,
        long disabledServices,

        long upServices,
        long downServices,
        long unknownServices,

        long openIncidents,
        long resolvedIncidents,

        long checksLast24Hours,

        Integer averageHealthScore,
        HealthLevel averageHealthLevel,

        long healthyServices,
        long degradedServices,
        long unstableServices,
        long criticalServices,

        Double availabilityPercent24h,
        Double averageResponseTimeMs24h
) {
}