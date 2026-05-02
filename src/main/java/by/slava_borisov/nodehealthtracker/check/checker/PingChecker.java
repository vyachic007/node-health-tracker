package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class PingChecker implements ServiceChecker {

    private static final int DEFAULT_TIMEOUT_MS = 3000;

    @Override
    public CheckType getSupportedCheckType() {
        return CheckType.PING;
    }

    @Override
    public CheckProbeResult check(NetworkService service) {
        try {
            InetAddress address = InetAddress.getByName(service.getTargetHost());
            boolean reachable = address.isReachable(DEFAULT_TIMEOUT_MS);

            if (reachable) {
                return CheckProbeResult.success();
            }

            return CheckProbeResult.pingFailed(null);
        } catch (Exception exception) {
            return CheckProbeResult.pingFailed(exception.getMessage());
        }
    }
}