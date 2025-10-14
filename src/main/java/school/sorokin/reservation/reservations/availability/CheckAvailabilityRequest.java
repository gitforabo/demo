package school.sorokin.reservation.reservations.availability;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record CheckAvailabilityRequest(
    @NotNull
    Long roomId,
    @NotNull
    LocalDate startDate,
    @NotNull
    LocalDate endDate
) {

}
