package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    long countByRole(RoleName role);

    @Query(
            value = """
                SELECT u
                FROM User u
                WHERE (:status IS NULL OR u.status = :status)
                  AND (:role IS NULL OR u.role = :role)
                  AND (
                        :query = ''
                        OR LOWER(u.username) LIKE CONCAT('%', LOWER(:query), '%')
                        OR LOWER(u.email) LIKE CONCAT('%', LOWER(:query), '%')
                  )
                """,
            countQuery = """
                SELECT COUNT(u)
                FROM User u
                WHERE (:status IS NULL OR u.status = :status)
                  AND (:role IS NULL OR u.role = :role)
                  AND (
                        :query = ''
                        OR LOWER(u.username) LIKE CONCAT('%', LOWER(:query), '%')
                        OR LOWER(u.email) LIKE CONCAT('%', LOWER(:query), '%')
                  )
                """
    )
    Page<User> findAllByAdminFilters(
            @Param("status") UserStatus status,
            @Param("role") RoleName role,
            @Param("query") String query,
            Pageable pageable
    );
}