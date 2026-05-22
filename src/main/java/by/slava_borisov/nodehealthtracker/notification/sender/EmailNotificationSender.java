package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.config.MailProperties;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(
            NotificationMessage message,
            UserNotificationSetting setting
    ) {
        String subject = buildSubject(message);
        String body = buildBody(message);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(mailProperties.getFrom());
        email.setTo(setting.getDestination());
        email.setSubject(subject);
        email.setText(body);

        log.info(
                "Отправка email-уведомления: incidentId={}, serviceId={}, event={}, destination={}",
                message.incidentId(),
                message.serviceId(),
                message.event(),
                setting.getDestination()
        );

        javaMailSender.send(email);

        log.info(
                "Email-уведомление отправлено: incidentId={}, serviceId={}, event={}, destination={}",
                message.incidentId(),
                message.serviceId(),
                message.event(),
                setting.getDestination()
        );
    }

    private String buildSubject(NotificationMessage message) {
        if (message.event() == NotificationEvent.INCIDENT_OPENED) {
            return "Node Health Tracker: открыт инцидент";
        }

        return "Node Health Tracker: инцидент закрыт";
    }

    private String buildBody(NotificationMessage message) {
        String eventName = switch (message.event()) {
            case INCIDENT_OPENED -> "Открыт инцидент";
            case INCIDENT_RESOLVED -> "Инцидент закрыт";
        };

        return """
                %s

                Сервис: %s
                ID сервиса: %d
                ID инцидента: %d
                Причина: %s
                Время события: %s

                Это автоматическое уведомление Node Health Tracker.
                """.formatted(
                eventName,
                message.serviceName(),
                message.serviceId(),
                message.incidentId(),
                message.reason(),
                message.eventTime()
        );
    }
}