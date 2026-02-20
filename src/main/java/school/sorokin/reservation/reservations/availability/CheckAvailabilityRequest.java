package school.sorokin.reservation.reservations.availability;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

// DTO запроса на проверку доступности комнаты.
// Клиент отправляет этот объект в теле POST-запроса на /reservation/availability/check.
// @NotNull — поле обязательно, иначе Spring вернёт HTTP 400 (обрабатывается в GlobalExceptionHandler)
public record CheckAvailabilityRequest(
        @NotNull Long roomId, // id комнаты, доступность которой нужно проверить
        @NotNull LocalDate startDate, // желаемая дата начала бронирования
        @NotNull LocalDate endDate // желаемая дата окончания бронирования
) {

}
