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

    @Mapping(source = "node.id", target = "nodeId")
    ServiceResponse toServiceResponse(NetworkService networkService);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "heartbeatToken", ignore = true)
    @Mapping(target = "lastHeartbeatAt", ignore = true)
    @Mapping(target = "lastCheckedAt", ignore = true)
    @Mapping(target = "isEnabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "node", ignore = true)
    NetworkService toEntity(ServiceCreateRequest serviceCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "heartbeatToken", ignore = true)
    @Mapping(target = "lastHeartbeatAt", ignore = true)
    @Mapping(target = "lastCheckedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "node", ignore = true)
    void updateEntityFromDto(
            ServiceUpdateRequest serviceUpdateRequest,
            @MappingTarget NetworkService networkService
    );
}