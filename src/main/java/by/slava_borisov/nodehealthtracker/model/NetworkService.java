package by.slava_borisov.nodehealthtracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "network_services")
public class NetworkService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type", nullable = false, length = 20)
    private CheckType checkType;

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