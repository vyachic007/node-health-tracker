package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.node.NodeResponse;
import by.slava_borisov.nodehealthtracker.dto.node.NodeUpdateRequest;
import by.slava_borisov.nodehealthtracker.service.NetworkNodeService;
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
        name = "Сетевые узлы",
        description = """
                Создание, просмотр, изменение и удаление сетевых узлов \
                авторизованного пользователя.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class NetworkNodeController {

    private final NetworkNodeService networkNodeService;

    @Operation(
            summary = "Создать сетевой узел",
            description = """
                    Создаёт новый сетевой узел для текущего авторизованного пользователя. \
                    Созданный узел автоматически получает активное состояние.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сетевой узел успешно создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NodeResponse.class)
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
                                              "timestamp": "2026-06-25T20:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "name: must not be blank",
                                              "path": "/api/nodes"
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
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:30:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/nodes"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public NodeResponse createNode(
            @Valid @RequestBody NodeCreateRequest request
    ) {
        return networkNodeService.createNode(request);
    }


    @Operation(
            summary = "Изменить сетевой узел",
            description = """
                    Изменяет название, адрес, описание и активное состояние сетевого узла. \
                    Изменять можно только узлы текущего авторизованного пользователя.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сетевой узел успешно изменён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NodeResponse.class)
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
                                              "timestamp": "2026-06-25T20:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "host: must not be blank",
                                              "path": "/api/nodes/1"
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
                    description = "Сетевой узел принадлежит другому пользователю",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:30:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Доступ к сетевому узлу запрещён",
                                              "path": "/api/nodes/1"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Сетевой узел не найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:30:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Сетевой узел не найден",
                                              "path": "/api/nodes/1"
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
    @PutMapping("/{nodeId}")
    public NodeResponse updateNode(
            @Parameter(
                    description = "Уникальный идентификатор сетевого узла",
                    example = "1",
                    required = true
            )
            @PathVariable Long nodeId,

            @Valid @RequestBody NodeUpdateRequest request
    ) {
        return networkNodeService.updateNode(nodeId, request);
    }


    @Operation(
            summary = "Удалить сетевой узел",
            description = """
                    Удаляет сетевой узел текущего авторизованного пользователя. \
                    Вместе с узлом могут быть удалены связанные с ним данные \
                    в соответствии с настройками связей базы данных.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сетевой узел успешно удалён",
                    content = @Content
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
    @DeleteMapping("/{nodeId}")
    public void deleteNode(
            @Parameter(
                    description = "Уникальный идентификатор удаляемого сетевого узла",
                    example = "1",
                    required = true
            )
            @PathVariable Long nodeId
    ) {
        networkNodeService.deleteNode(nodeId);
    }


    @Operation(
            summary = "Получить сетевой узел",
            description = """
                    Возвращает сетевой узел вместе с агрегированной информацией \
                    о сервисах, инцидентах, доступности и состоянии здоровья. \
                    Получить можно только собственный узел.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сетевой узел успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NodeResponse.class)
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
    @GetMapping("/{nodeId}")
    public NodeResponse getNodeById(
            @Parameter(
                    description = "Уникальный идентификатор сетевого узла",
                    example = "1",
                    required = true
            )
            @PathVariable Long nodeId
    ) {
        return networkNodeService.getNodeById(nodeId);
    }


    @Operation(
            summary = "Получить сетевые узлы текущего пользователя",
            description = """
                    Возвращает все сетевые узлы текущего авторизованного пользователя. \
                    Узлы сортируются по дате создания от новых к старым.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список сетевых узлов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = NodeResponse.class)
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
    public List<NodeResponse> getCurrentUserNodes() {
        return networkNodeService.getCurrentUserNodes();
    }
}