package by.slava_borisov.nodehealthtracker.repository;

import by.slava_borisov.nodehealthtracker.model.entity.TelegramBindingToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramBindingTokenRepository extends JpaRepository<TelegramBindingToken, Long> {

    Optional<TelegramBindingToken> findByToken(String token);
}