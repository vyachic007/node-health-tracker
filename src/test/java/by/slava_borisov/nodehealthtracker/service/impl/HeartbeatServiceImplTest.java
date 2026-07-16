package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.heartbeat.HeartbeatResponse;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты HeartbeatServiceImpl")
class HeartbeatServiceImplTest {

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @InjectMocks
    private HeartbeatServiceImpl heartbeatService;

    private NetworkService networkService;

    @BeforeEach
    void setUp() {
        networkService = new NetworkService();
        networkService.setId(1L);
        networkService.setName("Test Service");
        networkService.setHeartbeatToken("valid-token");
    }

    @Test
    @DisplayName("Принять heartbeat - токен null, выбрасывает исключение")
    void acceptHeartbeat_nullToken_throwsException() {
        assertThrows(
                InvalidOperationException.class,
                () -> heartbeatService.acceptHeartbeat(null)
        );
    }

    @Test
    @DisplayName("Принять heartbeat - токен пустой, выбрасывает исключение")
    void acceptHeartbeat_blankToken_throwsException() {
        assertThrows(
                InvalidOperationException.class,
                () -> heartbeatService.acceptHeartbeat("   ")
        );
    }

    @Test
    @DisplayName("Принять heartbeat - токен не найден, выбрасывает исключение")
    void acceptHeartbeat_tokenNotFound_throwsException() {
        String invalidToken = "invalid-token";
        when(networkServiceRepository.findByHeartbeatToken(invalidToken))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> heartbeatService.acceptHeartbeat(invalidToken)
        );
    }

    @Test
    @DisplayName("Принять heartbeat - успешно")
    void acceptHeartbeat_success() {
        String validToken = "valid-token";
        when(networkServiceRepository.findByHeartbeatToken(validToken))
                .thenReturn(Optional.of(networkService));
        when(networkServiceRepository.save(any(NetworkService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HeartbeatResponse response = heartbeatService.acceptHeartbeat(validToken);

        assertNotNull(response);
        assertEquals(1L, response.serviceId());
        assertEquals("Test Service", response.serviceName());
        assertEquals(Messages.HEARTBEAT_ACCEPTED, response.message());
        assertNotNull(response.lastHeartbeatAt());

        verify(networkServiceRepository, times(1)).save(any(NetworkService.class));
    }
}