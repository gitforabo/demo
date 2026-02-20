package school.sorokin.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Точка входа в приложение. Spring Boot запускает весь контекст (IoC-контейнер) отсюда.
@SpringBootApplication // — включает автоконфигурацию, сканирование компонентов и другие базовые
						// функции Spring Boot
public class ReservationSystemApplication {

	public static void main(String[] args) {
		// Запускает Spring-приложение: загружает конфигурацию, создаёт бины, поднимает веб-сервер (Tomcat)
		SpringApplication.run(ReservationSystemApplication.class, args);
	}

}
