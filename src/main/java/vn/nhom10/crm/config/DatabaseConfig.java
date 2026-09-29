package vn.nhom10.crm.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý cấu hình kết nối cơ sở dữ liệu JDBC.
 * Hỗ trợ nạp cấu hình từ database.properties và biến môi trường.
 */
public class DatabaseConfig {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());

    private static String driver;
    private static String url;
    private static String user;
    private static String password;

    // Cho phép override connection dùng cho kiểm thử tự động
    private static java.util.function.Supplier<Connection> connectionSupplier;

    static {
        loadConfig();
    }

    private static synchronized void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("config/database.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc file database.properties, sử dụng giá trị mặc định", e);
        }

        // Đọc cấu hình local nếu có (bảo mật, không commit lên git)
        try (InputStream isLocal = DatabaseConfig.class.getClassLoader().getResourceAsStream("database-local.properties")) {
            if (isLocal != null) {
                props.load(isLocal);
            }
        } catch (Exception ignored) {
        }

        driver = System.getenv("DB_DRIVER") != null ? System.getenv("DB_DRIVER") 
                : props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") 
                : props.getProperty("db.url", "jdbc:mysql://localhost:3306/crm_ban_hang?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8");

        user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") 
                : props.getProperty("db.user", "root");

        password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") 
                : props.getProperty("db.password", "123456");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Không tìm thấy JDBC Driver: " + driver, e);
        }
    }

    /**
     * Lấy kết nối cơ sở dữ liệu.
     *
     * @return Connection JDBC
     * @throws SQLException khi không thể kết nối
     */
    public static Connection getConnection() throws SQLException {
        if (connectionSupplier != null) {
            Connection conn = connectionSupplier.get();
            if (conn != null) {
                return conn;
            }
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Thiết lập supplier cấp kết nối tạm dùng cho kiểm thử tự động (ví dụ: H2 in-memory).
     *
     * @param supplier Supplier cấp Connection
     */
    public static void setConnectionSupplier(java.util.function.Supplier<Connection> supplier) {
        connectionSupplier = supplier;
    }

    public static void resetConnectionSupplier() {
        connectionSupplier = null;
    }
}
