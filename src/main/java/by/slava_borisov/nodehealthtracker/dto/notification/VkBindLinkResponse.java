package by.slava_borisov.nodehealthtracker.dto.notification;

import java.time.LocalDateTime;

public record VkBindLinkResponse(
        String bindToken,
        String vkGroupId,
        String vkLink,
        String command,
        LocalDateTime expiresAt
) {
}
