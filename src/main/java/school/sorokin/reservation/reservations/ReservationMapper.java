package school.sorokin.reservation.reservations;

public class ReservationMapper {

    public Reservation toDomain(ReservationEntity reservationEntity) {
        return new Reservation(
                    reservationEntity.getId(),
                    reservationEntity.getUserId(),
                    reservationEntity.getRoomId(),
                    reservationEntity.getStartDate(),
                    reservationEntity.getEndDate(),
                    reservationEntity.getStatus()
        );
    }
    
    public ReservationEntity toEntity(Reservation reservation) {
        return new ReservationEntity(
                    reservation.id(),
                    reservation.userId(),
                    reservation.roomId(),
                    reservation.startDate(),
                    reservation.endDate(),
                    reservation.status()
        );
    }
    
}
