package by.slava_borisov.nodehealthtracker.mapper;

import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserRegistrationRequest;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface UserMapper {

    UserProfileResponse toUserProfileResponse(User user);

    UserAdminResponse toUserAdminResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationRequest userRegistrationRequest);
}
