package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.HopDongDAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý business logic cho quan hệ Công ty mẹ - Công ty con và tính toán giá trị hợp đồng tập đoàn.
 * Đáp ứng đầy đủ Acceptance Criteria của Story S3-05:
 * 1. Gắn một khách hàng làm công ty con của khách hàng khác (AC1)
 * 2. Trang công ty mẹ hiển thị tổng giá trị hợp đồng của cả nhóm công ty (AC2)
 */
public class CongTyMeConService {

    private static final Logger LOGGER = Logger.getLogger(CongTyMeConService.class.getName());

    private final KhachHangDAO khachHangDAO;
    private final HopDongDAO hopDongDAO;
    private final PhanQuyenDuLieuService phanQuyenService;

    public CongTyMeConService() {
        this.khachHangDAO = new KhachHangDAO();
        this.hopDongDAO = new HopDongDAO();
        this.phanQuyenService = new PhanQuyenDuLieuService();
    }

    public CongTyMeConService(KhachHangDAO khachHangDAO, HopDongDAO hopDongDAO, PhanQuyenDuLieuService phanQuyenService) {
        this.khachHangDAO = khachHangDAO;
        this.hopDongDAO = hopDongDAO;
        this.phanQuyenService = phanQuyenService;
    }

    /**
     * Gắn một khách hàng làm công ty con của khách hàng khác (Story S3-05, AC1).
     *
     * @param khachHangConId ID của khách hàng sẽ trở thành công ty con
     * @param congTyMeId     ID của công ty mẹ
     * @param nguoiThucHien  Thông tin người dùng đang thực hiện thao tác
     * @throws IllegalArgumentException khi tham số không hợp lệ hoặc gây vòng lặp
     * @throws LoiKhongTimThayException khi khách hàng không tồn tại
     * @throws LoiPhanQuyenException    khi người dùng không có quyền sửa khách hàng
     * @throws SQLException             khi có lỗi database
     */
    public void ganCongTyCon(Long khachHangConId, Long congTyMeId, NguoiDungDTO nguoiThucHien)
            throws SQLException, IllegalArgumentException, LoiKhongTimThayException, LoiPhanQuyenException {

        if (khachHangConId == null) {
            throw new IllegalArgumentException("Vui lòng chọn khách hàng cần gán làm công ty con.");
        }
        if (congTyMeId == null) {
            throw new IllegalArgumentException("Vui lòng chọn công ty mẹ.");
        }
        if (khachHangConId.equals(congTyMeId)) {
            throw new IllegalArgumentException("Một khách hàng không thể tự làm công ty mẹ hoặc công ty con của chính mình.");
        }

        KhachHang khCon = khachHangDAO.timTheoId(khachHangConId);
        if (khCon == null) {
            throw new LoiKhongTimThayException("Không tìm thấy khách hàng con với mã ID: " + khachHangConId);
        }

        KhachHang khMe = khachHangDAO.timTheoId(congTyMeId);
        if (khMe == null) {
            throw new LoiKhongTimThayException("Không tìm thấy công ty mẹ với mã ID: " + congTyMeId);
        }

        // Chống vòng lặp (Circular Dependency)
        if (khachHangDAO.kiemTraVongLapCongTyMeCon(khachHangConId, congTyMeId)) {
            throw new IllegalArgumentException(
                    "Không thể gán quan hệ mẹ - con vì sẽ tạo ra vòng lặp chu kỳ giữa '"
                            + khCon.getTenCongTy() + "' và '" + khMe.getTenCongTy() + "'."
            );
        }

        // Kiểm tra phân quyền sửa dữ liệu khách hàng theo Data Scope
        kiemTraQuyenSuaKhachHang(khCon, nguoiThucHien);

        boolean thanhCong = khachHangDAO.ganCongTyMe(khachHangConId, congTyMeId);
        if (!thanhCong) {
            throw new SQLException("Không thể cập nhật quan hệ công ty mẹ - con vào cơ sở dữ liệu.");
        }

        LOGGER.info(String.format("Người dùng '%s' (ID: %d) đã gán khách hàng '%s' (ID: %d) làm công ty con của '%s' (ID: %d)",
                nguoiThucHien != null ? nguoiThucHien.getHoTen() : "Hệ thống",
                nguoiThucHien != null ? nguoiThucHien.getId() : 0,
                khCon.getTenCongTy(), khachHangConId,
                khMe.getTenCongTy(), congTyMeId));
    }

    /**
     * Gỡ bỏ quan hệ công ty con (đưa cong_ty_me_id về NULL).
     */
    public void goCongTyCon(Long khachHangConId, NguoiDungDTO nguoiThucHien)
            throws SQLException, IllegalArgumentException, LoiKhongTimThayException, LoiPhanQuyenException {

        if (khachHangConId == null) {
            throw new IllegalArgumentException("Vui lòng chỉ định khách hàng cần gỡ bỏ quan hệ công ty mẹ.");
        }

        KhachHang khCon = khachHangDAO.timTheoId(khachHangConId);
        if (khCon == null) {
            throw new LoiKhongTimThayException("Không tìm thấy khách hàng với mã ID: " + khachHangConId);
        }

        kiemTraQuyenSuaKhachHang(khCon, nguoiThucHien);

        boolean thanhCong = khachHangDAO.ganCongTyMe(khachHangConId, null);
        if (!thanhCong) {
            throw new SQLException("Không thể gỡ bỏ quan hệ công ty mẹ khỏi cơ sở dữ liệu.");
        }

        LOGGER.info(String.format("Người dùng '%s' đã gỡ bỏ quan hệ công ty mẹ cho khách hàng '%s' (ID: %d)",
                nguoiThucHien != null ? nguoiThucHien.getHoTen() : "Hệ thống",
                khCon.getTenCongTy(), khachHangConId));
    }

