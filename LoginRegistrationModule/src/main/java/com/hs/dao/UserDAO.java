package com.hs.dao;

import com.ncu.Common.DbUtil;
import com.ncu.Common.Users;
import java.sql.*;
import java.time.LocalDate;

public class UserDAO {
    public static final String ADMIN_INITIAL_PASSWORD = "admin";
    public static final String USER_INITIAL_PASSWORD = "user1";

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
                if(rs.getDate("birth_date") != null) {
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

    public boolean addUser(Users user) {
        String sql = "INSERT INTO users (phone, password, name, birth_date, gender, role, id_number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 验证phone字段(20字符限制)
            stmt.setString(1, user.getPhone() != null ?
                    user.getPhone().length() > 20 ? user.getPhone().substring(0, 20) : user.getPhone() : "");
            stmt.setString(2, user.getPassword()); // 密码已在业务层验证
            // 验证name字段(50字符限制)
            stmt.setString(3, user.getName() != null ?
                    user.getName().length() > 50 ? user.getName().substring(0, 50) : user.getName() : "");

            LocalDate birthDate = user.getBirthDate() != null ? user.getBirthDate() : LocalDate.now().minusYears(20);
            stmt.setDate(4, java.sql.Date.valueOf(birthDate));

            // 严格验证gender字段
            String gender = "MALE".equalsIgnoreCase(user.getGender()) ? "MALE" :
                    "FEMALE".equalsIgnoreCase(user.getGender()) ? "FEMALE" : "MALE";
            stmt.setString(5, gender);

            // 严格验证role字段
            String role = "USER".equalsIgnoreCase(user.getRole()) ? "USER" :
                    "MEDICAL".equalsIgnoreCase(user.getRole()) ? "MEDICAL" : "USER";
            stmt.setString(6, role);

            // 验证id_number字段(18字符限制)
            stmt.setString(7, "MEDICAL".equals(role) && user.getIdNumber() != null ?
                    user.getIdNumber().length() > 18 ? user.getIdNumber().substring(0, 18) : user.getIdNumber() : null);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserPassword(long userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
