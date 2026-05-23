package by.slava_borisov.nodehealthtracker.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramWebhookRequest(
        @JsonProperty("update_id")
        Long updateId,

        TelegramMessage message
) {
    public record TelegramMessage(
            @JsonProperty("message_id")
            Long messageId,

            TelegramChat chat,

            TelegramUser from,

            String text,

            Long date
    ) {
    }

    public record TelegramChat(
            Long id,

            String type,

            String username,

            @JsonProperty("first_name")
            String firstName,

            @JsonProperty("last_name")
            String lastName
    ) {
    }

    public record TelegramUser(
            Long id,

            @JsonProperty("is_bot")
            Boolean isBot,

            String username,

            @JsonProperty("first_name")
            String firstName,

            @JsonProperty("last_name")
            String lastName
    ) {
    }
}