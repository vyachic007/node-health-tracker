package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.check.checker.ServiceChecker;
import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.check.factory.ServiceCheckerFactory;
import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.CheckResultMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService;
import by.slava_borisov.nodehealthtracker.service.IncidentLifecycleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты CheckExecutionServiceImpl")
class CheckExecutionServiceImplTest {

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private CheckResultMapper checkResultMapper;

    @Mock
    private DiagnosticService diagnosticService;

    @Mock
    private ServiceCheckerFactory serviceCheckerFactory;

    @Mock
    private IncidentLifecycleService incidentLifecycleService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CheckExecutionServiceImpl checkExecutionService;

    private User ownerUser;
    private User differentUser;
    private NetworkService networkService;
    private CheckResult checkResult;
    private CheckResultResponse checkResultResponse;
    private ServiceChecker serviceChecker;

    @BeforeEach
    void setUp() {
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setUsername("owner");

        differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("other");

        NetworkNode networkNode = mock(NetworkNode.class);
        lenient().when(networkNode.getOwner()).thenReturn(ownerUser);

        networkService = new NetworkService();
        networkService.setId(10L);
        networkService.setName("Test Service");
        networkService.setCheckType(CheckType.HTTP);
        networkService.setNode(networkNode);
        networkService.setResponseTimeThresholdMs(1000);
        networkService.setConsecutiveDegradations(0);

        checkResult = new CheckResult();
        checkResult.setId(100L);
        checkResult.setService(networkService);
        checkResult.setStatus(ServiceStatus.UP);

        checkResultResponse = mock(CheckResultResponse.class);
        serviceChecker = mock(ServiceChecker.class);
    }

    @Test
    @DisplayName("Ручная проверка сервиса - успешно")
    void runCheck_success() {
        CheckProbeResult checkProbeResult = mock(CheckProbeResult.class);
        when(checkProbeResult.dnsAvailable()).thenReturn(true);
        when(checkProbeResult.pingAvailable()).thenReturn(true);
        when(checkProbeResult.tcpAvailable()).thenReturn(true);
        when(checkProbeResult.sslValid()).thenReturn(true);
        when(checkProbeResult.heartbeatAvailable()).thenReturn(true);
        when(checkProbeResult.httpStatusCode()).thenReturn(200);

        DiagnosticService.DiagnosticResult diagnosticResult = new DiagnosticService.DiagnosticResult(
                FailureLayer.UNKNOWN,
                "All good",
                "Keep running"
        );

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(serviceCheckerFactory.getChecker(CheckType.HTTP)).thenReturn(serviceChecker);
        when(serviceChecker.check(networkService)).thenReturn(checkProbeResult);
        when(diagnosticService.diagnose(
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyInt(),
                anyInt()
        )).thenReturn(diagnosticResult);
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(checkResultRepository.save(any(CheckResult.class))).thenReturn(checkResult);
        when(checkResultMapper.toCheckResultResponse(checkResult)).thenReturn(checkResultResponse);

        CheckResultResponse result = checkExecutionService.runCheck(10L);

        assertNotNull(result);
        verify(incidentLifecycleService, times(1)).processCheckResult(checkResult);
    }

