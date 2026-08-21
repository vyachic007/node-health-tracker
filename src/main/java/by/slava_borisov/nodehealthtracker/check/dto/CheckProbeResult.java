package by.slava_borisov.nodehealthtracker.check.dto;

public record CheckProbeResult(

        boolean dnsAvailable,

        boolean pingAvailable,

        boolean tcpAvailable,

        boolean sslValid,

        Integer httpStatusCode,

        String errorMessage
) {

    public static CheckProbeResult success() {
        return new CheckProbeResult(
                true,
                true,
                true,
                true,
                null,
                null
        );
    }

    public static CheckProbeResult dnsFailed(String errorMessage) {
        return new CheckProbeResult(
                false,
                true,
                true,
                true,
                null,
                errorMessage
        );
    }

    public static CheckProbeResult pingFailed(String errorMessage) {
        return new CheckProbeResult(
                true,
                false,
                true,
                true,
                null,
                errorMessage
        );
    }

    public static CheckProbeResult tcpFailed(String errorMessage) {
        return new CheckProbeResult(
                true,
                true,
                false,
                true,
                null,
                errorMessage
        );
    }

    public static CheckProbeResult sslFailed(String errorMessage) {
        return new CheckProbeResult(
                true,
                true,
                true,
                false,
                null,
                errorMessage
        );
    }

    public static CheckProbeResult httpResult(Integer httpStatusCode) {
        return new CheckProbeResult(
                true,
                true,
                true,
                true,
                httpStatusCode,
                null
        );
    }

    public static CheckProbeResult httpFailed(String errorMessage) {
        return new CheckProbeResult(
                true,
                true,
                false,
                true,
                null,
                errorMessage
        );
    }
}