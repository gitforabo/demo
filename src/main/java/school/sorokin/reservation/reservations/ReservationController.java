package school.sorokin.reservation.reservations;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

// REST API для получения бронирования по id.
@RestController  // — говорит Spring, что этот класс принимает запросы от пользователя и возвращает JSON
@RequestMapping("/reservation") // базовый путь, все URL начинаются с /reservations
public class ReservationController {  // контроллер, который обрабатывает HTTP-запросы.
    
    private static final Logger log = LoggerFactory.getLogger(RestController.class);

    private final ReservationService reservationService; // контроллер не хранит данные сам, а обращается к сервису.
   // контроллер → принимает запрос, сервис → бизнес-логика (например, работа с БД)

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService; // Spring автоматически подставит (инжектит) объект ReservationService.
    } // это называется Dependency Injection (DI).

    // ------ GET reservation by id ------
    @GetMapping("/{id}") // — метод срабатывает на GET (например: http://localhost:8080/reservations/1)
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id  // @PathVariable("id") → извлекает параметры из URL (id)
    ) { 
        log.info("Called getReservationById id = " + id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(reservationService.getReservationById(id));
    }      // возвращает объект Reservation → Spring автоматически превращает его в JSON.

    // ------ GET ALL reservations ------
    @GetMapping() // — метод срабатывает на GET (например: http://localhost:8080/reservations/1)
    public ResponseEntity<List<Reservation>> getAllReservations(
        @RequestParam("roomId") Long roomId,
        @RequestParam("userId") Long userId,
        @RequestParam("pageSize") Integer pageSize,
        @RequestParam("pageNumber") Integer pageNumber
    ) {
        log.info("Called getAllReservations");
        var filter = new ReservationSearchFilter(roomId, userId, pageSize, pageNumber);
        
        return ResponseEntity.ok(reservationService.searchAllByFilter(filter));
    } 

    // ------ CREATE reservation ------
    @PostMapping()
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservationToCreate
    ) {
        log.info("Called createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("test-header", "123")
                .body(reservationService.createReservation(reservationToCreate));
    }

    // ------ UPDATE reservation ------
    @PostMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id, 
            @RequestBody @Valid Reservation reservationToUpdate
    ) {
        log.info("Called updateReservation id = {}, reservationToUpdate = {}",
                id, reservationToUpdate);
        var updated = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.ok(updated);
    }

    // ------ DELETE reservation ------
     @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation: id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok()
                    .build();
    }

    // ------ APPROVE reservation ------
    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }
}

// CRUD
// post create: http://localhost:8081/reservations
//  {
//     "userId": 8,
//     "roomId": 7,
//     "startDate": "2025-09-20",
//     "endDate": "2025-09-29"
//  }
// get getAll: http://localhost:8081/reservations
   