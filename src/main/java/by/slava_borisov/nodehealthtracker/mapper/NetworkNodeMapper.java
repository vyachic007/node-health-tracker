package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface NetworkNodeMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "healthStatus", ignore = true)
    @Mapping(target = "totalServices", ignore = true)
    @Mapping(target = "enabledServices", ignore = true)
    @Mapping(target = "disabledServices", ignore = true)
    @Mapping(target = "upServices", ignore = true)
    @Mapping(target = "downServices", ignore = true)
    @Mapping(target = "unknownServices", ignore = true)
    @Mapping(target = "openIncidents", ignore = true)
    @Mapping(target = "lastCheckedAt", ignore = true)
    @Mapping(target = "availabilityPercent24h", ignore = true)
    @Mapping(target = "averageResponseTimeMs24h", ignore = true)
    NodeResponse toNodeResponse(NetworkNode networkNode);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    NetworkNode toEntity(NodeCreateRequest nodeCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updateEntityFromDto(
            NodeUpdateRequest nodeUpdateRequest,
            @MappingTarget NetworkNode networkNode
    );
}