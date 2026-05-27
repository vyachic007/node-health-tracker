package by.slava_borisov.nodehealthtracker.dto.notification;

import java.time.LocalDateTime;

public record TelegramBindLinkResponse(

        String bindToken,

        String botUsername,

        String telegramLink,

        LocalDateTime expiresAt
) {
}