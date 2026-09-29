package vn.nhom10.crm.util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tiện ích băm, kiểm tra và tự động sinh mật khẩu tạm sử dụng BCrypt và SecureRandom.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    private static final String CHU_HOA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHU_THUONG = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHU_SO = "0123456789";
    private static final String KY_TU_DAC_BIET = "@#$%&*!";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Băm mật khẩu người dùng bằng BCrypt.
     *
     * @param rawPassword mật khẩu thô
     * @return chuỗi hash BCrypt
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * So khớp mật khẩu thô với hash BCrypt đã lưu trong DB.
     *
     * @param rawPassword    mật khẩu thô do người dùng nhập
     * @param hashedPassword chuỗi hash BCrypt trong database
     * @return true nếu mật khẩu chính xác
     */
    public static boolean checkPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra độ mạnh của mật khẩu:
     * - Tối thiểu 8 ký tự.
     * - Chứa ít nhất một chữ cái (a-z, A-Z).
     * - Chứa ít nhất một chữ số (0-9).
     *
     * @param password mật khẩu cần kiểm tra
     * @return true nếu thỏa mãn
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean coChu = false;
        boolean coSo = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                coChu = true;
            } else if (Character.isDigit(c)) {
                coSo = true;
            }
            if (coChu && coSo) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tự động sinh mật khẩu tạm ngẫu nhiên mạnh (10 ký tự) gồm chữ hoa, chữ thường, chữ số và ký tự đặc biệt.
     * Phục vụ AC: "Tạo tài khoản gửi email kích hoạt kèm mật khẩu tạm"
     *
     * @return mật khẩu tạm ngẫu nhiên
     */
    public static String taoMatKhauTam() {
        List<Character> kyTuList = new ArrayList<>();

        // Bắt buộc có đủ các nhóm ký tự
        kyTuList.add(CHU_HOA.charAt(RANDOM.nextInt(CHU_HOA.length())));
        kyTuList.add(CHU_HOA.charAt(RANDOM.nextInt(CHU_HOA.length())));
        kyTuList.add(CHU_THUONG.charAt(RANDOM.nextInt(CHU_THUONG.length())));
        kyTuList.add(CHU_THUONG.charAt(RANDOM.nextInt(CHU_THUONG.length())));
        kyTuList.add(CHU_SO.charAt(RANDOM.nextInt(CHU_SO.length())));
        kyTuList.add(CHU_SO.charAt(RANDOM.nextInt(CHU_SO.length())));
        kyTuList.add(KY_TU_DAC_BIET.charAt(RANDOM.nextInt(KY_TU_DAC_BIET.length())));

        // Bổ sung các ký tự còn lại cho đủ 10 ký tự
        String tatCaKyTu = CHU_HOA + CHU_THUONG + CHU_SO + KY_TU_DAC_BIET;
        while (kyTuList.size() < 10) {
            kyTuList.add(tatCaKyTu.charAt(RANDOM.nextInt(tatCaKyTu.length())));
        }

        // Xáo trộn ngẫu nhiên
        Collections.shuffle(kyTuList, RANDOM);

        StringBuilder sb = new StringBuilder();
        for (char c : kyTuList) {
            sb.append(c);
        }
        return sb.toString();
    }
}
