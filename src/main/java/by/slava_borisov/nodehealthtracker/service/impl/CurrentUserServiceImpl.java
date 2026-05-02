package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.exception.ResourceNotFoundException;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import by.slava_borisov.nodehealthtracker.service.CurrentUserService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {

    private static final Long TEMPORARY_USER_ID = 1L;

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        return userRepository.findById(TEMPORARY_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.USER_NOT_FOUND));
    }
}
