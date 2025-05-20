package com.example.ChatService.model;

import java.util.List;
import java.util.Map;

public class UserDetails {
    private Long userId;
    private String phoneNumber;
    private List<Map<String, Object>> roles;

    public UserDetails() {
    }

    public UserDetails(Long userId, String phoneNumber, List<Map<String, Object>> roles) {
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Map<String, Object>> getRoles() {
        return roles;
    }

    public void setRoles(List<Map<String, Object>> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "UserDetails{" +
                "userId=" + userId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", roles=" + roles +
                '}';
    }
}

