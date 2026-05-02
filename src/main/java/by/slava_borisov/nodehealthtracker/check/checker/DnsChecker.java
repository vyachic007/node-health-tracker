package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class DnsChecker implements ServiceChecker {

    @Override
    public CheckType getSupportedCheckType() {
        return CheckType.DNS;
    }

    @Override
    public CheckProbeResult check(NetworkService service) {
        try {
            InetAddress.getByName(service.getTargetHost());
            return CheckProbeResult.success();
        } catch (Exception exception) {
            return CheckProbeResult.dnsFailed(exception.getMessage());
        }
    }
}