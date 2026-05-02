package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URL;

@Component
public class SslChecker implements ServiceChecker {

    private static final int DEFAULT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_HTTPS_PORT = 443;

    @Override
    public CheckType getSupportedCheckType() {
        return CheckType.SSL;
    }

    @Override
    public CheckProbeResult check(NetworkService service) {
        try {
            Integer httpStatusCode = executeSslRequest(service);
            return CheckProbeResult.httpResult(httpStatusCode);
        } catch (Exception exception) {
            return CheckProbeResult.sslFailed(exception.getMessage());
        }
    }

    private Integer executeSslRequest(NetworkService service) throws Exception {
        Integer port = service.getPort() != null ? service.getPort() : DEFAULT_HTTPS_PORT;
        String path = resolvePath(service.getPath());

        URI uri = new URI(
                "https",
                null,
                service.getTargetHost(),
                port,
                path,
                null,
                null
        );

        URL url = uri.toURL();

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
        connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
        connection.setRequestMethod("GET");

        return connection.getResponseCode();
    }

    private String resolvePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        return path.startsWith("/") ? path : "/" + path;
    }
}