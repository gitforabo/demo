package school.sorokin.reservation.reservations.availability;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import school.sorokin.reservation.reservations.ReservationRepository;
import school.sorokin.reservation.reservations.ReservationStatus;

// Сервис для проверки доступности комнаты.
// Используется как контроллером доступности, так и основным ReservationService (при подтверждении бронирования).
// Отвечает на вопрос: "Есть ли уже подтверждённые бронирования на комнату в данный период?"
@Service
public class ReservationAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    private final ReservationRepository repository; // используем репозиторий для поиска конфликтов в БД

    // Dependency Injection через конструктор
    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    // Возвращает true, если комната свободна (нет конфликтующих APPROVED бронирований),
    // и false, если комната уже занята на пересекающийся период.
    public boolean isReservationAvailable(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate) {
        // Базовая валидация дат
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
        }

        // Ищем в БД уже подтверждённые (APPROVED) бронирования на эту комнату, которые пересекаются по датам.
        // Если список пуст — комната свободна.
        List<Long> conflictingIds = repository.findConflictReservationIds(
                roomId,
                startDate,
                endDate,
                ReservationStatus.APPROVED // проверяем только подтверждённые бронирования (не PENDING, не CANCELLED)
        );
        if (conflictingIds.isEmpty()) {
            return true; // конфликтов нет — комната доступна
        }
        log.info("Conflicting with: ids = {}", conflictingIds); // логируем, с какими бронированиями конфликт
        return false; // есть конфликт — комната занята
    }
}
