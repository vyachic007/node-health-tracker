package by.slava_borisov.nodehealthtracker.util;

public final class Messages {

    private Messages() {
    }

    public static final String DNS_RESOLUTION_FAILED =
            "Ошибка DNS-разрешения.";

    public static final String DNS_RESOLUTION_FAILED_RECOMMENDATION =
            "Проверьте доменное имя, DNS-записи и доступность DNS-провайдера.";

    public static final String HOST_UNREACHABLE =
            "Узел недоступен на сетевом уровне.";

    public static final String HOST_UNREACHABLE_RECOMMENDATION =
            "Проверьте доступность сервера, маршрутизацию, firewall и доступность ICMP.";

    public static final String TCP_CONNECTION_FAILED =
            "Не удалось установить TCP-соединение с целевым портом.";

    public static final String TCP_CONNECTION_FAILED_RECOMMENDATION =
            "Проверьте, запущен ли сервис, открыт ли порт и не блокирует ли подключение firewall.";

    public static final String SSL_VALIDATION_FAILED =
            "Проверка SSL-сертификата не пройдена.";

    public static final String SSL_VALIDATION_FAILED_RECOMMENDATION =
            "Проверьте срок действия сертификата, цепочку сертификатов и настройки HTTPS.";

    public static final String HTTP_SERVER_ERROR =
            "Приложение вернуло серверную ошибку.";

    public static final String HTTP_SERVER_ERROR_RECOMMENDATION =
            "Проверьте логи приложения, зависимости backend-сервиса и последние изменения в развертывании.";

    public static final String HTTP_CLIENT_ERROR =
            "Приложение вернуло клиентскую ошибку.";

    public static final String HTTP_CLIENT_ERROR_RECOMMENDATION =
            "Проверьте путь запроса, требования авторизации и маршрутизацию приложения.";

    public static final String SLOW_RESPONSE =
            "Время ответа сервиса выше ожидаемого.";

    public static final String SLOW_RESPONSE_RECOMMENDATION =
            "Проверьте нагрузку на сервер, производительность базы данных, сетевую задержку и использование ресурсов.";

    public static final String NO_CRITICAL_PROBLEM_DETECTED =
            "Критическая проблема не обнаружена.";

    public static final String NO_ACTION_REQUIRED =
            "На данный момент дополнительных действий не требуется.";

    public static final String NETWORK_SERVICE_NOT_FOUND =
            "Сервис мониторинга не найден";

    public static final String TCP_PORT_REQUIRED =
            "Для TCP-проверки должен быть указан порт";

    public static final String CHECKER_NOT_FOUND =
            "Обработчик проверки для типа %s не найден";

    public static final String HEARTBEAT_NOT_RECEIVED =
            "Heartbeat от сервиса еще не поступал";

    public static final String HEARTBEAT_EXPIRED =
            "Heartbeat от сервиса давно не поступал";

    public static final String HEARTBEAT_FAILED =
            "Heartbeat-мониторинг обнаружил отсутствие сигнала от сервиса.";

    public static final String HEARTBEAT_FAILED_RECOMMENDATION =
            "Проверьте, запущен ли внешний сервис, отправляет ли он heartbeat-запросы и корректно ли настроен heartbeat token.";

    public static final String INCIDENT_NOT_FOUND =
            "Инцидент не найден";

    public static final String INCIDENT_ALREADY_CLOSED =
            "Инцидент уже закрыт";

    public static final String USER_NOT_FOUND =
            "Пользователь не найден";

    public static final String INTERNAL_SERVER_ERROR =
            "Внутренняя ошибка сервера";

    public static final String VALIDATION_ERROR =
            "Ошибка валидации входных данных";

    public static final String BAD_REQUEST =
            "Некорректный запрос";

    public static final String NOT_FOUND =
            "Ресурс не найден";

    public static final String NETWORK_NODE_NOT_FOUND =
            "Сетевой узел не найден";

    public static final String NETWORK_NODE_ACCESS_DENIED =
            "Нет доступа к этому сетевому узлу";

    public static final String NETWORK_SERVICE_ACCESS_DENIED =
            "Нет доступа к этому сервису мониторинга";

    public static final String HEARTBEAT_TOKEN_NOT_FOUND =
            "Heartbeat token не найден";

