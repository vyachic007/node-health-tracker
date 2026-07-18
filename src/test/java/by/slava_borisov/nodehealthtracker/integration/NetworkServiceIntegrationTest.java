package by.slava_borisov.nodehealthtracker.integration;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.service.impl.NetworkServiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NetworkServiceIntegrationTest {

    @Autowired
    private NetworkServiceServiceImpl networkServiceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NetworkNodeRepository networkNodeRepository;

    @Autowired
    private NetworkServiceRepository networkServiceRepository;

    @MockBean
    private CurrentUserService currentUserService;

    private User testUser;
    private NetworkNode testNode;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        testUser = new User();
        testUser.setUsername("integration_test_user");
        testUser.setEmail("integration@test.com");
        testUser.setPasswordHash("hashed_password_for_test");
        testUser.setRole(RoleName.ROLE_USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);
        testUser.setCredentialsChangedAt(now);
        testUser.setPasswordChangedAt(now);
        testUser = userRepository.saveAndFlush(testUser);

        testNode = new NetworkNode();
        testNode.setName("Test Node for Integration");
        testNode.setHost("192.168.1.100");
        testNode.setOwner(testUser);
        testNode.setIsActive(true);
        testNode.setDescription("Test description");
        testNode.setCreatedAt(now);
        testNode.setUpdatedAt(now);
        testNode = networkNodeRepository.saveAndFlush(testNode);

        when(currentUserService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    @DisplayName("Создание сервиса через сервисный слой должно сохранять данные в БД")
    void createService_successfullySavesToDatabase() {
        ServiceCreateRequest request = new ServiceCreateRequest(
                testNode.getId(),
                CheckType.HTTP,
                "My HTTP Service",
                "https://example.com",
                443,
                "/health",
                60,
                1000,
                3,
                true,
                true,
                true
        );

        ServiceResponse response = networkServiceService.createService(request);

        assertNotNull(response.id());
        assertEquals("My HTTP Service", response.name());

        List<NetworkService> servicesInDb = networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(testNode.getId());
        assertFalse(servicesInDb.isEmpty());

        NetworkService savedService = servicesInDb.get(0);
        assertEquals("My HTTP Service", savedService.getName());
        assertEquals(CheckType.HTTP, savedService.getCheckType());
        assertTrue(savedService.getIsEnabled());
        assertEquals(testNode.getId(), savedService.getNode().getId());
    }

    @Test
    @DisplayName("Получение списка сервисов узла должно возвращать реальные данные из БД")
    void getServicesByNode_returnsRealDataFromDatabase() {
        LocalDateTime now = LocalDateTime.now();

        NetworkService service1 = new NetworkService();
        service1.setName("Service A");
        service1.setCheckType(CheckType.HTTP);
        service1.setTargetHost("https://a.com");
        service1.setPort(443);
        service1.setPath("/");
        service1.setIntervalSeconds(30);
        service1.setIsEnabled(true);
        service1.setNotifyEmail(true);
        service1.setNotifyTelegram(true);
        service1.setNotifyVk(true);
        service1.setResponseTimeThresholdMs(1000);
        service1.setDegradationThreshold(3);
        service1.setConsecutiveDegradations(0);
        service1.setFailureThreshold(3);
        service1.setRecoveryThreshold(2);
        service1.setConsecutiveFailures(0);
        service1.setConsecutiveSuccesses(0);
        service1.setCreatedAt(now);
        service1.setUpdatedAt(now);
        service1.setNode(testNode);
        networkServiceRepository.save(service1);

        NetworkService service2 = new NetworkService();
        service2.setName("Service B");
        service2.setCheckType(CheckType.PING);
        service2.setTargetHost("192.168.1.1");
        service2.setPort(null);
        service2.setPath(null);
        service2.setIntervalSeconds(60);
        service2.setIsEnabled(false);
        service2.setNotifyEmail(false);
        service2.setNotifyTelegram(false);
        service2.setNotifyVk(false);
        service2.setResponseTimeThresholdMs(500);
        service2.setDegradationThreshold(2);
        service2.setConsecutiveDegradations(0);
        service2.setFailureThreshold(2);
        service2.setRecoveryThreshold(1);
        service2.setConsecutiveFailures(0);
        service2.setConsecutiveSuccesses(0);
        service2.setCreatedAt(now);
        service2.setUpdatedAt(now);
        service2.setNode(testNode);
        networkServiceRepository.save(service2);

        List<NetworkService> services = networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(testNode.getId());

        assertEquals(2, services.size());
        assertTrue(services.stream().anyMatch(s -> s.getName().equals("Service A")));
        assertTrue(services.stream().anyMatch(s -> s.getName().equals("Service B")));
        assertTrue(services.stream().allMatch(s -> s.getNode().getId().equals(testNode.getId())));
    }

    @Test
    @DisplayName("Транзакционная изоляция: база данных должна быть чистой перед каждым тестом")
    void dataIsolation_check() {
        List<NetworkService> services = networkServiceRepository.findAllByNodeIdOrderByCreatedAtDesc(testNode.getId());
        assertTrue(services.isEmpty());
    }
}