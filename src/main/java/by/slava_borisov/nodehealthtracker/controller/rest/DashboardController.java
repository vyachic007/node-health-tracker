package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.dashboard.AdminDashboardSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.dashboard.DashboardSummaryResponse;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Dashboard",
        description = """
                Сводные показатели мониторинга: узлы, сервисы, проверки, инциденты, \
                доступность и средняя оценка здоровья.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Получить dashboard текущего пользователя",
            description = """
                    Возвращает сводку по узлам, сервисам, проверкам, инцидентам \
                    и показателям здоровья только для текущего авторизованного пользователя.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сводка dashboard успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DashboardSummaryResponse.class)
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
    public DashboardSummaryResponse getCurrentUserSummary() {
        return dashboardService.getCurrentUserSummary();
    }


    @Operation(
            summary = "Получить административный dashboard",
            description = """
                    Возвращает системную сводку по всем пользователям, узлам, сервисам, \
                    проверкам и инцидентам. Endpoint доступен только пользователю с ролью ADMIN.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Административная сводка dashboard успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AdminDashboardSummaryResponse.class)
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
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardSummaryResponse getAdminSummary() {
        return dashboardService.getAdminSummary();
    }
}