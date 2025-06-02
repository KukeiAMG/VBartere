package com.example.payment.kafka.DTO;




public class UserIdDTO{
    private Long userId;


    public UserIdDTO() {
    }

    public UserIdDTO(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserIdDTO{" +
                "userId=" + userId +
                '}';
    }
}