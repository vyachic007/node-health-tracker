package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.check.checker.ServiceChecker;
import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.check.factory.ServiceCheckerFactory;
import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.CheckResultMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CheckExecutionService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService.DiagnosticResult;
import by.slava_borisov.nodehealthtracker.service.IncidentLifecycleService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckExecutionServiceImpl implements CheckExecutionService {

    private final NetworkServiceRepository networkServiceRepository;
    private final CheckResultRepository checkResultRepository;
    private final CheckResultMapper checkResultMapper;
    private final DiagnosticService diagnosticService;
    private final ServiceCheckerFactory serviceCheckerFactory;
    private final IncidentLifecycleService incidentLifecycleService;

    @Override
    @Transactional
    public CheckResultResponse runCheck(Long serviceId) {
        NetworkService service = networkServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_SERVICE_NOT_FOUND));

        CheckResult checkResult = executeCheck(service);
        CheckResult savedCheckResult = saveAndProcessIncident(checkResult);

        return checkResultMapper.toCheckResultResponse(savedCheckResult);
    }

    @Override
    @Transactional
    public List<CheckResultResponse> runEnabledChecks() {
        return networkServiceRepository.findAllByIsEnabledTrue()
                .stream()
                .map(this::executeCheck)
                .map(this::saveAndProcessIncident)
                .map(checkResultMapper::toCheckResultResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<CheckResultResponse> runDueChecks() {
        return networkServiceRepository.findServicesDueForCheck()
                .stream()
                .map(this::executeCheck)
                .map(this::saveAndProcessIncident)
                .map(checkResultMapper::toCheckResultResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckResultResponse> getCheckHistory(Long serviceId) {
        return checkResultRepository.findAllByServiceIdOrderByCheckedAtDesc(serviceId)
                .stream()
                .map(checkResultMapper::toCheckResultResponse)
                .toList();
    }

    private CheckResult executeCheck(NetworkService service) {
        LocalDateTime startedAt = LocalDateTime.now();

        ServiceChecker serviceChecker = serviceCheckerFactory.getChecker(service.getCheckType());
        CheckProbeResult probeResult = serviceChecker.check(service);

        LocalDateTime finishedAt = LocalDateTime.now();
        int responseTimeMs = calculateResponseTimeMs(startedAt, finishedAt);

        service.setLastCheckedAt(finishedAt);

        DiagnosticResult diagnosticResult = diagnosticService.diagnose(
                probeResult.dnsAvailable(),
                probeResult.pingAvailable(),
                probeResult.tcpAvailable(),
                probeResult.sslValid(),
                probeResult.heartbeatAvailable(),
                probeResult.httpStatusCode(),
                responseTimeMs
        );

        ServiceStatus status = determineStatus(probeResult);

        CheckResult checkResult = new CheckResult();
        checkResult.setService(service);
        checkResult.setStatus(status);
        checkResult.setFailureLayer(diagnosticResult.failureLayer());
        checkResult.setDiagnosticMessage(diagnosticResult.diagnosticMessage());
        checkResult.setRecommendation(diagnosticResult.recommendation());
        checkResult.setStartedAt(startedAt);
        checkResult.setFinishedAt(finishedAt);
        checkResult.setResponseTimeMs(responseTimeMs);
        checkResult.setHttpStatusCode(probeResult.httpStatusCode());
        checkResult.setErrorMessage(probeResult.errorMessage());
        checkResult.setCheckedAt(finishedAt);

        return checkResult;
    }

    private int calculateResponseTimeMs(LocalDateTime startedAt, LocalDateTime finishedAt) {
        return Math.toIntExact(Duration.between(startedAt, finishedAt).toMillis());
    }

    private ServiceStatus determineStatus(CheckProbeResult probeResult) {
        if (!probeResult.dnsAvailable()
                || !probeResult.pingAvailable()
                || !probeResult.tcpAvailable()
                || !probeResult.sslValid()
                || !probeResult.heartbeatAvailable()) {
            return ServiceStatus.DOWN;
        }

        if (probeResult.httpStatusCode() != null && httpStatusCodeIsError(probeResult.httpStatusCode())) {
            return ServiceStatus.DOWN;
        }

        return ServiceStatus.UP;
    }

    private boolean httpStatusCodeIsError(Integer httpStatusCode) {
        return httpStatusCode >= 400;
    }

    private CheckResult saveAndProcessIncident(CheckResult checkResult) {
        CheckResult savedCheckResult = checkResultRepository.save(checkResult);
        incidentLifecycleService.processCheckResult(savedCheckResult);

        return savedCheckResult;
    }
}