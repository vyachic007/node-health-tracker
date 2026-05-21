package by.slava_borisov.nodehealthtracker.bootstrap;

import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
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

    private final UserRepository userRepository;
    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceRepository networkServiceRepository;
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

        createServiceIfNotExists(
                productionNode,
                "Demo Website HTTP",
                CheckType.HTTP,
                "example.com",
                80,
                "/",
                null
        );

        createServiceIfNotExists(
                productionNode,
                "Demo API HTTPS",
                CheckType.HTTPS,
                "example.com",
                443,
                "/api/health",
                null
        );

        createServiceIfNotExists(
                productionNode,
                "Demo TCP Database Port",
                CheckType.TCP,
                "127.0.0.1",
                5432,
                null,
                null
        );

        createServiceIfNotExists(
                infrastructureNode,
                "Demo DNS Resolver",
                CheckType.DNS,
                "google.com",
                null,
                null,
                null
        );

        createServiceIfNotExists(
                infrastructureNode,
                "Demo SSL Certificate",
                CheckType.SSL,
                "example.com",
                443,
                null,
                null
        );

        createServiceIfNotExists(
                infrastructureNode,
                "Demo Heartbeat Agent",
                CheckType.HEARTBEAT,
                "agent-demo.local",
                null,
                null,
                "demo-heartbeat-token"
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
        service.setIntervalSeconds(60);
        service.setIsEnabled(true);
        service.setFailureThreshold(2);
        service.setRecoveryThreshold(2);
        service.setConsecutiveFailures(0);
        service.setConsecutiveSuccesses(0);
        service.setCreatedAt(now);
        service.setUpdatedAt(now);

        return networkServiceRepository.save(service);
    }
}