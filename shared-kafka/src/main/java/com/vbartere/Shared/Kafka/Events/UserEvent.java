package com.vbartere.Shared.Kafka.Events;

import com.vbartere.Shared.Kafka.Enum.UserEventType;

public class UserEvent {

    private Long id;
    private String name;
    private String email;
    private UserEventType event;

    public UserEvent(Long id, String name, String email, UserEventType event) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserEventType getEvent() {
        return event;
    }

    public void setEvent(UserEventType event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", event=" + event +
                '}';
    }
}
