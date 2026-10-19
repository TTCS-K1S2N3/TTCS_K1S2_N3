package vn.nhom10.crm.util;

import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTroEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility định dạng hiển thị dữ liệu Audit Log (Story S2-04) thân thiện cho người dùng trên giao diện UI.
 * Giữ nguyên dữ liệu kỹ thuật/raw trong CSDL để phục vụ kiểm toán đối soát,
 * đồng thời chuyển đổi vai trò, trường kỹ thuật, trạng thái, nhóm kinh doanh sang tiếng Việt dễ đọc.
 */
public final class AuditLogFormatter {

    private static final Logger LOGGER = Logger.getLogger(AuditLogFormatter.class.getName());

    public static final String KHONG_CO = "Không có";
    public static final String CHUA_THIET_LAP = "Chưa thiết lập";

    private static final Pattern BRACKET_PATTERN = Pattern.compile("^\\[(.*)\\]$", Pattern.DOTALL);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"([^\"]*)\"|'([^']*)'|([^,;\\[\\]\\s]+)");

    // Cache tên nhóm kinh doanh để tránh N+1 query khi hiển thị danh sách nhật ký
    private static volatile Map<String, String> cacheTenNhom = new ConcurrentHashMap<>();
    private static volatile long thoiDiemNapCacheNhom = 0L;
    private static final long CACHE_TTL_MS = 60_000L; // 60 giây

    // Mapping vai trò mở rộng nếu có mã không nằm trong VaiTroEnum
    private static final Map<String, String> VAI_TRO_MAP = new HashMap<>();

    static {
        for (VaiTroEnum vt : VaiTroEnum.values()) {
            VAI_TRO_MAP.put(vt.getMaVaiTro().toUpperCase(), vt.getTenTiengViet());
        }
        VAI_TRO_MAP.put("QUAN_TRI", "Quản trị hệ thống");
        VAI_TRO_MAP.put("GIAM_DOC", "Giám đốc kinh doanh");
        VAI_TRO_MAP.put("TRUONG_NHOM", "Trưởng nhóm kinh doanh");
        VAI_TRO_MAP.put("NHAN_VIEN_KD", "Nhân viên kinh doanh");
        VAI_TRO_MAP.put("KE_TOAN", "Kế toán");
        VAI_TRO_MAP.put("CSKH", "Chăm sóc khách hàng");
    }

    // Mapping trường kỹ thuật sang nhãn tiếng Việt thân thiện
    private static final Map<String, String> TRUONG_MAP = new HashMap<>();

    static {
        TRUONG_MAP.put("vai_tro", "Vai trò");
        TRUONG_MAP.put("nguoi_dung_vai_tro", "Vai trò");
        TRUONG_MAP.put("ds_vai_tro", "Vai trò");
        TRUONG_MAP.put("vai_tro_ids", "Vai trò");
        TRUONG_MAP.put("vaitro", "Vai trò");
        TRUONG_MAP.put("vai_tro_nguoi_dung", "Vai trò");

        TRUONG_MAP.put("ho_ten", "Họ và tên");
        TRUONG_MAP.put("hoten", "Họ và tên");
        TRUONG_MAP.put("ten", "Họ và tên");

        TRUONG_MAP.put("email", "Email");

        TRUONG_MAP.put("so_dien_thoai", "Số điện thoại");
        TRUONG_MAP.put("sdt", "Số điện thoại");
        TRUONG_MAP.put("dienthoai", "Số điện thoại");

        TRUONG_MAP.put("nhom_kinh_doanh_id", "Nhóm kinh doanh");
        TRUONG_MAP.put("nhom_kinh_doanh", "Nhóm kinh doanh");
        TRUONG_MAP.put("nhom_id", "Nhóm kinh doanh");
        TRUONG_MAP.put("nhomid", "Nhóm kinh doanh");

        TRUONG_MAP.put("trang_thai", "Trạng thái");
        TRUONG_MAP.put("trangthai", "Trạng thái");

        TRUONG_MAP.put("mat_khau", "Mật khẩu");
        TRUONG_MAP.put("password", "Mật khẩu");

        TRUONG_MAP.put("chiet_khau", "Chiết khấu");
        TRUONG_MAP.put("ty_le_chiet_khau", "Chiết khấu");

        TRUONG_MAP.put("chi_tieu", "Chỉ tiêu");
        TRUONG_MAP.put("kpi", "Chỉ tiêu");

        TRUONG_MAP.put("quyen_so_huu", "Quyền sở hữu");
        TRUONG_MAP.put("owner", "Quyền sở hữu");

        TRUONG_MAP.put("chu_ky_email", "Chữ ký email");
    }

    private AuditLogFormatter() {
    }

    /**
     * Định dạng tên trường kỹ thuật sang nhãn tiếng Việt dễ hiểu trên giao diện.
     * Ví dụ:
     *   "vai_tro" -> "Vai trò"
     *   "ho_ten" -> "Họ và tên"
     *   "nhom_kinh_doanh_id" -> "Nhóm kinh doanh"
     */
    public static String dinhDangTruong(String truong) {
        if (truong == null || truong.isBlank() || "null".equalsIgnoreCase(truong.trim())) {
            return "Dữ liệu đối tượng";
        }
        String key = truong.trim().toLowerCase();
        if (TRUONG_MAP.containsKey(key)) {
            return TRUONG_MAP.get(key);
        }
        // Nếu tên trường đã có dấu tiếng Việt hoặc khoảng trắng (ví dụ "Vai trò người dùng"), giữ nguyên
        if (truong.contains(" ")) {
            return truong.trim();
        }
        // Trường kỹ thuật chưa có mapping cụ thể: chuyển snake_case sang chữ thường an toàn, không ném ngoại lệ
        return truong.trim().replace('_', ' ');
    }

    /**
     * Định dạng giá trị trước hoặc sau khi thay đổi sang nội dung thân thiện cho người dùng.
     *
     * @param truong  Tên trường thay đổi (có thể là mã kỹ thuật hoặc nhãn tiếng Việt)
     * @param giaTri  Giá trị thô (raw) lưu trong CSDL
     * @return Giá trị đã định dạng thân thiện
     */
    public static String dinhDangGiaTri(String truong, String giaTri) {
        return dinhDangGiaTri(truong, giaTri, null);
    }

    /**
     * Định dạng giá trị kèm lookup map tùy chọn (ví dụ tên nhóm).
     */
    public static String dinhDangGiaTri(String truong, String giaTri, Map<String, String> lookupMap) {
        if (giaTri == null || giaTri.isBlank() || "null".equalsIgnoreCase(giaTri.trim())) {
            return KHONG_CO;
        }

        String trimVal = giaTri.trim();

        // Xử lý mảng rỗng []
        if ("[]".equals(trimVal) || "[\"\"]".equals(trimVal) || "['']".equals(trimVal)) {
            return KHONG_CO;
        }

        // Nếu là mật khẩu hoặc bí mật đã bị redact
        if ("******".equals(trimVal)) {
            return "******";
        }

        String truongKey = (truong != null) ? truong.trim().toLowerCase() : "";

        // Tuyệt đối không để lộ mật khẩu, token, secret...
        if (truong != null && vn.nhom10.crm.model.NhatKyThayDoi.SENSITIVE_KEY_PATTERN.matcher(truongKey).matches()) {
            return "******";
        }

        // 1. Nếu là trường vai trò hoặc giá trị có dạng mảng vai trò ["..."]
        if (laTruongVaiTro(truongKey) || (trimVal.startsWith("[") && trimVal.endsWith("]"))) {
            return dinhDangVaiTro(trimVal);
        }

        // 2. Nếu là vai trò đơn lẻ (ví dụ "TEAM_LEAD", "ADMIN")
        if (laMaVaiTroHopLe(trimVal)) {
            return dinhDangVaiTro(trimVal);
        }

        // 3. Nếu là nhóm kinh doanh (ví dụ "nhom_kinh_doanh_id", "2 -> 3")
        if (laTruongNhomKinhDoanh(truongKey)) {
            return dinhDangNhomKinhDoanh(trimVal, lookupMap);
        }

        // 4. Nếu là trạng thái người dùng (ví dụ "HOAT_DONG", "CHO_KICH_HOAT")
        if (laTruongTrangThai(truongKey) || "HOAT_DONG".equalsIgnoreCase(trimVal)
                || "CHO_KICH_HOAT".equalsIgnoreCase(trimVal) || "KHOA".equalsIgnoreCase(trimVal)
                || "BI_KHOA".equalsIgnoreCase(trimVal)) {
            return dinhDangTrangThai(trimVal);
        }

        // 5. Giá trị thông thường khác: nếu chuỗi literal là "null", đổi sang "Không có"
        if ("null".equalsIgnoreCase(trimVal)) {
            return KHONG_CO;
        }

        return trimVal;
    }

    /**
     * Định dạng chuỗi vai trò từ mảng JSON hoặc mã đơn lẻ sang danh sách tên tiếng Việt.
     * Ví dụ:
     *   "[\"ACCOUNTANT\"]" -> "Kế toán"
     *   "[\"ADMIN\",\"ACCOUNTANT\"]" -> "Quản trị hệ thống, Kế toán"
     *   "TEAM_LEAD" -> "Trưởng nhóm kinh doanh"
     *   "[]" -> "Không có"
     *   null -> "Không có"
     */
    public static String dinhDangVaiTro(String vaiTroRaw) {
        if (vaiTroRaw == null || vaiTroRaw.isBlank() || "null".equalsIgnoreCase(vaiTroRaw.trim())) {
            return KHONG_CO;
        }

        String trimmed = vaiTroRaw.trim();
        if ("[]".equals(trimmed)) {
            return KHONG_CO;
        }

        Matcher bracketMatcher = BRACKET_PATTERN.matcher(trimmed);
        if (bracketMatcher.matches()) {
            String content = bracketMatcher.group(1).trim();
            if (content.isEmpty()) {
                return KHONG_CO;
            }

            List<String> items = new ArrayList<>();
            Matcher tokenMatcher = TOKEN_PATTERN.matcher(content);
            while (tokenMatcher.find()) {
                String token = null;
                if (tokenMatcher.group(1) != null) token = tokenMatcher.group(1);
                else if (tokenMatcher.group(2) != null) token = tokenMatcher.group(2);
                else if (tokenMatcher.group(3) != null) token = tokenMatcher.group(3);

                if (token != null && !token.isBlank() && !"null".equalsIgnoreCase(token.trim())) {
                    items.add(dichMaVaiTroDon(token.trim()));
                }
            }

            if (items.isEmpty()) {
                return KHONG_CO;
            }
            return String.join(", ", items);
        }

        // Mã đơn lẻ không nằm trong mảng []
        return dichMaVaiTroDon(trimmed);
    }

    /**
     * Dịch một mã vai trò duy nhất sang tiếng Việt. Nếu không có mapping, trả về mã an toàn, không ném exception.
     */
    private static String dichMaVaiTroDon(String code) {
        if (code == null || code.isBlank() || "null".equalsIgnoreCase(code.trim())) {
            return KHONG_CO;
        }
        String clean = code.trim().toUpperCase();
        if (VAI_TRO_MAP.containsKey(clean)) {
            return VAI_TRO_MAP.get(clean);
        }
        VaiTroEnum vte = VaiTroEnum.tuMa(clean);
        if (vte != null) {
            return vte.getTenTiengViet();
        }
        // Giá trị không xác định: trả về an toàn, không gây crash trang
        return code.trim();
    }

    /**
     * Định dạng nhóm kinh doanh. Nếu là ID số (ví dụ "2"), ưu tiên lấy tên nhóm (ví dụ "Kinh doanh Miền Bắc").
     */
    public static String dinhDangNhomKinhDoanh(String nhomGiaTri, Map<String, String> lookupMap) {
        if (nhomGiaTri == null || nhomGiaTri.isBlank() || "null".equalsIgnoreCase(nhomGiaTri.trim())) {
            return KHONG_CO;
        }

        String val = nhomGiaTri.trim();

        // Xử lý dạng chuyển đổi "2 -> 3" nếu có
        if (val.contains("->")) {
            String[] parts = val.split("->");
            List<String> formattedParts = new ArrayList<>();
            for (String p : parts) {
                formattedParts.add(dinhDangNhomKinhDoanh(p.trim(), lookupMap));
            }
            return String.join(" -> ", formattedParts);
        }

        // Kiểm tra trong custom lookup map trước
        if (lookupMap != null && lookupMap.containsKey(val)) {
            return lookupMap.get(val);
        }

        // Thử tìm trong cache tên nhóm kinh doanh hệ thống
        Map<String, String> cache = layCacheNhomKinhDoanh();
        if (cache != null && cache.containsKey(val)) {
            return cache.get(val);
        }

        // Nếu là số ID nhưng chưa có tên trong DB, hiển thị "Nhóm #ID" thân thiện hơn số trần
        if (val.matches("^\\d+$")) {
            return "Nhóm #" + val;
        }

        return val;
    }

    /**
     * Định dạng trạng thái người dùng sang tiếng Việt.
     */
    public static String dinhDangTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank() || "null".equalsIgnoreCase(trangThai.trim())) {
            return KHONG_CO;
        }
        String clean = trangThai.trim().toUpperCase();
        switch (clean) {
            case "HOAT_DONG":
            case "ACTIVE":
                return "Hoạt động";
            case "CHO_KICH_HOAT":
            case "PENDING":
                return "Chờ kích hoạt";
            case "KHOA":
            case "BI_KHOA":
            case "DA_KHOA":
            case "LOCKED":
                return "Đã khóa";
            default:
                return trangThai.trim();
        }
    }

    private static boolean laTruongVaiTro(String truongKey) {
        return truongKey.contains("vai_tro") || truongKey.contains("vaitro")
                || truongKey.contains("vai trò") || "quyen".equals(truongKey)
                || "quyền".equals(truongKey);
    }

    private static boolean laTruongNhomKinhDoanh(String truongKey) {
        return truongKey.contains("nhom") || truongKey.contains("nhóm");
    }

    private static boolean laTruongTrangThai(String truongKey) {
        return truongKey.contains("trang_thai") || truongKey.contains("trangthai")
                || truongKey.contains("trạng thái");
    }

    private static boolean laMaVaiTroHopLe(String code) {
        if (code == null) return false;
        String c = code.trim().toUpperCase();
        return VAI_TRO_MAP.containsKey(c) || VaiTroEnum.tuMa(c) != null;
    }

    /**
     * Nạp và cache danh sách tên nhóm kinh doanh phục vụ hiển thị nhanh không bị N+1 query.
     */
    private static Map<String, String> layCacheNhomKinhDoanh() {
        long now = System.currentTimeMillis();
        if (cacheTenNhom != null && !cacheTenNhom.isEmpty() && (now - thoiDiemNapCacheNhom < CACHE_TTL_MS)) {
            return cacheTenNhom;
        }

        try {
            NhomKinhDoanhDAO dao = new NhomKinhDoanhDAO();
            List<NhomKinhDoanh> ds = dao.layTatCa();
            if (ds != null && !ds.isEmpty()) {
                Map<String, String> mapMoi = new ConcurrentHashMap<>();
                for (NhomKinhDoanh nhom : ds) {
                    if (nhom.getTenNhom() != null) {
                        mapMoi.put(String.valueOf(nhom.getId()), nhom.getTenNhom());
                        if (nhom.getMaNhom() != null) {
                            mapMoi.put(nhom.getMaNhom().toUpperCase(), nhom.getTenNhom());
                        }
                    }
                }
                cacheTenNhom = mapMoi;
                thoiDiemNapCacheNhom = now;
                return cacheTenNhom;
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Không thể nạp danh mục nhóm kinh doanh vào cache: " + e.getMessage());
        }

        return cacheTenNhom;
    }

    /**
     * Cho phép cấu hình hoặc gán danh mục nhóm kinh doanh chủ động (dùng cho Unit test).
     */
    public static void setCacheTenNhom(Map<String, String> map) {
        if (map != null) {
            cacheTenNhom = new ConcurrentHashMap<>(map);
            thoiDiemNapCacheNhom = System.currentTimeMillis();
        }
    }

    public static void xoaCacheTenNhom() {
        cacheTenNhom.clear();
        thoiDiemNapCacheNhom = 0L;
    }
}
