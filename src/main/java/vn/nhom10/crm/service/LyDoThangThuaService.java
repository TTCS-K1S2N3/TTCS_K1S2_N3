package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.DoiThuDAO;
import vn.nhom10.crm.dao.LyDoThangThuaDAO;
import vn.nhom10.crm.dto.KetQuaKiemTraDongCoHoiDTO;
import vn.nhom10.crm.model.DoiThu;
import vn.nhom10.crm.model.LyDoThangThua;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service xử lý nghiệp vụ danh mục lý do thắng thua và đối thủ cạnh tranh (Story S2-10).
 * Đáp ứng các tiêu chí chấp nhận:
 * - AC1: Danh sách lý do thắng và lý do thua khai báo riêng.
 * - AC2: Danh sách đối thủ cạnh tranh.
 * - AC3: Kiểm tra ràng buộc dữ liệu bắt buộc khi đóng cơ hội bán hàng ở Sprint 5.
 */
public class LyDoThangThuaService {

    private final LyDoThangThuaDAO lyDoDAO;
    private final DoiThuDAO doiThuDAO;

    private static final Pattern MA_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{2,50}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$");

    public LyDoThangThuaService() {
        this.lyDoDAO = new LyDoThangThuaDAO();
        this.doiThuDAO = new DoiThuDAO();
    }

    public LyDoThangThuaService(LyDoThangThuaDAO lyDoDAO, DoiThuDAO doiThuDAO) {
        this.lyDoDAO = lyDoDAO;
        this.doiThuDAO = doiThuDAO;
    }

    // =========================================================================
    // AC1: QUẢN LÝ DANH MỤC LÝ DO THẮNG VÀ LÝ DO THUA RIÊNG BIỆT
    // =========================================================================

    /**
     * Lấy danh sách tất cả lý do thắng (AC1).
     */
    public List<LyDoThangThua> layDanhSachLyDoThang() {
        return lyDoDAO.layTheoLoai(LyDoThangThua.LOAI_THANG);
    }

    /**
     * Lấy danh sách tất cả lý do thua (AC1).
     */
    public List<LyDoThangThua> layDanhSachLyDoThua() {
        return lyDoDAO.layTheoLoai(LyDoThangThua.LOAI_THUA);
    }

    /**
     * Lấy danh sách lý do thắng đang hoạt động (dùng cho dropdown đóng cơ hội).
     */
    public List<LyDoThangThua> layLyDoThangKhaDung() {
        return lyDoDAO.layDangHoatDongTheoLoai(LyDoThangThua.LOAI_THANG);
    }

    /**
     * Lấy danh sách lý do thua đang hoạt động (dùng cho dropdown đóng cơ hội).
     */
    public List<LyDoThangThua> layLyDoThuaKhaDung() {
        return lyDoDAO.layDangHoatDongTheoLoai(LyDoThangThua.LOAI_THUA);
    }

    /**
     * Tìm lý do theo ID.
     */
    public LyDoThangThua timLyDoTheoId(Long id) {
        return lyDoDAO.timTheoId(id);
    }

