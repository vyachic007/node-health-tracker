# Node Health Tracker

**Node Health Tracker** — веб-приложение для мониторинга сетевых узлов и сервисов.

Система проверяет доступность сервисов, фиксирует сбои, открывает инциденты, определяет примерную причину проблемы и отправляет уведомления пользователю.

## Основные возможности

- регистрация и авторизация пользователей;
- JWT-аутентификация;
- управление сетевыми узлами;
- управление сервисами мониторинга;
- ручной и автоматический запуск проверок;
- история проверок;
- автоматическое открытие и закрытие инцидентов;
- определение уровня сбоя;
- anti-flapping-подтверждение сбоя;
- расчёт health score;
- уведомления через Email, Telegram и VK;
- административная панель;
- управление пользователями администратором.

## Типы проверок

| Тип | Назначение |
|---|---|
| `HTTP` | Проверка HTTP-сервиса |
| `HTTPS` | Проверка HTTPS-сервиса |
| `TCP` | Проверка доступности порта |
| `DNS` | Проверка DNS-разрешения домена |
| `SSL` | Проверка SSL/TLS-сертификата |
| `PING` | Проверка сетевой доступности узла |
| `HEARTBEAT` | Проверка регулярного сигнала от внешнего сервиса |

## Диагностика сбоев

При сбое система определяет примерный уровень проблемы.

| Уровень | Описание |
|---|---|
| `DNS` | Ошибка разрешения доменного имени |
| `NETWORK` | Узел недоступен на сетевом уровне |
| `PORT` | Не удалось подключиться к целевому порту |
| `SSL` | Проблема с SSL/TLS-сертификатом |
| `APPLICATION` | Ошибка приложения или HTTP-ответа |
| `PERFORMANCE` | Медленный ответ сервиса |
| `HEARTBEAT` | Не поступает heartbeat-сигнал |
| `UNKNOWN` | Причина точно не определена |

## Anti-flapping

Чтобы не создавать инцидент из-за единичного кратковременного сбоя, используется подтверждение несколькими проверками подряд.

Пример:

```text
failureThreshold = 2
```

Это значит, что инцидент будет открыт только после двух неуспешных проверок подряд.

Для закрытия инцидента используется порог восстановления:

```text
recoveryThreshold = 2
```

То есть сервис должен успешно пройти несколько проверок подряд, прежде чем инцидент будет закрыт.

## Технологический стек

### Backend

- Java 17
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- PostgreSQL
- Liquibase
- MapStruct
- Maven

### Frontend

- React
- TypeScript
- Vite
- Material UI
- TanStack Query
- React Router
- Notistack

### Инфраструктура

- Docker
- Docker Compose
- PostgreSQL 17
- Nginx
- ngrok для локального тестирования webhook

## Структура проекта

```text
node-health-tracker/
├── src/
│   └── main/
│       ├── java/
│       │   └── by/slava_borisov/nodehealthtracker/
│       │       ├── bootstrap/
│       │       ├── config/
│       │       ├── controller/
│       │       ├── dto/
│       │       ├── exception/
│       │       ├── mapper/
│       │       ├── model/
│       │       ├── notification/
│       │       ├── repository/
│       │       ├── scheduler/
│       │       ├── security/
│       │       ├── service/
│       │       └── util/
│       └── resources/
│           ├── application.yaml
│           └── db/changelog/
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   ├── features/
│   │   └── shared/
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── Dockerfile
├── docker-compose.prod.yml
├── .env.example
├── pom.xml
└── README.md
```

## Переменные окружения

Перед запуском нужно создать файл `.env` на основе `.env.example`.

Пример основных переменных:

```env
POSTGRES_DB=network_services_db
POSTGRES_USER=your_db_user
POSTGRES_PASSWORD=your_db_password
POSTGRES_PORT=5433

APP_PORT=8081

JWT_SECRET=your_jwt_secret
JWT_EXPIRATION_MS=86400000

ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=admin

MAIL_HOST=smtp.example.com
MAIL_PORT=465
MAIL_USERNAME=your_mail_username
MAIL_PASSWORD=your_mail_password
MAIL_FROM=your_mail@example.com
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=false
MAIL_SMTP_SSL=true

TELEGRAM_API_URL=https://api.telegram.org
TELEGRAM_BOT_TOKEN=your_telegram_bot_token

VK_API_URL=https://api.vk.com/method
VK_API_VERSION=5.199
VK_ACCESS_TOKEN=your_vk_access_token
VK_GROUP_ID=your_vk_group_id
VK_CONFIRMATION_CODE=your_vk_confirmation_code
```

## Запуск через Docker

### 1. Клонировать проект

```bash
git clone https://github.com/vyachic007/node-health-tracker.git
cd node-health-tracker
```

