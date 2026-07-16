package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingCreateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.NotificationSettingUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.notification.SentNotificationResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.TelegramBindLinkResponse;
import by.slava_borisov.nodehealthtracker.dto.notification.VkBindLinkResponse;
import by.slava_borisov.nodehealthtracker.service.NotificationService;
import by.slava_borisov.nodehealthtracker.service.TelegramBindingService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(
        name = "Уведомления",
        description = """
                Управление настройками уведомлений пользователя, история отправленных уведомлений, \
                а также создание ссылок для привязки Telegram и VK.
                """
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final TelegramBindingService telegramBindingService;

    @Operation(
            summary = "Получить настройки уведомлений",
            description = "Возвращает все настройки уведомлений текущего авторизованного пользователя."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Настройки уведомлений успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationSettingResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/settings")
    public List<NotificationSettingResponse> getCurrentUserNotificationSettings() {
        return notificationService.getCurrentUserNotificationSettings();
    }


    @Operation(
            summary = "Создать настройку уведомлений",
            description = """
                    Создаёт настройку уведомлений для выбранного канала. \
                    Для одного пользователя нельзя создать две настройки с одинаковым каналом.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Настройка уведомлений успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = NotificationSettingResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации или настройка для такого канала уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-26T16:30:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Настройка уведомлений для этого канала уже существует",
                                              "path": "/api/notifications/settings"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/settings")
    public NotificationSettingResponse createNotificationSetting(
            @Valid @RequestBody NotificationSettingCreateRequest request
    ) {
        return notificationService.createNotificationSetting(request);
    }


    @Operation(
            summary = "Изменить настройку уведомлений",
            description = """
                    Изменяет состояние настройки, адрес назначения и события, \
                    по которым нужно отправлять уведомления. Изменять можно только собственные настройки.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Настройка уведомлений успешно изменена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationSettingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Переданные данные не прошли валидацию", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Настройка принадлежит другому пользователю", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Настройка уведомлений не найдена", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/settings/{settingId}")
    public NotificationSettingResponse updateNotificationSetting(
            @Parameter(description = "Уникальный идентификатор настройки уведомлений", example = "1", required = true)
            @PathVariable Long settingId,

            @Valid @RequestBody NotificationSettingUpdateRequest request
    ) {
        return notificationService.updateNotificationSetting(settingId, request);
    }


    @Operation(
            summary = "Удалить настройку уведомлений",
            description = "Удаляет настройку уведомлений текущего пользователя."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Настройка уведомлений успешно удалена", content = @Content),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "403", description = "Настройка принадлежит другому пользователю", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Настройка уведомлений не найдена", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/settings/{settingId}")
    public void deleteNotificationSetting(
            @Parameter(description = "Уникальный идентификатор удаляемой настройки уведомлений", example = "1", required = true)
            @PathVariable Long settingId
    ) {
        notificationService.deleteNotificationSetting(settingId);
    }


    @Operation(
            summary = "Получить историю отправленных уведомлений",
            description = "Возвращает историю уведомлений, отправленных текущему пользователю."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "История отправленных уведомлений успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = SentNotificationResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/sent")
    public List<SentNotificationResponse> getCurrentUserSentNotifications() {
        return notificationService.getCurrentUserSentNotifications();
    }


    @Operation(
            summary = "Создать ссылку привязки Telegram",
            description = """
                    Создаёт временную ссылку для подключения Telegram к уведомлениям пользователя. \
                    Пользователь должен перейти по ссылке и отправить команду боту.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ссылка привязки Telegram успешно создана",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TelegramBindLinkResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/telegram/bind-link")
    public TelegramBindLinkResponse createTelegramBindLink() {
        return telegramBindingService.createCurrentUserBindLink();
    }


    @Operation(
            summary = "Создать ссылку привязки VK",
            description = """
                    Создаёт временную команду привязки VK. \
                    Пользователь должен открыть диалог с группой VK и отправить команду /start с токеном.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ссылка привязки VK успешно создана",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VkBindLinkResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "VK_GROUP_ID не настроен или операция привязки невозможна",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT-токен отсутствует, недействителен или просрочен", content = @Content),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/vk/bind-link")
    public VkBindLinkResponse createVkBindLink() {
        return notificationService.createVkBindLink();
    }
}