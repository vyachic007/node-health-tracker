package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecoveryChecklistResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentRecurrenceAnalysisResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentReportResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentResponse;
import by.slava_borisov.nodehealthtracker.dto.incident.IncidentTimelineEventResponse;
import by.slava_borisov.nodehealthtracker.service.IncidentRecoveryChecklistService;
import by.slava_borisov.nodehealthtracker.service.IncidentRecurrenceAnalysisService;
import by.slava_borisov.nodehealthtracker.service.IncidentReportService;
import by.slava_borisov.nodehealthtracker.service.IncidentService;
import by.slava_borisov.nodehealthtracker.service.IncidentTimelineService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Инциденты",
        description = """
                Просмотр, закрытие и диагностическое сопровождение инцидентов: \
                timeline, recovery checklist, отчёт и анализ повторяемости.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;
    private final IncidentTimelineService incidentTimelineService;
    private final IncidentRecoveryChecklistService incidentRecoveryChecklistService;
    private final IncidentReportService incidentReportService;
    private final IncidentRecurrenceAnalysisService incidentRecurrenceAnalysisService;

    @Operation(
            summary = "Получить инцидент",
            description = "Возвращает инцидент по идентификатору. Получить можно только инцидент собственного сервиса."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Инцидент успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT-токен отсутствует, недействителен или просрочен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Инцидент принадлежит сервису другого пользователя",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T16:00:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Доступ к инциденту запрещён",
                                              "path": "/api/incidents/1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Инцидент не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T16:00:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Инцидент не найден",
                                              "path": "/api/incidents/1"
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
    @GetMapping("/{incidentId}")
    public IncidentResponse getIncidentById(
            @Parameter(
                    description = "Уникальный идентификатор инцидента",
                    example = "1",
                    required = true
            )
            @PathVariable Long incidentId
    ) {
        return incidentService.getIncidentById(incidentId);
    }

    @Operation(
            summary = "Получить timeline инцидента",
            description = """
                    Возвращает хронологию событий инцидента: открытие, повторные проверки, \
                    изменение состояния и закрытие.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Timeline инцидента успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = IncidentTimelineEventResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Инцидент принадлежит сервису другого пользователя",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Инцидент не найден",
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
    @GetMapping("/{incidentId}/timeline")
    public List<IncidentTimelineEventResponse> getIncidentTimeline(
            @Parameter(description = "Уникальный идентификатор инцидента", example = "1", required = true)
            @PathVariable Long incidentId
    ) {
        return incidentTimelineService.getIncidentTimeline(incidentId);
    }

    @Operation(
            summary = "Получить recovery checklist",
            description = """
                    Возвращает чек-лист восстановления по инциденту. \
                    Список шагов формируется на основе уровня сбоя: DNS, NETWORK, PORT, SSL, \
                    APPLICATION, PERFORMANCE, HEARTBEAT или UNKNOWN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recovery checklist успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentRecoveryChecklistResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Инцидент принадлежит сервису другого пользователя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Инцидент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{incidentId}/recovery-checklist")
    public IncidentRecoveryChecklistResponse getRecoveryChecklist(
            @Parameter(description = "Уникальный идентификатор инцидента", example = "1", required = true)
            @PathVariable Long incidentId
    ) {
        return incidentRecoveryChecklistService.getRecoveryChecklist(incidentId);
    }

    @Operation(
            summary = "Получить отчёт по инциденту",
            description = """
                    Возвращает сводный отчёт по инциденту: статус, severity, уровень сбоя, \
                    причину, рекомендацию, длительность, связанные проверки и краткое резюме.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Отчёт по инциденту успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentReportResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Инцидент принадлежит сервису другого пользователя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Инцидент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{incidentId}/report")
    public IncidentReportResponse getIncidentReport(
            @Parameter(description = "Уникальный идентификатор инцидента", example = "1", required = true)
            @PathVariable Long incidentId
    ) {
        return incidentReportService.getIncidentReport(incidentId);
    }

    @Operation(
            summary = "Получить анализ повторяемости инцидента",
            description = """
                    Анализирует количество похожих инцидентов за 24 часа, 7 дней и 30 дней. \
                    Похожесть определяется по сервису и уровню сбоя.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Анализ повторяемости успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentRecurrenceAnalysisResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Инцидент принадлежит сервису другого пользователя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Инцидент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{incidentId}/recurrence-analysis")
    public IncidentRecurrenceAnalysisResponse getIncidentRecurrenceAnalysis(
            @Parameter(description = "Уникальный идентификатор инцидента", example = "1", required = true)
            @PathVariable Long incidentId
    ) {
        return incidentRecurrenceAnalysisService.analyzeRecurrence(incidentId);
    }

    @Operation(
            summary = "Получить все инциденты текущего пользователя",
            description = "Возвращает все инциденты сервисов текущего пользователя, отсортированные по дате открытия от новых к старым."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список инцидентов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = IncidentResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/my")
    public List<IncidentResponse> getCurrentUserIncidents() {
        return incidentService.getCurrentUserIncidents();
    }

    @Operation(
            summary = "Получить инциденты сервиса",
            description = "Возвращает все инциденты указанного сервиса, если сервис принадлежит текущему пользователю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список инцидентов сервиса успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = IncidentResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Сервис принадлежит другому пользователю",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сервис мониторинга не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/services/{serviceId}")
    public List<IncidentResponse> getServiceIncidents(
            @Parameter(description = "Уникальный идентификатор сервиса мониторинга", example = "1", required = true)
            @PathVariable Long serviceId
    ) {
        return incidentService.getServiceIncidents(serviceId);
    }

    @Operation(
            summary = "Закрыть инцидент вручную",
            description = """
                    Переводит открытый инцидент в статус RESOLVED. \
                    Если инцидент уже закрыт, возвращается ошибка 400.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Инцидент успешно закрыт",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IncidentResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Инцидент уже закрыт",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T16:00:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Инцидент уже закрыт",
                                              "path": "/api/incidents/1/close"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "Инцидент принадлежит сервису другого пользователя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Инцидент не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping("/{incidentId}/close")
    public IncidentResponse closeIncident(
            @Parameter(description = "Уникальный идентификатор закрываемого инцидента", example = "1", required = true)
            @PathVariable Long incidentId
    ) {
        return incidentService.closeIncident(incidentId);
    }
}