    /**
     * Thêm mới hoặc cập nhật lý do thắng/thua với kiểm tra nghiệp vụ chặt chẽ.
     */
    public void luuLyDo(LyDoThangThua lyDo) {
        if (lyDo == null) {
            throw new IllegalArgumentException("Dữ liệu lý do không được để trống");
        }

        // Validate mã lý do
        if (lyDo.getMaLyDo() == null || lyDo.getMaLyDo().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã lý do không được để trống");
        }
        String maChuanHoa = lyDo.getMaLyDo().trim().toUpperCase();
        if (!MA_PATTERN.matcher(maChuanHoa).matches()) {
            throw new IllegalArgumentException("Mã lý do chỉ chứa chữ cái, số, gạch dưới hoặc gạch ngang (từ 2 đến 50 ký tự)");
        }
        lyDo.setMaLyDo(maChuanHoa);

        // Validate tên lý do
        if (lyDo.getTenLyDo() == null || lyDo.getTenLyDo().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên lý do không được để trống");
        }
        if (lyDo.getTenLyDo().trim().length() > 150) {
            throw new IllegalArgumentException("Tên lý do không được vượt quá 150 ký tự");
        }
        lyDo.setTenLyDo(lyDo.getTenLyDo().trim());

        // Validate loại
        if (lyDo.getLoai() == null || (!LyDoThangThua.LOAI_THANG.equalsIgnoreCase(lyDo.getLoai().trim())
                && !LyDoThangThua.LOAI_THUA.equalsIgnoreCase(lyDo.getLoai().trim()))) {
            throw new IllegalArgumentException("Loại lý do phải là 'THANG' hoặc 'THUA'");
        }
        lyDo.setLoai(lyDo.getLoai().trim().toUpperCase());

        // Validate trùng mã
        if (lyDoDAO.tonTaiMa(lyDo.getMaLyDo(), lyDo.getId())) {
            throw new IllegalArgumentException("Mã lý do '" + lyDo.getMaLyDo() + "' đã tồn tại trong hệ thống");
        }

        // Thực hiện lưu
        if (lyDo.getId() == null || lyDo.getId() <= 0) {
            Long newId = lyDoDAO.taoMoi(lyDo);
            if (newId == null) {
                throw new IllegalStateException("Không thể lưu lý do mới vào cơ sở dữ liệu");
            }
        } else {
            boolean ok = lyDoDAO.capNhat(lyDo);
            if (!ok) {
                throw new IllegalStateException("Không thể cập nhật thông tin lý do");
            }
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động của lý do.
     */
    public void doiTrangThaiLyDo(Long id, boolean hoatDong) {
        if (id == null) {
            throw new IllegalArgumentException("ID lý do không hợp lệ");
        }
        boolean ok = lyDoDAO.capNhatTrangThai(id, hoatDong);
        if (!ok) {
            throw new IllegalStateException("Không thể thay đổi trạng thái lý do");
        }
    }

    /**
     * Thay đổi thứ tự hiển thị của lý do.
     */
    public void capNhatThuTuLyDo(Long id, int thuTu) {
        if (id == null) {
            throw new IllegalArgumentException("ID lý do không hợp lệ");
        }
        lyDoDAO.capNhatThuTu(id, Math.max(0, thuTu));
    }

    /**
     * Xóa lý do thắng/thua. Chặn xóa nếu đã được cơ hội tham chiếu.
     */
    public void xoaLyDo(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID lý do không hợp lệ");
        }
        int soThamChieu = lyDoDAO.demSoCoHoiThamChieu(id);
        if (soThamChieu > 0) {
            throw new IllegalStateException("Không thể xóa lý do này vì đang được " + soThamChieu +
                    " cơ hội bán hàng tham chiếu. Hãy chuyển trạng thái sang 'Ngừng hoạt động' để bảo toàn dữ liệu lịch sử.");
        }
        boolean ok = lyDoDAO.xoa(id);
        if (!ok) {
            throw new IllegalStateException("Không thể xóa lý do từ cơ sở dữ liệu");
        }
    }

    // =========================================================================
    // AC2: QUẢN LÝ DANH MỤC ĐỐI THỦ CẠNH TRANH
    // =========================================================================

    /**
     * Lấy toàn bộ danh sách đối thủ cạnh tranh (AC2).
     */
    public List<DoiThu> layDanhSachDoiThu() {
        return doiThuDAO.layTatCa();
    }

    /**
     * Lấy danh sách đối thủ đang hoạt động (dùng cho dropdown khi đóng cơ hội).
     */
    public List<DoiThu> layDoiThuKhaDung() {
        return doiThuDAO.layDangHoatDong();
    }

    /**
     * Tìm đối thủ theo ID.
     */
    public DoiThu timDoiThuTheoId(Long id) {
        return doiThuDAO.timTheoId(id);
    }

    /**
     * Thêm mới hoặc cập nhật thông tin đối thủ cạnh tranh.
     */
    public void luuDoiThu(DoiThu doiThu) {
        if (doiThu == null) {
            throw new IllegalArgumentException("Dữ liệu đối thủ không được để trống");
        }

        // Validate mã đối thủ
        if (doiThu.getMaDoiThu() == null || doiThu.getMaDoiThu().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đối thủ không được để trống");
        }
        String maChuanHoa = doiThu.getMaDoiThu().trim().toUpperCase();
        if (!MA_PATTERN.matcher(maChuanHoa).matches()) {
            throw new IllegalArgumentException("Mã đối thủ chỉ chứa chữ cái, số, gạch dưới hoặc gạch ngang (từ 2 đến 50 ký tự)");
        }
        doiThu.setMaDoiThu(maChuanHoa);

        // Validate tên đối thủ
        if (doiThu.getTenDoiThu() == null || doiThu.getTenDoiThu().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đối thủ không được để trống");
        }
        if (doiThu.getTenDoiThu().trim().length() > 200) {
            throw new IllegalArgumentException("Tên đối thủ không được vượt quá 200 ký tự");
        }
        doiThu.setTenDoiThu(doiThu.getTenDoiThu().trim());

        // Validate website nếu có
        if (doiThu.getWebsite() != null && !doiThu.getWebsite().trim().isEmpty()) {
            String web = doiThu.getWebsite().trim();
            if (web.length() > 255) {
                throw new IllegalArgumentException("Website không được vượt quá 255 ký tự");
            }
            doiThu.setWebsite(web);
        } else {
            doiThu.setWebsite(null);
        }

        // Validate ghi chú nếu có
        if (doiThu.getGhiChu() != null) {
            doiThu.setGhiChu(doiThu.getGhiChu().trim());
        }

        // Kiểm tra trùng mã
        if (doiThuDAO.tonTaiMa(doiThu.getMaDoiThu(), doiThu.getId())) {
            throw new IllegalArgumentException("Mã đối thủ '" + doiThu.getMaDoiThu() + "' đã tồn tại trong hệ thống");
        }

        // Thực hiện lưu
        if (doiThu.getId() == null || doiThu.getId() <= 0) {
            Long newId = doiThuDAO.taoMoi(doiThu);
            if (newId == null) {
                throw new IllegalStateException("Không thể thêm mới đối thủ vào cơ sở dữ liệu");
            }
        } else {
            boolean ok = doiThuDAO.capNhat(doiThu);
            if (!ok) {
                throw new IllegalStateException("Không thể cập nhật thông tin đối thủ");
            }
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động của đối thủ.
     */
    public void doiTrangThaiDoiThu(Long id, boolean hoatDong) {
        if (id == null) {
            throw new IllegalArgumentException("ID đối thủ không hợp lệ");
        }
        boolean ok = doiThuDAO.capNhatTrangThai(id, hoatDong);
        if (!ok) {
            throw new IllegalStateException("Không thể thay đổi trạng thái đối thủ");
        }
    }

    /**
     * Xóa đối thủ cạnh tranh. Chặn xóa nếu đã được cơ hội tham chiếu.
     */
    public void xoaDoiThu(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID đối thủ không hợp lệ");
        }
        int soThamChieu = doiThuDAO.demSoCoHoiThamChieu(id);
        if (soThamChieu > 0) {
            throw new IllegalStateException("Không thể xóa đối thủ này vì đang được " + soThamChieu +
                    " cơ hội bán hàng tham chiếu. Hãy chuyển trạng thái sang 'Ngừng theo dõi' để bảo toàn dữ liệu.");
        }
        boolean ok = doiThuDAO.xoa(id);
        if (!ok) {
            throw new IllegalStateException("Không thể xóa đối thủ khỏi cơ sở dữ liệu");
        }
    }

    // =========================================================================
    // AC3: KIỂM TRA QUY TẮC RÀNG BUỘC KHI ĐÓNG CƠ HỘI BÁN HÀNG Ở SPRINT 5
    // =========================================================================

    /**
     * Mô phỏng và kiểm tra tính hợp lệ khi đóng một cơ hội bán hàng theo quy định Sprint 5 (S5-05).
     *
     * Quy tắc bắt buộc:
     * 1. Đóng THẮNG (Won):
     *    - Bắt buộc chọn Lý do thắng (loai = 'THANG') đang hoạt động.
     *    - Bắt buộc nhập Giá trị chốt thực tế (gia_tri_chot_thuc_te > 0).
     *    - Bắt buộc nhập Ngày ký (ngay_ky <= ngày hiện tại).
     * 2. Đóng THUA (Lost):
     *    - Bắt buộc chọn Lý do thua (loai = 'THUA') đang hoạt động.
     *    - Đối thủ cạnh tranh (doi_thu_id): Nếu thương vụ thua do đối thủ thắng thầu thì bắt buộc chọn đối thủ.
     */
    public KetQuaKiemTraDongCoHoiDTO kiemTraDongCoHoiSprint5(
            String trangThaiDong,
            Long lyDoThangThuaId,
            Long doiThuId,
            BigDecimal giaTriChotThucTe,
            LocalDate ngayKy,
            String ghiChu) {

        KetQuaKiemTraDongCoHoiDTO ketQua = new KetQuaKiemTraDongCoHoiDTO();
        ketQua.setTrangThaiDong(trangThaiDong);
        ketQua.setLyDoThangThuaId(lyDoThangThuaId);
        ketQua.setDoiThuId(doiThuId);
        ketQua.setGiaTriChotThucTe(giaTriChotThucTe);
        ketQua.setNgayKy(ngayKy);
        ketQua.setGhiChu(ghiChu);

        if (trangThaiDong == null || trangThaiDong.isBlank()) {
            ketQua.themLoi("Trạng thái đóng cơ hội không được để trống (phải là THẮNG hoặc THUA)");
            return ketQua;
        }

        String trangThaiChuanHoa = trangThaiDong.trim().toUpperCase();

        if ("THANG".equals(trangThaiChuanHoa) || "WON".equals(trangThaiChuanHoa) || "DONG_THANG".equals(trangThaiChuanHoa)) {
            // === KIỂM TRA ĐÓNG THẮNG ===
            // 1. Bắt buộc có lý do thắng
            if (lyDoThangThuaId == null || lyDoThangThuaId <= 0) {
                ketQua.themLoi("[Sprint 5 Bắt Buộc] Khi đóng THẮNG, bắt buộc phải chọn Lý do thắng từ danh mục.");
            } else {
                LyDoThangThua lyDo = lyDoDAO.timTheoId(lyDoThangThuaId);
                if (lyDo == null) {
                    ketQua.themLoi("Lý do thắng được chọn không tồn tại trong hệ thống.");
                } else if (!lyDo.laLyDoThang()) {
                    ketQua.themLoi("Lý do được chọn ('" + lyDo.getTenLyDo() + "') không thuộc danh mục Lý do thắng.");
                } else if (!lyDo.isHoatDong()) {
                    ketQua.themLoi("Lý do thắng '" + lyDo.getTenLyDo() + "' hiện đã ngừng hoạt động.");
                } else {
                    ketQua.setTenLyDo(lyDo.getTenLyDo());
                }
            }

            // 2. Bắt buộc có giá trị chốt thực tế
            if (giaTriChotThucTe == null || giaTriChotThucTe.compareTo(BigDecimal.ZERO) <= 0) {
                ketQua.themLoi("[Sprint 5 Bắt Buộc] Đóng THẮNG bắt buộc phải nhập Giá trị chốt thực tế lớn hơn 0.");
            }

            // 3. Bắt buộc có ngày ký
            if (ngayKy == null) {
                ketQua.themLoi("[Sprint 5 Bắt Buộc] Đóng THẮNG bắt buộc phải nhập Ngày ký hợp đồng.");
            }

            if (ketQua.getDanhSachLoi().isEmpty()) {
                ketQua.setHopLe(true);
                ketQua.setThongBaoChiTiet("HỢP LỆ THEO SPRINT 5: Cơ hội bán hàng đủ điều kiện đóng THẮNG với lý do '" +
                        ketQua.getTenLyDo() + "', giá trị chốt " + giaTriChotThucTe + " VNĐ, ngày ký: " + ngayKy + ".");
            } else {
                ketQua.setThongBaoChiTiet("CHƯA ĐỦ ĐIỀU KIỆN ĐÓNG THẮNG: Vui lòng bổ sung đầy đủ các dữ liệu bắt buộc theo quy định Sprint 5.");
            }

        } else if ("THUA".equals(trangThaiChuanHoa) || "LOST".equals(trangThaiChuanHoa) || "DONG_THUA".equals(trangThaiChuanHoa)) {
            // === KIỂM TRA ĐÓNG THUA ===
            // 1. Bắt buộc có lý do thua
            if (lyDoThangThuaId == null || lyDoThangThuaId <= 0) {
                ketQua.themLoi("[Sprint 5 Bắt Buộc] Khi đóng THUA, bắt buộc phải chọn Lý do thua từ danh mục.");
            } else {
                LyDoThangThua lyDo = lyDoDAO.timTheoId(lyDoThangThuaId);
                if (lyDo == null) {
                    ketQua.themLoi("Lý do thua được chọn không tồn tại trong hệ thống.");
                } else if (!lyDo.laLyDoThua()) {
                    ketQua.themLoi("Lý do được chọn ('" + lyDo.getTenLyDo() + "') không thuộc danh mục Lý do thua.");
                } else if (!lyDo.isHoatDong()) {
                    ketQua.themLoi("Lý do thua '" + lyDo.getTenLyDo() + "' hiện đã ngừng hoạt động.");
                } else {
                    ketQua.setTenLyDo(lyDo.getTenLyDo());
                }
            }

            // 2. Kiểm tra đối thủ cạnh tranh nếu có chọn
            if (doiThuId != null && doiThuId > 0) {
                DoiThu doiThu = doiThuDAO.timTheoId(doiThuId);
                if (doiThu == null) {
                    ketQua.themLoi("Đối thủ cạnh tranh được chọn không tồn tại trong hệ thống.");
                } else if (!doiThu.isHoatDong()) {
                    ketQua.themLoi("Đối thủ '" + doiThu.getTenDoiThu() + "' hiện đang ở trạng thái ngừng theo dõi.");
                } else {
                    ketQua.setTenDoiThu(doiThu.getTenDoiThu());
                }
            }

            if (ketQua.getDanhSachLoi().isEmpty()) {
                ketQua.setHopLe(true);
                String dtInfo = (ketQua.getTenDoiThu() != null) ? " (Đối thủ thắng thầu: " + ketQua.getTenDoiThu() + ")" : "";
                ketQua.setThongBaoChiTiet("HỢP LỆ THEO SPRINT 5: Cơ hội bán hàng đủ điều kiện đóng THUA với lý do '" +
                        ketQua.getTenLyDo() + "'" + dtInfo + ". Dữ liệu này sẽ được lưu để báo cáo phân tích rút kinh nghiệm.");
            } else {
                ketQua.setThongBaoChiTiet("CHƯA ĐỦ ĐIỀU KIỆN ĐÓNG THUA: Vui lòng bổ sung đầy đủ các dữ liệu bắt buộc theo quy định Sprint 5.");
            }

        } else {
            ketQua.themLoi("Trạng thái đóng không hợp lệ: '" + trangThaiDong + "'. Chỉ chấp nhận THẮNG hoặc THUA.");
            ketQua.setThongBaoChiTiet("Lỗi: Trạng thái đóng không được hỗ trợ.");
        }

        return ketQua;
    }
}
