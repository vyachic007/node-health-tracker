package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticServiceImpl implements DiagnosticService {

    private static final int SLOW_RESPONSE_THRESHOLD_MS = 3000;

    @Override
    public DiagnosticResult diagnose(
            boolean dnsAvailable,
            boolean pingAvailable,
            boolean tcpAvailable,
            boolean sslValid,
            boolean heartbeatAvailable,
            Integer httpStatusCode,
            Integer responseTimeMs
    ) {
        if (!dnsAvailable) {
            return new DiagnosticResult(
                    FailureLayer.DNS,
                    Messages.DNS_RESOLUTION_FAILED,
                    Messages.DNS_RESOLUTION_FAILED_RECOMMENDATION
            );
        }

        if (!pingAvailable) {
            return new DiagnosticResult(
                    FailureLayer.NETWORK,
                    Messages.HOST_UNREACHABLE,
                    Messages.HOST_UNREACHABLE_RECOMMENDATION
            );
        }

        if (!tcpAvailable) {
            return new DiagnosticResult(
                    FailureLayer.PORT,
                    Messages.TCP_CONNECTION_FAILED,
                    Messages.TCP_CONNECTION_FAILED_RECOMMENDATION
            );
        }

        if (!sslValid) {
            return new DiagnosticResult(
                    FailureLayer.SSL,
                    Messages.SSL_VALIDATION_FAILED,
                    Messages.SSL_VALIDATION_FAILED_RECOMMENDATION
            );
        }

        if (!heartbeatAvailable) {
            return new DiagnosticResult(
                    FailureLayer.HEARTBEAT,
                    Messages.HEARTBEAT_FAILED,
                    Messages.HEARTBEAT_FAILED_RECOMMENDATION
            );
        }

        if (httpStatusCode != null && httpStatusCode >= 500) {
            return new DiagnosticResult(
                    FailureLayer.APPLICATION,
                    Messages.HTTP_SERVER_ERROR,
                    Messages.HTTP_SERVER_ERROR_RECOMMENDATION
            );
        }

        if (httpStatusCode != null && httpStatusCode >= 400) {
            return new DiagnosticResult(
                    FailureLayer.APPLICATION,
                    Messages.HTTP_CLIENT_ERROR,
                    Messages.HTTP_CLIENT_ERROR_RECOMMENDATION
            );
        }

        if (responseTimeMs != null && responseTimeMs > SLOW_RESPONSE_THRESHOLD_MS) {
            return new DiagnosticResult(
                    FailureLayer.PERFORMANCE,
                    Messages.SLOW_RESPONSE,
                    Messages.SLOW_RESPONSE_RECOMMENDATION
            );
        }

        return new DiagnosticResult(
                FailureLayer.UNKNOWN,
                Messages.NO_CRITICAL_PROBLEM_DETECTED,
                Messages.NO_ACTION_REQUIRED
        );
    }
}