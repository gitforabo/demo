package school.sorokin.reservation.reservations;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Репозиторий — слой доступа к данным (DAO).
// Расширяет JpaRepository, который уже содержит готовые методы: save(), findById(), findAll(), delete() и т.д.
// Spring Data JPA автоматически создаёт реализацию этого интерфейса — писать SQL вручную не нужно.
// JpaRepository<ReservationEntity, Long>:
//   ReservationEntity — тип сущности
//   Long              — тип первичного ключа (id)
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    // ------ Изменить статус бронирования ------
    // @Modifying — указывает, что запрос изменяет данные (UPDATE/DELETE), а не читает их
    // Транзакция (@Transactional) должна быть обеспечена на уровне вызывающего сервиса
    @Modifying
    @Query("""
            update ReservationEntity r
            set r.status = :status
            where r.id = :id
            """)
    void setStatus(
            @Param("id") Long id,
            @Param("status") ReservationStatus reservationStatus);

    // ------ Найти конфликтующие бронирования ------
    // Выбирает id бронирований, которые пересекаются по дате с запрашиваемым периодом.
    // Условие пересечения: startDate < r.endDate AND r.startDate < endDate
    // (две даты пересекаются, если одна начинается раньше, чем заканчивается другая)
    // r.roomId — поле сущности ReservationEntity (столбец в БД)
    // :roomId — аргумент метода, переданный через @Param("roomId")
    @Query("""
            SELECT r.id from ReservationEntity r
                 WHERE r.roomId = :roomId
                 AND :startDate < r.endDate
                 AND r.startDate < :endDate
                 AND r.status = :status
            """)
    List<Long> findConflictReservationIds(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") ReservationStatus status);

    // ------ Поиск по фильтру с пагинацией ------
    // (:roomId IS NULL OR r.roomId = :roomId) — если roomId не передан (null), фильтр по нему игнорируется
    // Pageable — объект пагинации (номер страницы + размер), передаётся из сервиса
    @Query("""
            SELECT r from ReservationEntity r
                 WHERE (:roomId IS NULL OR r.roomId = :roomId)
                 AND (:userId IS NULL OR r.userId = :userId)
            """)
    List<ReservationEntity> searchByFilter(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            Pageable pageable);
}
