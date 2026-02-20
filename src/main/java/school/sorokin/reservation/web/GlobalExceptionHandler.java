package school.sorokin.reservation.web;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

// Глобальный обработчик исключений.
// Перехватывает исключения из всех контроллеров и возвращает клиенту понятный JSON-ответ вместо "500 Internal Server Error".
// Без этого класса Spring вернёт HTML-страницу с ошибкой — неудобно для API.
@ControllerAdvice // — применяет этот класс ко всем контроллерам в приложении
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------ Обработка любого непредвиденного исключения ------
    // Срабатывает, если ни один другой обработчик не подошёл → HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handlerGenericException(Exception e) {

        log.error("Handle exception", e);

        var errorDto = new ErrorResponseDto(
                "Internal server error",
                e.getMessage(),
                LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorDto);
    }

    // ------ Обработка: сущность не найдена в БД ------
    // Выбрасывается в сервисе через orElseThrow() → HTTP 404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(
            EntityNotFoundException e) {

        log.error("Handle entityNotFoundException", e);

        var errorDto = new ErrorResponseDto(
                "EntityNotFoundException",
                e.getMessage(),
                LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorDto);
    }

    // ------ Обработка: неверные входные данные ------
    // IllegalArgumentException — бизнес-ошибка (например, endDate раньше startDate)
    // IllegalStateException — ошибка состояния (например, нельзя изменить не-PENDING бронирование)
    // MethodArgumentNotValidException — ошибка валидации аннотаций (@NotNull, @FutureOrPresent и т.д.)
    // Все они → HTTP 400 Bad Request
    @ExceptionHandler(exception = {
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequest(
            Exception e) {

        log.error("Handle handleBadRequest", e);

        var errorDto = new ErrorResponseDto(
                "Bad request",
                e.getMessage(),
                LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }
}
