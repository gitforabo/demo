package school.sorokin.reservation;

import java.time.LocalDate;

public record Reservation(  // Reservation (модель / DTO). record — упрощённый класс в Java, запись.
                          // Здесь описывается объект бронирования:
    Long id,
    Long userId,
    Long roomId,
    LocalDate startDate,
    LocalDate endDate,
    ReservationStatus status
) {
}

