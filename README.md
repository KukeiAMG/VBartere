# Advertisement Management System

![Docker](https://img.shields.io/badge/Docker-3.8-%232496ED?logo=docker)
![Kafka](https://img.shields.io/badge/Kafka-7.3.1-%23000000?logo=apachekafka)
![Redis](https://img.shields.io/badge/Redis-7.2-%23DC382D?logo=redis)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-%236DB33F?logo=spring)

Управление объявлениями с поддержкой категорий, изображений и асинхронной обработкой через **Apache Kafka**.  
Проект включает полноценный REST API, мониторинг через Kafka UI и кэширование на Redis.

---

## Особенности
- **Мультифайловая загрузка** изображений
- **Иерархия категорий** с привязкой подкатегорий
- **Асинхронная обработка** событий через Kafka
- **Визуальный мониторинг** топиков и потребителей
- **Кэширование** данных через Redis

---

## Технологический стек
| Компонент       | Версия   | Назначение                          |
|-----------------|----------|-------------------------------------|
| Java            | 17       | Основной язык разработки           |
| Spring Boot     | 3.0      | Бэкенд-фреймворк                   |
| Apache Kafka    | 7.3.1    | Брокер сообщений                   |
| Redis           | 7.2      | Кэширование и сессии               |
| Docker          | 3.8      | Контейнеризация сервисов           |

---

## Быстрый старт с Docker

### 1. Запуск инфраструктуры
```bash
docker-compose up -d --build
```

### 2. Доступ к сервисам
```bash
Сервис      | URL/Команда
Kafka UI    | http://localhost:8080
Redis CLI   | docker exec -it redis-container redis-cli
```
---

## API Документация

### Аутентификация (Аутентификация происходит в микросервисе UserService)
#### Шаг 1: Регистрация пользователя

Для начала необходимо зарегистрировать нового пользователя. Отправьте POST-запрос на следующий URL:

```
POST /api/users/register
```

**Пример тела запроса (JSON):**

```json
{
  "password": "your_password",
  "phoneNumber": "your_phone_number"
}
```

#### Шаг 2: Аутентификация пользователя

После успешной регистрации получите JWT-токен. Для этого отправьте POST-запрос на следующий URL, указав в заголовках `Authorization` значение `Bearer + jwtToken`:

```
POST /api/users/login
```

**Пример тела запроса (JSON):**

```json
{
  "phoneNumber": "your_phone_number",
  "password": "your_password"
}
```

**Важно:** В ответе вы получите `jwtToken`, который потребуется для последующих запросов.

#### Шаг 3: Добавление объявления

Используя полученный `jwtToken`, отправьте POST-запрос на следующий URL, чтобы добавить объявление:

```
POST /api/advertisements/{advertisementId}/add
```

**Заголовки запроса:**
- `Authorization`: Bearer + jwtToken

**Где:**
- `advertisementId` — это идентификатор объявления, которое вы хотите добавить.

---

### **Работа с объявлениями**


**Пример тела запроса (JSON):**
```
POST /api/advertisements/create
```

```
Content-Type: multipart/form-data
user-ID: 123
```

``` json
advertisement: {
  "title": "MacBook Pro 16",
  "price": 249999,
  "subCategoryId": 2
}
files: @photo1.jpg, @photo2.jpg
```

#### Пример ответа:
``` json
{
  "id": 42,
  "status": "PENDING_KAFKA_APPROVAL",
  "images": ["photo1.jpg", "photo2.jpg"]
}
```
---
## Конфигурация
Настройки приложения (application.properties)
##### Kafka
spring.kafka.bootstrap-servers=kafka:9092
spring.kafka.consumer.auto-offset-reset=earliest

##### Redis
spring.data.redis.timeout=5000ms
