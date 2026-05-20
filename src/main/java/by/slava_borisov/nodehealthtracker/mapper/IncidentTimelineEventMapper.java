package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
import by.slava_borisov.nodehealthtracker.model.entity.IncidentTimelineEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface IncidentTimelineEventMapper {

    @Mapping(target = "incidentId", source = "incident.id")
    @Mapping(target = "checkResultId", source = "checkResult.id")
    IncidentTimelineEventResponse toResponse(IncidentTimelineEvent incidentTimelineEvent);
}