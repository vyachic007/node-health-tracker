package by.slava_borisov.nodehealthtracker.dto.notification;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для создания настройки уведомлений")
public record NotificationSettingCreateRequest(

        @Schema(
                description = "Канал отправки уведомлений",
                example = "TELEGRAM",
                allowableValues = {
                        "EMAIL",
                        "TELEGRAM",
                        "VK"
                }
        )
        @NotNull
        NotificationChannel channel,

        @Schema(
                description = "Включена ли настройка уведомлений",
                example = "true"
        )
        @NotNull
        Boolean isEnabled,

        @Schema(
                description = "Адрес назначения для уведомлений: email, Telegram chatId или VK peerId",
                example = "123456789",
                maxLength = 255,
                nullable = true
        )
        @Size(max = 255)
        String destination,

        @Schema(
                description = "Отправлять уведомление при открытии инцидента",
                example = "true"
        )
        @NotNull
        Boolean notifyOnIncidentOpen,

        @Schema(
                description = "Отправлять уведомление при закрытии инцидента",
                example = "true"
        )
        @NotNull
        Boolean notifyOnIncidentResolved
) {
}