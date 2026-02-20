package school.sorokin.reservation.reservations;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import school.sorokin.reservation.reservations.availability.ReservationAvailabilityService;

// Сервис — слой бизнес-логики.
// Принимает запросы от контроллера, применяет правила (валидация, проверки статусов)
// и обращается к репозиторию для работы с базой данных.
// Цепочка вызовов: Controller → Service → Repository → БД
@Service // — говорит Spring зарегистрировать этот класс как сервис (бин) и управлять им
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository repository; // работа с БД
    private final ReservationMapper mapper; // конвертация Entity ↔ DTO
    private final ReservationAvailabilityService availabilityService; // проверка доступности комнаты

    // Dependency Injection (DI) — Spring сам передаёт нужные объекты в конструктор.
    // Нет необходимости создавать их вручную (new Repository() и т.д.)
    public ReservationService(
            ReservationRepository repository,
            ReservationMapper mapper,
            ReservationAvailabilityService availabilityService) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }

    // ------ GET reservation by id ------
    public Reservation getReservationById(Long id) {
        // findById возвращает Optional — если запись не найдена, выбрасываем исключение
        // Это исключение перехватит GlobalExceptionHandler и вернёт HTTP 404
        ReservationEntity reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found reservation by id = " + id));
        return mapper.toDomain(reservationEntity); // конвертируем из Entity в DTO и возвращаем клиенту
    }

    // ------ GET ALL reservations ------
    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter) {
        // Если клиент не передал pageSize/pageNumber — используем значения по умолчанию
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;

        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber); // создаём объект пагинации

        List<ReservationEntity> allEntities = repository.searchByFilter(
                filter.roomId(),
                filter.userId(),
                pageable);

        // Преобразуем каждую сущность в DTO с помощью маппера
        return allEntities.stream().map(mapper::toDomain).toList();
    }

    // ------ CREATE reservation ------
    public Reservation createReservation(Reservation reservationToCreate) {

        // Валидация: статус не должен быть указан — он устанавливается системой (PENDING)
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status shoud be empty");
        }
        // Валидация: дата окончания должна быть позже даты начала
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
        }

        var entityToSave = mapper.toEntity(reservationToCreate); // конвертируем DTO → Entity для сохранения в БД
        entityToSave.setStatus(ReservationStatus.PENDING); // новое бронирование всегда начинается в статусе PENDING

        ReservationEntity savedEntity = repository.save(entityToSave); // сохраняем в БД, получаем обратно с присвоенным id
        return mapper.toDomain(savedEntity);
    }

    // ------ UPDATE reservation ------
    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Not found reservation by id = " + id);
        }

        // Загружаем существующую запись, чтобы проверить текущий статус
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        // Редактировать можно только бронирования в статусе PENDING
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservatoion: status = " + reservationEntity);
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date must be 1 day erlier than end date");
        }

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId()); // сохраняем тот же id, чтобы обновить, а не создать новую запись
        reservationToSave.setStatus(ReservationStatus.PENDING); // статус остаётся PENDING после обновления

        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
    }

    // ------ CANCEL reservation ------
    // @Transactional — гарантирует, что операция выполнится целиком или откатится при ошибке
    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        // Нельзя отменить уже подтверждённое бронирование — нужно обращаться к менеджеру
        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation. Contact with manager please");
        }
        // Нельзя отменить уже отменённое бронирование
        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel the reservation. Reservation was already cancelled");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED); // меняем статус в БД через кастомный запрос
        log.info("Successfully cancelled reservation: id={}", id);
    }

    // ------ APPROVE reservation ------
    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        // Подтверждать можно только бронирования в статусе PENDING
        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approved reservatoion: status = " + reservationEntity.getStatus());
        }

        // Проверяем, нет ли конфликтующих (уже подтверждённых) бронирований на это время
        var isAvailableToApprove = availabilityService.isReservationAvailable(
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate());

        if (!isAvailableToApprove) {
            throw new IllegalStateException(
                    "Cannot approve reservatoion because of conflict" + reservationEntity.getStatus());
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity); // сохраняем обновлённый статус в БД

        return mapper.toDomain(reservationEntity);
    }

}
