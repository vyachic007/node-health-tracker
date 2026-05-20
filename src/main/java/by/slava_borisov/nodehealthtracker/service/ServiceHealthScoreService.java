package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;

public interface ServiceHealthScoreService {

    ServiceHealthScoreResponse calculateHealthScore(Long serviceId);
}