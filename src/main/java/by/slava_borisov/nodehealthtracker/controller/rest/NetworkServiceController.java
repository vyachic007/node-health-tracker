package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceHealthScoreResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceResponse;
import by.slava_borisov.nodehealthtracker.dto.service.ServiceUpdateRequest;
import by.slava_borisov.nodehealthtracker.service.NetworkServiceService;
import by.slava_borisov.nodehealthtracker.service.ServiceHealthScoreService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

        import java.util.List;

@Tag(
        name = "Сетевые сервисы",
        description = """
                Управление сервисами мониторинга, их состоянием, настройками проверок \
                и интегральной оценкой здоровья.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class NetworkServiceController {

    private final NetworkServiceService networkServiceService;
    private final ServiceHealthScoreService serviceHealthScoreService;

    @Operation(
            summary = "Создать сервис мониторинга",
            description = """
                    Создаёт новый сервис мониторинга внутри указанного сетевого узла. \
                    Сервис автоматически создаётся во включённом состоянии. \
                    Для проверки типа HEARTBEAT автоматически генерируется уникальный токен.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис мониторинга успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Переданные данные не прошли валидацию",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T14:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "name: must not be blank",
                                              "path": "/api/services"
                                            }
                                            """
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
                    description = "Указанный сетевой узел принадлежит другому пользователю",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Указанный сетевой узел не найден",
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
    @PostMapping
    public ServiceResponse createService(
            @Valid @RequestBody ServiceCreateRequest request
    ) {
        return networkServiceService.createService(request);
    }


    @Operation(
            summary = "Изменить сервис мониторинга",
            description = """
                    Изменяет тип проверки, адрес назначения, интервал, пороги, \
                    каналы уведомлений и активное состояние сервиса. \
                    Изменять можно только собственные сервисы.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис мониторинга успешно изменён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Переданные данные не прошли валидацию",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
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
    @PutMapping("/{serviceId}")
    public ServiceResponse updateService(
            @Parameter(
                    description = "Уникальный идентификатор сервиса мониторинга",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId,

            @Valid @RequestBody ServiceUpdateRequest request
    ) {
        return networkServiceService.updateService(serviceId, request);
    }


    @Operation(
            summary = "Получить оценку здоровья сервиса",
            description = """
                    Рассчитывает интегральную оценку здоровья сервиса от 0 до 100. \
                    При расчёте учитываются последний статус, открытый инцидент, \
                    его серьёзность, доступность, время ответа и повторяемость сбоев.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Оценка здоровья успешно рассчитана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceHealthScoreResponse.class)
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
    @GetMapping("/{serviceId}/health-score")
    public ServiceHealthScoreResponse getServiceHealthScore(
            @Parameter(
                    description = "Уникальный идентификатор сервиса мониторинга",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        return serviceHealthScoreService.calculateHealthScore(serviceId);
    }


    @Operation(
            summary = "Удалить сервис мониторинга",
            description = "Удаляет сервис мониторинга, принадлежащий текущему пользователю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис мониторинга успешно удалён",
                    content = @Content
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
    @DeleteMapping("/{serviceId}")
    public void deleteService(
            @Parameter(
                    description = "Уникальный идентификатор удаляемого сервиса",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        networkServiceService.deleteService(serviceId);
    }


    @Operation(
            summary = "Получить сервис мониторинга",
            description = """
                    Возвращает сервис вместе с результатами последней проверки, \
                    состоянием инцидента, доступностью, временем ответа \
                    и рассчитанной оценкой здоровья.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис мониторинга успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceResponse.class)
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
    @GetMapping("/{serviceId}")
    public ServiceResponse getServiceById(
            @Parameter(
                    description = "Уникальный идентификатор сервиса мониторинга",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        return networkServiceService.getServiceById(serviceId);
    }


    @Operation(
            summary = "Получить сервисы сетевого узла",
            description = """
                    Возвращает все сервисы указанного сетевого узла, \
                    отсортированные по дате создания от новых к старым.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список сервисов узла успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ServiceResponse.class)
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
                    description = "Сетевой узел принадлежит другому пользователю",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сетевой узел не найден",
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
    @GetMapping("/node/{nodeId}")
    public List<ServiceResponse> getServicesByNodeId(
            @Parameter(
                    description = "Уникальный идентификатор сетевого узла",
                    example = "1",
                    required = true
            )
            @PathVariable Long nodeId
    ) {
        return networkServiceService.getServicesByNodeId(nodeId);
    }


    @Operation(
            summary = "Получить все сервисы текущего пользователя",
            description = """
                    Возвращает сервисы всех сетевых узлов текущего пользователя, \
                    отсортированные по дате создания от новых к старым.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список сервисов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = ServiceResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "JWT-токен отсутствует, недействителен или просрочен",
                    content = @Content
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
    @GetMapping("/my")
    public List<ServiceResponse> getCurrentUserServices() {
        return networkServiceService.getCurrentUserServices();
    }


    @Operation(
            summary = "Включить сервис мониторинга",
            description = """
                    Включает выполнение автоматических проверок сервиса. \
                    Повторное включение уже включённого сервиса не считается ошибкой.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис мониторинга включён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceResponse.class)
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
    @PostMapping("/{serviceId}/enable")
    public ServiceResponse enableService(
            @Parameter(
                    description = "Уникальный идентификатор включаемого сервиса",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        return networkServiceService.enableService(serviceId);
    }


    @Operation(
            summary = "Отключить сервис мониторинга",
            description = """
                    Отключает выполнение автоматических проверок сервиса. \
                    Повторное отключение уже отключённого сервиса не считается ошибкой.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сервис мониторинга отключён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceResponse.class)
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
    @PostMapping("/{serviceId}/disable")
    public ServiceResponse disableService(
            @Parameter(
                    description = "Уникальный идентификатор отключаемого сервиса",
                    example = "1",
                    required = true
            )
            @PathVariable Long serviceId
    ) {
        return networkServiceService.disableService(serviceId);
    }
}