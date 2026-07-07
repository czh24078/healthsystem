package com.healthsys.service;

import com.healthsys.dao.ExamRecordDAO;
import com.healthsys.common.entity.ExamRecord;

import java.util.List;

public class ExamService {
    private final ExamRecordDAO examRecordDAO = new ExamRecordDAO();

    // 添加体检记录
    public boolean addExamRecord(ExamRecord record) {
        return examRecordDAO.addExamRecord(record);
    }

    // 获取某次预约的所有体检结果
    public List<ExamRecord> getExamRecordsByAppointment(Long appointmentId) {
        return examRecordDAO.getExamRecordsByAppointment(appointmentId);
    }

    // 更新体检结果
    public boolean updateExamResult(Long appointmentId, Long testId, String resultValue) {
        return examRecordDAO.updateExamResult(appointmentId, testId, resultValue);
    }

    // 判断是否存在记录
    public boolean existsRecord(Long appointmentId, Long testId) {
        return examRecordDAO.existsRecord(appointmentId, testId);
    }
}
