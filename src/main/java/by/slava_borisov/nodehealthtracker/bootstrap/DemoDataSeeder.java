package by.slava_borisov.nodehealthtracker.bootstrap;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import by.slava_borisov.nodehealthtracker.model.entity.IncidentTimelineEvent;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.FailureLayer;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentSeverity;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentTimelineEventType;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentTimelineEventRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_ADMIN_USERNAME = "demo_admin";
    private static final String DEMO_USER_USERNAME = "demo_user";

    private static final String DEMO_ADMIN_EMAIL = "demo_admin@example.com";
    private static final String DEMO_USER_EMAIL = "demo_user@example.com";

    private static final String DEMO_PASSWORD = "12345678";

    private static final Integer DEMO_SERVICE_INTERVAL_SECONDS = 3600;

    private final UserRepository userRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final CheckResultRepository checkResultRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentTimelineEventRepository incidentTimelineEventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createUserIfNotExists(
                DEMO_ADMIN_USERNAME,
                DEMO_ADMIN_EMAIL,
                RoleName.ROLE_ADMIN
        );

        User demoUser = createUserIfNotExists(
                DEMO_USER_USERNAME,
                DEMO_USER_EMAIL,
                RoleName.ROLE_USER
        );

        NetworkNode productionNode = createNodeIfNotExists(
                demoUser,
                "Demo Production Node",
                "demo-production.local",
                "Демонстрационный production-узел для проверки сервисов."
        );

        NetworkNode infrastructureNode = createNodeIfNotExists(
                demoUser,
                "Demo Infrastructure Node",
                "demo-infrastructure.local",
                "Демонстрационный инфраструктурный узел."
        );

        NetworkService websiteService = createServiceIfNotExists(
                productionNode,
                "Demo Website HTTP",
                CheckType.HTTP,
                "example.com",
                80,
                "/",
                null
        );

        NetworkService apiService = createServiceIfNotExists(
                productionNode,
                "Demo API HTTPS",
                CheckType.HTTPS,
                "example.com",
                443,
                "/api/health",
                null
        );

        NetworkService databaseService = createServiceIfNotExists(
                productionNode,
                "Demo TCP Database Port",
                CheckType.TCP,
                "127.0.0.1",
                5432,
                null,
                null
        );

        NetworkService dnsService = createServiceIfNotExists(
                infrastructureNode,
                "Demo DNS Resolver",
                CheckType.DNS,
                "google.com",
                null,
                null,
                null
        );

        NetworkService sslService = createServiceIfNotExists(
                infrastructureNode,
                "Demo SSL Certificate",
                CheckType.SSL,
                "example.com",
                443,
                null,
                null
        );

        NetworkService heartbeatService = createServiceIfNotExists(
                infrastructureNode,
                "Demo Heartbeat Agent",
                CheckType.HEARTBEAT,
                "agent-demo.local",
                null,
                null,
                "demo-heartbeat-token"
        );

        createDemoMonitoringDataIfNotExists(
                websiteService,
                apiService,
                databaseService,
                dnsService,
                sslService,
                heartbeatService
        );
    }

    private User createUserIfNotExists(
            String username,
            String email,
            RoleName role
    ) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> createUser(username, email, role));
    }

    private User createUser(
            String username,
            String email,
            RoleName role
    ) {
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setPasswordChangedAt(now);
        user.setCredentialsChangedAt(now);

        return userRepository.save(user);
    }

    private NetworkNode createNodeIfNotExists(
            User owner,
            String name,
            String host,
            String description
    ) {
        return networkNodeRepository.findAllByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .filter(node -> node.getName().equals(name))
                .findFirst()
                .orElseGet(() -> createNode(owner, name, host, description));
    }

    private NetworkNode createNode(
            User owner,
            String name,
            String host,
            String description
    ) {
        LocalDateTime now = LocalDateTime.now();

        NetworkNode node = new NetworkNode();
        node.setOwner(owner);
        node.setName(name);
        node.setHost(host);
        node.setDescription(description);
        node.setIsActive(true);
        node.setCreatedAt(now);
        node.setUpdatedAt(now);

        return networkNodeRepository.save(node);
    }

    private NetworkService createServiceIfNotExists(
            NetworkNode node,
            String name,
            CheckType checkType,
            String targetHost,
            Integer port,
            String path,
            String heartbeatToken
    ) {
        return networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(node.getId())
                .stream()
                .filter(service -> service.getName().equals(name))
                .findFirst()
                .map(this::updateDemoServiceInterval)
                .orElseGet(() -> createService(
                        node,
                        name,
                        checkType,
                        targetHost,
                        port,
                        path,
                        heartbeatToken
                ));
    }

    private NetworkService updateDemoServiceInterval(NetworkService service) {
        if (!DEMO_SERVICE_INTERVAL_SECONDS.equals(service.getIntervalSeconds())) {
            service.setIntervalSeconds(DEMO_SERVICE_INTERVAL_SECONDS);
            service.setUpdatedAt(LocalDateTime.now());

            return networkServiceRepository.save(service);
        }

        return service;
    }

    private NetworkService createService(
            NetworkNode node,
            String name,
            CheckType checkType,
            String targetHost,
            Integer port,
            String path,
            String heartbeatToken
    ) {
        LocalDateTime now = LocalDateTime.now();

        NetworkService service = new NetworkService();
        service.setNode(node);
        service.setName(name);
        service.setCheckType(checkType);
        service.setTargetHost(targetHost);
        service.setPort(port);
        service.setPath(path);
        service.setHeartbeatToken(heartbeatToken);
        service.setLastHeartbeatAt(null);
        service.setLastCheckedAt(null);
        service.setIntervalSeconds(DEMO_SERVICE_INTERVAL_SECONDS);
        service.setIsEnabled(true);
        service.setFailureThreshold(2);
        service.setRecoveryThreshold(2);
        service.setConsecutiveFailures(0);
        service.setConsecutiveSuccesses(0);
        service.setCreatedAt(now);
        service.setUpdatedAt(now);

        return networkServiceRepository.save(service);
    }

    private void createDemoMonitoringDataIfNotExists(
            NetworkService websiteService,
            NetworkService apiService,
            NetworkService databaseService,
            NetworkService dnsService,
            NetworkService sslService,
            NetworkService heartbeatService
    ) {
        if (checkResultRepository.countByServiceIdAndCheckedAtAfter(
                websiteService.getId(),
                LocalDateTime.now().minusYears(10)
        ) > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        CheckResult websiteUpResult = createCheckResult(
                websiteService,
                ServiceStatus.UP,
                FailureLayer.UNKNOWN,
                "Критическая проблема не обнаружена.",
                "На данный момент дополнительных действий не требуется.",
                now.minusMinutes(50),
                142,
                200,
                null
        );

        CheckResult apiUpResult = createCheckResult(
                apiService,
                ServiceStatus.UP,
                FailureLayer.UNKNOWN,
                "Критическая проблема не обнаружена.",
                "На данный момент дополнительных действий не требуется.",
                now.minusMinutes(45),
                238,
                200,
                null
        );

        CheckResult databaseDownResult = createCheckResult(
                databaseService,
                ServiceStatus.DOWN,
                FailureLayer.PORT,
                "Не удалось установить TCP-соединение с целевым портом.",
                "Проверьте, запущен ли сервис, открыт ли порт и не блокирует ли подключение firewall.",
                now.minusMinutes(40),
                null,
                null,
                "Connection refused"
        );

        CheckResult databaseRecoveryResult = createCheckResult(
                databaseService,
                ServiceStatus.UP,
                FailureLayer.UNKNOWN,
                "Критическая проблема не обнаружена.",
                "На данный момент дополнительных действий не требуется.",
                now.minusMinutes(25),
                18,
                null,
                null
        );

        CheckResult dnsUpResult = createCheckResult(
                dnsService,
                ServiceStatus.UP,
                FailureLayer.UNKNOWN,
                "DNS-резолвинг выполняется успешно.",
                "DNS-проблем не обнаружено.",
                now.minusMinutes(35),
                64,
                null,
                null
        );

        CheckResult sslDownResult = createCheckResult(
                sslService,
                ServiceStatus.DOWN,
                FailureLayer.SSL,
                "Обнаружена проблема SSL/TLS-сертификата.",
                "Проверьте срок действия сертификата, домен и цепочку сертификатов.",
                now.minusMinutes(20),
                null,
                null,
                "SSL certificate validation failed"
        );

        CheckResult heartbeatDownResult = createCheckResult(
                heartbeatService,
                ServiceStatus.DOWN,
                FailureLayer.HEARTBEAT,
                "Heartbeat от агента давно не поступал.",
                "Проверьте, запущен ли агент и может ли он подключиться к серверу мониторинга.",
                now.minusMinutes(15),
                null,
                null,
                "Heartbeat timeout"
        );

        createResolvedIncidentIfNotExists(
                databaseService,
                databaseDownResult,
                databaseRecoveryResult,
                now.minusMinutes(40),
                now.minusMinutes(25)
        );

        createOpenIncidentIfNotExists(
                sslService,
                sslDownResult,
                IncidentSeverity.HIGH,
                now.minusMinutes(20)
        );

        createOpenIncidentIfNotExists(
                heartbeatService,
                heartbeatDownResult,
                IncidentSeverity.MEDIUM,
                now.minusMinutes(15)
        );

        updateLastCheckedAt(websiteService, websiteUpResult.getCheckedAt());
        updateLastCheckedAt(apiService, apiUpResult.getCheckedAt());
        updateLastCheckedAt(databaseService, databaseRecoveryResult.getCheckedAt());
        updateLastCheckedAt(dnsService, dnsUpResult.getCheckedAt());
        updateLastCheckedAt(sslService, sslDownResult.getCheckedAt());
        updateLastCheckedAt(heartbeatService, heartbeatDownResult.getCheckedAt());
    }

    private CheckResult createCheckResult(
            NetworkService service,
            ServiceStatus status,
            FailureLayer failureLayer,
            String diagnosticMessage,
            String recommendation,
            LocalDateTime checkedAt,
            Integer responseTimeMs,
            Integer httpStatusCode,
            String errorMessage
    ) {
        CheckResult checkResult = new CheckResult();
        checkResult.setService(service);
        checkResult.setStatus(status);
        checkResult.setFailureLayer(failureLayer);
        checkResult.setDiagnosticMessage(diagnosticMessage);
        checkResult.setRecommendation(recommendation);
        checkResult.setStartedAt(checkedAt.minusSeconds(1));
        checkResult.setFinishedAt(checkedAt);
        checkResult.setResponseTimeMs(responseTimeMs);
        checkResult.setHttpStatusCode(httpStatusCode);
        checkResult.setErrorMessage(errorMessage);
        checkResult.setCheckedAt(checkedAt);

        return checkResultRepository.save(checkResult);
    }

    private void createResolvedIncidentIfNotExists(
            NetworkService service,
            CheckResult openedByCheckResult,
            CheckResult closedByCheckResult,
            LocalDateTime openedAt,
            LocalDateTime closedAt
    ) {
        if (incidentRepository.countByServiceIdAndStatus(service.getId(), IncidentStatus.RESOLVED) > 0) {
            return;
        }

        Incident incident = new Incident();
        incident.setService(service);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setSeverity(IncidentSeverity.HIGH);
        incident.setOpenedAt(openedAt);
        incident.setClosedAt(closedAt);
        incident.setReason(openedByCheckResult.getDiagnosticMessage());
        incident.setOpenedByCheckResult(openedByCheckResult);
        incident.setClosedByCheckResult(closedByCheckResult);

        Incident savedIncident = incidentRepository.save(incident);

        createTimelineEvent(
                savedIncident,
                openedByCheckResult,
                IncidentTimelineEventType.CHECK_FAILED,
                "Проверка завершилась ошибкой: " + openedByCheckResult.getDiagnosticMessage(),
                openedAt
        );

        createTimelineEvent(
                savedIncident,
                openedByCheckResult,
                IncidentTimelineEventType.SEVERITY_ASSIGNED,
                "Назначена критичность инцидента: " + savedIncident.getSeverity(),
                openedAt.plusSeconds(1)
        );

        createTimelineEvent(
                savedIncident,
                openedByCheckResult,
                IncidentTimelineEventType.INCIDENT_OPENED,
                "Открыт инцидент по сервису: " + service.getName(),
                openedAt.plusSeconds(2)
        );

        createTimelineEvent(
                savedIncident,
                closedByCheckResult,
                IncidentTimelineEventType.CHECK_RECOVERED,
                "Проверка снова завершилась успешно.",
                closedAt
        );

        createTimelineEvent(
                savedIncident,
                closedByCheckResult,
                IncidentTimelineEventType.INCIDENT_RESOLVED,
                "Инцидент закрыт, сервис восстановлен.",
                closedAt.plusSeconds(1)
        );
    }

    private void createOpenIncidentIfNotExists(
            NetworkService service,
            CheckResult openedByCheckResult,
            IncidentSeverity severity,
            LocalDateTime openedAt
    ) {
        if (incidentRepository.findByServiceIdAndStatus(service.getId(), IncidentStatus.OPEN).isPresent()) {
            return;
        }

        Incident incident = new Incident();
        incident.setService(service);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setSeverity(severity);
        incident.setOpenedAt(openedAt);
        incident.setClosedAt(null);
        incident.setReason(openedByCheckResult.getDiagnosticMessage());
        incident.setOpenedByCheckResult(openedByCheckResult);
        incident.setClosedByCheckResult(null);

        Incident savedIncident = incidentRepository.save(incident);

        createTimelineEvent(
                savedIncident,
                openedByCheckResult,
                IncidentTimelineEventType.CHECK_FAILED,
                "Проверка завершилась ошибкой: " + openedByCheckResult.getDiagnosticMessage(),
                openedAt
        );

        createTimelineEvent(
                savedIncident,
                openedByCheckResult,
                IncidentTimelineEventType.SEVERITY_ASSIGNED,
                "Назначена критичность инцидента: " + savedIncident.getSeverity(),
                openedAt.plusSeconds(1)
        );

        createTimelineEvent(
                savedIncident,
                openedByCheckResult,
                IncidentTimelineEventType.INCIDENT_OPENED,
                "Открыт инцидент по сервису: " + service.getName(),
                openedAt.plusSeconds(2)
        );
    }

    private void createTimelineEvent(
            Incident incident,
            CheckResult checkResult,
            IncidentTimelineEventType eventType,
            String message,
            LocalDateTime createdAt
    ) {
        IncidentTimelineEvent event = new IncidentTimelineEvent();
        event.setIncident(incident);
        event.setCheckResult(checkResult);
        event.setEventType(eventType);
        event.setMessage(message);
        event.setCreatedAt(createdAt);

        incidentTimelineEventRepository.save(event);
    }

    private void updateLastCheckedAt(
            NetworkService service,
            LocalDateTime lastCheckedAt
    ) {
        service.setLastCheckedAt(lastCheckedAt);
        service.setUpdatedAt(LocalDateTime.now());

        networkServiceRepository.save(service);
    }
}