package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.HopDongDAO;
import vn.nhom10.crm.dao.KhachHang360DAO;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dto.BaoGiaHopLeDTO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.ThongKeNhomCongTyDTO;
import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.model.HopDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
    private final KhachHang360DAO khachHang360DAO;

    public CongTyMeConService() {
        this(new KhachHangDAO(), new HopDongDAO(), new PhanQuyenDuLieuService(), new KhachHang360DAO());
    }

    public CongTyMeConService(KhachHangDAO khachHangDAO, HopDongDAO hopDongDAO, PhanQuyenDuLieuService phanQuyenService) {
        this(khachHangDAO, hopDongDAO, phanQuyenService, new KhachHang360DAO());
    }

    public CongTyMeConService(KhachHangDAO khachHangDAO, HopDongDAO hopDongDAO, PhanQuyenDuLieuService phanQuyenService, KhachHang360DAO khachHang360DAO) {
        this.khachHangDAO = khachHangDAO;
        this.hopDongDAO = hopDongDAO;
        this.phanQuyenService = phanQuyenService;
        this.khachHang360DAO = khachHang360DAO != null ? khachHang360DAO : new KhachHang360DAO();
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

        // Ghi lịch sử hoạt động vào dòng thời gian của công ty mẹ
        HoatDong hdMe = new HoatDong();
        hdMe.setMaHoatDong("HD_AUDIT_" + System.currentTimeMillis() + "_ME_" + congTyMeId);
        hdMe.setKhachHangId(congTyMeId);
        hdMe.setLoaiHoatDong("CHINH_SUA");
        hdMe.setTieuDe("Gắn công ty con");
        hdMe.setNoiDung("Gắn công ty con: '" + khCon.getTenCongTy() + "' (Mã: " + (khCon.getMaKhachHang() != null ? khCon.getMaKhachHang() : "KH" + khachHangConId) + ") trực thuộc tập đoàn.");
        hdMe.setNguoiPhuTrachId(nguoiThucHien != null && nguoiThucHien.getId() != null ? nguoiThucHien.getId() : 1L);
        hdMe.setNhomKinhDoanhId(nguoiThucHien != null && nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
        hdMe.setThoiGianBatDau(new Timestamp(System.currentTimeMillis()));
        hdMe.setTrangThai("HOAN_THANH");
        hdMe.setLaGhiNhanNhanh(false);

        // Ghi lịch sử hoạt động vào dòng thời gian của công ty con
        HoatDong hdCon = new HoatDong();
        hdCon.setMaHoatDong("HD_AUDIT_" + System.currentTimeMillis() + "_CON_" + khachHangConId);
        hdCon.setKhachHangId(khachHangConId);
        hdCon.setLoaiHoatDong("CHINH_SUA");
        hdCon.setTieuDe("Cập nhật công ty mẹ");
        hdCon.setNoiDung("Gán vào công ty mẹ: '" + khMe.getTenCongTy() + "' (Mã: " + (khMe.getMaKhachHang() != null ? khMe.getMaKhachHang() : "KH" + congTyMeId) + ").");
        hdCon.setNguoiPhuTrachId(nguoiThucHien != null && nguoiThucHien.getId() != null ? nguoiThucHien.getId() : 1L);
        hdCon.setNhomKinhDoanhId(nguoiThucHien != null && nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
        hdCon.setThoiGianBatDau(new Timestamp(System.currentTimeMillis()));
        hdCon.setTrangThai("HOAN_THANH");
        hdCon.setLaGhiNhanNhanh(false);

        try {
            khachHang360DAO.themHoatDong(hdMe);
            khachHang360DAO.themHoatDong(hdCon);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể ghi nhận nhật ký lịch sử gắn công ty con: " + e.getMessage(), e);
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

        Long oldMeId = khCon.getCongTyMeId();
        boolean thanhCong = khachHangDAO.ganCongTyMe(khachHangConId, null);
        if (!thanhCong) {
            throw new SQLException("Không thể gỡ bỏ quan hệ công ty mẹ khỏi cơ sở dữ liệu.");
        }

        // Ghi nhận nhật ký cho công ty con
        HoatDong hdCon = new HoatDong();
        hdCon.setMaHoatDong("HD_AUDIT_" + System.currentTimeMillis() + "_CON_" + khachHangConId);
        hdCon.setKhachHangId(khachHangConId);
        hdCon.setLoaiHoatDong("CHINH_SUA");
        hdCon.setTieuDe("Hủy liên kết công ty mẹ");
        hdCon.setNoiDung("Đã gỡ bỏ quan hệ công ty con và tách khỏi nhóm tập đoàn.");
        hdCon.setNguoiPhuTrachId(nguoiThucHien != null && nguoiThucHien.getId() != null ? nguoiThucHien.getId() : 1L);
        hdCon.setNhomKinhDoanhId(nguoiThucHien != null && nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
        hdCon.setThoiGianBatDau(new Timestamp(System.currentTimeMillis()));
        hdCon.setTrangThai("HOAN_THANH");
        hdCon.setLaGhiNhanNhanh(false);

        try {
            khachHang360DAO.themHoatDong(hdCon);
            if (oldMeId != null && oldMeId > 0) {
                HoatDong hdMe = new HoatDong();
                hdMe.setMaHoatDong("HD_AUDIT_" + System.currentTimeMillis() + "_ME_" + oldMeId);
                hdMe.setKhachHangId(oldMeId);
                hdMe.setLoaiHoatDong("CHINH_SUA");
                hdMe.setTieuDe("Gỡ công ty con");
                hdMe.setNoiDung("Đã gỡ bỏ công ty con '" + khCon.getTenCongTy() + "' khỏi tập đoàn.");
                hdMe.setNguoiPhuTrachId(nguoiThucHien != null && nguoiThucHien.getId() != null ? nguoiThucHien.getId() : 1L);
                hdMe.setNhomKinhDoanhId(nguoiThucHien != null && nguoiThucHien.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()) : null);
                hdMe.setThoiGianBatDau(new Timestamp(System.currentTimeMillis()));
                hdMe.setTrangThai("HOAN_THANH");
                hdMe.setLaGhiNhanNhanh(false);
                khachHang360DAO.themHoatDong(hdMe);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể ghi nhận nhật ký lịch sử gỡ công ty con: " + e.getMessage(), e);
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

    /**
     * Lấy danh sách báo giá hợp lệ có thể liên kết để tạo hợp đồng theo phân quyền Data Scope.
     */
    public List<BaoGiaHopLeDTO> layDanhSachBaoGiaHopLe(Long khachHangId, NguoiDungDTO nguoiThucHien) throws SQLException {
        if (khachHangId == null) {
            return Collections.emptyList();
        }
        List<BaoGiaHopLeDTO> ds = hopDongDAO.layDanhSachBaoGiaHopLe(khachHangId);
        if (ds == null || ds.isEmpty() || nguoiThucHien == null) {
            return ds != null ? ds : Collections.emptyList();
        }

        // Kiểm tra quyền theo Data Scope
        if (nguoiThucHien.getVaiTro() == VaiTroEnum.ADMIN
                || nguoiThucHien.getVaiTro() == VaiTroEnum.DIRECTOR
                || nguoiThucHien.getPhamViToiDa() == PhamViDuLieu.TOAN_BO) {
            return ds;
        }

        List<BaoGiaHopLeDTO> ketQua = new ArrayList<>();
        for (BaoGiaHopLeDTO bg : ds) {
            if (nguoiThucHien.getPhamViToiDa() == PhamViDuLieu.CA_NHAN) {
                if (bg.getNguoiPhuTrachId() == null || bg.getNguoiPhuTrachId().equals(nguoiThucHien.getId())) {
                    ketQua.add(bg);
                }
            } else if (nguoiThucHien.getPhamViToiDa() == PhamViDuLieu.NHOM) {
                if (bg.getNhomKinhDoanhId() == null || (nguoiThucHien.getNhomKinhDoanhId() != null
                        && bg.getNhomKinhDoanhId().equals(Long.valueOf(nguoiThucHien.getNhomKinhDoanhId())))) {
                    ketQua.add(bg);
                }
            } else {
                ketQua.add(bg);
            }
        }
        return ketQua;
    }

    /**
     * Thêm mới một hợp đồng pháp lý cho khách hàng (Task 3).
     */
    public Long themHopDong(HopDong hd, NguoiDungDTO nguoiThucHien)
            throws SQLException, LoiPhanQuyenException, IllegalArgumentException {
        if (hd == null || hd.getKhachHangId() == null) {
            throw new IllegalArgumentException("Khách hàng không được để trống.");
        }
        if (hd.getSoHopDong() == null || hd.getSoHopDong().trim().isEmpty()) {
            throw new IllegalArgumentException("Số hợp đồng không được để trống.");
        }
        if (hd.getGiaTriHopDong() == null || hd.getGiaTriHopDong().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị hợp đồng phải là số hợp lệ không âm.");
        }

        KhachHang kh = khachHangDAO.timTheoId(hd.getKhachHangId());
        if (kh == null) {
            throw new LoiKhongTimThayException("Không tìm thấy khách hàng ID: " + hd.getKhachHangId());
        }

        // Kiểm tra Data Scope trên khách hàng
        kiemTraQuyenSuaKhachHang(kh, nguoiThucHien);

        // Bắt buộc hợp đồng phải gắn với báo giá hợp lệ (uk_hd_bg)
        if (hd.getBaoGiaId() == null || hd.getBaoGiaId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn báo giá hợp lệ liên kết với hợp đồng.");
        }

        BaoGiaHopLeDTO bg = hopDongDAO.timBaoGiaTheoId(hd.getBaoGiaId());
        if (bg == null) {
            throw new IllegalArgumentException("Báo giá được chọn không tồn tại trên hệ thống.");
        }

        if (bg.getKhachHangId() != null && !bg.getKhachHangId().equals(hd.getKhachHangId())) {
            throw new IllegalArgumentException("Báo giá được chọn không thuộc về khách hàng này.");
        }

        if (bg.getTrangThai() != null) {
            String tt = bg.getTrangThai().toUpperCase().trim();
            if ("TU_CHOI".equals(tt) || "DA_HUY".equals(tt) || "HET_HAN".equals(tt)) {
                throw new IllegalArgumentException("Báo giá ở trạng thái '" + bg.getTrangThai() + "' không thể dùng để tạo hợp đồng.");
            }
        }

        // Kiểm tra ràng buộc UNIQUE: 1 báo giá chỉ gắn tối đa 1 hợp đồng
        if (hopDongDAO.kiemTraBaoGiaDaSuDung(hd.getBaoGiaId())) {
            throw new IllegalArgumentException("Báo giá này đã được sử dụng cho một hợp đồng khác. Mỗi báo giá chỉ được tạo tối đa 1 hợp đồng.");
        }

        // Kiểm tra Data Scope trên báo giá
        if (nguoiThucHien != null
                && nguoiThucHien.getVaiTro() != VaiTroEnum.ADMIN
                && nguoiThucHien.getVaiTro() != VaiTroEnum.DIRECTOR
                && nguoiThucHien.getPhamViToiDa() != PhamViDuLieu.TOAN_BO) {
            if (nguoiThucHien.getPhamViToiDa() == PhamViDuLieu.CA_NHAN) {
                if (bg.getNguoiPhuTrachId() != null && !bg.getNguoiPhuTrachId().equals(nguoiThucHien.getId())) {
                    throw new LoiPhanQuyenException("Bạn chỉ có quyền tạo hợp đồng từ báo giá do chính mình phụ trách.");
                }
            } else if (nguoiThucHien.getPhamViToiDa() == PhamViDuLieu.NHOM) {
                if (bg.getNhomKinhDoanhId() != null && nguoiThucHien.getNhomKinhDoanhId() != null
                        && !bg.getNhomKinhDoanhId().equals(Long.valueOf(nguoiThucHien.getNhomKinhDoanhId()))) {
                    throw new LoiPhanQuyenException("Bạn chỉ có quyền tạo hợp đồng từ báo giá thuộc nhóm kinh doanh của mình.");
                }
            }
        }

        // Điền phiên bản báo giá và cơ hội nếu chưa có
        if (hd.getPhienBanBaoGiaId() == null || hd.getPhienBanBaoGiaId() <= 0) {
            hd.setPhienBanBaoGiaId(bg.getPhienBanId() != null ? bg.getPhienBanId() : 1L);
        }
        if (hd.getCoHoiId() == null || hd.getCoHoiId() <= 0) {
            hd.setCoHoiId(bg.getCoHoiId() != null ? bg.getCoHoiId() : 1L);
        }

        return hopDongDAO.themMoi(hd);
    }
}
