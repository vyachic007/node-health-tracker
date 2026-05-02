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
}