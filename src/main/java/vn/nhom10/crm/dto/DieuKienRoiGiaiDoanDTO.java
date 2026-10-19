package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO phục vụ kiểm tra và phản hồi điều kiện bắt buộc để rời giai đoạn pipeline (S2-09 - AC 3).
 */
public class DieuKienRoiGiaiDoanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int giaiDoanId;
    private String tenGiaiDoan;
    private boolean thoaDieuKien = true;
    private List<String> danhSachYeuCauThieu = new ArrayList<>();
    private String thongBaoChiTiet;

    // Dữ liệu hoạt động thực tế kiểm tra
    private int soCuocGapDaCo = 0;
    private int soCuocGoiDaCo = 0;
    private boolean daCoBaoGia = false;
    private boolean daKhaoSatNhuCau = false;

    public DieuKienRoiGiaiDoanDTO() {
    }

    public DieuKienRoiGiaiDoanDTO(int giaiDoanId, String tenGiaiDoan) {
        this.giaiDoanId = giaiDoanId;
        this.tenGiaiDoan = tenGiaiDoan;
    }

    public void themYeuCauThieu(String yeuCau) {
        this.danhSachYeuCauThieu.add(yeuCau);
        this.thoaDieuKien = false;
    }

    public int getGiaiDoanId() {
        return giaiDoanId;
    }

    public void setGiaiDoanId(int giaiDoanId) {
        this.giaiDoanId = giaiDoanId;
    }

    public String getTenGiaiDoan() {
        return tenGiaiDoan;
    }

    public void setTenGiaiDoan(String tenGiaiDoan) {
        this.tenGiaiDoan = tenGiaiDoan;
    }

    public int getSoCuocGapDaCo() {
        return soCuocGapDaCo;
    }

    public void setSoCuocGapDaCo(int soCuocGapDaCo) {
        this.soCuocGapDaCo = soCuocGapDaCo;
    }

    public int getSoCuocGoiDaCo() {
        return soCuocGoiDaCo;
    }

    public void setSoCuocGoiDaCo(int soCuocGoiDaCo) {
        this.soCuocGoiDaCo = soCuocGoiDaCo;
    }

    public boolean isDaCoBaoGia() {
        return daCoBaoGia;
    }

    public void setDaCoBaoGia(boolean daCoBaoGia) {
        this.daCoBaoGia = daCoBaoGia;
    }

    public boolean isDaKhaoSatNhuCau() {
        return daKhaoSatNhuCau;
    }

    public void setDaKhaoSatNhuCau(boolean daKhaoSatNhuCau) {
        this.daKhaoSatNhuCau = daKhaoSatNhuCau;
    }

    public boolean isThoaDieuKien() {
        return thoaDieuKien;
    }

    public void setThoaDieuKien(boolean thoaDieuKien) {
        this.thoaDieuKien = thoaDieuKien;
    }

    public List<String> getDanhSachYeuCauThieu() {
        return danhSachYeuCauThieu;
    }

    public void setDanhSachYeuCauThieu(List<String> danhSachYeuCauThieu) {
        this.danhSachYeuCauThieu = danhSachYeuCauThieu != null ? danhSachYeuCauThieu : new ArrayList<>();
    }

    public String getThongBaoChiTiet() {
        if (!thoaDieuKien && (thongBaoChiTiet == null || thongBaoChiTiet.isBlank())) {
            return "Chưa đủ điều kiện rời giai đoạn '" + tenGiaiDoan + "': " + String.join("; ", danhSachYeuCauThieu);
        }
        return thongBaoChiTiet != null ? thongBaoChiTiet : "Đã thỏa mãn tất cả điều kiện rời giai đoạn.";
    }

    public void setThongBaoChiTiet(String thongBaoChiTiet) {
        this.thongBaoChiTiet = thongBaoChiTiet;
    }
}
