package vn.nhom10.crm.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Service gửi email thông báo và liên kết đặt lại mật khẩu (S1-03).
 * Hỗ trợ chuyển đổi linh hoạt giữa REAL SMTP (Gmail/công ty) và MOCK mode cho testing.
 */
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private String smtpHost = "smtp.gmail.com";
    private int smtpPort = 587;
    private boolean smtpAuth = true;
    private boolean starttlsEnable = true;
    private String smtpUser = "";
    private String smtpPassword = "";
    private String fromEmail = "no-reply@crm.vn";
    private String fromName = "CRM Hệ Thống Bán Hàng B2B";
    private boolean mockEnabled = false;

    // Dành cho kiểm thử tự động
    private String lastRecipient;
    private String lastSubject;
    private String lastResetLink;

    public EmailService() {
        loadConfig();
    }

    private void loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/db.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);

                smtpHost = props.getProperty("mail.smtp.host", smtpHost);
                smtpPort = Integer.parseInt(props.getProperty("mail.smtp.port", String.valueOf(smtpPort)));
                smtpAuth = Boolean.parseBoolean(props.getProperty("mail.smtp.auth", String.valueOf(smtpAuth)));
                starttlsEnable = Boolean.parseBoolean(props.getProperty("mail.smtp.starttls.enable", String.valueOf(starttlsEnable)));
                smtpUser = props.getProperty("mail.smtp.user", smtpUser);
                smtpPassword = props.getProperty("mail.smtp.password", smtpPassword);
                fromEmail = props.getProperty("mail.from", fromEmail);
                fromName = props.getProperty("mail.from.name", fromName);
                mockEnabled = Boolean.parseBoolean(props.getProperty("mail.mock.enabled", "false"));
            }
        } catch (Exception e) {
            logger.warn("Không đọc được cấu hình email từ db.properties, dùng cấu hình mặc định: {}", e.getMessage());
        }
    }

    /**
     * S1-03-AC1: Gửi email chứa liên kết đặt lại mật khẩu có hiệu lực 30 phút.
     */
    public boolean guiEmailDatLaiMatKhau(String toEmail, String tenNguoiDung, String resetLink, int phutHetHan) {
        this.lastRecipient = toEmail;
        this.lastSubject = "Yêu cầu đặt lại mật khẩu — CRM Bán Hàng B2B";
        this.lastResetLink = resetLink;

        if (mockEnabled) {
            logger.info("[MOCK EMAIL] Gửi email đặt lại mật khẩu tới {} với link: {} (hạn {} phút)",
                    toEmail, resetLink, phutHetHan);
            return true;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.auth", String.valueOf(smtpAuth));
            props.put("mail.smtp.starttls.enable", String.valueOf(starttlsEnable));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(lastSubject, StandardCharsets.UTF_8.name());

            String noiDungHtml = buildHtmlContent(tenNguoiDung, resetLink, phutHetHan);
            message.setContent(noiDungHtml, "text/html; charset=UTF-8");

            Transport.send(message);
            logger.info("Đã gửi email thật đặt lại mật khẩu thành công tới {}", toEmail);
            return true;
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email thật đặt lại mật khẩu tới {}: {}", toEmail, e.getMessage());
            // Trả về false nhưng phía Controller vẫn trả cùng thông báo thành công cho user (chống enumeration)
            return false;
        }
    }

    private String buildHtmlContent(String tenNguoiDung, String resetLink, int phutHetHan) {
        String safeName = (tenNguoiDung != null && !tenNguoiDung.isBlank()) ? tenNguoiDung : "Quý khách";
        return "<!DOCTYPE html>" +
               "<html lang=\"vi\">" +
               "<head><meta charset=\"UTF-8\"></head>" +
               "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f6f8;\">" +
               "  <div style=\"max-width: 600px; margin: auto; background: #fff; padding: 30px; border-radius: 8px; border: 1px solid #e1e4e8;\">" +
               "    <h2 style=\"color: #1a73e8; margin-top: 0;\">CRM Bán Hàng B2B</h2>" +
               "    <p>Xin chào <strong>" + safeName + "</strong>,</p>" +
               "    <p>Hệ thống vừa nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với địa chỉ email này.</p>" +
               "    <p>Vui lòng nhấn vào nút bên dưới để tiến hành thiết lập mật khẩu mới:</p>" +
               "    <div style=\"text-align: center; margin: 30px 0;\">" +
               "      <a href=\"" + resetLink + "\" style=\"background-color: #1a73e8; color: #fff; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;\">" +
               "        Đặt lại mật khẩu" +
               "      </a>" +
               "    </div>" +
               "    <p style=\"font-size: 14px; color: #555;\">" +
               "      <em>Lưu ý:</em> Liên kết này chỉ có hiệu lực trong vòng <strong>" + phutHetHan + " phút</strong> và chỉ sử dụng được <strong>duy nhất một lần</strong>." +
               "    </p>" +
               "    <p style=\"font-size: 13px; color: #777;\">" +
               "      Nếu nút bấm trên không hoạt động, bạn có thể sao chép và dán liên kết sau vào trình duyệt:<br>" +
               "      <a href=\"" + resetLink + "\" style=\"color: #1a73e8; word-break: break-all;\">" + resetLink + "</a>" +
               "    </p>" +
               "    <hr style=\"border: none; border-top: 1px solid #eee; margin: 25px 0;\">" +
               "    <p style=\"font-size: 12px; color: #888;\">Nếu bạn không yêu cầu đặt lại mật khẩu, xin hãy bỏ qua email này. Tài khoản của bạn vẫn được an toàn.</p>" +
               "  </div>" +
               "</body>" +
               "</html>";
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public String getLastRecipient() {
        return lastRecipient;
    }

    public String getLastSubject() {
        return lastSubject;
    }

    public String getLastResetLink() {
        return lastResetLink;
    }
}
