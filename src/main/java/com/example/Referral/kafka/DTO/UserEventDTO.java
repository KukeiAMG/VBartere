package com.example.Referral.kafka.DTO;

public class UserEventDTO {

    //NOTE
    // Отсутствует пустой конструктор - необходим для Jackson при десериализации JSON
    // Нет аннотаций валидации (например, @NotNull для обязательных полей)
    // Не реализованы equals()/hashCode() - может вызвать проблемы при сравнении объектов
    // Уязвимость к null-значениям - нет обработки случая, когда invitedByCode может быть null

    private Long userId;
    private String invitedByCode;

    public UserEventDTO(Long userId, String invitedByCode) {
        this.userId = userId;
        this.invitedByCode = invitedByCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getInvitedByCode() {
        return invitedByCode;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setInvitedByCode(String invitedByCode) {
        this.invitedByCode = invitedByCode;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "userId=" + userId +
                ", invitedByCode='" + invitedByCode + '\'' +
                '}';
    }
}
