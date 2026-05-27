package by.slava_borisov.nodehealthtracker.dto.service;

import by.slava_borisov.nodehealthtracker.model.enums.*;

import java.time.LocalDateTime;

public record ServiceResponse(

        Long id,

        Long nodeId,

        CheckType checkType,

        String heartbeatToken,

        LocalDateTime lastHeartbeatAt,

        LocalDateTime lastCheckedAt,

        String name,

        String targetHost,

        Integer port,

        String path,

        Integer intervalSeconds,

        Boolean isEnabled,

        Integer responseTimeThresholdMs,

        Integer degradationThreshold,

        Integer consecutiveDegradations,

        Boolean notifyEmail,

        Boolean notifyTelegram,

        Boolean notifyVk,

        ServiceStatus lastStatus,

        Integer lastResponseTimeMs,

        Integer lastHttpStatusCode,

        FailureLayer lastFailureLayer,

        String lastDiagnosticMessage,

        String lastRecommendation,

        LocalDateTime nextCheckAt,

        Long secondsUntilNextCheck,

        Boolean hasOpenIncident,

        Long openIncidentId,

        Long currentDowntimeSeconds,

        Double availabilityPercent24h,

        Double averageResponseTimeMs24h,

        Integer healthScore,

        HealthLevel healthLevel,

        RecurrenceLevel recurrenceLevel,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
