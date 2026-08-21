package by.slava_borisov.nodehealthtracker.service;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;

public interface DiagnosticService {

    DiagnosticResult diagnose(
            boolean dnsAvailable,
            boolean pingAvailable,
            boolean tcpAvailable,
            boolean sslValid,
            Integer httpStatusCode,
            Integer responseTimeMs
    );

    record DiagnosticResult(
            FailureLayer failureLayer,
            String diagnosticMessage,
            String recommendation
    ) {
    }
}