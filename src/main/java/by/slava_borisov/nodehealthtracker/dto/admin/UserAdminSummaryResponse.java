package by.slava_borisov.nodehealthtracker.dto.admin;

public record UserAdminSummaryResponse(

        long totalUsers,

        long activeUsers,

        long blockedUsers,

        long adminUsers,

        long regularUsers
) {
}