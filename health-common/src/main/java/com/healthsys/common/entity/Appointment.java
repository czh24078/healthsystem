package com.healthsys.common.entity;

import java.time.LocalDateTime;

public class Appointment {
    private Long id;
    private Long userId;
    private Long packageId;
    private LocalDateTime appointmentTime;
    private LocalDateTime examTime;
    private String status; // "PENDING","IN_PROGRESS","COMPLETED","CANCELLED"
    private Boolean paymentStatus;
    private LocalDateTime createdAt;

    // 新增字段，用于显示
    private String userName;
    private String packageName;

    public Appointment() {}

    public Appointment(Long userId, Long packageId, LocalDateTime appointmentTime) {
        this.userId = userId;
        this.packageId = packageId;
        this.appointmentTime = appointmentTime;
        this.status = "PENDING";
        this.paymentStatus = false;
    }

    public String getStatusDisplay() {
        return switch(status) {
            case "PENDING" -> "待检查";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "IN_PROGRESS" -> "进行中";
            default -> status;
        };
    }

    public String getPaymentStatusDisplay() {
        return Boolean.TRUE.equals(paymentStatus) ? "已支付" : "未支付";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public LocalDateTime getExamTime() {
        return examTime;
    }

    public void setExamTime(LocalDateTime examTime) {
        this.examTime = examTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Boolean paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}

