package by.slava_borisov.nodehealthtracker.dto;

import by.slava_borisov.nodehealthtracker.model.enums.CheckType;

import java.time.LocalDateTime;

public record ServiceResponse(

        Long id,

        Long nodeId,

        CheckType checkType,

        String name,

        String targetHost,

        Integer port,

        String path,

        Integer intervalSeconds,

        Boolean isEnabled,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}