package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.dashboard.DashboardSummaryResponse;

public interface DashboardService {

    DashboardSummaryResponse getCurrentUserSummary();
}