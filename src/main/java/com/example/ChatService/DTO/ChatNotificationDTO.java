package com.example.ChatService.DTO;

import java.util.List;

public class ChatNotificationDTO {
    public static final String TYPE_ROOM_CREATED = "ROOM_CREATED";
    public static final String TYPE_ROOM_DELETED = "ROOM_DELETED";
    
    private String type;
    private String roomId;
    private List<String> participants;

    public ChatNotificationDTO() {
    }

    public ChatNotificationDTO(String type, String roomId, List<String> participants) {
        this.type = type;
        this.roomId = roomId;
        this.participants = participants;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "ChatNotificationDTO{" +
                "type='" + type + '\'' +
                ", roomId='" + roomId + '\'' +
                ", participants=" + participants +
                '}';
    }
}