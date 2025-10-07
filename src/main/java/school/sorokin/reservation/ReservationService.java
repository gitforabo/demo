package school.sorokin.reservation;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ReservationService { // Это сервис, который отвечает за бизнес-логику.

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository; // DI
    }

    // ------ GET reservation by id------
    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                            "Not found reservation by id = " + id));
        return toDomainReservation(reservationEntity);
    }

    // ------ GET ALL reservations ------
    public List<Reservation> findAllReservation() {
        List<ReservationEntity> allEntities = repository.findAll();
        return allEntities.stream()
            .map(this::toDomainReservation).toList();
        //return reservationMap.values().stream().toList();
    } // .map(it -> toDomainReservation(it)).toList();

    // ------ CREATE reservation ------
    public Reservation createReservation(Reservation reservationToCreate) {

        if(reservationToCreate.id() != null) {
            throw new IllegalArgumentException("Id shoud be empty");
        }
        if(reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }

        var entityToSave = new ReservationEntity(
                  null,
                    reservationToCreate.userId(),
                    reservationToCreate.roomId(), 
                    reservationToCreate.startDate(), 
                    reservationToCreate.endDate(), 
                    ReservationStatus.PENDING);

        ReservationEntity savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);            
    }

    // ------ UPDATE reservation ------
    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found reservation by id = " + id);
        }

        var reservationEntity = repository.findById(id) 
                    .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservatoion: status = " + reservationEntity);
        }

        var reservationToSave = new ReservationEntity(
                    reservationEntity.getId(),
                    reservationToUpdate.userId(),
                    reservationToUpdate.roomId(),
                    reservationToUpdate.startDate(),
                    reservationToUpdate.endDate(),
                    ReservationStatus.PENDING
        );

        var updatedReservation = repository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

    // ------ DELETE reservation ------
   @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        }
        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel the reservation. Reservation was already cancelled");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
    }

    // ------ APPROVE reservation ------
    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approved reservatoion: status = " + reservationEntity.getStatus());
        }

        var isConflict = isReservationConflict(reservationEntity);

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservatoion because of conflict" + reservationEntity.getStatus());
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return toDomainReservation(reservationEntity);
    }


    private boolean isReservationConflict(ReservationEntity reservation) {
        var allReservations = repository.findAll();

        for (ReservationEntity existingReservation : allReservations) {
            if (reservation.getId().equals(existingReservation.getId())) {
                continue;
            }
             if (!reservation.getRoomId().equals(existingReservation.getRoomId())) {
                continue;
            }
            if (!existingReservation.getStatus().equals(ReservationStatus.APPROVED)) {
                continue;
            }
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate())
                && existingReservation.getStartDate().isBefore(reservation.getEndDate())) {
                    return true;
            }
        }
        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservation) {
        return new Reservation(
                    reservation.getId(),
                    reservation.getUserId(),
                    reservation.getRoomId(),
                    reservation.getStartDate(),
                    reservation.getEndDate(),
                    reservation.getStatus()
                );
    }
}