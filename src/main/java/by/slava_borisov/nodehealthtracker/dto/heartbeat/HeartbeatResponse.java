package by.slava_borisov.nodehealthtracker.dto.heartbeat;

import java.time.LocalDateTime;

public record HeartbeatResponse(

        Long serviceId,

        String serviceName,

        LocalDateTime lastHeartbeatAt,

        String message
) {
}