    @Test
    @DisplayName("Ручная проверка сервиса - сервис не найден")
    void runCheck_serviceNotFound_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> checkExecutionService.runCheck(10L)
        );
    }

    @Test
    @DisplayName("Ручная проверка сервиса - отказ в доступе")
    void runCheck_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));

        assertThrows(
                AccessDeniedException.class,
                () -> checkExecutionService.runCheck(10L)
        );
    }

    @Test
    @DisplayName("Проверка всех включённых сервисов - успешно")
    void runEnabledChecks_success() {
        CheckProbeResult checkProbeResult = mock(CheckProbeResult.class);
        when(checkProbeResult.dnsAvailable()).thenReturn(true);
        when(checkProbeResult.pingAvailable()).thenReturn(true);
        when(checkProbeResult.tcpAvailable()).thenReturn(true);
        when(checkProbeResult.sslValid()).thenReturn(true);
        when(checkProbeResult.heartbeatAvailable()).thenReturn(true);
        when(checkProbeResult.httpStatusCode()).thenReturn(200);

        DiagnosticService.DiagnosticResult diagnosticResult = new DiagnosticService.DiagnosticResult(
                FailureLayer.UNKNOWN,
                "All good",
                "Keep running"
        );

        when(networkServiceRepository.findAllByIsEnabledTrue())
                .thenReturn(List.of(networkService));
        when(serviceCheckerFactory.getChecker(CheckType.HTTP)).thenReturn(serviceChecker);
        when(serviceChecker.check(networkService)).thenReturn(checkProbeResult);
        when(diagnosticService.diagnose(
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyInt(),
                anyInt()
        )).thenReturn(diagnosticResult);
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(checkResultRepository.save(any(CheckResult.class))).thenReturn(checkResult);
        when(checkResultMapper.toCheckResultResponse(checkResult)).thenReturn(checkResultResponse);

        List<CheckResultResponse> results = checkExecutionService.runEnabledChecks();

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(incidentLifecycleService, times(1)).processCheckResult(checkResult);
    }

    @Test
    @DisplayName("Проверка сервисов по расписанию - успешно")
    void runDueChecks_success() {
        CheckProbeResult checkProbeResult = mock(CheckProbeResult.class);
        when(checkProbeResult.dnsAvailable()).thenReturn(true);
        when(checkProbeResult.pingAvailable()).thenReturn(true);
        when(checkProbeResult.tcpAvailable()).thenReturn(true);
        when(checkProbeResult.sslValid()).thenReturn(true);
        when(checkProbeResult.heartbeatAvailable()).thenReturn(true);
        when(checkProbeResult.httpStatusCode()).thenReturn(200);

        DiagnosticService.DiagnosticResult diagnosticResult = new DiagnosticService.DiagnosticResult(
                FailureLayer.UNKNOWN,
                "All good",
                "Keep running"
        );

        when(networkServiceRepository.findServicesDueForCheck())
                .thenReturn(List.of(networkService));
        when(serviceCheckerFactory.getChecker(CheckType.HTTP)).thenReturn(serviceChecker);
        when(serviceChecker.check(networkService)).thenReturn(checkProbeResult);
        when(diagnosticService.diagnose(
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyInt(),
                anyInt()
        )).thenReturn(diagnosticResult);
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(checkResultRepository.save(any(CheckResult.class))).thenReturn(checkResult);
        when(checkResultMapper.toCheckResultResponse(checkResult)).thenReturn(checkResultResponse);

        List<CheckResultResponse> results = checkExecutionService.runDueChecks();

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Получение истории проверок сервиса - успешно")
    void getCheckHistory_success() {
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(checkResultRepository.findAllByServiceIdOrderByCheckedAtDesc(10L))
                .thenReturn(List.of(checkResult));
        when(checkResultMapper.toCheckResultResponse(checkResult)).thenReturn(checkResultResponse);

        List<CheckResultResponse> results = checkExecutionService.getCheckHistory(10L);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("Получение истории проверок - отказ в доступе")
    void getCheckHistory_accessDenied_throwsException() {
        when(currentUserService.getCurrentUser()).thenReturn(differentUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));

        assertThrows(
                AccessDeniedException.class,
                () -> checkExecutionService.getCheckHistory(10L)
        );
    }

    @Test
    @DisplayName("Определение статуса DOWN при ошибке HTTP")
    void executeCheck_httpError_setsStatusDown() {
        CheckProbeResult checkProbeResult = mock(CheckProbeResult.class);
        when(checkProbeResult.dnsAvailable()).thenReturn(true);
        when(checkProbeResult.pingAvailable()).thenReturn(true);
        when(checkProbeResult.tcpAvailable()).thenReturn(true);
        when(checkProbeResult.sslValid()).thenReturn(true);
        when(checkProbeResult.heartbeatAvailable()).thenReturn(true);
        when(checkProbeResult.httpStatusCode()).thenReturn(500);

        DiagnosticService.DiagnosticResult diagnosticResult = new DiagnosticService.DiagnosticResult(
                FailureLayer.APPLICATION,
                "HTTP error",
                "Check service"
        );

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(networkServiceRepository.findById(10L)).thenReturn(Optional.of(networkService));
        when(serviceCheckerFactory.getChecker(CheckType.HTTP)).thenReturn(serviceChecker);
        when(serviceChecker.check(networkService)).thenReturn(checkProbeResult);
        when(diagnosticService.diagnose(
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyInt(),
                anyInt()
        )).thenReturn(diagnosticResult);
        when(networkServiceRepository.save(any(NetworkService.class))).thenReturn(networkService);
        when(checkResultRepository.save(any(CheckResult.class))).thenReturn(checkResult);
        when(checkResultMapper.toCheckResultResponse(any(CheckResult.class))).thenReturn(checkResultResponse);

        checkExecutionService.runCheck(10L);

        ArgumentCaptor<CheckResult> captor = ArgumentCaptor.forClass(CheckResult.class);
        verify(checkResultRepository).save(captor.capture());
        assertEquals(ServiceStatus.DOWN, captor.getValue().getStatus());
    }
}