### 2. Создать `.env`

```bash
cp .env.example .env
```

После этого нужно заполнить переменные окружения.

### 3. Собрать backend

```bash
./mvnw clean package -DskipTests
```

### 4. Запустить контейнеры

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

### 5. Проверить контейнеры

```bash
docker ps
```

Ожидаемые контейнеры:

```text
node-health-tracker-db
node-health-tracker-app
node-health-tracker-frontend
```

### 6. Открыть приложение

```text
http://localhost:3000
```

## Локальный запуск backend

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

Или через jar:

```bash
java -jar target/node-health-tracker-0.0.1-SNAPSHOT.jar
```

## Локальный запуск frontend

```bash
cd frontend
npm install
npm run dev
```

Сборка frontend:

```bash
npm run build
```

## Работа с базой данных

Подключение к PostgreSQL внутри Docker:

```bash
docker exec -it node-health-tracker-db psql -U your_db_user -d network_services_db
```

Посмотреть настройки уведомлений:

```sql
SELECT id, user_id, channel, destination, is_enabled,
       notify_on_incident_open, notify_on_incident_resolved
FROM user_notification_settings
ORDER BY id;
```

Отключить все уведомления:

```sql
UPDATE user_notification_settings
SET is_enabled = false;
```

## Уведомления

Поддерживаемые каналы:

- Email;
- Telegram;
- VK.

Уведомления отправляются при:

- открытии инцидента;
- закрытии инцидента.

Пример уведомления:

```text
🚨 Открыт инцидент

Сервис: AutoJournal HTTPS
Тип проверки: HTTPS
Проверяемый адрес: autojournal-system.vercel.app
Порт: 443
Путь: /app/vehicles

ID сервиса: 10
ID инцидента: 5

Причина: Не удалось установить TCP-соединение с целевым портом.
Дата события: 28.05.2026
Время события: 14:04:36 МСК

Node Health Tracker
```

## Telegram webhook

Пример установки webhook:

```bash
TOKEN=$(docker exec node-health-tracker-app printenv TELEGRAM_BOT_TOKEN)

curl -X POST "https://api.telegram.org/bot${TOKEN}/setWebhook" \
  -d "url=https://your-ngrok-domain.ngrok-free.app/api/telegram/webhook"
```

Проверка webhook:

```bash
curl "https://api.telegram.org/bot${TOKEN}/getWebhookInfo"
```

## VK webhook

Пример проверки confirmation-запроса:

```bash
curl -X POST "https://your-ngrok-domain.ngrok-free.app/api/vk/webhook" \
  -H "Content-Type: application/json" \
  -d '{"type":"confirmation","group_id":123456789}'
```

Backend должен вернуть confirmation code из настроек.

## Основные API-разделы

```text
/api/auth
/api/nodes
/api/services
/api/checks
/api/incidents
/api/notifications
/api/admin
/api/telegram
/api/vk
```

## Сборка проекта

Backend:

```bash
./mvnw clean package -DskipTests
```

Frontend:

```bash
cd frontend
npm run build
```

Docker:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

## Полезные команды

Проверить статус Git:

```bash
git status
```

Посмотреть последние коммиты:

```bash
git log --oneline -5
```

Посмотреть контейнеры:

```bash
docker ps
```

Логи backend:

```bash
docker logs -f node-health-tracker-app
```

Логи базы данных:

```bash
docker logs -f node-health-tracker-db
```

Перезапуск проекта:

```bash
docker compose -f docker-compose.prod.yml down
docker compose -f docker-compose.prod.yml up -d --build
```

## Git workflow

Пример работы с веткой:

```bash
git checkout feature/frontend

git add .
git commit -m "Улучшил внешний вид страницы узлов"
git push origin feature/frontend

git checkout main
git pull origin main
git merge feature/frontend
git push origin main
```

## Возможные проблемы

### PING не работает, но HTTPS/TCP работают

Это может быть нормально. Некоторые публичные сайты блокируют ICMP-запросы.  
В таком случае PING будет показывать недоступность, хотя сайт открывается через HTTPS.

### Ошибка порта

Порт должен быть в диапазоне:

```text
1–65535
```

Пример:

```text
443     корректно
80      корректно
443345  некорректно
```

### Уведомления не приходят

Нужно проверить:

- включена ли настройка уведомлений у пользователя;
- включён ли канал уведомлений у конкретного сервиса;
- корректно ли указан Email, Telegram chat id или VK peer id;
- настроены ли переменные окружения;
- нет ли ошибок в логах backend.

### Frontend не обновился после изменений

Нужно пересобрать контейнер:

```bash
docker compose -f docker-compose.prod.yml up -d --build frontend
```

После этого обновить страницу:

```text
Cmd + Shift + R
```
