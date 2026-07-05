package by.slava_borisov.nodehealthtracker.check.checker;

import by.slava_borisov.nodehealthtracker.check.dto.CheckProbeResult;
import by.slava_borisov.nodehealthtracker.model.entity.NetworkService;
import by.slava_borisov.nodehealthtracker.model.enums.CheckType;
import by.slava_borisov.nodehealthtracker.util.Messages;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

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
            checkSslCertificate(service);
            return CheckProbeResult.success();
        } catch (Exception exception) {
            return CheckProbeResult.sslFailed(exception.getMessage());
        }
    }

    private void checkSslCertificate(NetworkService service) throws Exception {
        String host = service.getTargetHost();
        int port = service.getPort() != null ? service.getPort() : DEFAULT_HTTPS_PORT;

        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (SSLSocket socket = (SSLSocket) socketFactory.createSocket()) {
            socket.connect(new InetSocketAddress(host, port), DEFAULT_TIMEOUT_MS);
            socket.setSoTimeout(DEFAULT_TIMEOUT_MS);

            SSLParameters sslParameters = socket.getSSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
            socket.setSSLParameters(sslParameters);

            socket.startHandshake();

            Certificate[] certificates = socket.getSession().getPeerCertificates();

            if (certificates.length == 0) {
                throw new IllegalStateException(Messages.SSL_CERTIFICATE_NOT_RECEIVED);
            }

            if (!(certificates[0] instanceof X509Certificate certificate)) {
                throw new IllegalStateException(Messages.SSL_CERTIFICATE_UNSUPPORTED_TYPE);
            }

            certificate.checkValidity();
        }
    }
}