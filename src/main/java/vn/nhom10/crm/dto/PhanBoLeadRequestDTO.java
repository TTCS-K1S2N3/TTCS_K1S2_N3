package vn.nhom10.crm.dto;

import java.util.List;

/**
 * DTO nhận dữ liệu gửi lên khi tạo mới hoặc cập nhật quy tắc phân bổ lead (Story S4-06).
 */
public class PhanBoLeadRequestDTO {

    private Long id;
    private String tenQuyTac;
    private String kieuPhanBo; // "THEO_KHU_VUC", "THEO_NGANH_NGHE", "XOAY_VONG"
    private List<Long> khuVucIds;
    private List<Long> nganhNgheIds;
    private Long nhomDichId;
    private Long nguoiDungDichId;
    private Boolean hoatDong = true;

    public PhanBoLeadRequestDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenQuyTac() {
        return tenQuyTac;
    }

    public void setTenQuyTac(String tenQuyTac) {
        this.tenQuyTac = tenQuyTac;
    }

    public String getKieuPhanBo() {
        return kieuPhanBo;
    }

    public void setKieuPhanBo(String kieuPhanBo) {
        this.kieuPhanBo = kieuPhanBo;
    }

    public List<Long> getKhuVucIds() {
        return khuVucIds;
    }

    public void setKhuVucIds(List<Long> khuVucIds) {
        this.khuVucIds = khuVucIds;
    }

    public List<Long> getNganhNgheIds() {
        return nganhNgheIds;
    }

    public void setNganhNgheIds(List<Long> nganhNgheIds) {
        this.nganhNgheIds = nganhNgheIds;
    }

    public Long getNhomDichId() {
        return nhomDichId;
    }

    public void setNhomDichId(Long nhomDichId) {
        this.nhomDichId = nhomDichId;
    }

    public Long getNguoiDungDichId() {
        return nguoiDungDichId;
    }

    public void setNguoiDungDichId(Long nguoiDungDichId) {
        this.nguoiDungDichId = nguoiDungDichId;
    }

    public Boolean getHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(Boolean hoatDong) {
        this.hoatDong = hoatDong;
    }
}
