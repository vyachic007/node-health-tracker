package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {

    List<UserNotificationSetting> findAllByUserIdOrderByChannelAsc(Long userId);

    Optional<UserNotificationSetting> findByUserIdAndChannel(Long userId, NotificationChannel channel);

    List<UserNotificationSetting> findAllByUserIdAndIsEnabledTrue(Long userId);
}