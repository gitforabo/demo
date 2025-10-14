package school.sorokin.reservation.reservations.availability;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import school.sorokin.reservation.reservations.ReservationRepository;
import school.sorokin.reservation.reservations.ReservationStatus;

@Service
public class ReservationAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    private final ReservationRepository repository;

    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

     public boolean isReservationAvailable(
            Long roomId, 
            LocalDate startDate, 
            LocalDate endDate
    ) {
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
        }

        List<Long> conflictingIds = repository.findConflictReservationIds(
            roomId, 
            startDate, 
            endDate, 
            ReservationStatus.APPROVED
        );
        if (conflictingIds.isEmpty()) {
            return true;
        }
        log.info("Conflicting with: ids = {}", conflictingIds);
        return false;
    }
}
