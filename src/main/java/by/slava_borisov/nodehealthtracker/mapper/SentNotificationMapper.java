package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.notification.SentNotificationResponse;
import by.slava_borisov.nodehealthtracker.model.entity.SentNotification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface SentNotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "incident.id", target = "incidentId")
    SentNotificationResponse toSentNotificationResponse(SentNotification sentNotification);
}
