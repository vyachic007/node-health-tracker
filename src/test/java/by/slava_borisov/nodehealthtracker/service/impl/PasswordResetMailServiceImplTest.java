package by.slava_borisov.nodehealthtracker.service.impl;

import by.slava_borisov.nodehealthtracker.config.MailProperties;
import by.slava_borisov.nodehealthtracker.model.entity.User;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты PasswordResetMailServiceImpl")
class PasswordResetMailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private PasswordResetMailServiceImpl passwordResetMailService;

    private User user;
    private String rawToken;
    private LocalDateTime expiresAt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        rawToken = "secure-reset-token-123";
        expiresAt = LocalDateTime.now().plusHours(1);

        when(mailProperties.getFrom()).thenReturn("noreply@nodehealthtracker.com");
    }

    @Test
    @DisplayName("Отправка email для сброса пароля - успешно")
    void sendPasswordResetToken_success() {
        passwordResetMailService.sendPasswordResetToken(user, rawToken, expiresAt);

        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(emailCaptor.capture());

        SimpleMailMessage sentEmail = emailCaptor.getValue();

        assertEquals("noreply@nodehealthtracker.com", sentEmail.getFrom());
        assertEquals("test@example.com", sentEmail.getTo()[0]);
        assertEquals(Messages.PASSWORD_RESET_EMAIL_SUBJECT, sentEmail.getSubject());

        String expectedBody = Messages.PASSWORD_RESET_EMAIL_BODY.formatted(
                user.getUsername(),
                rawToken,
                expiresAt
        );
        assertEquals(expectedBody, sentEmail.getText());
    }
}