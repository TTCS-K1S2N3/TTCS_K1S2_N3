package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO đại diện cho một cặp khách hàng bị phát hiện nghi trùng lặp (Story S3-04, AC1).
 * Tiêu chí trùng:
 * - Mã số thuế (MST) trùng khớp
 * - Tên công ty gần giống (độ tương đồng cao)
 * - Website trùng khớp
 */
public class CapKhachHangTrungDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id; // ID cặp: ví dụ "1-2"
    private BanGhiNghiepVuDTO banGhiA;
    private BanGhiNghiepVuDTO banGhiB;
    private boolean trungMst;
    private boolean trungTen;
    private int tyLeTuongDongTen;
    private boolean trungWebsite;
    private boolean xungDotNhanVien; // Hai nhân viên khác nhau cùng chào một công ty
    private List<String> danhSachLyDo = new ArrayList<>();
    private String doTinCay; // Rất cao / Cao / Khá

    public CapKhachHangTrungDTO() {
    }

    public CapKhachHangTrungDTO(String id, BanGhiNghiepVuDTO banGhiA, BanGhiNghiepVuDTO banGhiB) {
        this.id = id;
        this.banGhiA = banGhiA;
        this.banGhiB = banGhiB;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BanGhiNghiepVuDTO getBanGhiA() {
        return banGhiA;
    }

    public void setBanGhiA(BanGhiNghiepVuDTO banGhiA) {
        this.banGhiA = banGhiA;
    }

    public BanGhiNghiepVuDTO getBanGhiB() {
        return banGhiB;
    }

    public void setBanGhiB(BanGhiNghiepVuDTO banGhiB) {
        this.banGhiB = banGhiB;
    }

    public boolean isTrungMst() {
        return trungMst;
    }

    public void setTrungMst(boolean trungMst) {
        this.trungMst = trungMst;
    }

    public boolean isTrungTen() {
        return trungTen;
    }

    public void setTrungTen(boolean trungTen) {
        this.trungTen = trungTen;
    }

    public int getTyLeTuongDongTen() {
        return tyLeTuongDongTen;
    }

    public void setTyLeTuongDongTen(int tyLeTuongDongTen) {
        this.tyLeTuongDongTen = tyLeTuongDongTen;
    }

    public boolean isTrungWebsite() {
        return trungWebsite;
    }

    public void setTrungWebsite(boolean trungWebsite) {
        this.trungWebsite = trungWebsite;
    }

    public boolean isXungDotNhanVien() {
        return xungDotNhanVien;
    }

    public void setXungDotNhanVien(boolean xungDotNhanVien) {
        this.xungDotNhanVien = xungDotNhanVien;
    }

    public List<String> getDanhSachLyDo() {
        return danhSachLyDo;
    }

    public void setDanhSachLyDo(List<String> danhSachLyDo) {
        this.danhSachLyDo = danhSachLyDo;
    }

    public String getDoTinCay() {
        return doTinCay;
    }

    public void setDoTinCay(String doTinCay) {
        this.doTinCay = doTinCay;
    }

    /**
     * Chuyển đổi CapKhachHangTrungDTO thành chuỗi JSON tương thích với client-side JS (Story S3-04).
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":\"").append(escapeJson(id)).append("\",");
        sb.append("\"isMst\":").append(trungMst).append(",");
        sb.append("\"isName\":").append(trungTen).append(",");
        sb.append("\"simPercent\":").append(tyLeTuongDongTen).append(",");
        sb.append("\"isWeb\":").append(trungWebsite).append(",");
        sb.append("\"isConflict\":").append(xungDotNhanVien).append(",");
        sb.append("\"doTinCay\":\"").append(escapeJson(doTinCay != null ? doTinCay : "")).append("\",");

        sb.append("\"reasons\":[");
        if (danhSachLyDo != null) {
            for (int i = 0; i < danhSachLyDo.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(danhSachLyDo.get(i))).append("\"");
            }
        }
        sb.append("],");

        sb.append("\"recordA\":").append(recordToJson(banGhiA)).append(",");
        sb.append("\"recordB\":").append(recordToJson(banGhiB));
        sb.append("}");
        return sb.toString();
    }

    private String recordToJson(BanGhiNghiepVuDTO r) {
        if (r == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":\"").append(r.getId() != null ? r.getId() : "").append("\",");
        sb.append("\"ma\":\"").append(escapeJson(r.getMaBanGhi())).append("\",");
        sb.append("\"ten\":\"").append(escapeJson(r.getTieuDe())).append("\",");
        sb.append("\"mst\":\"").append(escapeJson(r.getMaSoThue())).append("\",");
        sb.append("\"web\":\"").append(escapeJson(r.getWebsite())).append("\",");
        sb.append("\"ownerId\":\"").append(r.getNguoiPhuTrachId() != null ? r.getNguoiPhuTrachId() : "").append("\",");
        sb.append("\"ownerName\":\"").append(escapeJson(r.getTenNguoiPhuTrach())).append("\",");
        sb.append("\"teamName\":\"").append(escapeJson(r.getTenNhom())).append("\",");
        sb.append("\"giaTri\":\"").append(escapeJson(r.getGiaTri())).append("\",");
        sb.append("\"trangThai\":\"").append(escapeJson(r.getTrangThai())).append("\",");
        sb.append("\"ngayTao\":\"").append(r.getNgayTao() != null ? r.getNgayTao().toString() : "").append("\",");
        sb.append("\"moTa\":\"").append(escapeJson(r.getMoTaChiTiet())).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public static String danhSachToJson(List<CapKhachHangTrungDTO> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(list.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String t = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
