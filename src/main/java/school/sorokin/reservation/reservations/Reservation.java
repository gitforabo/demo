package school.sorokin.reservation.reservations;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

// DTO (Data Transfer Object) — объект для передачи данных между клиентом и сервером.
// record — упрощённый неизменяемый класс: Spring автоматически превращает его в JSON при отправке ответа
// и создаёт из JSON при получении запроса (@RequestBody).
public record Reservation(
        @Null // id должен быть null при создании — он генерируется базой данных
        Long id,
        @NotNull // userId обязателен — нельзя создать бронирование без пользователя
        Long userId,
        @NotNull // roomId обязателен — нельзя создать бронирование без комнаты
        Long roomId,
        @FutureOrPresent // дата начала не может быть в прошлом
        @NotNull LocalDate startDate,
        @FutureOrPresent // дата окончания не может быть в прошлом
        @NotNull LocalDate endDate,
        ReservationStatus status // статус бронирования (PENDING / APPROVED / CANCELLED)
) {
}
