package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.check.CheckResultResponse;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.service.CheckExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Проверки сервисов",
        description = """
                Ручной запуск проверок сервисов мониторинга и получение истории \
                результатов выполненных проверок.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/checks")
@RequiredArgsConstructor
public class CheckExecutionController {

    private final CheckExecutionService checkExecutionService;

    @Operation(
            summary = "Запустить проверку сервиса",
            description = """
                    Выполняет ручную проверку указанного сервиса мониторинга. \
                    Проверить можно только сервис, принадлежащий текущему пользователю. \
                    После проверки результат сохраняется, а система инцидентов обновляет \
                    состояние открытых или закрытых инцидентов.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Проверка сервиса успешно выполнена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CheckResultResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT-токен отсутствует, недействителен или просрочен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Сервис принадлежит другому пользователю",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T15:20:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Доступ к сервису запрещён",
                                              "path": "/api/checks/services/1/run"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сервис мониторинга не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T15:20:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Сервис мониторинга не найден",
                                              "path": "/api/checks/services/1/run"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка во время выполнения проверки",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PostMapping("/services/{serviceId}/run")
    public CheckResultResponse runCheck(
            @Parameter(
                    description = "Уникальный идентификатор сервиса мониторинга",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        return checkExecutionService.runCheck(serviceId);
    }


    @Operation(
            summary = "Запустить проверки всех включённых сервисов",
            description = """
                    Выполняет проверки всех включённых сервисов в системе. \
                    Метод не ограничивает выборку сервисами текущего пользователя, \
                    поэтому его лучше использовать как административную или техническую операцию.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Проверки включённых сервисов успешно выполнены",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CheckResultResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT-токен отсутствует, недействителен или просрочен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав: требуется роль администратора",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка во время выполнения одной из проверок",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/run-enabled")
    public List<CheckResultResponse> runEnabledChecks() {
        return checkExecutionService.runEnabledChecks();
    }


    @Operation(
            summary = "Получить историю проверок сервиса",
            description = """
                    Возвращает историю проверок указанного сервиса, отсортированную \
                    по времени проверки от новых результатов к старым. \
                    Получить историю можно только для собственного сервиса.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "История проверок успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CheckResultResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT-токен отсутствует, недействителен или просрочен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Сервис принадлежит другому пользователю",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сервис мониторинга не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
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
    @GetMapping("/services/{serviceId}/history")
    public List<CheckResultResponse> getCheckHistory(
            @Parameter(
                    description = "Уникальный идентификатор сервиса мониторинга",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        return checkExecutionService.getCheckHistory(serviceId);
    }
}