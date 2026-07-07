package com.healthsys.common.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Users {
    private Long id;
    private String phone;
    private String password;
    private String name;
    private LocalDate birthDate;
    private String gender; // "MALE","FEMALE","OTHER"
    private String role;   // "USER","MEDICAL"
    private String idNumber;
    private boolean firstLogin = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Users() {}

    public Users(Long id, String phone, String password, String name, LocalDate birthDate,
                 String gender, String role, String idNumber, boolean firstLogin) {
        this.id = id;
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.role = role;
        this.idNumber = idNumber;
        this.firstLogin = firstLogin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
