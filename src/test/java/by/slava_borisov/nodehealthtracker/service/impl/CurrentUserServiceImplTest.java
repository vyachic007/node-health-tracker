package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.exception.AccessDeniedException;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Тесты CurrentUserServiceImpl")
class CurrentUserServiceImplTest {

    private CurrentUserService currentUserService;
    private User mockUser;

    @BeforeEach
    void setUp() {
        currentUserService = new CurrentUserServiceImpl();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Получить текущего пользователя - успешно")
    void getCurrentUser_success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContextHolder.setContext(securityContext);

        User result = currentUserService.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("Получить текущего пользователя - аутентификация отсутствует")
    void getCurrentUser_authenticationNull_throwsException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);

        SecurityContextHolder.setContext(securityContext);

        assertThrows(
                AccessDeniedException.class,
                () -> currentUserService.getCurrentUser()
        );
    }

    @Test
    @DisplayName("Получить текущего пользователя - пользователь не аутентифицирован")
    void getCurrentUser_notAuthenticated_throwsException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        SecurityContextHolder.setContext(securityContext);

        assertThrows(
                AccessDeniedException.class,
                () -> currentUserService.getCurrentUser()
        );
    }

    @Test
    @DisplayName("Получить текущего пользователя - принципал не является объектом User")
    void getCurrentUser_principalNotUser_throwsException() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("some-string-username");

        SecurityContextHolder.setContext(securityContext);

        assertThrows(
                AccessDeniedException.class,
                () -> currentUserService.getCurrentUser()
        );
    }
}