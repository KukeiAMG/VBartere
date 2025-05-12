package com.vbartere.userservice.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterUserRequest {
    @NotNull
    private String phoneNumber;

    @Email(message = "Некорректный email")
    private String email;

    @NotNull
    @Size(min = 6, message = "Пароль должен быть не менее 6 символов")
    private String password;

    private String invitedByCode;

    public RegisterUserRequest(String phoneNumber, String email, String password, String invitedByCode) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.invitedByCode = invitedByCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInvitedByCode() {
        return invitedByCode;
    }

    public void setInvitedByCode(String invitedByCode) {
        this.invitedByCode = invitedByCode;
    }
}
