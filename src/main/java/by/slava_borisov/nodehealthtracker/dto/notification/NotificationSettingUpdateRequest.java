package by.slava_borisov.nodehealthtracker.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Данные для изменения настройки уведомлений")
public record NotificationSettingUpdateRequest(

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