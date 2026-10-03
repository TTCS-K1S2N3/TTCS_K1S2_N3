package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.GiaiDoanPipelineDAO;
import vn.nhom10.crm.dto.DieuKienRoiGiaiDoanDTO;
import vn.nhom10.crm.dto.DuBaoDoanhSoDTO;
import vn.nhom10.crm.dto.KetQuaGiaiDoanDTO;
import vn.nhom10.crm.model.GiaiDoanPipeline;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.TrangThaiGiaiDoanEnum;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service xử lý nghiệp vụ cấu hình pipeline, xác suất thắng và dự báo doanh số (Story S2-09).
 */
public class GiaiDoanPipelineService {

    private static final Logger LOGGER = Logger.getLogger(GiaiDoanPipelineService.class.getName());

    private static final Pattern MA_GIAI_DOAN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{2,50}$");

    private final GiaiDoanPipelineDAO giaiDoanPipelineDAO;

    public GiaiDoanPipelineService() {
        this.giaiDoanPipelineDAO = new GiaiDoanPipelineDAO();
    }

    public GiaiDoanPipelineService(GiaiDoanPipelineDAO giaiDoanPipelineDAO) {
        this.giaiDoanPipelineDAO = giaiDoanPipelineDAO;
    }

    /**
     * Kiểm tra quyền Giám đốc kinh doanh hoặc Quản trị viên (ADMIN / DIRECTOR).
     */
    public boolean coQuyenCauHinh(NguoiDung nguoiDung) {
        if (nguoiDung == null || !nguoiDung.dangHoatDong()) {
            return false;
        }
        return nguoiDung.coVaiTro(VaiTroEnum.DIRECTOR)
                || nguoiDung.coVaiTro(VaiTroEnum.ADMIN)
                || nguoiDung.coVaiTro("GIAM_DOC")
                || nguoiDung.coVaiTro("QUAN_TRI");
    }

    /**
     * Lấy toàn bộ danh sách giai đoạn theo thứ tự phễu bán hàng.
     */
    public List<GiaiDoanPipeline> layTatCaGiaiDoan() {
        return giaiDoanPipelineDAO.layTatCaGiaiDoan();
    }

    /**
     * Tìm thông tin giai đoạn theo ID.
     */
    public GiaiDoanPipeline timTheoId(int id) {
        return giaiDoanPipelineDAO.timTheoId(id);
    }

    /**
     * AC 1: Khai báo chuỗi giai đoạn bán hàng mới.
     */
    public KetQuaGiaiDoanDTO themGiaiDoan(GiaiDoanPipeline gd, NguoiDung nguoiDung) {
        if (!coQuyenCauHinh(nguoiDung)) {
            return KetQuaGiaiDoanDTO.thatBai("Bạn không có quyền cấu hình giai đoạn pipeline.");
        }

        KetQuaGiaiDoanDTO ketQua = new KetQuaGiaiDoanDTO();
        validateDuLieuGiaiDoan(gd, null, ketQua);
        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThongBao("Dữ liệu giai đoạn không hợp lệ. Vui lòng kiểm tra lại các trường.");
            return ketQua;
        }

        try {
            int newId = giaiDoanPipelineDAO.themGiaiDoan(gd);
            if (newId > 0) {
                gd.setId(newId);
                return KetQuaGiaiDoanDTO.thanhCong(gd, "Thêm giai đoạn '" + gd.getTenGiaiDoan() + "' thành công.");
            } else {
                return KetQuaGiaiDoanDTO.thatBai("Không thể tạo mới giai đoạn pipeline.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi thêm giai đoạn pipeline: " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("uk_gdp_ma")) {
                return KetQuaGiaiDoanDTO.loiTrungMa(gd.getMaGiaiDoan());
            }
            if (e.getMessage() != null && e.getMessage().contains("uk_gdp_tt")) {
                return KetQuaGiaiDoanDTO.thatBai("Thứ tự " + gd.getThuTu() + " đã tồn tại trong chuỗi pipeline.");
            }
            return KetQuaGiaiDoanDTO.thatBai("Lỗi hệ thống: " + e.getMessage());
        }
    }

    public KetQuaGiaiDoanDTO taoGiaiDoan(GiaiDoanPipeline gd, NguoiDung nguoiDung) {
        return themGiaiDoan(gd, nguoiDung);
    }

