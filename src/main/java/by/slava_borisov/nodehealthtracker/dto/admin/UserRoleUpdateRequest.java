package by.slava_borisov.nodehealthtracker.dto.admin;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Данные для изменения роли пользователя")
public record UserRoleUpdateRequest(

        @Schema(
                description = "Новая роль пользователя",
                example = "ROLE_ADMIN",
                allowableValues = {
                        "ROLE_USER",
                        "ROLE_ADMIN"
                }
        )
        @NotNull
        RoleName role
) {
}