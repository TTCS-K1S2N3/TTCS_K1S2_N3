package vn.nhom10.crm.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý gửi email thông báo và liên kết đặt lại mật khẩu.
 * Sử dụng Jakarta Mail kết nối giao thức SMTP.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private static volatile EmailService instance;

    private String smtpHost;
    private int smtpPort;
    private boolean smtpAuth;
    private boolean startTls;
    private String smtpUsername;
    private String smtpPassword;
    private String fromEmail;
    private String fromName;

    // Lưu liên kết gần nhất phục vụ kiểm thử tự động
    private final Map<String, String> lastSentLinks = new ConcurrentHashMap<>();

    public EmailService() {
        loadEmailConfig();
    }

    public static EmailService getInstance() {
        if (instance == null) {
            synchronized (EmailService.class) {
                if (instance == null) {
                    instance = new EmailService();
                }
            }
        }
        return instance;
    }

    private void loadEmailConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/email.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc file config/email.properties", e);
        }

        this.smtpHost = getEnvOrDefault("SMTP_HOST", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
        String portStr = getEnvOrDefault("SMTP_PORT", props.getProperty("mail.smtp.port", "587"));
        try {
            this.smtpPort = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            this.smtpPort = 587;
        }

        this.smtpAuth = Boolean.parseBoolean(getEnvOrDefault("SMTP_AUTH", props.getProperty("mail.smtp.auth", "true")));
        this.startTls = Boolean.parseBoolean(getEnvOrDefault("SMTP_STARTTLS", props.getProperty("mail.smtp.starttls.enable", "true")));
        this.smtpUsername = getEnvOrDefault("SMTP_USERNAME", props.getProperty("mail.smtp.username", ""));
        this.smtpPassword = getEnvOrDefault("SMTP_PASSWORD", props.getProperty("mail.smtp.password", ""));
        this.fromEmail = getEnvOrDefault("SMTP_FROM", props.getProperty("mail.from", "noreply@crmbanhang.vn"));
        this.fromName = getEnvOrDefault("SMTP_FROM_NAME", props.getProperty("mail.from.name", "CRM Bán Hàng"));
    }

    private String getEnvOrDefault(String envKey, String defaultValue) {
        String val = System.getenv(envKey);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    /**
     * Gửi email chứa liên kết đặt lại mật khẩu đến người dùng.
     *
     * @param toEmail       địa chỉ email người nhận
     * @param hoTenNguoiNhan họ tên người nhận
     * @param resetLink     liên kết đặt lại mật khẩu (có hiệu lực 30 phút)
     * @return true nếu gửi thành công hoặc đã ghi nhận
     */
    public boolean guiEmailDatLaiMatKhau(String toEmail, String hoTenNguoiNhan, String resetLink) {
        lastSentLinks.put(toEmail, resetLink);

        // Nạp lại cấu hình mới nhất để tránh lỗi cấu hình cũ
        loadEmailConfig();

        // Kiểm tra xem cấu hình SMTP có đủ thông tin để gửi email thật không
        if (smtpUsername == null || smtpUsername.trim().isEmpty() || 
            smtpPassword == null || smtpPassword.trim().isEmpty()) {
            LOGGER.warning("[CHẾ ĐỘ DEV/TEST] Chưa cấu hình tài khoản SMTP (SMTP_USERNAME/SMTP_PASSWORD). Đã sinh yêu cầu đặt lại mật khẩu cho: " + toEmail);
            return true;
        }

        Transport transport = null;
        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.transport.protocol", "smtp");
            mailProps.put("mail.smtp.host", smtpHost);
            mailProps.put("mail.smtp.port", String.valueOf(smtpPort));
            mailProps.put("mail.smtp.auth", String.valueOf(smtpAuth));
            mailProps.put("mail.smtp.starttls.enable", String.valueOf(startTls));
            mailProps.put("mail.smtp.starttls.required", String.valueOf(startTls));
            mailProps.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
            mailProps.put("mail.smtp.ssl.trust", smtpHost);
            mailProps.put("mail.smtp.connectiontimeout", "10000");
            mailProps.put("mail.smtp.timeout", "10000");
            mailProps.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(mailProps);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Yêu cầu đặt lại mật khẩu - CRM Bán Hàng", StandardCharsets.UTF_8.name());

            String greeting = (hoTenNguoiNhan != null && !hoTenNguoiNhan.trim().isEmpty()) ? hoTenNguoiNhan : "Người dùng";
            String emailContent = buildResetPasswordHtmlContent(greeting, resetLink);

            message.setContent(emailContent, "text/html; charset=UTF-8");

            transport = session.getTransport("smtp");
            if (smtpAuth) {
                transport.connect(smtpHost, smtpPort, smtpUsername, smtpPassword);
            } else {
                transport.connect();
            }
            transport.sendMessage(message, message.getAllRecipients());

            LOGGER.info("Gửi email đặt lại mật khẩu thành công qua SMTP.");
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi SMTP khi gửi email đặt lại mật khẩu: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private String buildResetPasswordHtmlContent(String recipientName, String resetLink) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f6f8;\">"
                + "<div style=\"max-width: 560px; margin: 0 auto; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">"
                + "  <div style=\"background-color: #2563eb; color: #fff; padding: 20px 24px; text-align: center;\">"
                + "    <h2 style=\"margin: 0; font-size: 20px;\">CRM Bán Hàng - Đặt Lại Mật Khẩu</h2>"
                + "  </div>"
                + "  <div style=\"padding: 24px;\">"
                + "    <p>Xin chào <strong>" + escapeHtml(recipientName) + "</strong>,</p>"
                + "    <p>Hệ thống nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>"
                + "    <p>Vui lòng bấm vào nút bên dưới để tiến hành đặt mật khẩu mới:</p>"
                + "    <div style=\"text-align: center; margin: 28px 0;\">"
                + "      <a href=\"" + resetLink + "\" style=\"display: inline-block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 12px 28px; border-radius: 6px; font-weight: bold; font-size: 15px;\">Đặt Lại Mật Khẩu</a>"
                + "    </div>"
                + "    <p style=\"font-size: 13px; color: #64748b;\">Hoặc sao chép liên kết sau dán vào trình duyệt của bạn:<br>"
                + "      <a href=\"" + resetLink + "\" style=\"color: #2563eb; word-break: break-all;\">" + resetLink + "</a>"
                + "    </p>"
                + "    <div style=\"background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px; margin-top: 20px; border-radius: 4px;\">"
                + "      <p style=\"margin: 0; font-size: 13px; color: #92400e;\"><strong>Lưu ý quan trọng:</strong></p>"
                + "      <ul style=\"margin: 4px 0 0; padding-left: 20px; font-size: 13px; color: #92400e;\">"
                + "        <li>Liên kết này chỉ có hiệu lực trong <strong>30 phút</strong>.</li>"
                + "        <li>Liên kết chỉ có thể sử dụng <strong>một lần duy nhất</strong>.</li>"
                + "        <li>Nếu bạn không yêu cầu đặt lại mật khẩu, xin hãy bỏ qua email này.</li>"
                + "      </ul>"
                + "    </div>"
                + "  </div>"
                + "  <div style=\"background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 14px 24px; text-align: center; font-size: 12px; color: #94a3b8;\">"
                + "    © CRM Bán Hàng. Đây là email tự động, vui lòng không phản hồi thư này."
                + "  </div>"
                + "</div>"
                + "</body></html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }

    /**
     * Lấy liên kết đặt lại mật khẩu vừa gửi (hỗ trợ kiểm thử).
     */
    public String getLastSentLink(String email) {
        return lastSentLinks.get(email);
    }

    public void clearLastSentLinks() {
        lastSentLinks.clear();
    }
}
