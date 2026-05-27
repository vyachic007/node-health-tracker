package by.slava_borisov.nodehealthtracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(
        String username,
        String email,
        String password
) {
}
