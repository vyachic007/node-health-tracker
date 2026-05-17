package by.slava_borisov.nodehealthtracker.dto.dashboard;

public record DashboardSummaryResponse(

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