package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;

@Component
public class TcpChecker implements ServiceChecker {

    private static final int DEFAULT_TIMEOUT_MS = 3000;

    @Override
    public CheckType getSupportedCheckType() {
        return CheckType.TCP;
    }

    @Override
    public CheckProbeResult check(NetworkService service) {
        if (service.getPort() == null) {
            return CheckProbeResult.tcpFailed(Messages.TCP_PORT_REQUIRED);
        }

        try (Socket socket = new Socket()) {
            socket.connect(
                    new InetSocketAddress(service.getTargetHost(), service.getPort()),
                    DEFAULT_TIMEOUT_MS
            );

            return CheckProbeResult.success();
        } catch (Exception exception) {
            return CheckProbeResult.tcpFailed(exception.getMessage());
        }
    }
}