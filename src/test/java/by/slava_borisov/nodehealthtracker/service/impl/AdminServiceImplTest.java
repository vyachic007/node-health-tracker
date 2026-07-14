package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.admin.AdminPlatformSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.dto.admin.UserRoleUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.common.PageResponse;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты AdminServiceImpl")
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private NetworkNodeRepository networkNodeRepository;

    @Mock
    private NetworkServiceRepository networkServiceRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = createUser(1L, "admin@test.com", "admin", RoleName.ROLE_ADMIN, UserStatus.ACTIVE);
        regularUser = createUser(2L, "user@test.com", "user", RoleName.ROLE_USER, UserStatus.ACTIVE);

        when(currentUserService.getCurrentUser())
                .thenReturn(adminUser);
    }

    // ==================== getAllUsers Tests ====================

    @Test
    @DisplayName("Получить список пользователей с фильтрами - успешно")
    void getAllUsers_withFilters_success() {
        Page<User> userPage = new PageImpl<>(List.of(regularUser));

        when(userRepository.findAllByAdminFilters(
                eq(UserStatus.ACTIVE),
                eq(RoleName.ROLE_USER),
                eq("test"),
                any(PageRequest.class)
        )).thenReturn(userPage);

        PageResponse<UserAdminResponse> result = adminService.getAllUsers(
                UserStatus.ACTIVE,
                RoleName.ROLE_USER,
                "test",
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals("user", result.content().get(0).username());
    }

    @Test
    @DisplayName("Получить список пользователей без фильтров - успешно")
    void getAllUsers_withoutFilters_success() {
        Page<User> userPage = new PageImpl<>(List.of(regularUser));
        when(userRepository.findAllByAdminFilters(
                isNull(),
                isNull(),
                eq(""),
                any(PageRequest.class)
        )).thenReturn(userPage);

        PageResponse<UserAdminResponse> result = adminService.getAllUsers(
                null,
                null,
                null,
                0,
                10
        );

        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Получить список пользователей - некорректный номер страницы")
    void getAllUsers_invalidPageNumber_throwsException() {
        assertThrows(InvalidOperationException.class, () ->
                adminService.getAllUsers(null, null, null, -1, 10)
        );
    }

    @Test
    @DisplayName("Получить список пользователей - некорректный размер страницы")
    void getAllUsers_invalidPageSize_throwsException() {
        assertThrows(InvalidOperationException.class, () ->
                adminService.getAllUsers(null, null, null, 0, 0)
        );

        assertThrows(InvalidOperationException.class, () ->
                adminService.getAllUsers(null, null, null, 0, 101)
        );
    }

    // ==================== getUserSummary Tests ====================

    @Test
    @DisplayName("Получить сводку пользователей - успешно")
    void getUserSummary_success() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(8L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(2L);
        when(userRepository.countByRole(RoleName.ROLE_ADMIN)).thenReturn(2L);
        when(userRepository.countByRole(RoleName.ROLE_USER)).thenReturn(8L);

        UserAdminSummaryResponse result = adminService.getUserSummary();

        assertNotNull(result);
        assertEquals(10L, result.totalUsers());
        assertEquals(8L, result.activeUsers());
        assertEquals(2L, result.blockedUsers());
        assertEquals(2L, result.adminUsers());
        assertEquals(8L, result.regularUsers());
    }

    // ==================== getUserById Tests ====================

    @Test
    @DisplayName("Получить пользователя по ID - успешно")
    void getUserById_success() {
        when(userRepository.findById(2L))
                .thenReturn(Optional.of(regularUser));

        UserAdminResponse result = adminService.getUserById(2L);

        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("user", result.username());
        assertEquals("user@test.com", result.email());
    }

    @Test
    @DisplayName("Получить пользователя по ID - пользователь не найден")
    void getUserById_userNotFound_throwsException() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminService.getUserById(999L)
        );
    }

    // ==================== updateUserStatus Tests ====================

    @Test
    @DisplayName("Обновить статус пользователя - успешно")
    void updateUserStatus_success() {
        User userToUpdate = createUser(2L, "user@test.com", "user", RoleName.ROLE_USER, UserStatus.ACTIVE);
        when(userRepository.findById(2L)).thenReturn(Optional.of(userToUpdate));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserBlockRequest request = new UserBlockRequest(UserStatus.BLOCKED);
        UserAdminResponse result = adminService.updateUserStatus(2L, request);

        assertNotNull(result);
        assertEquals(UserStatus.BLOCKED, result.status());
        verify(auditLogService).log(
                eq(AuditActionType.USER_BLOCKED),
                anyString(),
                eq("User"),
                eq(2L)
        );
    }

    @Test
    @DisplayName("Обновить статус пользователя - админ пытается заблокировать себя")
    void updateUserStatus_adminBlocksSelf_throwsException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(adminUser));

        UserBlockRequest request = new UserBlockRequest(UserStatus.BLOCKED);

        assertThrows(InvalidOperationException.class, () ->
                adminService.updateUserStatus(1L, request)
        );
    }

    @Test
    @DisplayName("Обновить статус пользователя - пользователь не найден")
    void updateUserStatus_userNotFound_throwsException() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserBlockRequest request = new UserBlockRequest(UserStatus.BLOCKED);

        assertThrows(ResourceNotFoundException.class, () ->
                adminService.updateUserStatus(999L, request)
        );
    }

    // ==================== updateUserRole Tests ====================

    @Test
    @DisplayName("Обновить роль пользователя - успешно")
    void updateUserRole_success() {
        User userToUpdate = createUser(
                2L,
                "user@test.com",
                "user",
                RoleName.ROLE_USER,
                UserStatus.ACTIVE);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(userToUpdate));

        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        UserRoleUpdateRequest request = new UserRoleUpdateRequest(RoleName.ROLE_ADMIN);
        UserAdminResponse result = adminService.updateUserRole(2L, request);

        assertNotNull(result);
        assertEquals(RoleName.ROLE_ADMIN, result.role());
        verify(auditLogService).log(
                eq(AuditActionType.USER_ROLE_UPDATED),
                anyString(),
                eq("User"),
                eq(2L)
        );
    }

    @Test
    @DisplayName("Обновить роль пользователя - админ пытается изменить свою роль")
    void updateUserRole_adminChangesOwnRole_throwsException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(adminUser));

        UserRoleUpdateRequest request = new UserRoleUpdateRequest(RoleName.ROLE_USER);

        assertThrows(InvalidOperationException.class, () ->
                adminService.updateUserRole(1L, request)
        );
    }

    @Test
    @DisplayName("Обновить роль пользователя - пользователь не найден")
    void updateUserRole_userNotFound_throwsException() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserRoleUpdateRequest request = new UserRoleUpdateRequest(RoleName.ROLE_ADMIN);

        assertThrows(ResourceNotFoundException.class, () ->
                adminService.updateUserRole(999L, request)
        );
    }

    // ==================== deleteUser Tests ====================

    @Test
    @DisplayName("Удалить пользователя - успешно (soft delete)")
    void deleteUser_success() {
        User userToDelete = createUser(
                2L,
                "user@test.com",
                "user",
                RoleName.ROLE_USER,
                UserStatus.ACTIVE
        );

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(userToDelete));

        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        UserAdminResponse result = adminService.deleteUser(2L);

        assertNotNull(result);
        assertEquals(UserStatus.BLOCKED, result.status());
        verify(auditLogService).log(
                eq(AuditActionType.USER_BLOCKED),
                anyString(),
                eq("User"),
                eq(2L)
        );
    }

    @Test
    @DisplayName("Удалить пользователя - админ пытается удалить себя")
    void deleteUser_adminDeletesSelf_throwsException() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(adminUser));

        assertThrows(InvalidOperationException.class, () ->
                adminService.deleteUser(1L)
        );
    }

    @Test
    @DisplayName("Удалить пользователя - пользователь не найден")
    void deleteUser_userNotFound_throwsException() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                adminService.deleteUser(999L)
        );
    }

    // ==================== getPlatformSummary Tests ====================

    @Test
    @DisplayName("Получить платформенную сводку - успешно")
    void getPlatformSummary_success() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByStatus(UserStatus.ACTIVE)).thenReturn(8L);
        when(userRepository.countByStatus(UserStatus.BLOCKED)).thenReturn(2L);
        when(userRepository.countByRole(RoleName.ROLE_ADMIN)).thenReturn(2L);
        when(userRepository.countByRole(RoleName.ROLE_USER)).thenReturn(8L);

        when(networkNodeRepository.count()).thenReturn(5L);
        when(networkServiceRepository.count()).thenReturn(20L);
        when(networkServiceRepository.countByIsEnabledTrue()).thenReturn(18L);
        when(networkServiceRepository.countByIsEnabledFalse()).thenReturn(2L);
        when(networkServiceRepository.countCurrentServicesByStatus("UP")).thenReturn(15L);
        when(networkServiceRepository.countCurrentServicesByStatus("DOWN")).thenReturn(3L);

        when(incidentRepository.countByStatus(IncidentStatus.OPEN)).thenReturn(4L);
        when(incidentRepository.countByStatus(IncidentStatus.RESOLVED)).thenReturn(10L);

        when(checkResultRepository.countByCheckedAtAfter(any(LocalDateTime.class))).thenReturn(1000L);

        AdminPlatformSummaryResponse result = adminService.getPlatformSummary();

        assertNotNull(result);
        assertEquals(10L, result.totalUsers());
        assertEquals(5L, result.totalNodes());
        assertEquals(20L, result.totalServices());
        assertEquals(18L, result.enabledServices());
        assertEquals(15L, result.upServices());
        assertEquals(3L, result.downServices());
        assertEquals(4L, result.openIncidents());
        assertEquals(10L, result.resolvedIncidents());
        assertEquals(1000L, result.checksLast24Hours());
    }

    // ==================== Helper Methods ====================

    private User createUser(Long id, String email, String username, RoleName role, UserStatus status) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setUsername(username);
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}