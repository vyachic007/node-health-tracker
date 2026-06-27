package by.slava_borisov.nodehealthtracker.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ссылка для привязки Telegram к уведомлениям пользователя")
public record TelegramBindLinkResponse(

        @Schema(
                description = "Временный токен привязки Telegram",
                example = "Qx7Yp9kLm2Nf4Rt8Vb1Cs6Zd0Aa3Ee5Gh"
        )
        String bindToken,

        @Schema(
                description = "Имя Telegram-бота без символа @",
                example = "node_health_tracker_bot"
        )
        String botUsername,

        @Schema(
                description = "Готовая ссылка для перехода к Telegram-боту",
                example = "https://t.me/node_health_tracker_bot?start=Qx7Yp9kLm2Nf4Rt8Vb1Cs6Zd0Aa3Ee5Gh"
        )
        String telegramLink,

        @Schema(
                description = "Дата и время истечения срока действия токена привязки",
                example = "2026-06-26T17:00:00"
        )
        LocalDateTime expiresAt
) {
}