    public static final String HEARTBEAT_ACCEPTED =
            "Heartbeat успешно принят";

    public static final String HEARTBEAT_TOKEN_REQUIRED =
            "Heartbeat token обязателен";

    public static final String JWT_TOKEN_INVALID =
            "JWT-токен недействителен";

    public static final String JWT_TOKEN_EXPIRED =
            "Срок действия JWT-токена истек";

    public static final String JWT_TOKEN_REQUIRED =
            "JWT-токен обязателен";

    public static final String USERNAME_ALREADY_EXISTS =
            "Пользователь с таким именем уже существует";

    public static final String USER_EMAIL_ALREADY_EXISTS =
            "Пользователь с таким email уже существует";

    public static final String INVALID_USERNAME_OR_PASSWORD =
            "Неверное имя пользователя или пароль";

    public static final String ACCESS_DENIED =
            "Доступ запрещен";

    public static final String INCIDENT_ACCESS_DENIED =
            "Нет доступа к этому инциденту";

    public static final String RESOURCE_NOT_FOUND =
            "Ресурс не найден";

    public static final String USER_BLOCKED =
            "Пользователь заблокирован";

    public static final String ADMIN_CANNOT_BLOCK_SELF =
            "Администратор не может заблокировать самого себя";

    public static final String ADMIN_CANNOT_DELETE_SELF =
            "Администратор не может удалить самого себя";

    public static final String ADMIN_CANNOT_CHANGE_OWN_ROLE =
            "Администратор не может изменить собственную роль";

    public static final String PAGE_NUMBER_INVALID =
            "Номер страницы не может быть отрицательным";

    public static final String PAGE_SIZE_INVALID =
            "Размер страницы должен быть от 1 до 100";

    public static final String CURRENT_PASSWORD_INVALID =
            "Текущий пароль указан неверно";

    public static final String NEW_PASSWORD_MUST_BE_DIFFERENT =
            "Новый пароль должен отличаться от текущего";

    public static final String JWT_TOKEN_REVOKED =
            "JWT-токен больше недействителен";

    public static final String INVALID_REQUEST_PARAMETER =
            "Некорректный параметр запроса";

    public static final String INVALID_REQUEST_BODY =
            "Некорректное тело запроса";

    public static final String NOTIFICATION_SETTING_ALREADY_EXISTS =
            "Настройка уведомлений для этого канала уже существует";

    public static final String NOTIFICATION_SETTING_NOT_FOUND =
            "Настройка уведомлений не найдена";

    public static final String NOTIFICATION_SENDER_NOT_FOUND =
            "Отправитель уведомлений для канала %s не найден";

    public static final String INCIDENT_TIMELINE_CHECK_FAILED =
            "Проверка завершилась ошибкой: ";

    public static final String INCIDENT_TIMELINE_SEVERITY_ASSIGNED =
            "Назначена критичность инцидента: ";

    public static final String INCIDENT_TIMELINE_INCIDENT_OPENED =
            "Открыт инцидент по сервису: ";

    public static final String INCIDENT_TIMELINE_CHECK_RECOVERED =
            "Проверка снова завершилась успешно.";

    public static final String INCIDENT_TIMELINE_INCIDENT_RESOLVED =
            "Инцидент закрыт, сервис восстановлен.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_DNS =
            "Проблема связана с разрешением доменного имени.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_NETWORK =
            "Проблема связана с сетевой доступностью узла.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_PORT =
            "Проблема связана с недоступностью целевого порта.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_SSL =
            "Проблема связана с SSL/TLS-сертификатом или HTTPS-настройками.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_APPLICATION =
            "Проблема связана с работой приложения или HTTP-ответом.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_PERFORMANCE =
            "Проблема связана с производительностью сервиса.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_HEARTBEAT =
            "Проблема связана с отсутствием heartbeat-сигнала от агента.";

    public static final String RECOVERY_CHECKLIST_SUMMARY_UNKNOWN =
            "Точный уровень проблемы не определён, требуется базовая диагностика.";

    public static final String RECOVERY_DNS_STEP_1_TITLE = "Проверить доменное имя";
    public static final String RECOVERY_DNS_STEP_1_DESCRIPTION =
            "Убедитесь, что домен указан без ошибки и соответствует нужному сервису.";

