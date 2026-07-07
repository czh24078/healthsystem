package com.healthsys.dao;

import com.healthsys.common.entity.Users;
import com.healthsys.common.util.DbUtil;
import com.healthsys.common.util.EncryptUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static final String INITIAL_PASSWORD = "123456";

    // ============ 从 HealthcareModule 迁移 ============

    public List<Users> search(Long id, String name) {
        List<Users> usersList = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");

        if (id != null) {
            sql.append(" AND id = ?");
        }
        if (name != null && !name.isEmpty()) {
            sql.append(" AND name LIKE ?");
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (id != null) {
                pstmt.setLong(paramIndex++, id);
            }
            if (name != null && !name.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + name + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Users user = new Users();
                    user.setId(rs.getLong("id"));
                    user.setPhone(rs.getString("phone"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
                    user.setGender(rs.getString("gender"));
                    user.setRole(rs.getString("role"));
                    user.setIdNumber(rs.getString("id_number"));
                    user.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
                    user.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));

                    usersList.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usersList;
    }

    public boolean add(Users user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (user.getBirthDate() == null) {
            throw new IllegalArgumentException("出生日期不能为空");
        }

        String sql = "INSERT INTO users (phone, password, name, birth_date, gender, role, id_number, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getPhone());
            pstmt.setString(2, EncryptUtil.encrypt(user.getPassword()));
            pstmt.setString(3, user.getName());
            pstmt.setObject(4, user.getBirthDate());
            pstmt.setString(5, user.getGender());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getIdNumber());
            pstmt.setObject(8, LocalDateTime.now());
            pstmt.setObject(9, LocalDateTime.now());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Users user) {
        String sql = "UPDATE users SET phone = ?, password = ?, name = ?, birth_date = ?, " +
                "gender = ?, role = ?, id_number = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getPhone());
            pstmt.setString(2, EncryptUtil.encrypt(user.getPassword()));
            pstmt.setString(3, user.getName());
            pstmt.setObject(4, user.getBirthDate());
            pstmt.setString(5, user.getGender());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getIdNumber());
            pstmt.setObject(8, LocalDateTime.now());
            pstmt.setLong(9, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Users> getAll() {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Users user = new Users();
                user.setId(rs.getLong("id"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
                user.setGender(rs.getString("gender"));
                user.setRole(rs.getString("role"));
                user.setIdNumber(rs.getString("id_number"));
                user.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
                user.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));

                usersList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usersList;
    }

    public Users getById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Users user = new Users();
                    user.setId(rs.getLong("id"));
                    user.setPhone(rs.getString("phone"));
                    user.setPassword(rs.getString("password"));
                    user.setName(rs.getString("name"));
                    user.setBirthDate(rs.getObject("birth_date", java.time.LocalDate.class));
                    user.setGender(rs.getString("gender"));
                    user.setRole(rs.getString("role"));
                    user.setIdNumber(rs.getString("id_number"));
                    user.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
                    user.setUpdatedAt(rs.getObject("updated_at", java.time.LocalDateTime.class));

                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

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

    // ============ 从 UserModule 迁移 ============

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

    // ============ 从 LoginRegistrationModule 迁移 ============

    public Users getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Users user = new Users();
                user.setId(rs.getLong("id"));
                user.setPhone(rs.getString("phone"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                if (rs.getDate("birth_date") != null) {
                    user.setBirthDate(rs.getDate("birth_date").toLocalDate());
                }
                user.setGender(rs.getString("gender"));
                user.setRole(rs.getString("role"));
                user.setIdNumber(rs.getString("id_number"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
