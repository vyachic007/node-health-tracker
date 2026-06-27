package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.heartbeat.HeartbeatResponse;
import by.slava_borisov.nodehealthtracker.service.HeartbeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Heartbeat",
        description = """
                Приём heartbeat-сигналов от внешних сервисов, агентов или скриптов. \
                Endpoint использует heartbeat-токен сервиса и не требует JWT-авторизации.
                """
)
@RestController
@RequestMapping("/api/heartbeat")
@RequiredArgsConstructor
public class HeartbeatController {

    private final HeartbeatService heartbeatService;

    @Operation(
            summary = "Принять heartbeat-сигнал",
            description = """
                    Принимает heartbeat-сигнал по уникальному токену сервиса. \
                    Если токен найден, система обновляет время последнего heartbeat \
                    и возвращает информацию о сервисе.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Heartbeat-сигнал успешно принят",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HeartbeatResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Heartbeat-токен не передан или пустой",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T15:40:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Heartbeat-токен обязателен",
                                              "path": "/api/heartbeat/"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сервис с указанным heartbeat-токеном не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T15:40:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Heartbeat-токен не найден",
                                              "path": "/api/heartbeat/550e8400-e29b-41d4-a716-446655440000"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/{token}")
    public HeartbeatResponse acceptHeartbeat(
            @Parameter(
                    description = "Уникальный heartbeat-токен сервиса",
                    example = "550e8400-e29b-41d4-a716-446655440000",
                    required = true
            )
            @PathVariable String token
    ) {
        return heartbeatService.acceptHeartbeat(token);
    }
}