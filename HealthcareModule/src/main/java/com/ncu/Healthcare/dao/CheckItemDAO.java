package com.ncu.Healthcare.dao;

import com.ncu.Common.CheckItem;
import com.ncu.Common.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckItemDAO {

    public List<CheckItem> search(String name, String code) {
        List<CheckItem> checkItems = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM medical_tests WHERE 1=1");

        if (name != null && !name.isEmpty()) {
            sql.append(" AND name LIKE ?");
        }
        if (code != null && !code.isEmpty()) {
            sql.append(" AND code LIKE ?");
        }

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (name != null && !name.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + name + "%");
            }
            if (code != null && !code.isEmpty()) {
                pstmt.setString(paramIndex++, "%" + code + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CheckItem item = new CheckItem();
                    item.setId(rs.getLong("id"));
                    item.setName(rs.getString("name"));
                    item.setCode(rs.getString("code"));
                    item.setDescription(rs.getString("description"));
                    item.setNormalRange(rs.getString("normal_range"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));

                    checkItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return checkItems;
    }

    public List<CheckItem> getAll() {
        List<CheckItem> checkItems = new ArrayList<>();
        String sql = "SELECT * FROM medical_tests";

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CheckItem item = new CheckItem();
                item.setId(rs.getLong("id"));
                item.setName(rs.getString("name"));
                item.setCode(rs.getString("code"));
                item.setDescription(rs.getString("description"));
                item.setNormalRange(rs.getString("normal_range"));
                item.setPrice(rs.getDouble("price"));
                item.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));

                checkItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return checkItems;
    }

    public CheckItem getById(Long id) {
        String sql = "SELECT * FROM medical_tests WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    CheckItem item = new CheckItem();
                    item.setId(rs.getLong("id"));
                    item.setName(rs.getString("name"));
                    item.setCode(rs.getString("code"));
                    item.setDescription(rs.getString("description"));
                    item.setNormalRange(rs.getString("normal_range"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));

                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 添加检查项
    public boolean add(CheckItem item) {
        String sql = "INSERT INTO medical_tests (name, code, description, normal_range, price, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCode());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getNormalRange());
            pstmt.setDouble(5, item.getPrice());
            pstmt.setObject(6, LocalDateTime.now());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新检查项
    public boolean update(CheckItem item) {
        String sql = "UPDATE medical_tests SET name = ?, code = ?, description = ?, " +
                "normal_range = ?, price = ? WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCode());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getNormalRange());
            pstmt.setDouble(5, item.getPrice());
            pstmt.setLong(6, item.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM medical_tests WHERE id = ?";

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
    public static List<CheckItem> getItemsByPackageId(Long packageId) {
        List<CheckItem> items = new ArrayList<>();
        String sql = "SELECT mt.* FROM medical_tests mt " +
                "JOIN package_tests pt ON mt.id = pt.test_id " +
                "WHERE pt.package_id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, packageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CheckItem item = new CheckItem();
                    item.setId(rs.getLong("id"));
                    item.setName(rs.getString("name"));
                    item.setCode(rs.getString("code"));
                    item.setDescription(rs.getString("description"));
                    item.setNormalRange(rs.getString("normal_range"));
                    item.setPrice(rs.getDouble("price"));
                    item.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));

                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}


