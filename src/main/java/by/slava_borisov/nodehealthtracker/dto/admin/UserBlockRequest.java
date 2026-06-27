package by.slava_borisov.nodehealthtracker.dto.admin;

import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Данные для изменения статуса пользователя")
public record UserBlockRequest(

        @Schema(
                description = "Новый статус учётной записи пользователя",
                example = "BLOCKED",
                allowableValues = {
                        "ACTIVE",
                        "BLOCKED"
                }
        )
        @NotNull
        UserStatus status
) {
}