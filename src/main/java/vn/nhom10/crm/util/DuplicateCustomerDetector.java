package vn.nhom10.crm.util;

import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.CapKhachHangTrungDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tiện ích phát hiện trùng lặp khách hàng (Story S3-04, AC1).
 * Tiêu chuẩn so khớp:
 * 1. Mã số thuế (MST): Trùng khớp tuyệt đối sau khi chuẩn hóa (loại bỏ ký tự đặc biệt, viết hoa).
 * 2. Tên công ty: Độ tương đồng >= 70% sau khi loại bỏ dấu tiếng Việt và các từ định danh pháp lý phổ biến.
 * 3. Website: Trùng tên miền chính sau khi chuẩn hóa (bỏ giao thức http/https, www, đường dẫn con).
 */
public final class DuplicateCustomerDetector {

    public static final double NGUONG_TUONG_DONG_TEN = 0.70; // 70%

    private static final List<String> TU_KHOA_PHAP_LY = Arrays.asList(
            "cong ty", "cty", "co phan", "cp", "tnhh", "trach nhiem huu han",
            "tap doan", "chi nhanh", "doanh nghiep", "tong cong ty", "tnhh mtv", "mtv"
    );

    private DuplicateCustomerDetector() {
        // Private constructor for utility class
    }

    /**
     * Chuẩn hóa mã số thuế: bỏ ký tự khoảng trắng, dấu gạch ngang, viết hoa.
     */
    public static String chuanHoaMaSoThue(String mst) {
        if (mst == null) {
            return "";
        }
        return mst.replaceAll("[^a-zA-Z0-9]", "").trim().toUpperCase();
    }

    /**
     * Chuẩn hóa website: lấy host/domain cốt lõi, bỏ http, https, www, path, query.
     */
    public static String chuanHoaWebsite(String url) {
        if (url == null) {
            return "";
        }
        String clean = url.trim().toLowerCase();
        clean = clean.replaceFirst("^https?://", "");
        clean = clean.replaceFirst("^www\\.", "");
        if (clean.contains("/")) {
            clean = clean.substring(0, clean.indexOf('/'));
        }
        if (clean.contains("?")) {
            clean = clean.substring(0, clean.indexOf('?'));
        }
        if (clean.contains(":")) {
            clean = clean.substring(0, clean.indexOf(':'));
        }
        return clean.trim();
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
     * Chuẩn hóa tên công ty phục vụ so sánh: loại bỏ loại hình doanh nghiệp và ký tự đặc biệt.
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
    public static int tinhKhoangCachLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0;
        }
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;

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
     * Tính toán độ tương đồng giữa hai tên công ty (kết hợp Token Dice & Levenshtein).
     * @return giá trị từ 0.0 đến 1.0 (1.0 là trùng 100%)
     */
    public static double tinhDoTuongDongTen(String ten1, String ten2) {
        String c1 = chuanHoaTenCongTy(ten1);
        String c2 = chuanHoaTenCongTy(ten2);

        if (c1.isEmpty() || c2.isEmpty()) {
            return 0.0;
        }
        if (c1.equals(c2)) {
            return 1.0;
        }

        // Nếu một chuỗi chứa trọn vẹn chuỗi kia và có độ dài cốt lõi >= 3
        if ((c1.contains(c2) || c2.contains(c1)) && Math.min(c1.length(), c2.length()) >= 3) {
            return 0.85;
        }

        // Tính Token Dice Coefficient
        String[] words1 = c1.split("\\s+");
        String[] words2 = c2.split("\\s+");
        Set<String> set1 = new HashSet<>();
        for (String w : words1) {
            if (w.length() > 1) set1.add(w);
        }
        Set<String> set2 = new HashSet<>();
        for (String w : words2) {
            if (w.length() > 1) set2.add(w);
        }

        double tokenDice = 0.0;
        if (!set1.isEmpty() && !set2.isEmpty()) {
            int giao = 0;
            for (String w : set1) {
                if (set2.contains(w)) giao++;
            }
            tokenDice = (2.0 * giao) / (set1.size() + set2.size());
        }

        // Tính Levenshtein similarity
        int maxLen = Math.max(c1.length(), c2.length());
        int dist = tinhKhoangCachLevenshtein(c1, c2);
        double levSim = 1.0 - ((double) dist / maxLen);

        return Math.max(tokenDice, levSim);
    }

    /**
     * Kiểm tra một cặp bản ghi xem có thỏa mãn điều kiện trùng lặp hay không (AC1).
     */
    public static CapKhachHangTrungDTO kiemTraCapKhachHang(BanGhiNghiepVuDTO a, BanGhiNghiepVuDTO b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null || a.getId().equals(b.getId())) {
            return null;
        }

        String mstA = chuanHoaMaSoThue(a.getMaSoThue());
        String mstB = chuanHoaMaSoThue(b.getMaSoThue());
        boolean isMst = !mstA.isEmpty() && !mstB.isEmpty() && mstA.equals(mstB);

        String webA = chuanHoaWebsite(a.getWebsite());
        String webB = chuanHoaWebsite(b.getWebsite());
        boolean isWeb = !webA.isEmpty() && !webB.isEmpty() && webA.equals(webB);

        double sim = tinhDoTuongDongTen(a.getTieuDe(), b.getTieuDe());
        boolean isName = sim >= NGUONG_TUONG_DONG_TEN;
        int simPercent = (int) Math.round(sim * 100);

        if (!isMst && !isName && !isWeb) {
            return null;
        }

        CapKhachHangTrungDTO cap = new CapKhachHangTrungDTO();
        cap.setId(a.getId() + "-" + b.getId());
        cap.setBanGhiA(a);
        cap.setBanGhiB(b);
        cap.setTrungMst(isMst);
        cap.setTrungWebsite(isWeb);
        cap.setTrungTen(isName);
        cap.setTyLeTuongDongTen(simPercent);

        List<String> reasons = new ArrayList<>();
        if (isMst) {
            reasons.add("Trùng Mã số thuế: " + a.getMaSoThue());
        }
        if (isName) {
            reasons.add("Tên tương đồng " + simPercent + "% (" + a.getTieuDe() + " ~ " + b.getTieuDe() + ")");
        }
        if (isWeb) {
            reasons.add("Trùng Website: " + webA);
        }
        cap.setDanhSachLyDo(reasons);

        // Phát hiện hai nhân viên kinh doanh cùng phụ trách (Story S3-04)
        boolean xungDot = (a.getNguoiPhuTrachId() != null && b.getNguoiPhuTrachId() != null
                && !a.getNguoiPhuTrachId().equals(b.getNguoiPhuTrachId()));
        cap.setXungDotNhanVien(xungDot);

        // Mức độ tin cậy
        if (isMst) {
            cap.setDoTinCay("Rất cao (95%)");
        } else if (isWeb) {
            cap.setDoTinCay("Cao (90%)");
        } else {
            cap.setDoTinCay("Khá (" + simPercent + "%)");
        }

        return cap;
    }

    /**
     * Quét toàn bộ danh sách khách hàng để tìm tất cả các cặp trùng lặp.
     */
    public static List<CapKhachHangTrungDTO> quetDanhSachTrung(List<BanGhiNghiepVuDTO> danhSach) {
        List<CapKhachHangTrungDTO> result = new ArrayList<>();
        if (danhSach == null || danhSach.size() < 2) {
            return result;
        }

        int n = danhSach.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                CapKhachHangTrungDTO cap = kiemTraCapKhachHang(danhSach.get(i), danhSach.get(j));
                if (cap != null) {
                    result.add(cap);
                }
            }
        }
        return result;
    }
}
