package school.sorokin.reservation.reservations;

import org.springframework.stereotype.Component;

// Маппер — конвертирует объекты между двумя представлениями:
//   ReservationEntity  ←→  Reservation (DTO / domain)
//
// Зачем разделять? ReservationEntity — это то, что хранится в БД (с аннотациями JPA).
// Reservation — это то, что мы отдаём пользователю через API (без лишних деталей БД).
// Маппер — «переводчик» между этими двумя мирами.
@Component // — говорит Spring зарегистрировать этот класс как компонент, чтобы его можно было инжектировать в сервис
public class ReservationMapper {

    // Из БД-сущности в DTO (domain-объект): используется после чтения из базы данных
    public Reservation toDomain(ReservationEntity reservationEntity) {
        return new Reservation(
                reservationEntity.getId(),
                reservationEntity.getUserId(),
                reservationEntity.getRoomId(),
                reservationEntity.getStartDate(),
                reservationEntity.getEndDate(),
                reservationEntity.getStatus());
    }

    // Из DTO в БД-сущность: используется перед сохранением в базу данных
    public ReservationEntity toEntity(Reservation reservation) {
        return new ReservationEntity(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                reservation.status());
    }

}
