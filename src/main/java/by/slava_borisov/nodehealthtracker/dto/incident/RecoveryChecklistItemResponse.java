package by.slava_borisov.nodehealthtracker.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Один шаг recovery checklist для восстановления после инцидента")
public record RecoveryChecklistItemResponse(

        @Schema(
                description = "Порядковый номер шага",
                example = "1"
        )
        Integer stepNumber,

        @Schema(
                description = "Краткое название шага восстановления",
                example = "Проверить DNS-записи"
        )
        String title,

        @Schema(
                description = "Подробное описание действия, которое нужно выполнить",
                example = "Проверьте, что доменное имя корректно разрешается в IP-адрес и DNS-записи не устарели"
        )
        String description,

        @Schema(
                description = "Признак критически важного шага",
                example = "true"
        )
        Boolean isCritical
) {
}