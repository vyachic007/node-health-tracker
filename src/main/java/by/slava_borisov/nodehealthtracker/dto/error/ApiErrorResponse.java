package by.slava_borisov.nodehealthtracker.dto.error;

import java.time.LocalDateTime;

public record ApiErrorResponse(

        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        String path
) {
}