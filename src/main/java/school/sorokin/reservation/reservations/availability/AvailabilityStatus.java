package school.sorokin.reservation.reservations.availability;

// Перечисление статусов доступности комнаты для бронирования.
// Используется в ответе на запрос проверки доступности (CheckAvailabilityResponse).
public enum AvailabilityStatus {
    AVAILABLE, // комната свободна в указанный период — можно бронировать
    RESERVED // комната уже занята (есть подтверждённое бронирование на эти даты)
}
