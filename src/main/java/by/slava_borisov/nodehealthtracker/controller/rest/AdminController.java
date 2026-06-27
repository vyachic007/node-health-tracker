package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.admin.AdminPlatformSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserAdminSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.admin.UserBlockRequest;
import by.slava_borisov.nodehealthtracker.dto.admin.UserRoleUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.common.PageResponse;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.model.enums.RoleName;
import by.slava_borisov.nodehealthtracker.model.enums.UserStatus;
import by.slava_borisov.nodehealthtracker.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Администрирование: пользователи",
        description = """
                Административное управление пользователями платформы: просмотр, фильтрация, \
                блокировка, разблокировка, изменение ролей и получение системных сводок.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Получить список пользователей",
            description = """
                    Возвращает страницу пользователей с фильтрацией по статусу, роли \
                    и поисковой строке. Endpoint доступен только пользователю с ролью ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры пагинации",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T17:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Некорректный размер страницы",
                                              "path": "/api/admin/users"
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
                    description = "Недостаточно прав: требуется роль администратора",
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
    @GetMapping("/users")
    public PageResponse<UserAdminResponse> getAllUsers(
            @Parameter(
                    description = "Фильтр по статусу пользователя",
                    example = "ACTIVE",
                    schema = @Schema(allowableValues = {"ACTIVE", "BLOCKED"})
            )
            @RequestParam(required = false) UserStatus status,

            @Parameter(
                    description = "Фильтр по роли пользователя",
                    example = "ROLE_USER",
                    schema = @Schema(allowableValues = {"ROLE_USER", "ROLE_ADMIN"})
            )
            @RequestParam(name = "role", required = false) RoleName role,

            @Parameter(
                    description = "Поиск по email или имени пользователя",
                    example = "admin"
            )
            @RequestParam(required = false) String query,

            @Parameter(
                    description = "Номер страницы, начиная с 0",
                    example = "0"
            )
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Размер страницы от 1 до 100",
                    example = "20"
            )
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminService.getAllUsers(status, role, query, page, size);
    }

    @Operation(
            summary = "Получить сводку пользователей",
            description = "Возвращает количество пользователей по статусам и ролям. Endpoint доступен только ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сводка пользователей успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserAdminSummaryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль администратора", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/summary")
    public UserAdminSummaryResponse getUserSummary() {
        return adminService.getUserSummary();
    }

    @Operation(
            summary = "Получить платформенную сводку",
            description = """
                    Возвращает общую административную сводку по пользователям, узлам, сервисам, \
                    инцидентам и проверкам за последние 24 часа.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Платформенная сводка успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminPlatformSummaryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль администратора", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/summary")
    public AdminPlatformSummaryResponse getPlatformSummary() {
        return adminService.getPlatformSummary();
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает административную карточку пользователя по идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserAdminResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль администратора", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/users/{userId}")
    public UserAdminResponse getUserById(
            @Parameter(description = "Уникальный идентификатор пользователя", example = "3", required = true)
            @PathVariable Long userId
    ) {
        return adminService.getUserById(userId);
    }

    @Operation(
            summary = "Изменить статус пользователя",
            description = """
                    Изменяет статус пользователя на ACTIVE или BLOCKED. \
                    Администратор не может заблокировать самого себя.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус пользователя успешно изменён",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserAdminResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или попытка администратора заблокировать самого себя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль администратора", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/users/{userId}/status")
    public UserAdminResponse updateUserStatus(
            @Parameter(description = "Уникальный идентификатор пользователя", example = "3", required = true)
            @PathVariable Long userId,

            @Valid @RequestBody UserBlockRequest request
    ) {
        return adminService.updateUserStatus(userId, request);
    }

    @Operation(
            summary = "Изменить роль пользователя",
            description = """
                    Изменяет роль пользователя на ROLE_USER или ROLE_ADMIN. \
                    Администратор не может изменить собственную роль.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Роль пользователя успешно изменена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserAdminResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или попытка администратора изменить собственную роль",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль администратора", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/users/{userId}/role")
    public UserAdminResponse updateUserRole(
            @Parameter(description = "Уникальный идентификатор пользователя", example = "3", required = true)
            @PathVariable Long userId,

            @Valid @RequestBody UserRoleUpdateRequest request
    ) {
        return adminService.updateUserRole(userId, request);
    }

    @Operation(
            summary = "Удалить пользователя",
            description = """
                    Выполняет soft-delete пользователя через перевод в статус BLOCKED. \
                    Администратор не может удалить самого себя.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно удалён через soft-delete",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserAdminResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Попытка администратора удалить самого себя",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав: требуется роль администратора", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/users/{userId}")
    public UserAdminResponse deleteUser(
            @Parameter(description = "Уникальный идентификатор удаляемого пользователя", example = "3", required = true)
            @PathVariable Long userId
    ) {
        return adminService.deleteUser(userId);
    }
}