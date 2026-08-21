package by.slava_borisov.nodehealthtracker.dto.service;

import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания сервиса мониторинга")
public record ServiceCreateRequest(

        @Schema(
                description = "Идентификатор сетевого узла, в котором создаётся сервис",
                example = "1"
        )
        @NotNull
        Long nodeId,

        @Schema(
                description = "Тип проверки сервиса",
                example = "HTTP",
                allowableValues = {
                        "HTTP",
                        "HTTPS",
                        "TCP",
                        "PING",
                        "DNS",
                        "SSL"
                }
        )
        @NotNull
        CheckType checkType,

        @Schema(
                description = "Название сервиса мониторинга",
                example = "Основной API",
                maxLength = 150
        )
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(
                description = "Доменное имя или IP-адрес проверяемого сервиса",
                example = "api.example.com",
                maxLength = 255
        )
        @NotBlank
        @Size(max = 255)
        String targetHost,

        @Schema(
                description = "Сетевой порт проверяемого сервиса",
                example = "443",
                nullable = true
        )
        Integer port,

        @Schema(
                description = "Путь HTTP- или HTTPS-запроса",
                example = "/actuator/health",
                maxLength = 500,
                nullable = true
        )
        @Size(max = 500)
        String path,

        @Schema(
                description = "Интервал между автоматическими проверками в секундах",
                example = "60"
        )
        @NotNull
        Integer intervalSeconds,

        @Schema(
                description = "Порог времени ответа в миллисекундах, после которого сервис считается замедленным",
                example = "1000",
                nullable = true
        )
        Integer responseTimeThresholdMs,

        @Schema(
                description = "Количество последовательных превышений порога для признания сервиса деградировавшим",
                example = "3",
                nullable = true
        )
        Integer degradationThreshold,

        @Schema(
                description = "Отправлять уведомления по электронной почте",
                example = "true",
                nullable = true
        )
        Boolean notifyEmail,

        @Schema(
                description = "Отправлять уведомления в Telegram",
                example = "true",
                nullable = true
        )
        Boolean notifyTelegram,

        @Schema(
                description = "Отправлять уведомления во ВКонтакте",
                example = "false",
                nullable = true
        )
        Boolean notifyVk
) {
}