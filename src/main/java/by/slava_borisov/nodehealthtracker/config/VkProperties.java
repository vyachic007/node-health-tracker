package by.slava_borisov.nodehealthtracker.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.vk")
public class VkProperties {

    private String apiUrl;
    private String accessToken;
    private String apiVersion;
}