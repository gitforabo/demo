# ЭТАП 1: СБОРКА (BUILD)
# Используем образ с JDK 21 для компиляции через Maven Wrapper
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# Даём права на выполнение Maven Wrapper и собираем JAR, пропуская тесты
RUN chmod +x mvnw && ./mvnw package -DskipTests

# ЭТАП 2: ЗАПУСК (RUNTIME)
# Используем легкий образ только с JRE (без лишних инструментов сборки)
FROM eclipse-temurin:21-jre
WORKDIR /app
# Копируем только готовый JAR из первого этапа (build)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]