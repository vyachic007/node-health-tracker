package by.slava_borisov.nodehealthtracker.dto.notification;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Настройка уведомлений пользователя")
public record NotificationSettingResponse(

        @Schema(
                description = "Уникальный идентификатор настройки уведомлений",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Идентификатор пользователя",
                example = "3"
        )
        Long userId,

        @Schema(
                description = "Канал отправки уведомлений",
                example = "TELEGRAM",
                allowableValues = {
                        "EMAIL",
                        "TELEGRAM",
                        "VK"
                }
        )
        NotificationChannel channel,

        @Schema(
                description = "Включена ли настройка уведомлений",
                example = "true"
        )
        Boolean isEnabled,

        @Schema(
                description = "Адрес назначения для уведомлений: email, Telegram chatId или VK peerId",
                example = "123456789",
                nullable = true
        )
        String destination,

        @Schema(
                description = "Отправлять уведомление при открытии инцидента",
                example = "true"
        )
        Boolean notifyOnIncidentOpen,

        @Schema(
                description = "Отправлять уведомление при закрытии инцидента",
                example = "true"
        )
        Boolean notifyOnIncidentResolved
) {
}