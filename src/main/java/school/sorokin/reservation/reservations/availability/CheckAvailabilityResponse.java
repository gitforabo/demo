package school.sorokin.reservation.reservations.availability;

public record CheckAvailabilityResponse(
        String message,
        AvailabilityStatus status
) {

}
