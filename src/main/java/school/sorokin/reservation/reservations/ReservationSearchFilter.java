package school.sorokin.reservation.reservations;

public record ReservationSearchFilter(
    Long roomId,
    Long userId,
    Integer pageSize,
    Integer pageNumber
) {

}
