package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.dto.admin.UserRoleUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.common.PageResponse;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.AdminService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;


    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> getAllUsers(
            UserStatus status,
            RoleName role,
            String query,
            int page,
            int size
    ) {
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
    public UserAdminResponse getUserById(Long userId) {
        User user = findUserById(userId);

        return toUserAdminResponse(user);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserStatus(Long userId, UserBlockRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId()) && request.status() == UserStatus.BLOCKED) {
            throw new InvalidOperationException(Messages.ADMIN_CANNOT_BLOCK_SELF);
        }

        user.setStatus(request.status());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return toUserAdminResponse(savedUser);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserRole(Long userId, UserRoleUpdateRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId())) {
            throw new InvalidOperationException(Messages.ADMIN_CANNOT_CHANGE_OWN_ROLE);
        }

        user.setRole(request.role());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return toUserAdminResponse(savedUser);
    }

    @Override
    @Transactional
    public UserAdminResponse deleteUser(Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId())) {
            throw new InvalidOperationException(Messages.ADMIN_CANNOT_DELETE_SELF);
        }

        user.setStatus(UserStatus.BLOCKED);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return toUserAdminResponse(savedUser);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.USER_NOT_FOUND));
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
            throw new InvalidOperationException(Messages.PAGE_NUMBER_INVALID);
        }
        if (size < 1 || size > 100) {
            throw new InvalidOperationException(Messages.PAGE_SIZE_INVALID);
        }
    }
}