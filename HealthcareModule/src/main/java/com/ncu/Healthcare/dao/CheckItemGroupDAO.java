package com.ncu.Healthcare.dao;

import com.ncu.Common.CheckItemGroup;
import com.ncu.Common.DbUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckItemGroupDAO {

    public List<CheckItemGroup> search(Long id, String name) {
        List<CheckItemGroup> groups = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM test_packages WHERE 1=1");

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
                    CheckItemGroup group = new CheckItemGroup();
                    group.setId(rs.getLong("id"));
                    group.setName(rs.getString("name"));
                    group.setDescription(rs.getString("description"));
                    group.setPrice(rs.getDouble("price"));
                    group.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

                    groups.add(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public List<CheckItemGroup> getAll() {
        List<CheckItemGroup> groups = new ArrayList<>();
        String sql = "SELECT * FROM test_packages";  // 修正：查询 test_packages 表

        try (Connection conn = DbUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CheckItemGroup group = new CheckItemGroup();
                group.setId(rs.getLong("id"));
                group.setName(rs.getString("name"));
                group.setDescription(rs.getString("description"));
                group.setPrice(rs.getDouble("price"));
                group.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));

                groups.add(group);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public boolean add(CheckItemGroup group) {
        String sql = "INSERT INTO test_packages (name, description, price, created_at) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getDescription());
            pstmt.setDouble(3, group.getPrice());
            pstmt.setObject(4, LocalDateTime.now());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(CheckItemGroup group) {
        String sql = "UPDATE test_packages SET name = ?, description = ?, price = ? " +
                "WHERE id = ?";

        try (Connection conn = DbUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, group.getName());
            pstmt.setString(2, group.getDescription());
            pstmt.setDouble(3, group.getPrice());
            pstmt.setLong(4, group.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM test_packages WHERE id = ?";

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

}


