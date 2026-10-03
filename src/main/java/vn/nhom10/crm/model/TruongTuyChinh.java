package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.*;

/**
 * Model đại diện cho một Trường tuỳ chỉnh (Story S2-08).
 * Tương thích hoàn toàn với schema bảng `truong_tuy_chinh` trong database MySQL 8.4 LTS
 * và tương thích giao diện JSP của frontend.
 */
public class TruongTuyChinh implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String loaiDoiTuong; // KHACH_HANG, CO_HOI
    private String maTruong;     // Tên kỹ thuật: vd nguon_khach_hang
    private String tenTruong;    // Nhãn hiển thị: vd Nguồn khách hàng
    private String kieuDuLieu;   // VAN_BAN, SO, NGAY, DANH_SACH_CHON
    private boolean batBuoc = false;
    private String luaChonJson;
    private String giaTriMacDinhJson;
    private int thuTuHienThi = 1;
    private boolean hoatDong = true;
    private Long createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Các thuộc tính mở rộng lưu kèm trong metadata JSON
    private boolean hienThiBoDac = true;
    private boolean hienThiExcel = true;
    private List<String> danhSachLuaChon = new ArrayList<>();

    public TruongTuyChinh() {
    }

    public TruongTuyChinh(String loaiDoiTuong, String maTruong, String tenTruong, String kieuDuLieu, boolean batBuoc) {
        this.loaiDoiTuong = loaiDoiTuong;
        this.maTruong = maTruong;
        this.tenTruong = tenTruong;
        this.kieuDuLieu = kieuDuLieu;
        this.batBuoc = batBuoc;
    }

    // --- Getters and Setters chuẩn ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoaiDoiTuong() {
        return loaiDoiTuong;
    }

    public void setLoaiDoiTuong(String loaiDoiTuong) {
        this.loaiDoiTuong = loaiDoiTuong;
    }

    public String getMaTruong() {
        return maTruong;
    }

    public void setMaTruong(String maTruong) {
        this.maTruong = maTruong;
    }

    /**
     * Trong database: cột `ten_truong` lưu nhãn hiển thị.
     * Để tương thích với EL JSP của frontend:
     * - `${t.nhanHien}` trả về nhãn hiển thị (`tenTruong`).
     * - `${t.tenTruong}` trả về tên kỹ thuật (`maTruong`).
     */
    public String getTenTruong() {
        return maTruong != null ? maTruong : "";
    }

    public void setTenTruong(String tenTruong) {
        this.tenTruong = tenTruong;
    }

    public String getNhanHien() {
        return (tenTruong != null && !tenTruong.isBlank()) ? tenTruong : maTruong;
    }

    public void setNhanHien(String nhanHien) {
        this.tenTruong = nhanHien;
    }

    public void setTenNhan(String tenNhan) {
        this.tenTruong = tenNhan;
    }

    public String getTenNhanGoc() {
        return tenTruong;
    }

    public String getDoiTuong() {
        return loaiDoiTuong;
    }

    public void setDoiTuong(String doiTuong) {
        this.loaiDoiTuong = doiTuong;
    }

    public String getKieuDuLieu() {
        return kieuDuLieu;
    }

    public void setKieuDuLieu(String kieuDuLieu) {
        this.kieuDuLieu = kieuDuLieu;
    }

    public boolean isBatBuoc() {
        return batBuoc;
    }

    public void setBatBuoc(boolean batBuoc) {
        this.batBuoc = batBuoc;
    }

    public String getLuaChonJson() {
        return luaChonJson;
    }

    public void setLuaChonJson(String luaChonJson) {
        this.luaChonJson = luaChonJson;
        this.danhSachLuaChon = parseLuaChonJson(luaChonJson);
    }

    public String getGiaTriMacDinhJson() {
        return giaTriMacDinhJson;
    }

    public void setGiaTriMacDinhJson(String giaTriMacDinhJson) {
        this.giaTriMacDinhJson = giaTriMacDinhJson;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public int getThuTu() {
        return thuTuHienThi;
    }

    public void setThuTu(int thuTu) {
        this.thuTuHienThi = thuTu;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public boolean isDangHoatDong() {
        return hoatDong;
    }

    public void setDangHoatDong(boolean dangHoatDong) {
        this.hoatDong = dangHoatDong;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isHienThiBoDac() {
        return hienThiBoDac;
    }

    public void setHienThiBoDac(boolean hienThiBoDac) {
        this.hienThiBoDac = hienThiBoDac;
    }

    public boolean isHienThiExcel() {
        return hienThiExcel;
    }

    public void setHienThiExcel(boolean hienThiExcel) {
        this.hienThiExcel = hienThiExcel;
    }

    public List<String> getDanhSachLuaChon() {
        if (danhSachLuaChon == null) {
            danhSachLuaChon = new ArrayList<>();
        }
        return danhSachLuaChon;
    }

    public void setDanhSachLuaChon(List<String> danhSachLuaChon) {
        List<String> cleanList = new ArrayList<>();
        if (danhSachLuaChon != null) {
            Set<String> seen = new HashSet<>();
            for (String opt : danhSachLuaChon) {
                if (opt != null && !opt.trim().isEmpty()) {
                    String trimmed = opt.trim();
                    if (seen.add(trimmed.toLowerCase())) {
                        cleanList.add(trimmed);
                    }
                }
            }
        }
        this.danhSachLuaChon = cleanList;
        this.luaChonJson = buildLuaChonJson(cleanList);
    }

    // --- Phương thức tiện ích chuyển đổi JSON đơn giản không phụ thuộc thư viện ngoài ---

    public static List<String> parseLuaChonJson(String json) {
        List<String> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || "[]".equals(json.trim()) || "null".equalsIgnoreCase(json.trim())) {
            return list;
        }
        String clean = json.trim();
        if (clean.startsWith("[") && clean.endsWith("]")) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }
        if (clean.isEmpty()) {
            return list;
        }

        // Tách chuỗi theo dấu phẩy, hỗ trợ trường hợp bọc trong ngoặc kép
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (escapeNext) {
                cur.append(c);
                escapeNext = false;
                continue;
            }
            if (c == '\\') {
                cur.append(c);
                escapeNext = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
                cur.append(c);
            } else if (c == ',' && !inQuotes) {
                String item = unescapeJsonItem(cur.toString().trim());
                if (!item.isEmpty()) {
                    list.add(item);
                }
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        String lastItem = unescapeJsonItem(cur.toString().trim());
        if (!lastItem.isEmpty()) {
            list.add(lastItem);
        }
        return list;
    }

    public static String buildLuaChonJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (String s : list) {
            if (s == null || s.trim().isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            sb.append("\"").append(escapeJsonItem(s.trim())).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String unescapeJsonItem(String item) {
        if (item == null) return "";
        String s = item.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\r", "\r")
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .trim();
    }

    private static String escapeJsonItem(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
