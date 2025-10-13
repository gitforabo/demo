package school.sorokin.reservation.reservations;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record Reservation(  // Reservation (модель / DTO). record — упрощённый класс в Java, запись.
                          // Здесь описывается объект бронирования:
    @Null
    Long id,
    @NotNull
    Long userId,
    @NotNull
    Long roomId,
    @FutureOrPresent
    @NotNull
    LocalDate startDate,
    @FutureOrPresent
    @NotNull
    LocalDate endDate,
    ReservationStatus status
) {
}

