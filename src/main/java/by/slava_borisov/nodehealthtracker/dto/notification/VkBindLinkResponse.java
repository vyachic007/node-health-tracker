package by.slava_borisov.nodehealthtracker.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Ссылка и команда для привязки VK к уведомлениям пользователя")
public record VkBindLinkResponse(

        @Schema(
                description = "Временный токен привязки VK",
                example = "Qx7Yp9kLm2Nf4Rt8Vb1Cs6Zd0Aa3Ee5Gh"
        )
        String bindToken,

        @Schema(
                description = "Идентификатор VK-группы, через которую выполняется привязка",
                example = "123456789"
        )
        String vkGroupId,

        @Schema(
                description = "Ссылка на диалог с VK-группой",
                example = "https://vk.com/im?sel=-123456789"
        )
        String vkLink,

        @Schema(
                description = "Команда, которую пользователь должен отправить в диалог с VK-группой",
                example = "/start Qx7Yp9kLm2Nf4Rt8Vb1Cs6Zd0Aa3Ee5Gh"
        )
        String command,

        @Schema(
                description = "Дата и время истечения срока действия токена привязки",
                example = "2026-06-26T17:00:00"
        )
        LocalDateTime expiresAt
) {
}