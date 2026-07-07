package com.ncu.Common;

import java.time.LocalDateTime;

public class ExamRecord {
    private Long id;
    private Long userId;          // 用户ID
    private Long groupId;         // 检查组ID
    private Long itemId;          // 检查项ID
    private Long appointmentId;   // 预约ID
    private Long testId;          // 测试ID
    private String resultValue;   // 检查结果
    private LocalDateTime examDate; // 体检时间
    private LocalDateTime createdAt; // 创建时间

    // 显示用字段
    private String userName;      // 用户名
    private String groupName;     // 检查组名称
    private String itemName;      // 检查项名称

    public ExamRecord() {}

    public ExamRecord(Long id, Long appointmentId, Long testId, String resultValue, LocalDateTime examDate) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.testId = testId;
        this.resultValue = resultValue;
        this.examDate = examDate;
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

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public LocalDateTime getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDateTime examDate) {
        this.examDate = examDate;
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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return "ExamRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", groupId=" + groupId +
                ", itemId=" + itemId +
                ", appointmentId=" + appointmentId +
                ", testId=" + testId +
                ", resultValue='" + resultValue + '\'' +
                ", examDate=" + examDate +
                ", createdAt=" + createdAt +
                ", userName='" + userName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}

