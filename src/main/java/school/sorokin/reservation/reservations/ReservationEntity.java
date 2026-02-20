package school.sorokin.reservation.reservations;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// JPA-сущность — класс, который отображается на таблицу базы данных.
// Spring Data JPA (через Hibernate) автоматически читает/записывает объекты этого класса в БД.
@Entity // — говорит JPA, что этот класс = таблица в базе данных
@Table(name = "reservations") // — имя таблицы в БД. Если не указать, будет использовано имя класса
public class ReservationEntity {

    @Id // — первичный ключ таблицы
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // — id генерируется автоматически базой данных (AUTO_INCREMENT)
    private Long id;

    @Column(name = "user_id", nullable = false) // nullable = false → в БД поле NOT NULL
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING) // — хранить статус как строку ("PENDING", "APPROVED", "CANCELLED"), а не число
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    // Пустой конструктор (обязателен для JPA — Hibernate создаёт объекты через него при чтении из БД)
    public ReservationEntity() {
    }

    // Конструктор со всеми полями (удобен для создания объекта в коде)
    public ReservationEntity(Long id, Long userId, Long roomId, LocalDate startDate, LocalDate endDate,
            ReservationStatus status) {
        this.id = id;
        this.userId = userId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // getters & setters — стандартный доступ к приватным полям класса
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}