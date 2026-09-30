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
 * Service xử lý gửi email kích hoạt tài khoản và các thông báo hệ thống qua SMTP.
 * Hỗ trợ chế độ gửi thực tế qua Jakarta Mail và chế độ dev/test lưu lại lịch sử gửi.
 */
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private final String smtpHost;
    private final int smtpPort;
    private final boolean smtpAuth;
    private final boolean startTls;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String fromEmail;
    private final String fromName;

    // Lưu lại thông tin mật khẩu tạm đã gửi để phục vụ kiểm thử tự động
    private final Map<String, String> lastSentPasswords = new ConcurrentHashMap<>();
    private final Map<String, String> lastSentEmails = new ConcurrentHashMap<>();

    public EmailService() {
        Properties props = new Properties();
        try (InputStream is = EmailService.class.getClassLoader().getResourceAsStream("config/email.properties")) {
            if (is != null) {
                props.load(new java.io.InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể đọc file config/email.properties: " + e.getMessage());
        }

        this.smtpHost = getEnvOrDefault("SMTP_HOST", props.getProperty("mail.smtp.host", "smtp.gmail.com"));
        String portStr = getEnvOrDefault("SMTP_PORT", props.getProperty("mail.smtp.port", "587"));
        int port = 587;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ignored) {
        }
        this.smtpPort = port;

        this.smtpAuth = Boolean.parseBoolean(getEnvOrDefault("SMTP_AUTH", props.getProperty("mail.smtp.auth", "true")));
        this.startTls = Boolean.parseBoolean(getEnvOrDefault("SMTP_STARTTLS", props.getProperty("mail.smtp.starttls.enable", "true")));

        // Hỗ trợ đồng thời cả SMTP_USER và SMTP_USERNAME (ưu tiên SMTP_USER nếu được thiết lập)
        String userEnv = System.getenv("SMTP_USER");
        if (userEnv == null || userEnv.trim().isEmpty()) {
            userEnv = System.getenv("SMTP_USERNAME");
        }
        this.smtpUsername = (userEnv != null && !userEnv.trim().isEmpty()) ? userEnv.trim() : props.getProperty("mail.smtp.username", "");

        this.smtpPassword = getEnvOrDefault("SMTP_PASSWORD", props.getProperty("mail.smtp.password", ""));
        this.fromEmail = getEnvOrDefault("SMTP_FROM", props.getProperty("mail.from", "noreply@crmbanhang.vn"));
        this.fromName = getEnvOrDefault("SMTP_FROM_NAME", props.getProperty("mail.from.name", "CRM Bán Hàng"));
    }

    private String getEnvOrDefault(String envKey, String defaultValue) {
        String val = System.getenv(envKey);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : defaultValue;
    }

    /**
     * AC: Gửi email kích hoạt tài khoản kèm mật khẩu tạm cho nhân viên mới.
     *
     * @param toEmail       địa chỉ email người nhận
     * @param hoTenNguoiNhan họ tên người nhận
     * @param matKhauTam    mật khẩu tạm ngẫu nhiên
     * @param loginUrl      đường dẫn trang đăng nhập
     * @return true nếu gửi thành công hoặc đã ghi nhận
     */
    public boolean guiEmailKichHoatTaiKhoan(String toEmail, String hoTenNguoiNhan, String matKhauTam, String loginUrl) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            return false;
        }

        lastSentPasswords.put(toEmail, matKhauTam);
        lastSentEmails.put(toEmail, hoTenNguoiNhan != null ? hoTenNguoiNhan : "");

        // Nếu chưa cấu hình tài khoản SMTP gửi thực, chuyển sang chế độ test/dev
        if (smtpUsername == null || smtpUsername.trim().isEmpty() ||
            smtpPassword == null || smtpPassword.trim().isEmpty()) {
            LOGGER.info(String.format(
                "[CHẾ ĐỘ DEV/TEST] Đã ghi nhận gửi email kích hoạt đến %s (%s). Mật khẩu tạm: [ĐÃ MÃ HÓA/BẢO MẬT]. Link: %s",
                toEmail, hoTenNguoiNhan, loginUrl));
            return true;
        }

        try {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.host", smtpHost);
            mailProps.put("mail.smtp.port", String.valueOf(smtpPort));
            mailProps.put("mail.smtp.auth", String.valueOf(smtpAuth));
            mailProps.put("mail.smtp.starttls.enable", String.valueOf(startTls));
            mailProps.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

            // Thiết lập timeout kết nối (10 giây) tránh treo luồng hệ thống
            mailProps.put("mail.smtp.connectiontimeout", "10000");
            mailProps.put("mail.smtp.timeout", "10000");
            mailProps.put("mail.smtp.writetimeout", "10000");

            // Hỗ trợ SSL tự động khi dùng port 465 hoặc cấu hình SMTP_SSL=true
            boolean isSsl = smtpPort == 465 || Boolean.parseBoolean(getEnvOrDefault("SMTP_SSL", "false"));
            if (isSsl) {
                mailProps.put("mail.smtp.ssl.enable", "true");
                mailProps.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
                mailProps.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }

            Session session;
            if (smtpAuth) {
                session = Session.getInstance(mailProps, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUsername, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(mailProps);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Kích hoạt tài khoản người dùng - CRM Bán Hàng", StandardCharsets.UTF_8.name());

            String greeting = (hoTenNguoiNhan != null && !hoTenNguoiNhan.trim().isEmpty()) ? hoTenNguoiNhan : "Nhân viên mới";
            String emailContent = buildActivationHtmlContent(greeting, toEmail, matKhauTam, loginUrl);

            message.setContent(emailContent, "text/html; charset=UTF-8");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "quoted-printable");

            Transport.send(message);
            LOGGER.info("Đã gửi email kích hoạt tài khoản thành công tới: " + toEmail);
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi gửi email kích hoạt tài khoản tới: " + toEmail, e);
            return false;
        }
    }

    private String buildActivationHtmlContent(String recipientName, String email, String tempPassword, String loginUrl) {
        String safeLoginUrl = (loginUrl != null && !loginUrl.isBlank()) ? loginUrl : "/dang-nhap";
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f6f8;\">"
                + "<div style=\"max-width: 580px; margin: 0 auto; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05);\">"
                + "  <div style=\"background-color: #2563eb; color: #fff; padding: 24px; text-align: center;\">"
                + "    <h2 style=\"margin: 0; font-size: 22px;\">Chào mừng bạn đến với Hệ thống CRM Bán Hàng</h2>"
                + "  </div>"
                + "  <div style=\"padding: 24px 30px;\">"
                + "    <p>Xin chào <strong>" + escapeHtml(recipientName) + "</strong>,</p>"
                + "    <p>Tài khoản nhân viên của bạn trên hệ thống CRM đã được quản trị viên khởi tạo thành công.</p>"
                + "    <div style=\"background-color: #f1f5f9; border-radius: 6px; padding: 18px 22px; margin: 20px 0;\">"
                + "      <p style=\"margin: 0 0 8px 0;\"><strong>Thông tin đăng nhập:</strong></p>"
                + "      <p style=\"margin: 4px 0;\">Tên đăng nhập (Email): <strong style=\"color:#2563eb;\">" + escapeHtml(email) + "</strong></p>"
                + "      <p style=\"margin: 4px 0;\">Mật khẩu tạm: <code style=\"background:#e2e8f0; padding:4px 8px; border-radius:4px; font-size:16px; font-weight:bold; color:#0f172a;\">" + escapeHtml(tempPassword) + "</code></p>"
                + "    </div>"
                + "    <p>Vui lòng đăng nhập vào hệ thống và đổi mật khẩu mới ngay trong lần truy cập đầu tiên:</p>"
                + "    <div style=\"text-align: center; margin: 28px 0;\">"
                + "      <a href=\"" + escapeHtml(safeLoginUrl) + "\" style=\"display: inline-block; background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 12px 30px; border-radius: 6px; font-weight: bold; font-size: 15px;\">Đăng Nhập CRM</a>"
                + "    </div>"
                + "    <div style=\"background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 12px 16px; margin-top: 20px; border-radius: 4px;\">"
                + "      <p style=\"margin: 0; font-size: 13px; color: #92400e;\"><strong>Lưu ý bảo mật:</strong></p>"
                + "      <ul style=\"margin: 4px 0 0; padding-left: 20px; font-size: 13px; color: #92400e;\">"
                + "        <li>Không chia sẻ mật khẩu tạm này cho bất kỳ ai.</li>"
                + "        <li>Đổi mật khẩu mới có tối thiểu 8 ký tự, bao gồm chữ và số ngay khi đăng nhập.</li>"
                + "      </ul>"
                + "    </div>"
                + "  </div>"
                + "  <div style=\"background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 14px 24px; text-align: center; font-size: 12px; color: #94a3b8;\">"
                + "    © CRM Bán Hàng. Đây là thư tự động từ hệ thống quản trị, vui lòng không phản hồi email này."
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

    public String getLastSentPassword(String email) {
        return lastSentPasswords.get(email);
    }

    public void clearHistory() {
        lastSentPasswords.clear();
        lastSentEmails.clear();
    }
}
