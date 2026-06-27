package by.slava_borisov.nodehealthtracker.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Webhook-запрос от Telegram")
public record TelegramWebhookRequest(

        @Schema(
                description = "Уникальный идентификатор обновления Telegram",
                example = "987654321"
        )
        @JsonProperty("update_id")
        Long updateId,

        @Schema(
                description = "Сообщение, отправленное пользователем боту"
        )
        TelegramMessage message
) {

    @Schema(description = "Сообщение Telegram")
    public record TelegramMessage(

            @Schema(
                    description = "Уникальный идентификатор сообщения",
                    example = "15"
            )
            @JsonProperty("message_id")
            Long messageId,

            @Schema(
                    description = "Чат, из которого пришло сообщение"
            )
            TelegramChat chat,

            @Schema(
                    description = "Пользователь, отправивший сообщение"
            )
            TelegramUser from,

            @Schema(
                    description = "Текст сообщения. Может содержать bind-token для привязки Telegram",
                    example = "/start tg_bind_abc123"
            )
            String text,

            @Schema(
                    description = "Дата сообщения в Unix timestamp",
                    example = "1719400000"
            )
            Long date
    ) {
    }

    @Schema(description = "Telegram-чат")
    public record TelegramChat(

            @Schema(
                    description = "Уникальный идентификатор Telegram-чата",
                    example = "123456789"
            )
            Long id,

            @Schema(
                    description = "Тип чата",
                    example = "private",
                    allowableValues = {
                            "private",
                            "group",
                            "supergroup",
                            "channel"
                    }
            )
            String type,

            @Schema(
                    description = "Username чата или пользователя, если есть",
                    example = "network_admin"
            )
            String username,

            @Schema(
                    description = "Имя пользователя в Telegram",
                    example = "Slava"
            )
            @JsonProperty("first_name")
            String firstName,

            @Schema(
                    description = "Фамилия пользователя в Telegram",
                    example = "Borisov"
            )
            @JsonProperty("last_name")
            String lastName
    ) {
    }

    @Schema(description = "Telegram-пользователь")
    public record TelegramUser(

            @Schema(
                    description = "Уникальный идентификатор Telegram-пользователя",
                    example = "123456789"
            )
            Long id,

            @Schema(
                    description = "Признак того, что отправитель является ботом",
                    example = "false"
            )
            @JsonProperty("is_bot")
            Boolean isBot,

            @Schema(
                    description = "Username пользователя в Telegram",
                    example = "network_admin"
            )
            String username,

            @Schema(
                    description = "Имя пользователя в Telegram",
                    example = "Slava"
            )
            @JsonProperty("first_name")
            String firstName,

            @Schema(
                    description = "Фамилия пользователя в Telegram",
                    example = "Borisov"
            )
            @JsonProperty("last_name")
            String lastName
    ) {
    }
}