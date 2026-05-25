package by.slava_borisov.nodehealthtracker.notification.sender;

import by.slava_borisov.nodehealthtracker.config.MailProperties;
import by.slava_borisov.nodehealthtracker.model.entity.UserNotificationSetting;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import by.slava_borisov.nodehealthtracker.model.enums.NotificationEvent;
import by.slava_borisov.nodehealthtracker.notification.dto.NotificationMessage;
import by.slava_borisov.nodehealthtracker.util.DateTimeUtils;
import by.slava_borisov.nodehealthtracker.util.Messages;
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
            return Messages.EMAIL_INCIDENT_OPENED_SUBJECT;
        }

        return Messages.EMAIL_INCIDENT_RESOLVED_SUBJECT;
    }

    private String buildBody(NotificationMessage message) {
        String eventName = switch (message.event()) {
            case INCIDENT_OPENED -> Messages.EMAIL_INCIDENT_OPENED_TITLE;
            case INCIDENT_RESOLVED -> Messages.EMAIL_INCIDENT_RESOLVED_TITLE;
        };

        return Messages.EMAIL_NOTIFICATION_TEXT.formatted(
                eventName,
                message.serviceName(),
                message.checkType(),
                formatNullable(message.targetHost()),
                formatNullable(message.port()),
                formatNullable(message.path()),
                message.serviceId(),
                message.incidentId(),
                message.reason(),
                DateTimeUtils.formatMoscowDateTime(message.eventTime())
        );
    }

    private String formatNullable(Object value) {
        if (value == null) {
            return "-";
        }

        return value.toString();
    }
}