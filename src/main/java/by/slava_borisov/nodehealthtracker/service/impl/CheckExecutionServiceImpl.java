package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.check.checker.ServiceChecker;
import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.check.factory.ServiceCheckerFactory;
import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.mapper.CheckResultMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CheckExecutionService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService.DiagnosticResult;
import by.slava_borisov.nodehealthtracker.service.IncidentLifecycleService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckExecutionServiceImpl implements CheckExecutionService {

    private final NetworkServiceRepository networkServiceRepository;
    private final CheckResultRepository checkResultRepository;
    private final CheckResultMapper checkResultMapper;
    private final DiagnosticService diagnosticService;
    private final ServiceCheckerFactory serviceCheckerFactory;
    private final IncidentLifecycleService incidentLifecycleService;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public CheckResultResponse runCheck(Long serviceId) {
        User currentUser = currentUserService.getCurrentUser();
        NetworkService service = findServiceById(serviceId);

        validateServiceAccess(service, currentUser);

        log.info(
                "Запущена ручная проверка сервиса: serviceId={}, serviceName={}, username={}",
                service.getId(),
                service.getName(),
                currentUser.getUsername()
        );

        CheckResult checkResult = executeCheck(service);
        CheckResult savedCheckResult = saveAndProcessIncident(checkResult);

        log.info(
                "Ручная проверка сервиса завершена: serviceId={}, checkResultId={}, status={}, failureLayer={}, responseTimeMs={}",
                service.getId(),
                savedCheckResult.getId(),
                savedCheckResult.getStatus(),
                savedCheckResult.getFailureLayer(),
                savedCheckResult.getResponseTimeMs()
        );

        return checkResultMapper.toCheckResultResponse(savedCheckResult);
    }

    @Override
    @Transactional
    public List<CheckResultResponse> runEnabledChecks() {
        List<NetworkService> enabledServices = networkServiceRepository.findAllByIsEnabledTrue();

        log.info(
                "Запущена проверка всех включённых сервисов: servicesCount={}",
                enabledServices.size()
        );

        List<CheckResultResponse> responses = enabledServices
                .stream()
                .map(this::executeCheck)
                .map(this::saveAndProcessIncident)
                .map(checkResultMapper::toCheckResultResponse)
                .toList();

        log.info(
                "Проверка всех включённых сервисов завершена: checkedServices={}",
                responses.size()
        );

        return responses;
    }

    @Override
    @Transactional
    public List<CheckResultResponse> runDueChecks() {
        List<NetworkService> dueServices = networkServiceRepository.findServicesDueForCheck();

        log.info(
                "Запущена проверка сервисов по расписанию: dueServicesCount={}",
                dueServices.size()
        );

        List<CheckResultResponse> responses = dueServices
                .stream()
                .map(this::executeCheck)
                .map(this::saveAndProcessIncident)
                .map(checkResultMapper::toCheckResultResponse)
                .toList();

        log.info(
                "Проверка сервисов по расписанию завершена: checkedServices={}",
                responses.size()
        );

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckResultResponse> getCheckHistory(Long serviceId) {
        User currentUser = currentUserService.getCurrentUser();
        NetworkService service = findServiceById(serviceId);

        validateServiceAccess(service, currentUser);

        log.info(
                "Запрошена история проверок сервиса: serviceId={}, serviceName={}, username={}",
                service.getId(),
                service.getName(),
                currentUser.getUsername()
        );

        return checkResultRepository.findAllByServiceIdOrderByCheckedAtDesc(serviceId)
                .stream()
                .map(checkResultMapper::toCheckResultResponse)
                .toList();
    }

    private CheckResult executeCheck(NetworkService service) {
        LocalDateTime startedAt = LocalDateTime.now();

        log.info(
                "Начата проверка сервиса: serviceId={}, serviceName={}, checkType={}, targetHost={}, port={}, path={}",
                service.getId(),
                service.getName(),
                service.getCheckType(),
                service.getTargetHost(),
                service.getPort(),
                service.getPath()
        );

        CheckProbeResult probeResult;

        try {
            ServiceChecker serviceChecker = serviceCheckerFactory.getChecker(service.getCheckType());
            probeResult = serviceChecker.check(service);
        } catch (RuntimeException exception) {
            log.error(
                    "Ошибка при выполнении проверки сервиса: serviceId={}, serviceName={}, checkType={}",
                    service.getId(),
                    service.getName(),
                    service.getCheckType(),
                    exception
            );

            throw exception;
        }

        LocalDateTime finishedAt = LocalDateTime.now();
        int responseTimeMs = calculateResponseTimeMs(startedAt, finishedAt);

        service.setLastCheckedAt(finishedAt);

        ServiceStatus status = determineStatus(probeResult);

        updateDegradationState(service, status, responseTimeMs);

        NetworkService savedService = networkServiceRepository.save(service);

        DiagnosticResult diagnosticResult = diagnosticService.diagnose(
                probeResult.dnsAvailable(),
                probeResult.pingAvailable(),
                probeResult.tcpAvailable(),
                probeResult.sslValid(),
                probeResult.heartbeatAvailable(),
                probeResult.httpStatusCode(),
                responseTimeMs
        );

        CheckResult checkResult = new CheckResult();
        checkResult.setService(savedService);
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

        log.info(
                "Проверка сервиса выполнена: serviceId={}, serviceName={}, status={}, failureLayer={}, responseTimeMs={}, httpStatusCode={}, responseTimeThresholdMs={}, consecutiveDegradations={}",
                savedService.getId(),
                savedService.getName(),
                checkResult.getStatus(),
                checkResult.getFailureLayer(),
                checkResult.getResponseTimeMs(),
                checkResult.getHttpStatusCode(),
                savedService.getResponseTimeThresholdMs(),
                savedService.getConsecutiveDegradations()
        );

        return checkResult;
    }

    private void updateDegradationState(
            NetworkService service,
            ServiceStatus status,
            int responseTimeMs
    ) {
        Integer responseTimeThresholdMs = service.getResponseTimeThresholdMs();
        Integer currentConsecutiveDegradations = service.getConsecutiveDegradations();

        if (responseTimeThresholdMs == null) {
            responseTimeThresholdMs = 1000;
            service.setResponseTimeThresholdMs(responseTimeThresholdMs);
        }

        if (currentConsecutiveDegradations == null) {
            currentConsecutiveDegradations = 0;
        }

        boolean serviceIsAvailableButSlow = status == ServiceStatus.UP
                && responseTimeMs > responseTimeThresholdMs;

        if (serviceIsAvailableButSlow) {
            int updatedConsecutiveDegradations = currentConsecutiveDegradations + 1;
            service.setConsecutiveDegradations(updatedConsecutiveDegradations);

            log.warn(
                    "Обнаружена деградация сервиса: serviceId={}, serviceName={}, responseTimeMs={}, responseTimeThresholdMs={}, consecutiveDegradations={}",
                    service.getId(),
                    service.getName(),
                    responseTimeMs,
                    responseTimeThresholdMs,
                    updatedConsecutiveDegradations
            );

            return;
        }

        if (currentConsecutiveDegradations > 0) {
            log.info(
                    "Счётчик деградаций сброшен: serviceId={}, serviceName={}, previousConsecutiveDegradations={}, status={}, responseTimeMs={}, responseTimeThresholdMs={}",
                    service.getId(),
                    service.getName(),
                    currentConsecutiveDegradations,
                    status,
                    responseTimeMs,
                    responseTimeThresholdMs
            );
        }

        service.setConsecutiveDegradations(0);
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

        log.info(
                "Результат проверки сохранён: checkResultId={}, serviceId={}, status={}",
                savedCheckResult.getId(),
                savedCheckResult.getService().getId(),
                savedCheckResult.getStatus()
        );

        incidentLifecycleService.processCheckResult(savedCheckResult);

        return savedCheckResult;
    }

    private NetworkService findServiceById(Long serviceId) {
        return networkServiceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.NETWORK_SERVICE_NOT_FOUND));
    }

    private void validateServiceAccess(NetworkService service, User currentUser) {
        if (!isServiceOwner(service, currentUser)) {
            log.warn(
                    "Отказано в доступе к сервису: serviceId={}, username={}",
                    service.getId(),
                    currentUser.getUsername()
            );

            throw new AccessDeniedException(Messages.NETWORK_SERVICE_ACCESS_DENIED);
        }
    }

    private boolean isServiceOwner(NetworkService service, User currentUser) {
        return service.getNode()
                .getOwner()
                .getId()
                .equals(currentUser.getId());
    }
}