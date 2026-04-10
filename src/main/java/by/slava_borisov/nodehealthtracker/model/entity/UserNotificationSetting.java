package by.slava_borisov.nodehealthtracker.model.entity;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_notification_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel"})
)
public class UserNotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "destination")
    private String destination;

    @Column(name = "notify_on_incident_open", nullable = false)
    private Boolean notifyOnIncidentOpen;

    @Column(name = "notify_on_incident_resolved", nullable = false)
    private Boolean notifyOnIncidentResolved;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}