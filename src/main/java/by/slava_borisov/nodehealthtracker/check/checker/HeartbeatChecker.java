package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class HeartbeatChecker implements ServiceChecker {

    private static final int HEARTBEAT_TIMEOUT_MULTIPLIER = 2;

    @Override
    public CheckType getSupportedCheckType() {
        return CheckType.HEARTBEAT;
    }

    @Override
    public CheckProbeResult check(NetworkService service) {
        if (service.getLastHeartbeatAt() == null) {
            return CheckProbeResult.heartbeatFailed(Messages.HEARTBEAT_NOT_RECEIVED);
        }

        LocalDateTime heartbeatDeadline = LocalDateTime.now()
                .minusSeconds((long) service.getIntervalSeconds() * HEARTBEAT_TIMEOUT_MULTIPLIER);

        if (service.getLastHeartbeatAt().isAfter(heartbeatDeadline)) {
            return CheckProbeResult.success();
        }

        return CheckProbeResult.heartbeatFailed(Messages.HEARTBEAT_EXPIRED);
    }
}