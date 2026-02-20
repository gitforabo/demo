package school.sorokin.reservation.web;

import java.time.LocalDateTime;

// DTO для ответа при возникновении ошибки.
// Вместо "белой страницы с ошибкой" клиент получает структурированный JSON с полями:
//   message        — краткое описание ошибки (например, "Bad request")
//   detailedMessage — детальное сообщение исключения (e.getMessage())
//   errorTime       — время, когда произошла ошибка
public record ErrorResponseDto(
                String message,
                String detailedMessage,
                LocalDateTime errorTime) {

}
