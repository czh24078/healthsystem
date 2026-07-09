package com.healthsys.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbUtil {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = DbUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                String url = props.getProperty("db.url");
                if (url != null && url.contains("utf8mb4")) {
                    url = url.replace("utf8mb4", "UTF-8");
                    props.setProperty("db.url", url);
                }
            } else {
                throw new RuntimeException("数据库配置文件 db.properties 未找到，请确保文件存在于 classpath 中");
            }
        } catch (IOException e) {
            throw new RuntimeException("无法加载数据库配置文件: " + e.getMessage(), e);
        }
    }

    // 私有构造方法防止实例化
    private DbUtil() {}

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    props.getProperty("db.url"),
                    props.getProperty("db.user"),
                    props.getProperty("db.password")
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * 回滚事务
     */
    public static void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                System.err.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }
}
