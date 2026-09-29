package vn.nhom10.crm.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiện ích quản lý kết nối JDBC MySQL 8.4 LTS theo chuẩn CODING_RULES và DATABASE_RULES.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static String url = "jdbc:mysql://localhost:3306/crm_ban_hang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8";
    private static String username = "root";
    private static String password = "root";
    private static String driver = "com.mysql.cj.jdbc.Driver";

    static {
        try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("config/db.properties")) {
            if (is != null) {
                Properties prop = new Properties();
                prop.load(is);
                url = prop.getProperty("db.url", url);
                username = prop.getProperty("db.username", username);
                password = prop.getProperty("db.password", password);
                driver = prop.getProperty("db.driver", driver);
            }
            Class.forName(driver);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể tải cấu hình db.properties, sử dụng cấu hình mặc định: " + e.getMessage());
        }
    }

    public static Connection layKetNoi() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
