package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findAllByStatusOrderByCreatedAtDesc(UserStatus status);

    List<User> findAllByRoleOrderByCreatedAtDesc(RoleName role);

    List<User> findAllByStatusAndRoleOrderByCreatedAtDesc(UserStatus status, RoleName role);

    @Query("""
            SELECT u
            FROM User u
            WHERE (:status IS NULL OR u.status = :status)
              AND (:role IS NULL OR u.role = :role)
              AND (
                    :query IS NULL
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY u.createdAt DESC
            """)
    List<User> findAllByAdminFilters(
            @Param("status") UserStatus status,
            @Param("role") RoleName role,
            @Param("query") String query
    );
}