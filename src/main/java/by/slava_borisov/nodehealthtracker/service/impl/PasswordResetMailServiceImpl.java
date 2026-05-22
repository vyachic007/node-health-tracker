package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.config.MailProperties;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.service.PasswordResetMailService;
import by.slava_borisov.nodehealthtracker.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetMailServiceImpl implements PasswordResetMailService {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Override
    public void sendPasswordResetToken(
            User user,
            String rawToken,
            LocalDateTime expiresAt
    ) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(mailProperties.getFrom());
        email.setTo(user.getEmail());
        email.setSubject(Messages.PASSWORD_RESET_EMAIL_SUBJECT);
        email.setText(buildBody(user, rawToken, expiresAt));

        log.info(
                "Отправка email для восстановления пароля: userId={}, username={}, email={}",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

        javaMailSender.send(email);

        log.info(
                "Email для восстановления пароля отправлен: userId={}, username={}, email={}",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    private String buildBody(
            User user,
            String rawToken,
            LocalDateTime expiresAt
    ) {
        return Messages.PASSWORD_RESET_EMAIL_BODY.formatted(
                user.getUsername(),
                rawToken,
                expiresAt
        );
    }
}