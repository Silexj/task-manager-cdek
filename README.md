# Task Time Tracker API

Backend REST-сервис учёта рабочего времени сотрудников на задачах: создание задач, смена статуса и фиксация временных отрезков (TimeRecord).

## Реализованный функционал (по ТЗ)

| Сущность | Поля |
|----------|------|
| **Task** | ID, название, описание, статус `NEW` / `IN_PROGRESS` / `DONE` |
| **TimeRecord** | ID, ID сотрудника, ID задачи, время начала/окончания, описание работы |

| Эндпоинт | Метод |
|----------|-------|
| Создание задачи | `POST /api/tasks` |
| Получение задачи по ID | `GET /api/tasks/{id}` |
| Изменение статуса задачи | `PATCH /api/tasks/{id}/status` |
| Создание записи о затраченном времени | `POST /api/tasks/{taskId}/time-records` |
| Затраты времени сотрудника за период | `GET /api/employees/{employeeId}/time-records?from=&to=` |

Даты/время в JSON — в формате ISO-8601 (`Instant`, например `2026-05-03T09:00:00Z`). Параметры `from` и `to` — границы периода включительно.

## Стек

- Java 21, Spring Boot 3.4.x  
- MyBatis, Maven, JUnit 5, Mockito  
- H2 (in-memory), схема инициализируется из `src/main/resources/schema.sql`  
- SpringDoc OpenAPI (Swagger UI), Bean Validation (`jakarta.validation`), глобальная обработка ошибок через `@RestControllerAdvice`

## Сборка и запуск

Требуется установленный JDK 21 и Maven 3.9+.

```bash
cd task-manager-cdek
mvn clean verify
mvn spring-boot:run
```

Приложение по умолчанию слушает порт **8080**. БД **H2** поднимается вместе с приложением, отдельно ничего запускать не нужно.

Если бы использовался PostgreSQL в Docker, понадобился бы контейнер БД, JDBC URL и миграции/скрипт схемы — в этом репозитории выбран упрощающий вариант **только H2**, как допускает ТЗ.

## Как проверить работоспособность

1. **Тесты:** после `mvn clean verify` выполняются unit-тесты сервисов, контрактные тесты контроллеров и интеграционные тесты слоя доступа к данным на H2.  
2. **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) — интерактивные вызовы всех эндпойнтов.  
3. **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)  

Готовые запросы см. в файле [`api-requests.http`](api-requests.http) (REST Client в VS Code / IntelliJ) и в коллекции Postman [`postman/TaskTimeTracker.postman_collection.json`](postman/TaskTimeTracker.postman_collection.json).

### Пример «с нуля» (curl)

После `mvn spring-boot:run` можно выполнить последовательность (подставьте свои id из ответов при необходимости):

```bash
# 1. Создать задачу
curl -s -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Демо-задача\",\"description\":\"Описание\"}"

# 2. Получить задачу (id=1)
curl -s http://localhost:8080/api/tasks/1

# 3. Обновить статус
curl -s -X PATCH http://localhost:8080/api/tasks/1/status ^
  -H "Content-Type: application/json" ^
  -d "{\"status\":\"IN_PROGRESS\"}"

# 4. Создать запись времени (taskId в пути, employeeId в теле)
curl -s -X POST http://localhost:8080/api/tasks/1/time-records ^
  -H "Content-Type: application/json" ^
  -d "{\"employeeId\":100,\"startedAt\":\"2026-05-03T09:00:00Z\",\"finishedAt\":\"2026-05-03T12:00:00Z\",\"workDescription\":\"Реализация API\"}"

# 5. Список записей сотрудника за период
curl -s "http://localhost:8080/api/employees/100/time-records?from=2026-05-01T00:00:00Z&to=2026-05-31T23:59:59Z"
```

На Unix/macOS замените символ переноса `^` на `\`.

## Дополнительные критерии из ТЗ (честно)

| Критерий | Статус |
|----------|--------|
| SpringDoc OpenAPI | Подключено, UI по ссылке выше |
| Валидация DTO | Bean Validation на входящих телах |
| `@RestControllerAdvice` | Централизованные ответы об ошибках и валидации |
| Интеграционные тесты DAO с **Testcontainers** | Не использовались: есть интеграционные тесты репозитория на **встроенном H2** |
| Bearer JWT | Зависимости в `pom.xml` есть; **защита эндпоинтов не включена** |

## ИИ-помощник

В репозитории закоммичена папка **`.cursor/rules/`** с правилами для ассистента в IDE — это сознательное решение для прозрачности: по ТЗ использование ИИ приветствуется, а фиксированные промпты/правила упрощают проверку контекста разработки.

## Структура пакетов (кратко)

- `controller` — REST-слой  
- `service` — бизнес-логика  
- `persistence` — MyBatis-мапперы и модели БД  
- `exception` — доменные исключения и `@RestControllerAdvice`  

Точка входа: `ru.silex.tasktracker.AppApplication`.
