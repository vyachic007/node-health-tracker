package by.slava_borisov.nodehealthtracker.controller.rest;

import by.slava_borisov.nodehealthtracker.config.OpenApiConfig;
import by.slava_borisov.nodehealthtracker.dto.error.ApiErrorResponse;
import by.slava_borisov.nodehealthtracker.dto.user.AuthResponse;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordChangeRequest;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetConfirmRequest;
import by.slava_borisov.nodehealthtracker.dto.user.PasswordResetRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserLoginRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileResponse;
import by.slava_borisov.nodehealthtracker.dto.user.UserProfileUpdateRequest;
import by.slava_borisov.nodehealthtracker.dto.user.UserRegistrationRequest;
import by.slava_borisov.nodehealthtracker.service.AuthService;
import by.slava_borisov.nodehealthtracker.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Авторизация и профиль",
        description = """
                Регистрация и авторизация пользователей, просмотр и изменение профиля, \
                смена пароля и восстановление доступа к учётной записи.
                """
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @Operation(
            summary = "Получить профиль текущего пользователя",
            description = """
                    Возвращает профиль авторизованного пользователя, определённого \
                    по JWT-токену из заголовка Authorization.
                    """
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль пользователя успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserProfileResponse.class
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
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/me"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/me")
    public UserProfileResponse getCurrentUserProfile() {
        return authService.getCurrentUserProfile();
    }


    @Operation(
            summary = "Изменить профиль текущего пользователя",
            description = """
                    Изменяет адрес электронной почты и имя текущего авторизованного \
                    пользователя. Новый email и username должны быть уникальными.
                    """
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль пользователя успешно изменён",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserProfileResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Переданные данные не прошли валидацию",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "email: must be a well-formed email address",
                                              "path": "/api/auth/me"
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
                    responseCode = "409",
                    description = "Указанный email или username уже используется",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Пользователь с указанным email уже существует",
                                              "path": "/api/auth/me"
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
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/me"
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/me")
    public UserProfileResponse updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        return authService.updateCurrentUserProfile(request);
    }


    @Operation(
            summary = "Изменить пароль текущего пользователя",
            description = """
                    Изменяет пароль авторизованного пользователя. Для выполнения операции \
                    необходимо правильно указать текущий пароль. Новый пароль не должен \
                    совпадать с текущим.
                    """
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пароль успешно изменён",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Переданные данные не прошли валидацию или новый пароль \
                            совпадает с текущим
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Новый пароль должен отличаться от текущего",
                                              "path": "/api/auth/me/password"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = """
                            JWT-токен отсутствует, недействителен или просрочен либо \
                            указан неправильный текущий пароль
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Указан неверный текущий пароль",
                                              "path": "/api/auth/me/password"
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
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/me/password"
                                            }
                                            """
                            )
                    )
            )
    })
    @PatchMapping("/me/password")
    public void changeCurrentUserPassword(
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        authService.changeCurrentUserPassword(request);
    }


    @Operation(
            summary = "Зарегистрировать пользователя",
            description = """
                    Создаёт новую учётную запись со статусом ACTIVE и ролью ROLE_USER. \
                    Email и username должны быть уникальными.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = UserProfileResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Переданные регистрационные данные не прошли валидацию",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "email: must be a well-formed email address",
                                              "path": "/api/auth/register"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с указанным email или username уже существует",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 409,
                                              "error": "Conflict",
                                              "message": "Пользователь с указанным email уже существует",
                                              "path": "/api/auth/register"
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
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/register"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/register")
    public UserProfileResponse register(
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        return authService.register(request);
    }


    @Operation(
            summary = "Авторизовать пользователя",
            description = """
                    Проверяет username и пароль пользователя. При успешной авторизации \
                    возвращает сведения о пользователе и JWT-токен типа Bearer.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно авторизован",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = AuthResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Переданные данные не прошли валидацию",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "username: must not be blank",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Указан неправильный username или пароль",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 401,
                                              "error": "Unauthorized",
                                              "message": "Неверное имя пользователя или пароль",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Учётная запись пользователя заблокирована",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 403,
                                              "error": "Forbidden",
                                              "message": "Пользователь заблокирован",
                                              "path": "/api/auth/login"
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
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/login"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody UserLoginRequest request
    ) {
        return authService.login(request);
    }


    @Operation(
            summary = "Запросить восстановление пароля",
            description = """
                    Создаёт одноразовый токен восстановления пароля со сроком действия \
                    30 минут и отправляет его на email пользователя.

                    Для защиты от определения существующих учётных записей endpoint \
                    возвращает одинаковый ответ независимо от того, зарегистрирован ли \
                    указанный email. Для заблокированного пользователя токен не создаётся.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Запрос на восстановление пароля обработан",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Email отсутствует или имеет неправильный формат",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "email: must be a well-formed email address",
                                              "path": "/api/auth/password-reset/request"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = """
                            Внутренняя ошибка сервера, включая ошибку создания токена \
                            или отправки сообщения
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/password-reset/request"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        passwordResetService.requestPasswordReset(request);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Подтвердить восстановление пароля",
            description = """
                    Устанавливает новый пароль с использованием одноразового токена. \
                    Токен должен существовать, не быть использованным и не быть просроченным. \
                    Новый пароль не должен совпадать с текущим паролем пользователя.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Пароль успешно восстановлен",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Запрос не прошёл валидацию; токен недействителен, просрочен \
                            или уже использован; пользователь заблокирован; новый пароль \
                            совпадает с текущим
                            """,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Токен восстановления пароля недействителен",
                                              "path": "/api/auth/password-reset/confirm"
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
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            ),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "timestamp": "2026-06-25T20:10:00",
                                              "status": 500,
                                              "error": "Internal Server Error",
                                              "message": "Внутренняя ошибка сервера",
                                              "path": "/api/auth/password-reset/confirm"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmPasswordReset(request);

        return ResponseEntity.noContent().build();
    }
}