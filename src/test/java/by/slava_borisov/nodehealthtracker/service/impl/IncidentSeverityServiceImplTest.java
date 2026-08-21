package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.service.IncidentSeverityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты IncidentSeverityServiceImpl")
class IncidentSeverityServiceImplTest {

    private IncidentSeverityService incidentSeverityService;

    @Mock
    private CheckResult checkResult;

    @BeforeEach
    void setUp() {
        incidentSeverityService = new IncidentSeverityServiceImpl();
    }

    @Test
    @DisplayName("Определение серьезности - слой DNS (CRITICAL)")
    void determineSeverity_dns_returnsCritical() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.DNS);

        assertEquals(
                IncidentSeverity.CRITICAL,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой NETWORK (CRITICAL)")
    void determineSeverity_network_returnsCritical() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.NETWORK);

        assertEquals(
                IncidentSeverity.CRITICAL,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой PORT (HIGH)")
    void determineSeverity_port_returnsHigh() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.PORT);

        assertEquals(
                IncidentSeverity.HIGH,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой SSL (HIGH)")
    void determineSeverity_ssl_returnsHigh() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.SSL);

        assertEquals(
                IncidentSeverity.HIGH,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой APPLICATION (MEDIUM)")
    void determineSeverity_application_returnsMedium() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.APPLICATION);

        assertEquals(
                IncidentSeverity.MEDIUM,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой PERFORMANCE (MEDIUM)")
    void determineSeverity_performance_returnsMedium() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.PERFORMANCE);

        assertEquals(
                IncidentSeverity.MEDIUM,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой UNKNOWN (LOW)")
    void determineSeverity_unknown_returnsLow() {
        when(checkResult.getFailureLayer()).thenReturn(FailureLayer.UNKNOWN);

        assertEquals(
                IncidentSeverity.LOW,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }

    @Test
    @DisplayName("Определение серьезности - слой null (MEDIUM)")
    void determineSeverity_nullLayer_returnsMedium() {
        when(checkResult.getFailureLayer()).thenReturn(null);

        assertEquals(
                IncidentSeverity.MEDIUM,
                incidentSeverityService.determineSeverity(checkResult)
        );
    }
}