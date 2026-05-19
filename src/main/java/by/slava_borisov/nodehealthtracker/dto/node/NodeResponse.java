package by.slava_borisov.nodehealthtracker.dto.node;

import by.slava_borisov.nodehealthtracker.model.enums.NodeHealthStatus;

import java.time.LocalDateTime;

public record NodeResponse(

        Long id,

        Long ownerId,

        String name,

        String host,

        String description,

        Boolean isActive,

        NodeHealthStatus healthStatus,

        Long totalServices,

        Long enabledServices,

        Long disabledServices,

        Long upServices,

        Long downServices,

        Long unknownServices,

        Long openIncidents,

        LocalDateTime lastCheckedAt,

        Double availabilityPercent24h,

        Double averageResponseTimeMs24h,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}