    /**
     * AC 1, AC 2, AC 4: Cập nhật cấu hình giai đoạn và xác suất thắng.
     * Đảm bảo thay đổi cấu hình không làm hỏng cơ hội đang chạy.
     */
    public KetQuaGiaiDoanDTO capNhatGiaiDoan(GiaiDoanPipeline gd, NguoiDung nguoiDung) {
        if (!coQuyenCauHinh(nguoiDung)) {
            return KetQuaGiaiDoanDTO.thatBai("Bạn không có quyền cập nhật cấu hình giai đoạn.");
        }

        if (gd == null || gd.getId() <= 0) {
            return KetQuaGiaiDoanDTO.thatBai("Giai đoạn cần cập nhật không hợp lệ.");
        }

        KetQuaGiaiDoanDTO ketQua = new KetQuaGiaiDoanDTO();
        validateDuLieuGiaiDoan(gd, gd.getId(), ketQua);
        if (!ketQua.getDanhSachLoi().isEmpty()) {
            ketQua.setThongBao("Dữ liệu giai đoạn không hợp lệ. Vui lòng kiểm tra lại.");
            return ketQua;
        }

        try {
            boolean capNhatOk = giaiDoanPipelineDAO.capNhatGiaiDoan(gd);
            if (capNhatOk) {
                return KetQuaGiaiDoanDTO.thanhCong(gd, "Cập nhật giai đoạn '" + gd.getTenGiaiDoan() + "' thành công. Dữ liệu cơ hội đang chạy vẫn được bảo toàn an toàn.");
            } else {
                return KetQuaGiaiDoanDTO.thatBai("Không tìm thấy giai đoạn để cập nhật.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi cập nhật giai đoạn pipeline ID=" + gd.getId() + ": " + e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("uk_gdp_ma")) {
                return KetQuaGiaiDoanDTO.loiTrungMa(gd.getMaGiaiDoan());
            }
            if (e.getMessage() != null && e.getMessage().contains("uk_gdp_tt")) {
                return KetQuaGiaiDoanDTO.thatBai("Thứ tự " + gd.getThuTu() + " đã tồn tại trong chuỗi pipeline.");
            }
            return KetQuaGiaiDoanDTO.thatBai("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * Đổi thứ tự 2 giai đoạn liền kề (Move Up / Move Down).
     */
    public KetQuaGiaiDoanDTO hoanDoiThuTu(int id1, int id2, NguoiDung nguoiDung) {
        if (!coQuyenCauHinh(nguoiDung)) {
            return KetQuaGiaiDoanDTO.thatBai("Bạn không có quyền sắp xếp thứ tự giai đoạn.");
        }

        GiaiDoanPipeline gd1 = giaiDoanPipelineDAO.timTheoId(id1);
        GiaiDoanPipeline gd2 = giaiDoanPipelineDAO.timTheoId(id2);
        if (gd1 == null || gd2 == null) {
            return KetQuaGiaiDoanDTO.thatBai("Không tìm thấy giai đoạn để đổi vị trí.");
        }

        try {
            int thuTu1 = gd1.getThuTu();
            int thuTu2 = gd2.getThuTu();
            boolean ok = giaiDoanPipelineDAO.hoanDoiThuTu(id1, thuTu1, id2, thuTu2);
            if (ok) {
                return KetQuaGiaiDoanDTO.thanhCong(gd1, "Đã hoán đổi thứ tự giữa '" + gd1.getTenGiaiDoan() + "' và '" + gd2.getTenGiaiDoan() + "'.");
            } else {
                return KetQuaGiaiDoanDTO.thatBai("Đổi thứ tự giai đoạn không thành công.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi hoán đổi thứ tự giai đoạn: " + e.getMessage(), e);
            return KetQuaGiaiDoanDTO.thatBai("Lỗi hệ thống: " + e.getMessage());
        }
    }

    public KetQuaGiaiDoanDTO doiThuTu(int id1, int id2, NguoiDung nguoiDung) {
        return hoanDoiThuTu(id1, id2, nguoiDung);
    }

    /**
     * AC 4: Thay đổi cấu hình không làm hỏng cơ hội đang chạy.
     * Nếu giai đoạn đã có cơ hội đang chạy, KHÔNG cho phép xóa cứng mà chỉ cho chuyển trạng thái sang NGUNG_AP_DUNG.
     */
    public KetQuaGiaiDoanDTO xoaGiaiDoan(int id, NguoiDung nguoiDung) {
        if (!coQuyenCauHinh(nguoiDung)) {
            return KetQuaGiaiDoanDTO.thatBai("Bạn không có quyền xoá giai đoạn pipeline.");
        }

        GiaiDoanPipeline gd = giaiDoanPipelineDAO.timTheoId(id);
        if (gd == null) {
            return KetQuaGiaiDoanDTO.thatBai("Không tìm thấy giai đoạn có ID=" + id);
        }

        // Ràng buộc AC 4: Kiểm tra xem có cơ hội nào đang liên kết với giai đoạn này không
        int soCoHoi = giaiDoanPipelineDAO.demSoCoHoiTrongGiaiDoan(id);
        if (soCoHoi > 0) {
            return KetQuaGiaiDoanDTO.loiDangCoCoHoi(gd.getTenGiaiDoan(), soCoHoi);
        }

        try {
            boolean xoaOk = giaiDoanPipelineDAO.xoaGiaiDoan(id);
            if (xoaOk) {
                return KetQuaGiaiDoanDTO.thanhCong(gd, "Đã xoá hoàn toàn giai đoạn '" + gd.getTenGiaiDoan() + "' khỏi pipeline.");
            } else {
                return KetQuaGiaiDoanDTO.thatBai("Xoá giai đoạn không thành công.");
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Lỗi xoá giai đoạn pipeline ID=" + id + ": " + e.getMessage(), e);
            return KetQuaGiaiDoanDTO.thatBai("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Chuyển trạng thái áp dụng giai đoạn (Đang áp dụng <-> Ngừng áp dụng).
     */
    public KetQuaGiaiDoanDTO chuyenTrangThai(int id, TrangThaiGiaiDoanEnum trangThaiMoi, NguoiDung nguoiDung) {
        if (!coQuyenCauHinh(nguoiDung)) {
            return KetQuaGiaiDoanDTO.thatBai("Bạn không có quyền thay đổi trạng thái giai đoạn.");
        }

        GiaiDoanPipeline gd = giaiDoanPipelineDAO.timTheoId(id);
        if (gd == null) {
            return KetQuaGiaiDoanDTO.thatBai("Không tìm thấy giai đoạn ID=" + id);
        }

        try {
            boolean ok = giaiDoanPipelineDAO.capNhatTrangThai(id, trangThaiMoi);
            if (ok) {
                gd.setTrangThai(trangThaiMoi);
                String msg = (trangThaiMoi == TrangThaiGiaiDoanEnum.NGUNG_AP_DUNG)
                        ? "Đã chuyển giai đoạn '" + gd.getTenGiaiDoan() + "' sang Ngừng áp dụng. Các cơ hội cũ vẫn được lưu giữ an toàn."
                        : "Đã kích hoạt lại giai đoạn '" + gd.getTenGiaiDoan() + "' trong chuỗi pipeline.";
                return KetQuaGiaiDoanDTO.thanhCong(gd, msg);
            } else {
                return KetQuaGiaiDoanDTO.thatBai("Cập nhật trạng thái giai đoạn thất bại.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi chuyển trạng thái giai đoạn ID=" + id + ": " + e.getMessage(), e);
            return KetQuaGiaiDoanDTO.thatBai("Lỗi hệ thống: " + e.getMessage());
        }
    }

    /**
     * AC 3: Khai báo điều kiện bắt buộc để rời một giai đoạn, ví dụ phải có ít nhất một cuộc gặp.
     * Kiểm tra xem các hoạt động thực tế của cơ hội đã thỏa mãn điều kiện rời giai đoạn hay chưa.
     */
    public DieuKienRoiGiaiDoanDTO kiemTraDieuKienRoiGiaiDoan(int giaiDoanId, int soCuocGapDaCo, int soCuocGoiDaCo,
                                                             boolean daCoBaoGia, boolean daKhaoSatNhuCau) {
        GiaiDoanPipeline gd = giaiDoanPipelineDAO.timTheoId(giaiDoanId);
        if (gd == null) {
            DieuKienRoiGiaiDoanDTO dto = new DieuKienRoiGiaiDoanDTO(giaiDoanId, "Không xác định");
            dto.themYeuCauThieu("Giai đoạn pipeline không tồn tại.");
            return dto;
        }

        DieuKienRoiGiaiDoanDTO dto = new DieuKienRoiGiaiDoanDTO(gd.getId(), gd.getTenGiaiDoan());
        dto.setSoCuocGapDaCo(soCuocGapDaCo);
        dto.setSoCuocGoiDaCo(soCuocGoiDaCo);
        dto.setDaCoBaoGia(daCoBaoGia);
        dto.setDaKhaoSatNhuCau(daKhaoSatNhuCau);

        // 1. Kiểm tra số cuộc gặp tối thiểu
        if (gd.getSoCuocGapToiThieu() > 0 && soCuocGapDaCo < gd.getSoCuocGapToiThieu()) {
            dto.themYeuCauThieu("Phải có ít nhất " + gd.getSoCuocGapToiThieu() + " cuộc gặp trực tiếp với khách hàng (hiện tại: " + soCuocGapDaCo + ")");
        }

        // 2. Kiểm tra số cuộc gọi tối thiểu
        if (gd.getSoCuocGoiToiThieu() > 0 && soCuocGoiDaCo < gd.getSoCuocGoiToiThieu()) {
            dto.themYeuCauThieu("Phải có ít nhất " + gd.getSoCuocGoiToiThieu() + " cuộc gọi kết nối (hiện tại: " + soCuocGoiDaCo + ")");
        }

        // 3. Kiểm tra yêu cầu báo giá
        if (gd.isYeuCauBaoGia() && !daCoBaoGia) {
            dto.themYeuCauThieu("Bắt buộc phải tạo và gửi báo giá niêm yết cho khách hàng trước khi rời giai đoạn này");
        }

        // 4. Kiểm tra yêu cầu khảo sát nhu cầu
        if (gd.isYeuCauKhaoSatNhuCau() && !daKhaoSatNhuCau) {
            dto.themYeuCauThieu("Bắt buộc phải hoàn tất xác nhận bảng khảo sát nhu cầu khách hàng");
        }

        return dto;
    }

    /**
     * AC 2: Mỗi giai đoạn có xác suất thắng mặc định dùng để tính dự báo.
     * Tính dự báo doanh số = Giá trị cơ hội * (Xác suất thắng / 100)
     */
    public BigDecimal tinhDuBaoDoanhSo(BigDecimal giaTriCoHoi, int xacSuatThang) {
        return DuBaoDoanhSoDTO.tinhDuBao(giaTriCoHoi, xacSuatThang);
    }

    /**
     * Lấy báo cáo tổng hợp dự báo pipeline theo trọng số xác suất.
     */
    public List<DuBaoDoanhSoDTO> layThongKeDuBaoPipeline() {
        return giaiDoanPipelineDAO.layThongKeDuBaoPipeline();
    }

    private void validateDuLieuGiaiDoan(GiaiDoanPipeline gd, Integer excludeId, KetQuaGiaiDoanDTO ketQua) {
        if (gd == null) {
            ketQua.themLoi("_global", "Dữ liệu giai đoạn không được để trống.");
            return;
        }

        // 1. Mã giai đoạn
        if (gd.getMaGiaiDoan() == null || gd.getMaGiaiDoan().trim().isEmpty()) {
            ketQua.themLoi("maGiaiDoan", "Mã giai đoạn không được để trống.");
        } else {
            String ma = gd.getMaGiaiDoan().trim();
            if (!MA_GIAI_DOAN_PATTERN.matcher(ma).matches()) {
                ketQua.themLoi("maGiaiDoan", "Mã giai đoạn từ 2-50 ký tự, chỉ gồm chữ cái, số, gạch ngang (-) hoặc gạch dưới (_).");
            } else if (giaiDoanPipelineDAO.kiemTraMaTonTai(ma, excludeId)) {
                ketQua.themLoi("maGiaiDoan", "Mã giai đoạn '" + ma + "' đã tồn tại trên hệ thống.");
            }
        }

        // 2. Tên giai đoạn
        if (gd.getTenGiaiDoan() == null || gd.getTenGiaiDoan().trim().isEmpty()) {
            ketQua.themLoi("tenGiaiDoan", "Tên giai đoạn không được để trống.");
        } else if (gd.getTenGiaiDoan().trim().length() > 100) {
            ketQua.themLoi("tenGiaiDoan", "Tên giai đoạn tối đa 100 ký tự.");
        }

        // 3. Thứ tự
        if (gd.getThuTu() <= 0) {
            ketQua.themLoi("thuTu", "Thứ tự giai đoạn phải lớn hơn 0.");
        }

        // 4. Xác suất thắng (%) (AC: 0 - 100%)
        if (gd.getXacSuatThang() < 0 || gd.getXacSuatThang() > 100) {
            ketQua.themLoi("xacSuatThang", "Xác suất thắng phải từ 0% đến 100%.");
        }

        // 5. Cảnh báo đình trệ
        if (gd.getSoNgayCanhBaoDinhTre() <= 0) {
            ketQua.themLoi("soNgayCanhBaoDinhTre", "Số ngày cảnh báo đình trệ phải lớn hơn 0.");
        }

        // 6. Mô tả quy tắc điều kiện bắt buộc tối đa 500 ký tự (theo schema DB)
        if (gd.getDieuKienBatBuoc() != null && gd.getDieuKienBatBuoc().trim().length() > 500) {
            ketQua.themLoi("dieuKienBatBuoc", "Mô tả điều kiện bắt buộc tối đa 500 ký tự.");
        }
    }
}
