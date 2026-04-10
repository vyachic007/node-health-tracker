package by.slava_borisov.nodehealthtracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "check_results")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private NetworkService service;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private ServiceStatus status;
}
