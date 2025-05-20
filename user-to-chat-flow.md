# Процесс создания и удаления чата между пользователями

Данная диаграмма описывает процесс взаимодействия между микросервисами при создании и удалении чата между пользователями.

## Описание потока

1. User Microservice инициирует событие, отправляя JSON-сообщение в Kafka топик "user-chat"
2. KafkaConsumerService прослушивает этот топик и обрабатывает входящие события
3. При получении события выполняется:
   - Проверка сообщения на null
   - Десериализация JSON в объект UserEventDTO
   - Валидация обязательных полей (eventType, userId, targetUserId)
4. В зависимости от типа события (CREATE_CHAT или DELETE_CHAT) вызывается соответствующий метод ChatRoomService
5. ChatRoomService выполняет бизнес-логику:
   - Валидирует ID пользователей
   - Генерирует ID комнаты
   - Сохраняет или удаляет запись в базе данных через ChatRoomRepository

## Задействованные файлы

- `KafkaConsumerService.java` - обработка Kafka событий и их маршрутизация
- `ChatRoomService.java` - бизнес-логика управления чат-комнатами
- `ChatRoomRepository.java` - взаимодействие с базой данных


```mermaid
sequenceDiagram
    participant UserMS as User Microservice
    participant Kafka as Kafka Topic:<br/>user-chat
    participant Consumer as KafkaConsumerService
    participant Service as ChatRoomService
    participant Producer as KafkaProducerService
    participant DB as ChatRoomRepository

    Note over UserMS,Consumer: Входящий JSON в user-chat:<br/>{<br/>  "eventType": "CREATE_CHAT",<br/>  "userId": "user1",<br/>  "targetUserId": "user2"<br/>}

    UserMS->>Kafka: Публикация события создания чата
    Kafka->>Consumer: @KafkaListener(topics = "user-chat")
    
    Note over Consumer: Десериализация и валидация

    Consumer->>Service: createChatRoom(userId, targetUserId)
    Service->>DB: save(new ChatRoom())
    DB-->>Service: Возврат сохраненной комнаты

    %% Следующие 2-3 шага после создания комнаты
    Service->>Producer: Отправка уведомления о создании
    Note over Producer: Исходящий JSON в chat.notifications:<br/>{<br/>  "type": "ROOM_CREATED",<br/>  "roomId": "user1_user2",<br/>  "participants": ["user1", "user2"]<br/>}
    Producer->>Kafka: Публикация в топик chat.notifications
``` 

```mermaid
sequenceDiagram
    participant ChatService as Chat Service
    participant Kafka as Kafka Topic:<br/>chat.notifications
    participant UserConsumer as UserKafkaConsumer
    participant UserService as UserNotificationService
    participant UserWS as UserWebSocketHandler

    Note over ChatService,Kafka: Исходящий JSON в chat.notifications:<br/>{<br/>  "type": "ROOM_CREATED",<br/>  "roomId": "user1_user2",<br/>  "participants": ["user1", "user2"]<br/>}

    ChatService->>Kafka: Публикация уведомления
    Kafka->>UserConsumer: @KafkaListener(topics = "chat.notifications")
    
    Note over UserConsumer: Десериализация в<br/>ChatNotificationDTO

    UserConsumer->>UserService: processNotification(notification)
    
    loop Для каждого участника
        UserService->>UserWS: sendNotification(userId, notification)
        Note over UserWS: Отправка через WebSocket<br/>подключенному пользователю
    end
``` 