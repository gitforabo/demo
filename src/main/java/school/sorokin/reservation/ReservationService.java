package school.sorokin.reservation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

@Service
public class ReservationService { // Это сервис, который отвечает за бизнес-логику.

    private final Map<Long, Reservation> reservationMap;
  // Map.of(1L, new Reservation(1L, 14L, 33L, LocalDate.now(), LocalDate.now().plusDays(5), ReservationStatus.PENDING));
    private final AtomicLong isCounter;

    public ReservationService() {
        this.reservationMap = new HashMap<>();
        this.isCounter = new AtomicLong();
    }

    public Reservation getReservationById(Long id) {
        if(!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Not found reservation by id" + id);
        }
        return reservationMap.get(id);
    }

    public List<Reservation> findAllReservation() {
       return reservationMap.values().stream().toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {

        if(reservationToCreate.id() != null) {
            throw new IllegalArgumentException("Id shoud be empty");
        }
        if(reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }

        Reservation newReservation = new Reservation(
                    isCounter.incrementAndGet(), 
                    reservationToCreate.userId(),
                    reservationToCreate.roomId(), 
                    reservationToCreate.startDate(), 
                    reservationToCreate.endDate(), 
                    ReservationStatus.PENDING);

        reservationMap.put(newReservation.id(), newReservation);
        return newReservation;            

    }
}