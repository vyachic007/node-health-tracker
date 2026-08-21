package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService.DiagnosticResult;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Тесты DiagnosticServiceImpl")
class DiagnosticServiceImplTest {

    private DiagnosticService diagnosticService;

    @BeforeEach
    void setUp() {
        diagnosticService = new DiagnosticServiceImpl();
    }

    @Test
    @DisplayName("Диагностика - сбой DNS")
    void diagnose_dnsFailure_returnsDnsLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                false,
                true,
                true,
                true,
                200,
                100
        );

        assertEquals(FailureLayer.DNS, result.failureLayer());
        assertEquals(Messages.DNS_RESOLUTION_FAILED, result.diagnosticMessage());
        assertEquals(
                Messages.DNS_RESOLUTION_FAILED_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - сбой сети (пинг)")
    void diagnose_pingFailure_returnsNetworkLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                false,
                true,
                true,
                200,
                100
        );

        assertEquals(FailureLayer.NETWORK, result.failureLayer());
        assertEquals(Messages.HOST_UNREACHABLE, result.diagnosticMessage());
        assertEquals(
                Messages.HOST_UNREACHABLE_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - сбой порта (TCP)")
    void diagnose_tcpFailure_returnsPortLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                false,
                true,
                200,
                100
        );

        assertEquals(FailureLayer.PORT, result.failureLayer());
        assertEquals(Messages.TCP_CONNECTION_FAILED, result.diagnosticMessage());
        assertEquals(
                Messages.TCP_CONNECTION_FAILED_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - сбой SSL")
    void diagnose_sslFailure_returnsSslLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                true,
                false,
                200,
                100
        );

        assertEquals(FailureLayer.SSL, result.failureLayer());
        assertEquals(Messages.SSL_VALIDATION_FAILED, result.diagnosticMessage());
        assertEquals(
                Messages.SSL_VALIDATION_FAILED_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - ошибка сервера (5xx)")
    void diagnose_http5xxError_returnsApplicationLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                true,
                true,
                503,
                100
        );

        assertEquals(FailureLayer.APPLICATION, result.failureLayer());
        assertEquals(Messages.HTTP_SERVER_ERROR, result.diagnosticMessage());
        assertEquals(
                Messages.HTTP_SERVER_ERROR_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - ошибка клиента (4xx)")
    void diagnose_http4xxError_returnsApplicationLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                true,
                true,
                404,
                100
        );

        assertEquals(FailureLayer.APPLICATION, result.failureLayer());
        assertEquals(Messages.HTTP_CLIENT_ERROR, result.diagnosticMessage());
        assertEquals(
                Messages.HTTP_CLIENT_ERROR_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - медленный ответ")
    void diagnose_slowResponse_returnsPerformanceLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                true,
                true,
                200,
                3001
        );

        assertEquals(FailureLayer.PERFORMANCE, result.failureLayer());
        assertEquals(Messages.SLOW_RESPONSE, result.diagnosticMessage());
        assertEquals(
                Messages.SLOW_RESPONSE_RECOMMENDATION,
                result.recommendation()
        );
    }

    @Test
    @DisplayName("Диагностика - сервис работает без критических проблем")
    void diagnose_healthyService_returnsUnknownLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                true,
                true,
                200,
                100
        );

        assertEquals(FailureLayer.UNKNOWN, result.failureLayer());
        assertEquals(
                Messages.NO_CRITICAL_PROBLEM_DETECTED,
                result.diagnosticMessage()
        );
        assertEquals(Messages.NO_ACTION_REQUIRED, result.recommendation());
    }

    @Test
    @DisplayName("Диагностика - null значения HTTP и времени не вызывают ошибок")
    void diagnose_nullHttpAndTime_returnsUnknownLayer() {
        DiagnosticResult result = diagnosticService.diagnose(
                true,
                true,
                true,
                true,
                null,
                null
        );

        assertEquals(FailureLayer.UNKNOWN, result.failureLayer());
        assertEquals(
                Messages.NO_CRITICAL_PROBLEM_DETECTED,
                result.diagnosticMessage()
        );
        assertEquals(Messages.NO_ACTION_REQUIRED, result.recommendation());
    }
}