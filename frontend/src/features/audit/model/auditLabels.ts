import type { AuditEventType, AuditSeverity } from './auditTypes';

export const auditEventTypeLabels: Record<AuditEventType, string> = {

    USER_LOGIN: 'Вход пользователя',

    USER_LOGOUT: 'Выход пользователя',

    USER_REGISTERED: 'Регистрация пользователя',

    NODE_CREATED: 'Создание узла',

    NODE_UPDATED: 'Обновление узла',

    NODE_DELETED: 'Удаление узла',

    SERVICE_CREATED: 'Создание сервиса',

    SERVICE_UPDATED: 'Обновление сервиса',

    SERVICE_DELETED: 'Удаление сервиса',

    CHECK_STARTED: 'Запуск проверки',

    CHECK_FINISHED: 'Завершение проверки',

    INCIDENT_OPENED: 'Инцидент открыт',

    INCIDENT_RESOLVED: 'Инцидент закрыт',

    NOTIFICATION_SENT: 'Уведомление отправлено',

    NOTIFICATION_FAILED: 'Ошибка уведомления',

    TELEGRAM_CONNECTED: 'Telegram подключён',

    TELEGRAM_DISCONNECTED: 'Telegram отключён',

    PASSWORD_RESET_REQUESTED: 'Запрос сброса пароля',

    PASSWORD_RESET_CONFIRMED: 'Сброс пароля подтверждён',

    UNKNOWN: 'Неизвестное событие',

};

export const auditSeverityLabels: Record<AuditSeverity, string> = {

    INFO: 'Информация',

    SUCCESS: 'Успешно',

    WARNING: 'Предупреждение',

    ERROR: 'Ошибка',

};

export function getAuditSeverityColor(severity: AuditSeverity) {

    switch (severity) {

        case 'SUCCESS':

            return 'success';

        case 'WARNING':

            return 'warning';

        case 'ERROR':

            return 'error';

        case 'INFO':

        default:

            return 'info';

    }

}