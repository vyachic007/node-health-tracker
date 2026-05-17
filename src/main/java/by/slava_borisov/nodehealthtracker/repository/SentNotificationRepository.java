package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.SentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentNotificationRepository extends JpaRepository<SentNotification, Long> {

    List<SentNotification> findAllByUserIdOrderBySentAtDesc(Long userId);
}