    public static final String RECOVERY_DNS_STEP_2_TITLE = "Проверить DNS-записи";
    public static final String RECOVERY_DNS_STEP_2_DESCRIPTION =
            "Проверьте A, AAAA или CNAME-записи домена.";

    public static final String RECOVERY_DNS_STEP_3_TITLE = "Проверить резолвинг";
    public static final String RECOVERY_DNS_STEP_3_DESCRIPTION =
            "Выполните nslookup или dig и убедитесь, что домен преобразуется в IP-адрес.";

    public static final String RECOVERY_DNS_STEP_4_TITLE = "Проверить DNS-провайдера";
    public static final String RECOVERY_DNS_STEP_4_DESCRIPTION =
            "Убедитесь, что у DNS-провайдера нет сбоя или задержки обновления записей.";

    public static final String RECOVERY_NETWORK_STEP_1_TITLE = "Проверить доступность хоста";
    public static final String RECOVERY_NETWORK_STEP_1_DESCRIPTION =
            "Выполните ping или traceroute до целевого узла.";

    public static final String RECOVERY_NETWORK_STEP_2_TITLE = "Проверить сетевой маршрут";
    public static final String RECOVERY_NETWORK_STEP_2_DESCRIPTION =
            "Убедитесь, что между клиентом и сервером нет проблем маршрутизации.";

    public static final String RECOVERY_NETWORK_STEP_3_TITLE = "Проверить firewall";
    public static final String RECOVERY_NETWORK_STEP_3_DESCRIPTION =
            "Проверьте, не блокирует ли firewall входящий или исходящий трафик.";

    public static final String RECOVERY_NETWORK_STEP_4_TITLE = "Проверить состояние сервера";
    public static final String RECOVERY_NETWORK_STEP_4_DESCRIPTION =
            "Убедитесь, что сервер включён и доступен из сети.";

    public static final String RECOVERY_PORT_STEP_1_TITLE = "Проверить запуск сервиса";
    public static final String RECOVERY_PORT_STEP_1_DESCRIPTION =
            "Убедитесь, что приложение или служба действительно запущены на сервере.";

    public static final String RECOVERY_PORT_STEP_2_TITLE = "Проверить целевой порт";
    public static final String RECOVERY_PORT_STEP_2_DESCRIPTION =
            "Проверьте, слушает ли сервис нужный порт через netstat, ss или lsof.";

    public static final String RECOVERY_PORT_STEP_3_TITLE = "Проверить firewall";
    public static final String RECOVERY_PORT_STEP_3_DESCRIPTION =
            "Убедитесь, что порт не заблокирован firewall или правилами безопасности.";

    public static final String RECOVERY_PORT_STEP_4_TITLE = "Проверить Docker port mapping";
    public static final String RECOVERY_PORT_STEP_4_DESCRIPTION =
            "Если сервис работает в Docker, проверьте проброс портов в docker-compose.yml.";

    public static final String RECOVERY_PORT_STEP_5_TITLE = "Проверить reverse proxy";
    public static final String RECOVERY_PORT_STEP_5_DESCRIPTION =
            "Если используется Nginx или другой proxy, проверьте upstream и proxy_pass.";

    public static final String RECOVERY_SSL_STEP_1_TITLE = "Проверить срок сертификата";
    public static final String RECOVERY_SSL_STEP_1_DESCRIPTION =
            "Убедитесь, что SSL/TLS-сертификат не истёк.";

    public static final String RECOVERY_SSL_STEP_2_TITLE = "Проверить цепочку сертификатов";
    public static final String RECOVERY_SSL_STEP_2_DESCRIPTION =
            "Проверьте, корректно ли настроены intermediate certificates.";

    public static final String RECOVERY_SSL_STEP_3_TITLE = "Проверить HTTPS-конфигурацию";
    public static final String RECOVERY_SSL_STEP_3_DESCRIPTION =
            "Проверьте настройки HTTPS на сервере или reverse proxy.";

    public static final String RECOVERY_SSL_STEP_4_TITLE = "Проверить домен сертификата";
    public static final String RECOVERY_SSL_STEP_4_DESCRIPTION =
            "Убедитесь, что сертификат выпущен именно для проверяемого домена.";

