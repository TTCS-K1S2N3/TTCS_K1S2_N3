package vn.nhom10.crm.util;

import vn.nhom10.crm.model.Lead;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tiện ích chuẩn hóa và phát hiện trùng lặp Lead (Story S4-04, AC1 & AC2).
 * Tiêu chí so khớp:
 * 1. Email: Chuẩn hóa trim và lowercase, so khớp tuyệt đối.
 * 2. Số điện thoại: Loại bỏ ký tự không phải số, quy chuẩn mã quốc gia +84 về 0, so khớp chuỗi số.
 * 3. Tên công ty: Bỏ dấu tiếng Việt, loại bỏ từ khóa pháp lý (công ty, TNHH, CP...), so khớp tương đồng Levenshtein >= 70%.
 */
public final class DuplicateLeadDetector {

    public static final double NGUONG_TUONG_DONG_TEN = 0.70; // 70%

    private static final List<String> TU_KHOA_PHAP_LY = Arrays.asList(
            "cong ty", "cty", "co phan", "cp", "tnhh", "trach nhiem huu han",
            "tap doan", "chi nhanh", "doanh nghiep", "tong cong ty", "tnhh mtv", "mtv"
    );

    private DuplicateLeadDetector() {
    }

    /**
     * Chuẩn hóa email: cắt khoảng trắng và đưa về chữ thường.
     */
    public static String chuanHoaEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    /**
     * Chuẩn hóa số điện thoại: loại bỏ toàn bộ ký tự không phải số, quy chuẩn đầu 84 về 0.
     */
    public static String chuanHoaSoDienThoai(String phone) {
        if (phone == null) {
            return "";
        }
        String clean = phone.trim().replaceAll("[^0-9]", "");
        if (clean.startsWith("84") && clean.length() >= 10) {
            clean = "0" + clean.substring(2);
        }
        return clean;
    }

    /**
     * Bỏ dấu tiếng Việt và chuyển sang chữ thường.
     */
    public static String boDauTiengViet(String str) {
        if (str == null) {
            return "";
        }
        String s = str.toLowerCase();
        s = s.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        s = s.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        s = s.replaceAll("[ìíịỉĩ]", "i");
        s = s.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        s = s.replaceAll("[ùúụủũưừứựửữ]", "u");
        s = s.replaceAll("[ỳýỵỷỹ]", "y");
        s = s.replaceAll("đ", "d");
        return s;
    }

    /**
     * Chuẩn hóa tên công ty: bỏ dấu tiếng Việt, bỏ từ khóa pháp lý và ký tự đặc biệt.
     */
    public static String chuanHoaTenCongTy(String ten) {
        if (ten == null) {
            return "";
        }
        String clean = boDauTiengViet(ten);
        for (String w : TU_KHOA_PHAP_LY) {
            clean = clean.replaceAll("\\b" + w + "\\b", "");
        }
        clean = clean.replaceAll("[^a-z0-9\\s]", " ");
        clean = clean.replaceAll("\\s+", " ").trim();
        return clean;
    }

    /**
     * Tính toán khoảng cách Levenshtein giữa hai chuỗi.
     */
    public static int khoangCachLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return (s1 == null ? 0 : s1.length()) + (s2 == null ? 0 : s2.length());
        }
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[len1][len2];
    }

    /**
     * Tính tỷ lệ tương đồng giữa hai chuỗi từ 0.0 đến 1.0 dựa trên khoảng cách Levenshtein.
     */
    public static double tinhTyLeTuongDong(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }
        if (s1.equalsIgnoreCase(s2)) {
            return 1.0;
        }
        int maxLen = Math.max(s1.length(), s2.length());
        int dist = khoangCachLevenshtein(s1, s2);
        return 1.0 - ((double) dist / maxLen);
    }

    /**
     * Kiểm tra hai Lead có dấu hiệu trùng lặp theo 3 tín hiệu (AC1).
     * Trả về danh sách lý do: "EMAIL", "PHONE", "COMPANY". Trả về rỗng nếu không trùng.
     */
    public static List<String> kiemTraTrungLead(Lead a, Lead b) {
        if (a == null || b == null) {
            return Collections.emptyList();
        }
        if (a.getId() != null && b.getId() != null && a.getId().equals(b.getId())) {
            return Collections.emptyList(); // Không so khớp với chính nó
        }

        List<String> reasons = new ArrayList<>();

        // 1. So khớp Email
        String emailA = a.getEmailChuanHoa() != null ? a.getEmailChuanHoa() : chuanHoaEmail(a.getEmail());
        String emailB = b.getEmailChuanHoa() != null ? b.getEmailChuanHoa() : chuanHoaEmail(b.getEmail());
        if (!emailA.isEmpty() && !emailB.isEmpty() && emailA.equalsIgnoreCase(emailB)) {
            reasons.add("EMAIL");
        }

        // 2. So khớp Số điện thoại
        String phoneA = a.getSoDienThoaiChuanHoa() != null ? a.getSoDienThoaiChuanHoa() : chuanHoaSoDienThoai(a.getSoDienThoai());
        String phoneB = b.getSoDienThoaiChuanHoa() != null ? b.getSoDienThoaiChuanHoa() : chuanHoaSoDienThoai(b.getSoDienThoai());
        if (!phoneA.isEmpty() && !phoneB.isEmpty() && phoneA.equals(phoneB)) {
            reasons.add("PHONE");
        }

        // 3. So khớp Tên công ty
        String compA = chuanHoaTenCongTy(a.getTenCongTy());
        String compB = chuanHoaTenCongTy(b.getTenCongTy());
        if (!compA.isEmpty() && !compB.isEmpty()) {
            if (compA.equalsIgnoreCase(compB) || tinhTyLeTuongDong(compA, compB) >= NGUONG_TUONG_DONG_TEN) {
                reasons.add("COMPANY");
            }
        }

        return reasons;
    }

    /**
     * Làm mờ (mask) email bảo vệ PII ngoài Data Scope.
     * Ví dụ: contact01@example.com -> co***01@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }
        String clean = email.trim();
        int atIndex = clean.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        String local = clean.substring(0, atIndex);
        String domain = clean.substring(atIndex);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        if (local.length() <= 4) {
            return local.substring(0, 2) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + local.substring(local.length() - 2) + domain;
    }

    /**
     * Làm mờ (mask) số điện thoại bảo vệ PII ngoài Data Scope.
     * Ví dụ: 0901234567 -> 090****567
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }
        String digits = phone.trim();
        if (digits.length() <= 4) {
            return "****";
        }
        if (digits.length() <= 7) {
            return digits.substring(0, 2) + "****" + digits.substring(digits.length() - 2);
        }
        return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 3);
    }
}
