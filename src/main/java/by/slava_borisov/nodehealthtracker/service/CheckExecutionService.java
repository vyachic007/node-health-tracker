package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;

import java.util.List;

public interface CheckExecutionService {

    CheckResultResponse runCheck(Long serviceId);

    List<CheckResultResponse> runEnabledChecks();

    List<CheckResultResponse> getCheckHistory(Long serviceId);
}