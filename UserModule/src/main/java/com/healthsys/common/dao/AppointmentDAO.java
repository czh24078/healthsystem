package com.healthsys.common.dao;

import com.ncu.Common.Appointment;
import com.ncu.Common.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public Appointment getAppointmentById(Long appointmentId) {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getLong("user_id"),
                        rs.getObject("package_id", Long.class),
                        rs.getObject("appointment_time", LocalDateTime.class)
                );
                appointment.setId(rs.getLong("id"));
                appointment.setStatus(rs.getString("status"));
                appointment.setPaymentStatus(rs.getBoolean("payment_status"));

                if (rs.getTimestamp("exam_time") != null) {
                    appointment.setExamTime(rs.getObject("exam_time", LocalDateTime.class));
                }

                return appointment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO appointments (user_id, package_id,appointment_time, status, payment_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, appointment.getUserId());
            stmt.setObject(2, appointment.getPackageId(), Types.BIGINT);
            stmt.setObject(3, appointment.getAppointmentTime());
            stmt.setString(4, appointment.getStatus());
            stmt.setBoolean(5, Boolean.TRUE.equals(appointment.getPaymentStatus()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        appointment.setId(rs.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<Appointment> getUserAppointments(long userId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE user_id = ? ORDER BY appointment_time DESC";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = new Appointment(
                        rs.getLong("user_id"),
                        rs.getObject("package_id") != null ? rs.getLong("package_id") : null,
                        rs.getObject("appointment_time", LocalDateTime.class)
                );
                appointment.setId(rs.getLong("id"));
                appointment.setStatus(rs.getString("status"));
                appointment.setPaymentStatus(rs.getBoolean("payment_status"));

                if (rs.getTimestamp("exam_time") != null) {
                    appointment.setExamTime(rs.getObject("exam_time", LocalDateTime.class));
                }

                appointments.add(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    public boolean cancelAppointment(long appointmentId) {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean completeAppointment(long appointmentId) {
        String sql = "UPDATE appointments SET status = 'COMPLETED', exam_time = NOW() WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentStatus(Long appointmentId, boolean paid) {
        String sql = "UPDATE appointments SET payment_status = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, paid);
            stmt.setLong(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}