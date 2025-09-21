package school.sorokin.reservation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST API для получения бронирования по id.
@RestController  // — говорит Spring, что этот класс принимает запросы от пользователя и возвращает JSON
@RequestMapping("/reservations") // базовый путь, все URL начинаются с /reservations
public class ReservationController {  // контроллер, который обрабатывает HTTP-запросы.
    
    private static final Logger log = LoggerFactory.getLogger(RestController.class);

    private final ReservationService reservationService; // контроллер не хранит данные сам, а обращается к сервису.
   // контроллер → принимает запрос, сервис → бизнес-логика (например, работа с БД)

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService; // Spring автоматически подставит (инжектит) объект ReservationService.
    } // это называется Dependency Injection (DI).

    @GetMapping("/{id}") // — метод срабатывает на GET (например: http://localhost:8080/reservations/1)
    public Reservation getReservationById(
        @PathVariable("id") Long id           // @PathVariable("id") → извлекает параметры из URL (id);
    ) {
        log.info("Called getReservationById id = " + id);
        return reservationService.getReservationById(id); 
    }       // возвращает объект Reservation → Spring автоматически превращает его в JSON.

    @GetMapping() // — метод срабатывает на GET (например: http://localhost:8080/reservations/1)
    public List<Reservation> getReservations() {
        log.info("Called getReservations:");
        return reservationService.findAllReservation(); 
    } 
}
   