    /**
     * Lấy thông tin thống kê nhóm công ty và tổng giá trị hợp đồng của cả tập đoàn (Story S3-05, AC2).
     *
     * @param congTyId ID của khách hàng (công ty mẹ)
     * @return ThongKeNhomCongTyDTO chứa chi tiết công ty mẹ, các công ty con và các tổng giá trị hợp đồng
     * @throws SQLException khi có lỗi truy vấn database
     */
    public ThongKeNhomCongTyDTO layThongKeNhomCongTy(Long congTyId) throws SQLException {
        if (congTyId == null) {
            return null;
        }

        KhachHang congTyMe = khachHangDAO.timTheoId(congTyId);
        if (congTyMe == null) {
            return null;
        }

        ThongKeNhomCongTyDTO dto = new ThongKeNhomCongTyDTO(congTyMe);

        // 1. Tính tổng giá trị hợp đồng của riêng công ty mẹ
        BigDecimal tongHopDongMe = hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(congTyId);
        congTyMe.setTongGiaTriHopDong(tongHopDongMe);
        dto.setTongGiaTriHopDongCongTyMe(tongHopDongMe);

        // 2. Lấy danh sách toàn bộ các công ty con
        List<KhachHang> dsCongTyCon = khachHangDAO.layDanhSachCongTyCon(congTyId);
        List<Long> dsConIds = new ArrayList<>();
        BigDecimal tongHopDongCacCon = BigDecimal.ZERO;

        for (KhachHang con : dsCongTyCon) {
            dsConIds.add(con.getId());
            // Tính tổng giá trị hợp đồng của từng công ty con
            BigDecimal giaTriHopDongCon = hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(con.getId());
            con.setTongGiaTriHopDong(giaTriHopDongCon);
            tongHopDongCacCon = tongHopDongCacCon.add(giaTriHopDongCon);
        }

        dto.setDanhSachCongTyCon(dsCongTyCon);
        dto.setTongGiaTriHopDongCacCongTyCon(tongHopDongCacCon);

        // 3. Tổng giá trị hợp đồng của cả nhóm công ty (Tập đoàn = Mẹ + Các con)
        BigDecimal tongGiaTriNhomCongTy = tongHopDongMe.add(tongHopDongCacCon);
        dto.setTongGiaTriHopDongNhomCongTy(tongGiaTriNhomCongTy);

        // 4. Lấy danh sách tất cả hợp đồng của cả nhóm công ty
        List<HopDong> dsHopDongNhom = hopDongDAO.layDanhSachHopDongNhomCongTy(congTyId, dsConIds);
        dto.setDanhSachHopDongNhom(dsHopDongNhom);

        return dto;
    }

    /**
     * Lấy danh sách các khách hàng khả dụng có thể gắn làm công ty con cho một công ty mẹ.
     */
    public List<KhachHang> layDanhSachKhachHangKhaDungLamCongTyCon(Long congTyMeId) throws SQLException {
        return khachHangDAO.layDanhSachKhachHangKhaDungLamCongTyCon(congTyMeId);
    }

    /**
     * Lấy danh sách các khách hàng khả dụng có thể chọn làm công ty mẹ cho một khách hàng.
     */
    public List<KhachHang> layDanhSachKhachHangKhaDungLamCongTyMe(Long khachHangId) throws SQLException {
        return khachHangDAO.layDanhSachKhachHangKhaDungLamCongTyMe(khachHangId);
    }

    /**
     * Kiểm tra quyền sửa khách hàng theo chuẩn Data Scope hệ thống.
     */
    private void kiemTraQuyenSuaKhachHang(KhachHang khachHang, NguoiDungDTO nguoiThucHien) throws LoiPhanQuyenException {
        if (nguoiThucHien == null) {
            throw new LoiPhanQuyenException("Yêu cầu đăng nhập để thực hiện thao tác này.");
        }

        // Quản trị viên (ADMIN), Giám đốc (DIRECTOR) hoặc có phạm vi TOAN_BO thì có toàn quyền
        if (nguoiThucHien.getVaiTro() == vn.nhom10.crm.model.VaiTroEnum.ADMIN 
                || nguoiThucHien.getVaiTro() == vn.nhom10.crm.model.VaiTroEnum.DIRECTOR
                || nguoiThucHien.getPhamViToiDa() == PhamViDuLieu.TOAN_BO) {
            return;
        }

        // Tạo BanGhiNghiepVuDTO tạm thời để dùng chung cơ chế kiểm tra quyền của PhanQuyenDuLieuService
        BanGhiNghiepVuDTO banGhi = new BanGhiNghiepVuDTO(
                khachHang.getId(),
                khachHang.getMaKhachHang(),
                khachHang.getTenCongTy(),
                BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                khachHang.getNguoiSoHuuId(),
                khachHang.getTenNguoiSoHuu(),
                khachHang.getNhomKinhDoanhId(),
                khachHang.getTenNhomKinhDoanh(),
                khachHang.getDoanhThuUocTinh() != null ? khachHang.getDoanhThuUocTinh().toString() : "0",
                khachHang.getTrangThai(),
                khachHang.getNgayTao(),
                khachHang.getMoTaChiTiet()
        );

        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenSua(nguoiThucHien, banGhi);
        if (!ketQua.isCoQuyen()) {
            throw new LoiPhanQuyenException(
                    "Bạn không có quyền chỉnh sửa quan hệ khách hàng '" + khachHang.getTenCongTy() + "'. " + ketQua.getThongBao()
            );
        }
    }
}
