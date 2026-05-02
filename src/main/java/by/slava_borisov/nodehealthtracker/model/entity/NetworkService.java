package by.slava_borisov.nodehealthtracker.model.entity;

import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "network_services")
public class NetworkService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type", nullable = false, length = 20)
    private CheckType checkType;

    @Column(name = "heartbeat_token", unique = true)
    private String heartbeatToken;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "name", length = 150, nullable = false)
    private String name;

    @Column(name = "target_host", nullable = false)
    private String targetHost;

    @Column(name = "port")
    private Integer port;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "interval_seconds", nullable = false)
    private Integer intervalSeconds;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "node_id", nullable = false)
    private NetworkNode node;
}