    public static final String RECOVERY_APPLICATION_STEP_1_TITLE = "Проверить HTTP-статус";
    public static final String RECOVERY_APPLICATION_STEP_1_DESCRIPTION =
            "Посмотрите, какой HTTP-код возвращает приложение.";

    public static final String RECOVERY_APPLICATION_STEP_2_TITLE = "Проверить логи приложения";
    public static final String RECOVERY_APPLICATION_STEP_2_DESCRIPTION =
            "Проверьте backend-логи на наличие исключений или ошибок обработки запроса.";

    public static final String RECOVERY_APPLICATION_STEP_3_TITLE = "Проверить базу данных";
    public static final String RECOVERY_APPLICATION_STEP_3_DESCRIPTION =
            "Убедитесь, что приложение может подключиться к базе данных.";

    public static final String RECOVERY_APPLICATION_STEP_4_TITLE = "Проверить reverse proxy";
    public static final String RECOVERY_APPLICATION_STEP_4_DESCRIPTION =
            "Проверьте настройки Nginx, gateway или другого прокси-сервера.";

    public static final String RECOVERY_PERFORMANCE_STEP_1_TITLE = "Проверить нагрузку на сервер";
    public static final String RECOVERY_PERFORMANCE_STEP_1_DESCRIPTION =
            "Оцените CPU, RAM, disk I/O и сетевую нагрузку.";

    public static final String RECOVERY_PERFORMANCE_STEP_2_TITLE = "Проверить время ответа БД";
    public static final String RECOVERY_PERFORMANCE_STEP_2_DESCRIPTION =
            "Убедитесь, что база данных не является причиной задержки.";

    public static final String RECOVERY_PERFORMANCE_STEP_3_TITLE = "Проверить долгие запросы";
    public static final String RECOVERY_PERFORMANCE_STEP_3_DESCRIPTION =
            "Посмотрите slow queries, тяжёлые endpoints или зависшие операции.";

    public static final String RECOVERY_PERFORMANCE_STEP_4_TITLE = "Проверить количество подключений";
    public static final String RECOVERY_PERFORMANCE_STEP_4_DESCRIPTION =
            "Убедитесь, что пул соединений и лимиты сервера не исчерпаны.";

    public static final String RECOVERY_HEARTBEAT_STEP_1_TITLE = "Проверить работу агента";
    public static final String RECOVERY_HEARTBEAT_STEP_1_DESCRIPTION =
            "Убедитесь, что агент запущен на контролируемом узле.";

    public static final String RECOVERY_HEARTBEAT_STEP_2_TITLE = "Проверить heartbeat token";
    public static final String RECOVERY_HEARTBEAT_STEP_2_DESCRIPTION =
            "Проверьте, что агент отправляет heartbeat с корректным токеном.";

    public static final String RECOVERY_HEARTBEAT_STEP_3_TITLE = "Проверить расписание отправки";
    public static final String RECOVERY_HEARTBEAT_STEP_3_DESCRIPTION =
            "Убедитесь, что heartbeat отправляется с нужным интервалом.";

    public static final String RECOVERY_HEARTBEAT_STEP_4_TITLE = "Проверить сетевой доступ агента";
    public static final String RECOVERY_HEARTBEAT_STEP_4_DESCRIPTION =
            "Проверьте, может ли агент подключиться к серверу мониторинга.";

    public static final String RECOVERY_UNKNOWN_STEP_1_TITLE = "Проверить последние результаты проверок";
    public static final String RECOVERY_UNKNOWN_STEP_1_DESCRIPTION =
            "Посмотрите последние check results и errorMessage.";

    public static final String RECOVERY_UNKNOWN_STEP_2_TITLE = "Проверить доступность хоста";
    public static final String RECOVERY_UNKNOWN_STEP_2_DESCRIPTION =
            "Убедитесь, что узел доступен из сети.";

    public static final String RECOVERY_UNKNOWN_STEP_3_TITLE = "Проверить приложение и порт";
    public static final String RECOVERY_UNKNOWN_STEP_3_DESCRIPTION =
            "Проверьте, запущено ли приложение и открыт ли нужный порт.";

