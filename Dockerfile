# === Этап 1: сборка ===
# Используем Maven с Java 21 для компиляции
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Копируем pom.xml отдельно — Docker закэширует зависимости,
# и при изменении только кода они не будут перекачиваться заново
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходники и собираем JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# === Этап 2: запуск ===
# Минимальный образ только с JRE для запуска приложения
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем собранный JAR из этапа сборки
COPY --from=builder /app/target/*.jar app.jar

# Render выставляет переменную $PORT случайным образом — приложение
# уже учитывает её в application.properties (server.port=${PORT:8080})
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]