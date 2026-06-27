package by.slava_borisov.nodehealthtracker.dto.audit;

import by.slava_borisov.nodehealthtracker.model.enums.AuditActionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Запись журнала аудита")
public record AuditLogResponse(

        @Schema(
                description = "Уникальный идентификатор записи аудита",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Тип выполненного действия",
                example = "NODE_CREATED"
        )
        AuditActionType actionType,

        @Schema(
                description = "Текстовое описание действия",
                example = "Создан сетевой узел: Основной сервер"
        )
        String description,

        @Schema(
                description = "Тип сущности, над которой выполнено действие",
                example = "NetworkNode"
        )
        String entityType,

        @Schema(
                description = "Идентификатор сущности, над которой выполнено действие",
                example = "5",
                nullable = true
        )
        Long entityId,

        @Schema(
                description = "Идентификатор пользователя, выполнившего действие",
                example = "3",
                nullable = true
        )
        Long userId,

        @Schema(
                description = "Имя пользователя, выполнившего действие",
                example = "network_admin"
        )
        String username,

        @Schema(
                description = "Дата и время создания записи аудита",
                example = "2026-06-26T17:10:00"
        )
        LocalDateTime createdAt
) {
}