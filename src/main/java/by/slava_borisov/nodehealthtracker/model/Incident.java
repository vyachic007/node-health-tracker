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
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IncidentStatus status;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "reason")
    private String reason;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private NetworkService service;

    @ManyToOne
    @JoinColumn(name = "opened_by_check_result_id")
    private CheckResult openedByCheckResult;

    @ManyToOne
    @JoinColumn(name = "closed_by_check_result_id")
    private CheckResult closedByCheckResult;
}