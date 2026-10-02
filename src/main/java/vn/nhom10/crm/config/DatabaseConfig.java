package vn.nhom10.crm.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối cơ sở dữ liệu MySQL sử dụng HikariCP connection pool.
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static volatile HikariDataSource dataSource;
    private static final Object lock = new Object();

    private DatabaseConfig() {
    }

    public static HikariDataSource getDataSource() {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    initDataSource();
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static void initDataSource() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("config/db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                logger.warn("Không tìm thấy file config/db.properties, dùng cấu hình mặc định");
            }
        } catch (Exception e) {
            logger.warn("Không đọc được file config/db.properties: {}", e.getMessage());
        }

        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        String url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/crm_ban_hang?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false");
        String user = props.getProperty("db.user", "root");
        String password = props.getProperty("db.password", "123456");

        // Cho phép ghi đè qua biến môi trường nếu có
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl;
        }
        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser;
        }
        String envPassword = System.getenv("DB_PASSWORD");
        if (envPassword != null) {
            password = envPassword;
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        try {
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "10000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
        } catch (NumberFormatException e) {
            logger.warn("Lỗi đọc thông số pool, dùng giá trị mặc định của HikariCP");
        }

        config.setPoolName("CRMHikariPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        logger.info("HikariCP DataSource đã khởi tạo thành công với URL: {}", url);
    }

    public static void setDataSourceForTesting(HikariDataSource ds) {
        synchronized (lock) {
            dataSource = ds;
        }
    }

    public static void shutdown() {
        synchronized (lock) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
                logger.info("HikariCP DataSource đã đóng.");
            }
        }
    }
}
