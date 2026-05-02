package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;

public interface IncidentLifecycleService {

    void processCheckResult(CheckResult checkResult);
}