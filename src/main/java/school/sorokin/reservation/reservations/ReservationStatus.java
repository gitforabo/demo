package school.sorokin.reservation.reservations;

// Перечисление (enum) — список возможных статусов бронирования.
// Статус хранится в базе данных как строка (STRING), а не число — благодаря @Enumerated(EnumType.STRING) в ReservationEntity.
public enum ReservationStatus {
    PENDING, // Ожидает подтверждения — начальный статус после создания бронирования
    APPROVED, // Подтверждено менеджером
    CANCELLED // Отменено пользователем или системой
}