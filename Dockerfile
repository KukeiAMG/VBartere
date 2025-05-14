# Базовый образ
FROM eclipse-temurin:17-jdk-jammy

# Рабочая директория
WORKDIR /app

# Копируем собранный JAR
COPY target/ChatService-0.0.1-SNAPSHOT.jar app.jar

# Открываем порт
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]mn hjv