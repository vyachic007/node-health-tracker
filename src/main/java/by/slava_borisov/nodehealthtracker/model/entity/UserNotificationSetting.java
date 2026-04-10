package by.slava_borisov.nodehealthtracker.model.entity;

import by.slava_borisov.nodehealthtracker.model.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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