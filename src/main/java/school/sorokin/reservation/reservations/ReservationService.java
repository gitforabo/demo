package school.sorokin.reservation;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import school.sorokin.reservation.reservations.ReservationMapper;

@Service
public class ReservationService { // Это сервис, который отвечает за бизнес-логику.

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationMapper mapper;

    public ReservationService(
        ReservationRepository repository,
        ReservationMapper mapper
    ) {
        this.repository = repository; // DI
        this.mapper = mapper;
    }

    // ------ GET reservation by id------
    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                            "Not found reservation by id = " + id));
        return mapper.toDomain(reservationEntity);
    }

    // ------ GET ALL reservations ------
    public List<Reservation> findAllReservation() {
        List<ReservationEntity> allEntities = repository.findAll();
        return allEntities.stream()
            .map(this::mapper.toDomain()).toList();
        //return reservationMap.values().stream().toList();
    } // .map(it -> toDomainReservation(it)).toList();

    // ------ CREATE reservation ------
    public Reservation createReservation(Reservation reservationToCreate) {

        if(reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }
        if (reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
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
        if (reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
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

        var isConflict = isReservationConflict(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if (isConflict) {
            throw new IllegalStateException("Cannot approve reservatoion because of conflict" + reservationEntity.getStatus());
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return toDomainReservation(reservationEntity);
    }


    private boolean isReservationConflict(
            Long roomId, 
            LocalDate startDate, 
            LocalDate endDate
    ) {
        List<Long> conflictingIds = repository.findConflictReservationIds(
            roomId, 
            startDate, 
            endDate, 
            ReservationStatus.APPROVED
        );
        if (conflictingIds.isEmpty()) {
            return false;
        }
        log.info("Conflicting with: ids = {}", conflictingIds);
        return true;
    }

    
}