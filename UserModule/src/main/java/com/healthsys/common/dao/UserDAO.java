package com.healthsys.common.dao;

import com.ncu.Common.Users;
import com.ncu.Common.DbUtil;
import com.healthsys.common.util.EncryptUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static final String INITIAL_PASSWORD = "123456";

    public Users getUserByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Users(
                        rs.getLong("id"),
                        rs.getString("phone"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getDate("birth_date").toLocalDate(),
                        rs.getString("gender"),
                        rs.getString("role"),
                        rs.getString("id_number"),
                        rs.getBoolean("first_login")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addUser(Users user) {
        String sql = "INSERT INTO users (phone, password, name, birth_date, gender, role, id_number, first_login) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getPhone());
            stmt.setString(2, EncryptUtil.encrypt(user.getPassword()));
            stmt.setString(3, user.getName());
            stmt.setDate(4, java.sql.Date.valueOf(user.getBirthDate()));
            stmt.setString(5, user.getGender());
            stmt.setString(6, user.getRole());
            stmt.setString(7, user.getIdNumber());
            stmt.setBoolean(8, user.isFirstLogin());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserPassword(Long userId, String newPassword) {
        String sql = "UPDATE users SET password = ?, first_login = FALSE WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, EncryptUtil.encrypt(newPassword));
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Users> getAllMedicalUsers() {
        List<Users> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'MEDICAL' ORDER BY name";
        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new Users(
                        rs.getLong("id"),
                        rs.getString("phone"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getDate("birth_date").toLocalDate(),
                        rs.getString("gender"),
                        rs.getString("role"),
                        rs.getString("id_number"),
                        rs.getBoolean("first_login")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 在 UserDAO.java 中添加以下方法
    public boolean updateUserProfile(Users user) {
        String sql = "UPDATE users SET name = ?, birth_date = ?, gender = ?, id_number = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setDate(2, java.sql.Date.valueOf(user.getBirthDate()));
            stmt.setString(3, user.getGender());
            stmt.setString(4, user.getIdNumber());
            stmt.setLong(5, user.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}