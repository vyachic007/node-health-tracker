package by.slava_borisov.nodehealthtracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.telegram")
public class TelegramProperties {

    private String apiUrl;
    private String botToken;
}