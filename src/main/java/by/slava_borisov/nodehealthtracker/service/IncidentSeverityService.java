package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;

public interface IncidentSeverityService {

    IncidentSeverity determineSeverity(CheckResult checkResult);
}