package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO trả về kết quả sau khi upload và xử lý ảnh đại diện (avatar).
 */
public class KetQuaUploadAvatarDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private String thongDiep;
    private String anhDaiDienPath;
    private String anhDaiDienThumbPath;
    private String anhDaiDienUrl;
    private String anhDaiDienThumbUrl;
    private long dungLuongBytes;

    public KetQuaUploadAvatarDTO() {
    }

    public static KetQuaUploadAvatarDTO thanhCong(String thongDiep, String path, String thumbPath, String url, String thumbUrl, long dungLuong) {
        KetQuaUploadAvatarDTO dto = new KetQuaUploadAvatarDTO();
        dto.thanhCong = true;
        dto.thongDiep = thongDiep;
        dto.anhDaiDienPath = path;
        dto.anhDaiDienThumbPath = thumbPath;
        dto.anhDaiDienUrl = url;
        dto.anhDaiDienThumbUrl = thumbUrl;
        dto.dungLuongBytes = dungLuong;
        return dto;
    }

    public static KetQuaUploadAvatarDTO thatBai(String thongDiep) {
        KetQuaUploadAvatarDTO dto = new KetQuaUploadAvatarDTO();
        dto.thanhCong = false;
        dto.thongDiep = thongDiep;
        return dto;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public String getThongDiep() {
        return thongDiep;
    }

    public void setThongDiep(String thongDiep) {
        this.thongDiep = thongDiep;
    }

    public String getAnhDaiDienPath() {
        return anhDaiDienPath;
    }

    public void setAnhDaiDienPath(String anhDaiDienPath) {
        this.anhDaiDienPath = anhDaiDienPath;
    }

    public String getAnhDaiDienThumbPath() {
        return anhDaiDienThumbPath;
    }

    public void setAnhDaiDienThumbPath(String anhDaiDienThumbPath) {
        this.anhDaiDienThumbPath = anhDaiDienThumbPath;
    }

    public String getAnhDaiDienUrl() {
        return anhDaiDienUrl;
    }

    public void setAnhDaiDienUrl(String anhDaiDienUrl) {
        this.anhDaiDienUrl = anhDaiDienUrl;
    }

    public String getAnhDaiDienThumbUrl() {
        return anhDaiDienThumbUrl;
    }

    public void setAnhDaiDienThumbUrl(String anhDaiDienThumbUrl) {
        this.anhDaiDienThumbUrl = anhDaiDienThumbUrl;
    }

    public long getDungLuongBytes() {
        return dungLuongBytes;
    }

    public void setDungLuongBytes(long dungLuongBytes) {
        this.dungLuongBytes = dungLuongBytes;
    }
}
