package school.sorokin.reservation.reservations.availability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

// REST-контроллер для проверки доступности комнаты.
// Позволяет клиенту заранее узнать, свободна ли комната в нужный период — до того как создавать бронирование.
@RestController
@RequestMapping("/reservation/availability") // базовый путь: /reservation/availability
public class ReservationAvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvailabilityService service; // делегируем бизнес-логику сервису

    // Dependency Injection через конструктор — Spring автоматически передаёт сервис
    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    // ------ CHECK availability ------
    // POST /reservation/availability/check
    // Принимает: { "roomId": 7, "startDate": "2025-09-20", "endDate": "2025-09-29" }
    // Возвращает: { "message": "Room available to reservation", "status": "AVAILABLE" }
    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @RequestBody @Valid CheckAvailabilityRequest request // @Valid — запускает валидацию @NotNull полей
    ) {
        log.info("Called method checkAvailability: request = {}", request);

        var isAvailable = service.isReservationAvailable(
                request.roomId(), request.startDate(), request.endDate());

        // Формируем ответ в зависимости от результата проверки
        var message = isAvailable ? "Room available to resrevation" : "Room not available to resrevation";
        var status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.RESERVED;

        return ResponseEntity.status(200)
                .body(new CheckAvailabilityResponse(message, status));
    }
}
