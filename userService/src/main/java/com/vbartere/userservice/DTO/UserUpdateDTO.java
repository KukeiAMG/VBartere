package com.vbartere.userservice.DTO;

import java.util.Set;

public class UserUpdateDTO {
    private String phoneNumber;
    private String password;
    private String name;
    private String surname;
    private String invitedByCode;
    private String email;
    private Boolean isBanned;
    private Set<Long> roleIds;

    public UserUpdateDTO() {}

    public UserUpdateDTO(String phoneNumber, String password, String name, String surname, String invitedByCode, String email, Boolean isBanned, Set<Long> roleIds) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.invitedByCode = invitedByCode;
        this.email = email;
        this.isBanned = isBanned;
        this.roleIds = roleIds;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getInvitedByCode() {
        return invitedByCode;
    }

    public void setInvitedByCode(String invitedByCode) {
        this.invitedByCode = invitedByCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getBanned() {
        return isBanned;
    }

    public void setBanned(Boolean banned) {
        isBanned = banned;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
