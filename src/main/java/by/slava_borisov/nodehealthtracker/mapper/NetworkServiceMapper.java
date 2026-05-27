package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface NetworkServiceMapper {

    @Mapping(target = "nodeId", source = "node.id")
    @Mapping(target = "lastStatus", ignore = true)
    @Mapping(target = "lastResponseTimeMs", ignore = true)
    @Mapping(target = "lastHttpStatusCode", ignore = true)
    @Mapping(target = "lastFailureLayer", ignore = true)
    @Mapping(target = "lastDiagnosticMessage", ignore = true)
    @Mapping(target = "lastRecommendation", ignore = true)
    @Mapping(target = "nextCheckAt", ignore = true)
    @Mapping(target = "secondsUntilNextCheck", ignore = true)
    @Mapping(target = "hasOpenIncident", ignore = true)
    @Mapping(target = "openIncidentId", ignore = true)
    @Mapping(target = "currentDowntimeSeconds", ignore = true)
    @Mapping(target = "availabilityPercent24h", ignore = true)
    @Mapping(target = "averageResponseTimeMs24h", ignore = true)
    @Mapping(target = "healthScore", ignore = true)
    @Mapping(target = "healthLevel", ignore = true)
    @Mapping(target = "recurrenceLevel", ignore = true)
    @Mapping(target = "degraded", ignore = true)
    ServiceResponse toServiceResponse(NetworkService networkService);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "heartbeatToken", ignore = true)
    @Mapping(target = "lastHeartbeatAt", ignore = true)
    @Mapping(target = "lastCheckedAt", ignore = true)
    @Mapping(target = "isEnabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "node", ignore = true)
    @Mapping(target = "failureThreshold", ignore = true)
    @Mapping(target = "recoveryThreshold", ignore = true)
    @Mapping(target = "consecutiveFailures", ignore = true)
    @Mapping(target = "consecutiveSuccesses", ignore = true)
    @Mapping(target = "consecutiveDegradations", ignore = true)
    NetworkService toEntity(ServiceCreateRequest serviceCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "heartbeatToken", ignore = true)
    @Mapping(target = "lastHeartbeatAt", ignore = true)
    @Mapping(target = "lastCheckedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "node", ignore = true)
    @Mapping(target = "failureThreshold", ignore = true)
    @Mapping(target = "recoveryThreshold", ignore = true)
    @Mapping(target = "consecutiveFailures", ignore = true)
    @Mapping(target = "consecutiveSuccesses", ignore = true)
    @Mapping(target = "consecutiveDegradations", ignore = true)
    void updateEntityFromDto(
            ServiceUpdateRequest serviceUpdateRequest,
            @MappingTarget NetworkService networkService
    );
}