package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.exception.InvalidOperationException;
import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.AdminService;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
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
    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toUserAdminResponse)
                .toList();
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
    public void deleteUser(Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        User user = findUserById(userId);

        if (currentUser.getId().equals(user.getId())) {
            throw new InvalidOperationException(Messages.ADMIN_CANNOT_DELETE_SELF);
        }

        userRepository.delete(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.USER_NOT_FOUND));
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
}