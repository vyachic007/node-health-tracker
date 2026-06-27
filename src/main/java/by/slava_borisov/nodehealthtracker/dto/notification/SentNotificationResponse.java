package by.slava_borisov.nodehealthtracker.dto.notification;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Информация об отправленном уведомлении")
public record SentNotificationResponse(

        @Schema(
                description = "Уникальный идентификатор записи об отправке уведомления",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Идентификатор пользователя, которому отправлялось уведомление",
                example = "3"
        )
        Long userId,

        @Schema(
                description = "Идентификатор инцидента, по которому отправлялось уведомление",
                example = "15"
        )
        Long incidentId,

        @Schema(
                description = "Канал отправки уведомления",
                example = "TELEGRAM",
                allowableValues = {
                        "EMAIL",
                        "TELEGRAM",
                        "VK"
                }
        )
        NotificationChannel channel,

        @Schema(
                description = "Событие, по которому было отправлено уведомление",
                example = "INCIDENT_OPENED",
                allowableValues = {
                        "INCIDENT_OPENED",
                        "INCIDENT_RESOLVED"
                }
        )
        NotificationEvent event,

        @Schema(
                description = "Дата и время попытки отправки уведомления",
                example = "2026-06-26T16:45:00"
        )
        LocalDateTime sentAt,

        @Schema(
                description = "Статус отправки уведомления",
                example = "SENT",
                allowableValues = {
                        "SENT",
                        "FAILED"
                }
        )
        String status,

        @Schema(
                description = "Текст ошибки, если уведомление не удалось отправить",
                example = "Telegram chat not found",
                nullable = true
        )
        String errorMessage
) {
}