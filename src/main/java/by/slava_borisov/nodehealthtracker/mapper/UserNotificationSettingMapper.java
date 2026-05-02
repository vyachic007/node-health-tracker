package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingUpdateRequest;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserNotificationSettingMapper {

    @Mapping(source = "user.id", target = "userId")
    NotificationSettingResponse toResponse(UserNotificationSetting entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserNotificationSetting toEntity(NotificationSettingCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(NotificationSettingUpdateRequest request,
                      @MappingTarget UserNotificationSetting entity);
}