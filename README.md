# Система бронирования комнат

Учебный REST API-проект на Spring Boot, разработанный в рамках курса школы Сорокина.  
Реализует полный цикл управления бронированием комнат: создание, просмотр, обновление, отмена и подтверждение.

---

## Технологии

| Технология | Версия | Назначение |
|---|---|---|
| Java | 21 | Язык программирования |
| Spring Boot | 3.4.3 | Основной фреймворк |
| Spring Web | — | REST API |
| Spring Data JPA | — | Работа с базой данных |
| Spring Validation | — | Валидация входных данных |
| PostgreSQL | — | База данных |
| Maven | — | Сборка проекта |

---

## Архитектура проекта

Проект построен по классической трёхслойной архитектуре:

```
Клиент (HTTP) → Controller → Service → Repository → База данных (PostgreSQL)
```

### Структура пакетов

```
src/main/java/school/sorokin/reservation/
├── ReservationSystemApplication.java   # Точка входа
│
├── reservations/                       # Основная логика бронирования
│   ├── Reservation.java                # DTO (объект передачи данных)
│   ├── ReservationController.java      # REST-контроллер
│   ├── ReservationEntity.java          # JPA-сущность (таблица в БД)
│   ├── ReservationMapper.java          # Конвертация Entity ↔ DTO
│   ├── ReservationRepository.java      # Работа с БД (Spring Data JPA)
│   ├── ReservationSearchFilter.java    # Фильтр поиска
│   ├── ReservationService.java         # Бизнес-логика
│   ├── ReservationStatus.java          # Статусы бронирования (enum)
│   │
│   └── availability/                   # Проверка доступности комнат
│       ├── AvailabilityStatus.java
│       ├── CheckAvailabilityRequest.java
│       ├── CheckAvailabilityResponse.java
│       ├── ReservationAvailabilityController.java
│       └── ReservationAvailabilityService.java
│
└── web/                                # Обработка ошибок
    ├── ErrorResponseDto.java
    └── GlobalExceptionHandler.java
```

---

## Статусы бронирования

```
Создание → PENDING → APPROVED (подтверждено)
                   → CANCELLED (отменено)
```

| Статус | Описание |
|---|---|
| `PENDING` | Ожидает подтверждения (начальный статус) |
| `APPROVED` | Подтверждено — комната занята |
| `CANCELLED` | Отменено |

---

## API — Эндпоинты

### Бронирования `/reservation`

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/reservation/{id}` | Получить бронирование по ID |
| `GET` | `/reservation` | Получить список бронирований (с фильтрацией и пагинацией) |
| `POST` | `/reservation` | Создать новое бронирование |
| `POST` | `/reservation/{id}` | Обновить бронирование |
| `DELETE` | `/reservation/{id}/cancel` | Отменить бронирование |
| `POST` | `/reservation/{id}/approve` | Подтвердить бронирование |

### Доступность `/reservation/availability`

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/reservation/availability/check` | Проверить доступность комнаты на период |

---

## Примеры запросов

### Создать бронирование
```http
POST /reservation
Content-Type: application/json

{
  "userId": 8,
  "roomId": 7,
  "startDate": "2025-09-20",
  "endDate": "2025-09-29"
}
```

### Получить все бронирования (с фильтром)
```http
GET /reservation?userId=8&roomId=7&pageSize=10&pageNumber=0
```

### Проверить доступность комнаты
```http
POST /reservation/availability/check
Content-Type: application/json

{
  "roomId": 7,
  "startDate": "2025-09-20",
  "endDate": "2025-09-29"
}
```

Ответ:
```json
{
  "message": "Room available to reservation",
  "status": "AVAILABLE"
}
```

---

## Запуск проекта

### Требования
- Java 21+
- PostgreSQL (запущенный экземпляр)
- Maven

### Настройка базы данных

Создайте базу данных PostgreSQL и заполните `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservation_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Запуск

```bash
# Через Maven Wrapper
./mvnw spring-boot:run

# Или сборка и запуск JAR
./mvnw clean package
java -jar target/reservation-system-0.0.1-SNAPSHOT.jar
```

Приложение запустится на порту **8080** (по умолчанию).

---

## Обработка ошибок

Все ошибки возвращаются в едином JSON-формате:

```json
{
  "message": "Bad request",
  "detailedMessage": "Start date must be 1 day earlier than end date",
  "errorTime": "2025-09-20T10:15:30"
}
```

| HTTP-статус | Причина |
|---|---|
| `400 Bad Request` | Неверные входные данные / нарушение бизнес-правил |
| `404 Not Found` | Бронирование с указанным ID не найдено |
| `500 Internal Server Error` | Непредвиденная ошибка сервера |
