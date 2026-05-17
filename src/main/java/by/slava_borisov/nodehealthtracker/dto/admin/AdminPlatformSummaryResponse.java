package by.slava_borisov.nodehealthtracker.dto.admin;

public record AdminPlatformSummaryResponse(

        long totalUsers,

        long activeUsers,

        long blockedUsers,

        long adminUsers,

        long regularUsers,

        long totalNodes,

        long totalServices,

        long enabledServices,

        long disabledServices,

        long upServices,

        long downServices,

        long openIncidents,

        long resolvedIncidents,

        long checksLast24Hours
) {
}