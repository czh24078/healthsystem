package com.healthsys.dao;

import com.healthsys.common.entity.ExamRecord;
import com.healthsys.common.util.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamRecordDAO {

    // 添加体检记录
    public boolean addExamRecord(ExamRecord record) {
        String sql = "INSERT INTO exam_records (appointment_id, test_id, result_value, exam_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, record.getAppointmentId());
            stmt.setLong(2, record.getTestId());
            stmt.setString(3, record.getResultValue());
            stmt.setObject(4, record.getExamDate());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据预约ID获取所有体检结果
    public List<ExamRecord> getExamRecordsByAppointment(Long appointmentId) {
        List<ExamRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM exam_records WHERE appointment_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(new ExamRecord(
                        rs.getLong("id"),
                        rs.getLong("appointment_id"),
                        rs.getLong("test_id"),
                        rs.getString("result_value"),
                        rs.getObject("exam_date", LocalDateTime.class)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    // 更新体检结果
    public boolean updateExamResult(Long appointmentId, Long testId, String resultValue) {
        String sql = "UPDATE exam_records SET result_value = ?, exam_date = NOW() WHERE appointment_id = ? AND test_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resultValue);
            stmt.setLong(2, appointmentId);
            stmt.setLong(3, testId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 检查是否已有该检查项的记录
    public boolean existsRecord(Long appointmentId, Long testId) {
        String sql = "SELECT COUNT(*) FROM exam_records WHERE appointment_id = ? AND test_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, appointmentId);
            stmt.setLong(2, testId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
