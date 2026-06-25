package by.slava_borisov.nodehealthtracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI nodeHealthTrackerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Node Health Tracker API")
                        .version("1.0.0")
                        .description("""
                                REST API системы диагностического мониторинга сетевых узлов \
                                и сервисов.

                                API позволяет управлять узлами и сервисами мониторинга, \
                                выполнять проверки HTTP, HTTPS, TCP, DNS, SSL, PING и HEARTBEAT, \
                                просматривать результаты проверок, диагностировать причины сбоев, \
                                управлять инцидентами и настраивать уведомления через Email, \
                                Telegram и VK.
                                """)
                        .contact(new Contact()
                                .name("Вячеслав Борисов")
                                .email("salava.borisov@yandex.ru")))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                Введите JWT-токен авторизованного пользователя.

                                                Префикс Bearer вводить не требуется: \
                                                Swagger UI добавит его автоматически.
                                                """)
                        ));
    }
}