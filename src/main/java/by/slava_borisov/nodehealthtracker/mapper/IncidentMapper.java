package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.model.entity.Incident;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface IncidentMapper {

    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "openedByCheckResult.id", target = "openedByCheckResultId")
    @Mapping(source = "closedByCheckResult.id", target = "closedByCheckResultId")
    IncidentResponse toIncidentResponse(Incident incident);
}
