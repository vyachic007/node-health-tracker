package by.slava_borisov.nodehealthtracker.model.entity;

import by.slava_borisov.nodehealthtracker.model.enums.IncidentStatus;
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