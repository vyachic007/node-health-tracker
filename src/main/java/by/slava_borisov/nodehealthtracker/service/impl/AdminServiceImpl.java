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
import by.slava_borisov.nodehealthtracker.model.enums.ServiceStatus;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.CheckResultRepository;
import by.slava_borisov.nodehealthtracker.repository.IncidentRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkNodeRepository;
import by.slava_borisov.nodehealthtracker.repository.NetworkServiceRepository;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.AdminService;
import by.slava_borisov.nodehealthtracker.service.AuditLogService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String USER_ENTITY_TYPE = "User";

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NetworkNodeRepository networkNodeRepository;
    private final NetworkServiceRepository networkServiceRepository;
    private final IncidentRepository incidentRepository;
    private final CheckResultRepository checkResultRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> getAllUsers(
            UserStatus status,
            RoleName role,
            String query,
            int page,
            int size
    ) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор запросил список пользователей: adminId={}, adminUsername={}, statusFilter={}, roleFilter={}, query={}, page={}, size={}",
                currentUser.getId(),
                currentUser.getUsername(),
                status,
                role,
                query,
                page,
                size
        );

        validatePagination(page, size);
        String normalizedQuery = normalizeQuery(query);

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> usersPage = userRepository.findAllByAdminFilters(
                status,
                role,
                normalizedQuery,
                pageRequest
        );

        List<UserAdminResponse> content = usersPage.getContent()
                .stream()
                .map(this::toUserAdminResponse)
                .toList();

        log.info(
                "Список пользователей сформирован: adminId={}, returnedUsers={}, totalUsers={}, totalPages={}, page={}",
                currentUser.getId(),
                content.size(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.getNumber()
        );

        return new PageResponse<>(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminSummaryResponse getUserSummary() {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор запросил сводку пользователей: adminId={}, adminUsername={}",
                currentUser.getId(),
                currentUser.getUsername()
        );

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedUsers = userRepository.countByStatus(UserStatus.BLOCKED);
        long adminUsers = userRepository.countByRole(RoleName.ROLE_ADMIN);
        long regularUsers = userRepository.countByRole(RoleName.ROLE_USER);

        log.info(
                "Сводка пользователей сформирована: totalUsers={}, activeUsers={}, blockedUsers={}, adminUsers={}, regularUsers={}",
                totalUsers,
                activeUsers,
                blockedUsers,
                adminUsers,
                regularUsers
        );

        return new UserAdminSummaryResponse(
                totalUsers,
                activeUsers,
                blockedUsers,
                adminUsers,
                regularUsers
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserAdminResponse getUserById(Long userId) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор запросил пользователя по id: adminId={}, adminUsername={}, targetUserId={}",
                currentUser.getId(),
                currentUser.getUsername(),
                userId
        );

        User user = findUserById(userId);

        return toUserAdminResponse(user);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserStatus(Long userId, UserBlockRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор изменяет статус пользователя: adminId={}, adminUsername={}, targetUserId={}, newStatus={}",
                currentUser.getId(),
                currentUser.getUsername(),
                userId,
                request.status()
        );

        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId()) && request.status() == UserStatus.BLOCKED) {
            log.warn(
                    "Попытка администратора заблокировать самого себя отклонена: adminId={}, adminUsername={}",
                    currentUser.getId(),
                    currentUser.getUsername()
            );

            throw new InvalidOperationException(Messages.ADMIN_CANNOT_BLOCK_SELF);
        }

        UserStatus previousStatus = user.getStatus();

        user.setStatus(request.status());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        logUserStatusChange(savedUser, previousStatus, request.status());

        log.info(
                "Статус пользователя изменён: adminId={}, targetUserId={}, targetUsername={}, previousStatus={}, newStatus={}",
                currentUser.getId(),
                savedUser.getId(),
                savedUser.getUsername(),
                previousStatus,
                savedUser.getStatus()
        );

        return toUserAdminResponse(savedUser);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserRole(Long userId, UserRoleUpdateRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор изменяет роль пользователя: adminId={}, adminUsername={}, targetUserId={}, newRole={}",
                currentUser.getId(),
                currentUser.getUsername(),
                userId,
                request.role()
        );

        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId())) {
            log.warn(
                    "Попытка администратора изменить собственную роль отклонена: adminId={}, adminUsername={}",
                    currentUser.getId(),
                    currentUser.getUsername()
            );

            throw new InvalidOperationException(Messages.ADMIN_CANNOT_CHANGE_OWN_ROLE);
        }

        RoleName previousRole = user.getRole();

        user.setRole(request.role());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        auditLogService.log(
                AuditActionType.USER_ROLE_UPDATED,
                Messages.AUDIT_USER_ROLE_UPDATED + savedUser.getUsername(),
                USER_ENTITY_TYPE,
                savedUser.getId()
        );

        log.info(
                "Роль пользователя изменена: adminId={}, targetUserId={}, targetUsername={}, previousRole={}, newRole={}",
                currentUser.getId(),
                savedUser.getId(),
                savedUser.getUsername(),
                previousRole,
                savedUser.getRole()
        );

        return toUserAdminResponse(savedUser);
    }

    @Override
    @Transactional
    public UserAdminResponse deleteUser(Long userId) {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор выполняет удаление пользователя: adminId={}, adminUsername={}, targetUserId={}",
                currentUser.getId(),
                currentUser.getUsername(),
                userId
        );

        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId())) {
            log.warn(
                    "Попытка администратора удалить самого себя отклонена: adminId={}, adminUsername={}",
                    currentUser.getId(),
                    currentUser.getUsername()
            );

            throw new InvalidOperationException(Messages.ADMIN_CANNOT_DELETE_SELF);
        }

        user.setStatus(UserStatus.BLOCKED);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        auditLogService.log(
                AuditActionType.USER_BLOCKED,
                Messages.AUDIT_USER_DELETED + savedUser.getUsername(),
                USER_ENTITY_TYPE,
                savedUser.getId()
        );

        log.info(
                "Пользователь удалён через soft-delete: adminId={}, targetUserId={}, targetUsername={}, finalStatus={}",
                currentUser.getId(),
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getStatus()
        );

        return toUserAdminResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPlatformSummaryResponse getPlatformSummary() {
        User currentUser = currentUserService.getCurrentUser();

        log.info(
                "Администратор запросил платформенную сводку: adminId={}, adminUsername={}",
                currentUser.getId(),
                currentUser.getUsername()
        );

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long blockedUsers = userRepository.countByStatus(UserStatus.BLOCKED);
        long adminUsers = userRepository.countByRole(RoleName.ROLE_ADMIN);
        long regularUsers = userRepository.countByRole(RoleName.ROLE_USER);

        long totalNodes = networkNodeRepository.count();

        long totalServices = networkServiceRepository.count();
        long enabledServices = networkServiceRepository.countByIsEnabledTrue();
        long disabledServices = networkServiceRepository.countByIsEnabledFalse();

        long upServices = networkServiceRepository.countCurrentServicesByStatus(
                ServiceStatus.UP.name()
        );

        long downServices = networkServiceRepository.countCurrentServicesByStatus(
                ServiceStatus.DOWN.name()
        );

        long openIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long resolvedIncidents = incidentRepository.countByStatus(IncidentStatus.RESOLVED);

        long checksLast24Hours = checkResultRepository.countByCheckedAtAfter(
                LocalDateTime.now().minusHours(24)
        );

        log.info(
                "Платформенная сводка сформирована: totalUsers={}, totalNodes={}, totalServices={}, enabledServices={}, upServices={}, downServices={}, openIncidents={}, resolvedIncidents={}, checksLast24Hours={}",
                totalUsers,
                totalNodes,
                totalServices,
                enabledServices,
                upServices,
                downServices,
                openIncidents,
                resolvedIncidents,
                checksLast24Hours
        );

        return new AdminPlatformSummaryResponse(
                totalUsers,
                activeUsers,
                blockedUsers,
                adminUsers,
                regularUsers,
                totalNodes,
                totalServices,
                enabledServices,
                disabledServices,
                upServices,
                downServices,
                openIncidents,
                resolvedIncidents,
                checksLast24Hours
        );
    }

    private void logUserStatusChange(
            User savedUser,
            UserStatus previousStatus,
            UserStatus newStatus
    ) {
        if (previousStatus == newStatus) {
            log.info(
                    "Статус пользователя не изменился: userId={}, username={}, status={}",
                    savedUser.getId(),
                    savedUser.getUsername(),
                    newStatus
            );

            return;
        }

        if (newStatus == UserStatus.BLOCKED) {
            auditLogService.log(
                    AuditActionType.USER_BLOCKED,
                    Messages.AUDIT_USER_BLOCKED + savedUser.getUsername(),
                    USER_ENTITY_TYPE,
                    savedUser.getId()
            );

            log.info(
                    "В audit записана блокировка пользователя: userId={}, username={}",
                    savedUser.getId(),
                    savedUser.getUsername()
            );

            return;
        }

        if (newStatus == UserStatus.ACTIVE) {
            auditLogService.log(
                    AuditActionType.USER_UNBLOCKED,
                    Messages.AUDIT_USER_UNBLOCKED + savedUser.getUsername(),
                    USER_ENTITY_TYPE,
                    savedUser.getId()
            );

            log.info(
                    "В audit записана разблокировка пользователя: userId={}, username={}",
                    savedUser.getId(),
                    savedUser.getUsername()
            );
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn(
                            "Пользователь не найден: userId={}",
                            userId
                    );

                    return new ResourceNotFoundException(Messages.USER_NOT_FOUND);
                });
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        return query.trim();
    }

    private UserAdminResponse toUserAdminResponse(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getStatus(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            log.warn(
                    "Некорректный номер страницы при запросе пользователей: page={}",
                    page
            );

            throw new InvalidOperationException(Messages.PAGE_NUMBER_INVALID);
        }

        if (size < 1 || size > 100) {
            log.warn(
                    "Некорректный размер страницы при запросе пользователей: size={}",
                    size
            );

            throw new InvalidOperationException(Messages.PAGE_SIZE_INVALID);
        }
    }
}