package vn.nhom10.crm.util;

import vn.nhom10.crm.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Tiện ích lấy kết nối cơ sở dữ liệu JDBC dùng chung theo chuẩn CODING_RULES.
 */
public class DatabaseConnection {

    public static Connection layKetNoi() throws SQLException {
        return DatabaseConfig.getConnection();
    }
}
