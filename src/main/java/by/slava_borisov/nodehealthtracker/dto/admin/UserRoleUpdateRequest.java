package by.slava_borisov.nodehealthtracker.dto.admin;

import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(

        @NotNull
        RoleName role
) {
}