package by.slava_borisov.nodehealthtracker.bootstrap;

import by.slava_borisov.nodehealthtracker.config.AdminProperties;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminProperties adminProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.countByRole(RoleName.ROLE_ADMIN) > 0) {
            log.info("Администратор уже существует. Автоматическое создание администратора пропущено.");
            return;
        }

        if (!hasAdminCredentials()) {
            log.warn("ADMIN_USERNAME, ADMIN_EMAIL или ADMIN_PASSWORD не заданы. Первый администратор не создан.");
            return;
        }

        String username = adminProperties.username().trim();
        String email = adminProperties.email().trim();

        if (userRepository.existsByUsername(username)) {
            log.warn("Пользователь с username={} уже существует. Первый администратор не создан.", username);
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Пользователь с email={} уже существует. Первый администратор не создан.", email);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRole(RoleName.ROLE_ADMIN);
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);
        admin.setPasswordChangedAt(now);
        admin.setCredentialsChangedAt(now);

        userRepository.save(admin);

        log.info("Первый администратор создан: username={}, email={}", username, email);
    }

    private boolean hasAdminCredentials() {
        return StringUtils.hasText(adminProperties.username())
                && StringUtils.hasText(adminProperties.email())
                && StringUtils.hasText(adminProperties.password());
    }
}
