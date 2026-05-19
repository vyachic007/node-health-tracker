package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.CheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {

    List<CheckResult> findAllByServiceIdOrderByCheckedAtDesc(Long serviceId);

    Optional<CheckResult> findTopByServiceIdOrderByCheckedAtDesc(Long serviceId);

    long countByCheckedAtAfter(LocalDateTime checkedAt);

    long countByServiceNodeOwnerIdAndCheckedAtAfter(Long ownerId, LocalDateTime checkedAt);
}