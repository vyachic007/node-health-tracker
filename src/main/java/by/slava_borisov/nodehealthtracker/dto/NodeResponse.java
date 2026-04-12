package by.slava_borisov.nodehealthtracker.dto;

import java.time.LocalDateTime;

public record NodeResponse(

        Long id,

        Long ownerId,

        String name,

        String host,

        String description,

        Boolean isActive,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}