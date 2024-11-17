# Kafka Testing Project

Этот проект предназначен для тестирования интеграции с Apache Kafka. Следуйте приведенным ниже инструкциям для настройки и тестирования основных функций.

## Требования

- Запущенные экземпляры Zookeeper и Kafka.
- Инструмент для отправки HTTP-запросов, например, [Postman](https://www.postman.com/).

## Шаги по тестированию

### Шаг 1: Регистрация пользователя

Для начала необходимо зарегистрировать нового пользователя. Отправьте POST-запрос на следующий URL:

```
POST http://localhost:8081/api/users/register
```

**Пример тела запроса (JSON):**

```json
{
  "password": "your_password",
  "phoneNumber": "your_phone_number"
}
```

### Шаг 2: Аутентификация пользователя

После успешной регистрации получите JWT-токен. Для этого отправьте POST-запрос на следующий URL, указав в заголовках `Authorization` значение `Bearer + jwtToken`:

```
POST http://localhost:8081/api/users/login
```

**Пример тела запроса (JSON):**

```json
{
  "phoneNumber": "your_phone_number",
  "password": "your_password"
}
```

**Важно:** В ответе вы получите `jwtToken`, который потребуется для последующих запросов.

### Шаг 3: Добавление объявления

Используя полученный `jwtToken`, отправьте POST-запрос на следующий URL, чтобы добавить объявление:

```
POST http://localhost:8081/api/advertisements/{advertisementId}/add
```

**Заголовки запроса:**

- `Authorization`: Bearer + jwtToken

**Где:**
- `advertisementId` — это идентификатор объявления, которое вы хотите добавить.

## Настройка Kafka и Zookeeper

Перед началом работы убедитесь, что у вас запущены экземпляры Zookeeper и Kafka. Вы можете использовать следующие команды для запуска из директории установки Kafka:

### Запуск Zookeeper

```cmd
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties. 
```

### Запуск Kafka

```cmd
.\bin\windows\kafka-server-start.bat .\config\server.properties
```
