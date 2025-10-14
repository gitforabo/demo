package school.sorokin.reservation.reservations;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import school.sorokin.reservation.reservations.availability.ReservationAvailabilityService;

@Service
public class ReservationService { // Это сервис, который отвечает за бизнес-логику.

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(
        ReservationRepository repository,
        ReservationMapper mapper,
        ReservationAvailabilityService availabilityService
    ) {
        this.repository = repository; // DI
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    // ------ GET reservation by id------
    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(
                            "Not found reservation by id = " + id));
        return mapper.toDomain(reservationEntity);
    }

    // ------ GET ALL reservations ------
    public List<Reservation> searchAllByFilter(
        ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;

        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchByFilter(
            filter.roomId(), 
            filter.userId(), 
            pageable
        );

        return allEntities.stream().map(mapper::toDomain).toList();
    } 

    // ------ CREATE reservation ------
    public Reservation createReservation(Reservation reservationToCreate) {

        if(reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
        }

        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        ReservationEntity savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);            
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
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
        }

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
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

        var isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate()
        );

        if (!isAvailableToApprove) {
            throw new IllegalStateException("Cannot approve reservatoion because of conflict" + reservationEntity.getStatus());
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }


   
}