    public static final String RECOVERY_UNKNOWN_STEP_4_TITLE = "Проверить системные логи";
    public static final String RECOVERY_UNKNOWN_STEP_4_DESCRIPTION =
            "Изучите логи приложения, сервера и контейнеров.";

    public static final String INCIDENT_REPORT_SUMMARY_OPEN =
            "Инцидент ещё открыт. Проблема требует внимания.";

    public static final String INCIDENT_REPORT_SUMMARY_RESOLVED =
            "Инцидент закрыт. Сервис восстановлен.";

    public static final String INCIDENT_REPORT_SUMMARY_DNS =
            "Основная причина была связана с разрешением доменного имени.";

    public static final String INCIDENT_REPORT_SUMMARY_NETWORK =
            "Основная причина была связана с сетевой доступностью узла.";

    public static final String INCIDENT_REPORT_SUMMARY_PORT =
            "Основная причина была связана с недоступностью целевого порта.";

    public static final String INCIDENT_REPORT_SUMMARY_SSL =
            "Основная причина была связана с SSL/TLS-сертификатом или HTTPS-настройками.";

    public static final String INCIDENT_REPORT_SUMMARY_APPLICATION =
            "Основная причина была связана с работой приложения или HTTP-ответом.";

    public static final String INCIDENT_REPORT_SUMMARY_PERFORMANCE =
            "Основная причина была связана с производительностью сервиса.";

    public static final String INCIDENT_REPORT_SUMMARY_HEARTBEAT =
            "Основная причина была связана с отсутствием heartbeat-сигнала.";

    public static final String INCIDENT_REPORT_SUMMARY_UNKNOWN =
            "Основная причина не была точно определена.";

    public static final String RECURRENCE_RECOMMENDATION_LOW =
            "Проблема выглядит как разовый сбой. Достаточно наблюдать за следующими проверками.";

    public static final String RECURRENCE_RECOMMENDATION_MEDIUM =
            "Проблема повторяется. Рекомендуется проверить конфигурацию сервиса и связанные компоненты.";

    public static final String RECURRENCE_RECOMMENDATION_HIGH =
            "Проблема повторяется часто. Рекомендуется искать корневую причину: конфигурацию сервиса, сеть, firewall, инфраструктуру или зависимости приложения.";

    public static final String HEALTH_SCORE_SUMMARY_HEALTHY =
            "Сервис работает стабильно. Критичных признаков деградации не обнаружено.";

    public static final String HEALTH_SCORE_SUMMARY_DEGRADED =
            "Сервис доступен, но есть признаки деградации или нестабильности.";

    public static final String HEALTH_SCORE_SUMMARY_UNSTABLE =
            "Сервис работает нестабильно. Рекомендуется проверить причины сбоев и метрики доступности.";

    public static final String HEALTH_SCORE_SUMMARY_CRITICAL =
            "Сервис находится в критическом состоянии. Требуется немедленная диагностика.";

    public static final String AUDIT_NODE_CREATED = "Создан узел: ";

    public static final String AUDIT_NODE_UPDATED = "Обновлён узел: ";

    public static final String AUDIT_NODE_DELETED = "Удалён узел: ";

    public static final String AUDIT_SERVICE_CREATED = "Создан сервис: ";

    public static final String AUDIT_SERVICE_UPDATED = "Обновлён сервис: ";
    public static final String AUDIT_SERVICE_DELETED = "Удалён сервис: ";

    public static final String AUDIT_SERVICE_ENABLED = "Включён сервис: ";

    public static final String AUDIT_SERVICE_DISABLED = "Отключён сервис: ";

    public static final String AUDIT_INCIDENT_OPENED = "Система открыла инцидент по сервису: ";

    public static final String AUDIT_INCIDENT_RESOLVED = "Система закрыла инцидент по сервису: ";

    public static final String AUDIT_INCIDENT_MANUALLY_RESOLVED = "Пользователь закрыл инцидент по сервису: ";

    public static final String AUDIT_USER_BLOCKED = "Администратор заблокировал пользователя: ";

    public static final String AUDIT_USER_UNBLOCKED = "Администратор разблокировал пользователя: ";

    public static final String AUDIT_USER_ROLE_UPDATED = "Администратор изменил роль пользователя: ";

    public static final String AUDIT_USER_DELETED = "Администратор удалил пользователя: ";
}