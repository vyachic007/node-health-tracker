package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Component
public class HttpChecker implements ServiceChecker {

    private static final int DEFAULT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_HTTP_PORT = 80;

    @Override
    public CheckType getSupportedCheckType() {
        return CheckType.HTTP;
    }

    @Override
    public CheckProbeResult check(NetworkService service) {
        try {
            Integer httpStatusCode = executeHttpRequest(service, "http", DEFAULT_HTTP_PORT);
            return CheckProbeResult.httpResult(httpStatusCode);
        } catch (Exception exception) {
            return CheckProbeResult.httpFailed(exception.getMessage());
        }
    }

    private Integer executeHttpRequest(
            NetworkService service,
            String protocol,
            int defaultPort
    ) throws Exception {
        Integer port = service.getPort() != null ? service.getPort() : defaultPort;
        String path = resolvePath(service.getPath());

        URI uri = new URI(
                protocol,
                null,
                service.getTargetHost(),
                port,
                path,
                null,
                null
        );

        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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