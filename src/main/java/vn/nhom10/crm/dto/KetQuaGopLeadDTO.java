package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO kết quả thực hiện gộp Lead (Story S4-04, AC3).
 */
public class KetQuaGopLeadDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean thanhCong;
    private String thongDiep;
    private Long survivorLeadId;
    private Long mergedSourceLeadId;
    private int soHoatDongDaChuyen = 0;
    private int soCongViecDaChuyen = 0;
    private int soCoHoiDaChuyen = 0;
    private LocalDateTime thoiGianGop;

    public KetQuaGopLeadDTO() {
    }

    public static KetQuaGopLeadDTO thanhCong(Long survivorLeadId, Long mergedSourceLeadId,
                                            int soHoatDong, int soCongViec, int soCoHoi, String thongDiep) {
        KetQuaGopLeadDTO kq = new KetQuaGopLeadDTO();
        kq.setThanhCong(true);
        kq.setSurvivorLeadId(survivorLeadId);
        kq.setMergedSourceLeadId(mergedSourceLeadId);
        kq.setSoHoatDongDaChuyen(soHoatDong);
        kq.setSoCongViecDaChuyen(soCongViec);
        kq.setSoCoHoiDaChuyen(soCoHoi);
        kq.setThongDiep(thongDiep);
        kq.setThoiGianGop(LocalDateTime.now());
        return kq;
    }

    public static KetQuaGopLeadDTO thatBai(String thongDiep) {
        KetQuaGopLeadDTO kq = new KetQuaGopLeadDTO();
        kq.setThanhCong(false);
        kq.setThongDiep(thongDiep);
        kq.setThoiGianGop(LocalDateTime.now());
        return kq;
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

    public Long getSurvivorLeadId() {
        return survivorLeadId;
    }

    public void setSurvivorLeadId(Long survivorLeadId) {
        this.survivorLeadId = survivorLeadId;
    }

    public Long getMergedSourceLeadId() {
        return mergedSourceLeadId;
    }

    public void setMergedSourceLeadId(Long mergedSourceLeadId) {
        this.mergedSourceLeadId = mergedSourceLeadId;
    }

    public int getSoHoatDongDaChuyen() {
        return soHoatDongDaChuyen;
    }

    public void setSoHoatDongDaChuyen(int soHoatDongDaChuyen) {
        this.soHoatDongDaChuyen = soHoatDongDaChuyen;
    }

    public int getSoCongViecDaChuyen() {
        return soCongViecDaChuyen;
    }

    public void setSoCongViecDaChuyen(int soCongViecDaChuyen) {
        this.soCongViecDaChuyen = soCongViecDaChuyen;
    }

    public int getSoCoHoiDaChuyen() {
        return soCoHoiDaChuyen;
    }

    public void setSoCoHoiDaChuyen(int soCoHoiDaChuyen) {
        this.soCoHoiDaChuyen = soCoHoiDaChuyen;
    }

    public int getTongLichSuDaChuyen() {
        return soHoatDongDaChuyen + soCongViecDaChuyen + soCoHoiDaChuyen;
    }

    public LocalDateTime getThoiGianGop() {
        return thoiGianGop;
    }

    public void setThoiGianGop(LocalDateTime thoiGianGop) {
        this.thoiGianGop = thoiGianGop;
    }
}
