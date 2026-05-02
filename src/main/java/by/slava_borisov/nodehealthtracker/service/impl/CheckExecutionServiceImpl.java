package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.mapper.CheckResultMapper;
import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.service.CheckExecutionService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService;
import by.slava_borisov.nodehealthtracker.service.DiagnosticService.DiagnosticResult;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckExecutionServiceImpl implements CheckExecutionService {

    private static final int DEFAULT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int HEARTBEAT_TIMEOUT_MULTIPLIER = 2;

    private final NetworkServiceRepository networkServiceRepository;
    private final CheckResultRepository checkResultRepository;
    private final CheckResultMapper checkResultMapper;
    private final DiagnosticService diagnosticService;

    @Override
    @Transactional
    public CheckResultResponse runCheck(Long serviceId) {
        NetworkService service = networkServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException(Messages.NETWORK_SERVICE_NOT_FOUND));

        CheckResult checkResult = executeCheck(service);
        CheckResult savedCheckResult = checkResultRepository.save(checkResult);

        return checkResultMapper.toCheckResultResponse(savedCheckResult);
    }

    @Override
    @Transactional
    public List<CheckResultResponse> runEnabledChecks() {
        return networkServiceRepository.findAllByIsEnabledTrue()
                .stream()
                .map(this::executeCheck)
                .map(checkResultRepository::save)
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

        boolean dnsAvailable = true;
        boolean pingAvailable = true;
        boolean tcpAvailable = true;
        boolean sslValid = true;
        Integer httpStatusCode = null;
        String errorMessage = null;

        try {
            switch (service.getCheckType()) {
                case PING -> pingAvailable = checkPing(service.getTargetHost());

                case TCP -> tcpAvailable = checkTcp(
                        service.getTargetHost(),
                        requirePort(service)
                );

                case HTTP -> httpStatusCode = checkHttp(
                        service,
                        false
                );

                case HTTPS -> httpStatusCode = checkHttp(
                        service,
                        true
                );

                case DNS -> dnsAvailable = checkDns(service.getTargetHost());

                case SSL -> {
                    sslValid = checkSsl(service);
                    httpStatusCode = checkHttp(service, true);
                }

                case HEARTBEAT -> tcpAvailable = checkHeartbeat(service);
            }
        } catch (Exception exception) {
            errorMessage = exception.getMessage();

            switch (service.getCheckType()) {
                case DNS -> dnsAvailable = false;
                case PING -> pingAvailable = false;
                case TCP -> tcpAvailable = false;
                case HTTP, HTTPS -> tcpAvailable = false;
                case SSL -> sslValid = false;
                case HEARTBEAT -> tcpAvailable = false;
            }
        }

        LocalDateTime finishedAt = LocalDateTime.now();
        int responseTimeMs = calculateResponseTimeMs(startedAt, finishedAt);

        DiagnosticResult diagnosticResult = diagnosticService.diagnose(
                dnsAvailable,
                pingAvailable,
                tcpAvailable,
                httpStatusCode,
                sslValid,
                responseTimeMs
        );

        ServiceStatus status = determineStatus(
                dnsAvailable,
                pingAvailable,
                tcpAvailable,
                sslValid,
                httpStatusCode
        );

        CheckResult checkResult = new CheckResult();
        checkResult.setService(service);
        checkResult.setStatus(status);
        checkResult.setFailureLayer(diagnosticResult.failureLayer());
        checkResult.setDiagnosticMessage(diagnosticResult.diagnosticMessage());
        checkResult.setRecommendation(diagnosticResult.recommendation());
        checkResult.setStartedAt(startedAt);
        checkResult.setFinishedAt(finishedAt);
        checkResult.setResponseTimeMs(responseTimeMs);
        checkResult.setHttpStatusCode(httpStatusCode);
        checkResult.setErrorMessage(errorMessage);
        checkResult.setCheckedAt(finishedAt);

        return checkResult;
    }

    private boolean checkDns(String host) throws Exception {
        InetAddress.getByName(host);
        return true;
    }

    private boolean checkPing(String host) throws Exception {
        InetAddress address = InetAddress.getByName(host);
        return address.isReachable(DEFAULT_TIMEOUT_MS);
    }

    private boolean checkTcp(String host, Integer port) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(
                    new java.net.InetSocketAddress(host, port),
                    DEFAULT_TIMEOUT_MS
            );
            return true;
        }
    }

    private Integer checkHttp(NetworkService service, boolean secure) throws Exception {
        String protocol = secure ? "https" : "http";
        Integer port = resolveHttpPort(service, secure);
        String path = resolvePath(service.getPath());

        URL url = new URL(protocol, service.getTargetHost(), port, path);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
        connection.setRequestMethod("GET");

        return connection.getResponseCode();
    }

    private boolean checkSsl(NetworkService service) throws Exception {
        checkHttp(service, true);
        return true;
    }

    private boolean checkHeartbeat(NetworkService service) {
        if (service.getLastHeartbeatAt() == null) {
            return false;
        }

        LocalDateTime heartbeatDeadline = LocalDateTime.now()
                .minusSeconds((long) service.getIntervalSeconds() * HEARTBEAT_TIMEOUT_MULTIPLIER);

        return service.getLastHeartbeatAt().isAfter(heartbeatDeadline);
    }

    private Integer requirePort(NetworkService service) {
        if (service.getPort() == null) {
            throw new IllegalArgumentException(Messages.TCP_PORT_REQUIRED);
        }

        return service.getPort();
    }

    private Integer resolveHttpPort(NetworkService service, boolean secure) {
        if (service.getPort() != null) {
            return service.getPort();
        }

        return secure ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
    }

    private String resolvePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        return path.startsWith("/") ? path : "/" + path;
    }

    private int calculateResponseTimeMs(LocalDateTime startedAt, LocalDateTime finishedAt) {
        return Math.toIntExact(Duration.between(startedAt, finishedAt).toMillis());
    }

    private ServiceStatus determineStatus(
            boolean dnsAvailable,
            boolean pingAvailable,
            boolean tcpAvailable,
            boolean sslValid,
            Integer httpStatusCode
    ) {
        if (!dnsAvailable || !pingAvailable || !tcpAvailable || !sslValid) {
            return ServiceStatus.DOWN;
        }

        if (httpStatusCode != null && httpStatusCode >= 400) {
            return ServiceStatus.DOWN;
        }

        return ServiceStatus.UP;
    }
}