package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты JwtServiceImpl")
class JwtServiceImplTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "this-is-a-very-long-secret-key-for-jwt-testing-1234567890"
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);

        testUser = new User();
        testUser.setId(42L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(RoleName.ROLE_USER);
    }

    @Test
    @DisplayName("Генерация и извлечение данных из токена - успешно")
    void generateAndExtractToken_success() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(42L, jwtService.extractUserId(token));

        LocalDateTime issuedAt = jwtService.extractIssuedAt(token);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    @Test
    @DisplayName("Валидация токена - успешно (валидный токен и правильный пользователь)")
    void isTokenValid_success() {
        String token = jwtService.generateToken(testUser);

        boolean isValid = jwtService.isTokenValid(token, testUser);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Валидация токена - неверный пользователь")
    void isTokenValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(testUser);

        User differentUser = new User();
        differentUser.setId(99L);
        differentUser.setUsername("otheruser");

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("Валидация токена - истекший токен выбрасывает исключение")
    void isTokenValid_expiredToken_throwsException() {
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L);

        String expiredToken = jwtService.generateToken(testUser);

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(expiredToken, testUser)
        );
    }

    @Test
    @DisplayName("Извлечение данных из невалидного токена - выбрасывает исключение")
    void extractFromInvalidToken_throwsException() {
        String invalidToken = "this.is.not.a.valid.jwt.token";

        assertThrows(
                JwtException.class,
                () -> jwtService.extractUsername(invalidToken)
        );
    }
}