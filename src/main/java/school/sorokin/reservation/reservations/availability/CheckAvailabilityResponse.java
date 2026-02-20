package school.sorokin.reservation.reservations.availability;

// DTO ответа на запрос проверки доступности.
// Возвращается клиенту в теле HTTP-ответа в формате JSON.
// Пример ответа:
//   { "message": "Room available to reservation", "status": "AVAILABLE" }
public record CheckAvailabilityResponse(
                String message, // текстовое сообщение для пользователя
                AvailabilityStatus status // статус: AVAILABLE или RESERVED
) {

}
