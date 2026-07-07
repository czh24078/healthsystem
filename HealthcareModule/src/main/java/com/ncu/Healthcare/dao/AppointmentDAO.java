package com.ncu.Healthcare.dao;

import com.ncu.Common.Appointment;
import com.ncu.Common.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public List<Appointment> search(String userName) {
        List<Appointment> appointments = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT a.*, u.name as user_name, p.name as package_name " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.id " +
                "LEFT JOIN text_packages p ON a.package_id = p.id " +
                "WHERE 1=1");

        if (userName != null && !userName.isEmpty()) {
            sql.append(" AND u.name LIKE ?");
        }

        sql.append(" ORDER BY a.appointment_time DESC");

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (userName != null && !userName.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + userName + "%");
            }


            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setId(rs.getLong("id"));
                    appointment.setUserId(rs.getLong("user_id"));
                    appointment.setPackageId(rs.getLong("package_id"));
                    appointment.setAppointmentTime(rs.getObject("appointment_time", LocalDateTime.class));
                    appointment.setExamTime(rs.getObject("exam_time", LocalDateTime.class));
                    appointment.setStatus(rs.getString("status"));
                    String paymentStatus = rs.getString("payment_status");
                    appointment.setPaymentStatus("PAID".equalsIgnoreCase(paymentStatus));
                    appointment.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                    appointment.setUserName(rs.getString("user_name"));
                    appointment.setPackageName(rs.getString("package_name"));

                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }
    public enum PaymentStatus {
        PAID, UNPAID
    }
    public boolean add(Appointment appointment) {
        String sql = "INSERT INTO appointments (user_id, package_id, appointment_time, exam_time, status, payment_status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, appointment.getUserId());
            pstmt.setLong(2, appointment.getPackageId());
            pstmt.setObject(3, appointment.getAppointmentTime());
            pstmt.setObject(4, appointment.getExamTime());
            pstmt.setString(5, appointment.getStatus());
            pstmt.setBoolean(6, appointment.getPaymentStatus());
            pstmt.setObject(7, LocalDateTime.now());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        appointment.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Appointment appointment) {
        String sql = "UPDATE appointments SET user_id = ?, package_id = ?, appointment_time = ?, " +
                "exam_time = ?, status = ?, payment_status = ? WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, appointment.getUserId());
            pstmt.setLong(2, appointment.getPackageId());
            pstmt.setObject(3, appointment.getAppointmentTime());
            pstmt.setObject(4, appointment.getExamTime());
            pstmt.setString(5, appointment.getStatus());
            pstmt.setBoolean(6, appointment.getPaymentStatus());
            pstmt.setLong(7, appointment.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM appointments WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getAll() {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.*, u.name as user_name, p.name as package_name " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.id " +
                "LEFT JOIN test_packages p ON a.package_id = p.id " +
                "ORDER BY a.appointment_time DESC";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setId(rs.getLong("id"));
                appointment.setUserId(rs.getLong("user_id"));
                appointment.setPackageId(rs.getLong("package_id"));
                appointment.setAppointmentTime(rs.getObject("appointment_time", LocalDateTime.class));
                appointment.setExamTime(rs.getObject("exam_time", LocalDateTime.class));
                appointment.setStatus(rs.getString("status"));
                String paymentStatus = rs.getString("payment_status");
                appointment.setPaymentStatus("PAID".equalsIgnoreCase(paymentStatus));
                appointment.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                appointment.setUserName(rs.getString("user_name"));
                appointment.setPackageName(rs.getString("package_name"));

                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    public Appointment getById(Long id) {
        String sql = "SELECT a.*, u.name as user_name, p.name as package_name " +
                "FROM appointments a " +
                "LEFT JOIN users u ON a.user_id = u.id " +
                "LEFT JOIN packages p ON a.package_id = p.id " +
                "WHERE a.id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setId(rs.getLong("id"));
                    appointment.setUserId(rs.getLong("user_id"));
                    appointment.setPackageId(rs.getLong("package_id"));
                    appointment.setAppointmentTime(rs.getObject("appointment_time", LocalDateTime.class));
                    appointment.setExamTime(rs.getObject("exam_time", LocalDateTime.class));
                    appointment.setStatus(rs.getString("status"));
                    String paymentStatus = rs.getString("payment_status");
                    appointment.setPaymentStatus("PAID".equalsIgnoreCase(paymentStatus));
                    appointment.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                    appointment.setUserName(rs.getString("user_name"));
                    appointment.setPackageName(rs.getString("package_name"));

                    return appointment;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
