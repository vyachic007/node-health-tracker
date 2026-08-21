package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.service.IncidentSeverityService;
import org.springframework.stereotype.Service;

@Service
public class IncidentSeverityServiceImpl implements IncidentSeverityService {

    @Override
    public IncidentSeverity determineSeverity(CheckResult checkResult) {
        if (checkResult.getFailureLayer() == null) {
            return IncidentSeverity.MEDIUM;
        }

        FailureLayer failureLayer = checkResult.getFailureLayer();

        return switch (failureLayer) {
            case DNS, NETWORK -> IncidentSeverity.CRITICAL;
            case PORT, SSL -> IncidentSeverity.HIGH;
            case APPLICATION, PERFORMANCE -> IncidentSeverity.MEDIUM;
            case UNKNOWN -> IncidentSeverity.LOW;
        };
    }
}