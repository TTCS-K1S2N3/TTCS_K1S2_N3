package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO chứa thông tin trạng thái rủi ro rời bỏ của khách hàng (Story S3-08 AC2, AC3).
 * Dùng để hiển thị cờ rủi ro và cảnh báo trên trang 360 và gửi cho NVKD phụ trách.
 */
public class ThongTinRuiRoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long khachHangId;
    private String tenCongTy;
    private Long nguoiSoHuuId;
    private String tenNguoiSoHuu;
    private boolean coRuiRo;
    private LocalDateTime ruiRoCapNhatLuc;
    private int soYeuCauChuaXuLy;
    private int nguongRuiRo;
    private String thongDiepCanhBao;

    public ThongTinRuiRoDTO() {
    }

    public ThongTinRuiRoDTO(Long khachHangId, String tenCongTy, Long nguoiSoHuuId, String tenNguoiSoHuu,
                           boolean coRuiRo, LocalDateTime ruiRoCapNhatLuc, int soYeuCauChuaXuLy,
                           int nguongRuiRo, String thongDiepCanhBao) {
        this.khachHangId = khachHangId;
        this.tenCongTy = tenCongTy;
        this.nguoiSoHuuId = nguoiSoHuuId;
        this.tenNguoiSoHuu = tenNguoiSoHuu;
        this.coRuiRo = coRuiRo;
        this.ruiRoCapNhatLuc = ruiRoCapNhatLuc;
        this.soYeuCauChuaXuLy = soYeuCauChuaXuLy;
        this.nguongRuiRo = nguongRuiRo;
        this.thongDiepCanhBao = thongDiepCanhBao;
    }

    public Long getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(Long khachHangId) {
        this.khachHangId = khachHangId;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public Long getNguoiSoHuuId() {
        return nguoiSoHuuId;
    }

    public void setNguoiSoHuuId(Long nguoiSoHuuId) {
        this.nguoiSoHuuId = nguoiSoHuuId;
    }

    public String getTenNguoiSoHuu() {
        return tenNguoiSoHuu;
    }

    public void setTenNguoiSoHuu(String tenNguoiSoHuu) {
        this.tenNguoiSoHuu = tenNguoiSoHuu;
    }

    public boolean isCoRuiRo() {
        return coRuiRo;
    }

    public void setCoRuiRo(boolean coRuiRo) {
        this.coRuiRo = coRuiRo;
    }

    public LocalDateTime getRuiRoCapNhatLuc() {
        return ruiRoCapNhatLuc;
    }

    public void setRuiRoCapNhatLuc(LocalDateTime ruiRoCapNhatLuc) {
        this.ruiRoCapNhatLuc = ruiRoCapNhatLuc;
    }

    public int getSoYeuCauChuaXuLy() {
        return soYeuCauChuaXuLy;
    }

    public void setSoYeuCauChuaXuLy(int soYeuCauChuaXuLy) {
        this.soYeuCauChuaXuLy = soYeuCauChuaXuLy;
    }

    public int getNguongRuiRo() {
        return nguongRuiRo;
    }

    public void setNguongRuiRo(int nguongRuiRo) {
        this.nguongRuiRo = nguongRuiRo;
    }

    public String getThongDiepCanhBao() {
        return thongDiepCanhBao;
    }

    public void setThongDiepCanhBao(String thongDiepCanhBao) {
        this.thongDiepCanhBao = thongDiepCanhBao;
    }
}
