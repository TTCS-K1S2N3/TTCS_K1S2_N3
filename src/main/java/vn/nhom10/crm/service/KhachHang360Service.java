package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.KhachHang360DAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.KhachHang360DTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.util.LoiKhongTimThayException;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service nghiệp vụ xử lý dữ liệu Trang 360 Khách hàng (Story S3-03).
 * Đáp ứng các tiêu chí chấp nhận:
 * - AC1: Gom thông tin công ty, danh sách người liên hệ, cơ hội đang mở và đã đóng,
 *        dòng thời gian hoạt động, tệp đính kèm.
 * - AC2: Tổng hợp và hiển thị tổng giá trị đã ký và giá trị cơ hội đang mở.
 * - AC3: Tối ưu tải và xử lý 500 hoạt động dưới 1.5 giây.
 * - Kiểm soát chặt chẽ phân quyền Data Scope (Cá nhân, Nhóm, Toàn bộ) ở tầng server-side.
 */
public class KhachHang360Service {

    private static final Logger LOGGER = Logger.getLogger(KhachHang360Service.class.getName());

    private final KhachHang360DAO dao;
    private final PhanQuyenDuLieuService phanQuyenService;

    public KhachHang360Service() {
        this(new KhachHang360DAO(), new PhanQuyenDuLieuService());
    }

    public KhachHang360Service(KhachHang360DAO dao) {
        this(dao, new PhanQuyenDuLieuService());
    }

    public KhachHang360Service(KhachHang360DAO dao, PhanQuyenDuLieuService phanQuyenService) {
        this.dao = dao != null ? dao : new KhachHang360DAO();
        this.phanQuyenService = phanQuyenService != null ? phanQuyenService : new PhanQuyenDuLieuService();
    }

    /**
     * Lấy toàn bộ dữ liệu bức tranh 360 độ của một khách hàng với kiểm tra phân quyền server-side.
     *
     * @param khachHangId ID khách hàng cần xem
     * @param user Thông tin người dùng đăng nhập
     * @return KhachHang360DTO gom đủ 5 nhóm dữ liệu và chỉ số tài chính
     * @throws LoiKhongTimThayException nếu ID không tồn tại
     * @throws LoiPhanQuyenException nếu người dùng không có quyền truy cập theo Data Scope
     */
    public KhachHang360DTO layThongTin360(Long khachHangId, NguoiDungDTO user) {
        if (khachHangId == null || khachHangId <= 0) {
            throw new LoiKhongTimThayException("ID khách hàng không hợp lệ: " + khachHangId);
        }
        if (user == null) {
            throw new LoiPhanQuyenException("Vui lòng đăng nhập để truy cập trang 360 khách hàng.");
        }

        // 1. Kiểm tra quyền truy cập theo Data Scope (AC S1-05 & AC S3-03)
        BanGhiNghiepVuDTO banGhi = phanQuyenService.timBanGhiTheoId(khachHangId, "KHACH_HANG");

        if (banGhi == null) {
            // Thử kiểm tra trực tiếp từ database qua DAO
            try {
                KhachHang khDb = dao.timKhachHangTheoId(khachHangId);
                if (khDb == null) {
                    throw new LoiKhongTimThayException("Không tìm thấy khách hàng ID: " + khachHangId);
                }
                banGhi = new BanGhiNghiepVuDTO(
                        khDb.getId(),
                        khDb.getMaKhachHang(),
                        khDb.getTenCongTy(),
                        BanGhiNghiepVuDTO.LoaiNghiepVu.KHACH_HANG,
                        khDb.getNguoiSoHuuId(),
                        khDb.getTenNguoiSoHuu(),
                        khDb.getNhomKinhDoanhId(),
                        khDb.getTenNhomKinhDoanh(),
                        khDb.getDoanhThuUocTinh() != null ? khDb.getDoanhThuUocTinh().toPlainString() : "0",
                        khDb.getTrangThai(),
                        khDb.getNgayTao() != null ? khDb.getNgayTao() : LocalDate.now(),
                        khDb.getMoTaChiTiet()
                );
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi truy vấn khách hàng từ DB: " + e.getMessage(), e);
                throw new RuntimeException("Lỗi hệ thống khi tải thông tin khách hàng.", e);
            }
        }

        PhanQuyenDuLieuService.KetQuaKiemTra ketQua = phanQuyenService.kiemTraQuyenTruyCap(user, banGhi);
        if (!ketQua.isCoQuyen()) {
            throw new LoiPhanQuyenException("Từ chối truy cập: Bản ghi khách hàng nằm ngoài phạm vi phân quyền của bạn.");
        }

        // 2. Tải toàn bộ dữ liệu 360 độ từ Database (AC1, AC2, AC3)
        try {
            KhachHang360DTO dto = dao.layDuLieu360(khachHangId, 500);
            if (dto != null) {
                return dto;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Không thể tải dữ liệu 360 từ database: " + e.getMessage());
        }

        // 3. Fallback khởi tạo DTO an toàn nếu cơ sở dữ liệu chưa có đủ bảng vệ tinh
        KhachHang360DTO dtoFallback = new KhachHang360DTO();
        KhachHang khFallback = new KhachHang();
        khFallback.setId(banGhi.getId());
        khFallback.setMaKhachHang(banGhi.getMaBanGhi());
        khFallback.setTenCongTy(banGhi.getTieuDe());
        khFallback.setNguoiSoHuuId(banGhi.getNguoiPhuTrachId());
        khFallback.setTenNguoiSoHuu(banGhi.getTenNguoiPhuTrach());
        khFallback.setNhomKinhDoanhId(banGhi.getNhomKinhDoanhId());
        khFallback.setTenNhomKinhDoanh(banGhi.getTenNhom());
        khFallback.setTrangThai(banGhi.getTrangThai());
        khFallback.setMoTaChiTiet(banGhi.getMoTaChiTiet());
        khFallback.setNgayTao(banGhi.getNgayTao());
        dtoFallback.setKhachHang(khFallback);

        return dtoFallback;
    }

    /**
     * Lấy danh sách hoạt động tương tác cho trang 360 (hỗ trợ phân trang và kiểm tra hiệu năng AC3).
     */
    public List<HoatDong> layDsHoatDong(Long khachHangId, int limit, int offset) {
        if (khachHangId == null || khachHangId <= 0) {
            return Collections.emptyList();
        }
        try {
            return dao.layDsHoatDong(khachHangId, limit > 0 ? limit : 500, Math.max(0, offset));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy danh sách hoạt động: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Đếm tổng số lượng hoạt động của khách hàng.
     */
    public int demTongSoHoatDong(Long khachHangId) {
        if (khachHangId == null || khachHangId <= 0) {
            return 0;
        }
        try {
            return dao.demTongSoHoatDong(khachHangId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Lỗi khi đếm hoạt động: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Ghi nhận tức thời hoạt động tương tác mới với khách hàng.
     */
    public HoatDong themHoatDong(Long khachHangId,
                                 Long userId,
                                 Long nhomId,
                                 String loaiHoatDong,
                                 String tieuDe,
                                 String noiDung) {
        if (khachHangId == null || khachHangId <= 0) {
            throw new IllegalArgumentException("Khách hàng ID không hợp lệ.");
        }
        if (tieuDe == null || tieuDe.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề hoạt động không được để trống.");
        }

        HoatDong hd = new HoatDong();
        hd.setMaHoatDong("HD" + System.currentTimeMillis());
        hd.setKhachHangId(khachHangId);
        hd.setNguoiPhuTrachId(userId != null ? userId : 1L);
        hd.setNhomKinhDoanhId(nhomId);
        hd.setLoaiHoatDong(loaiHoatDong != null && !loaiHoatDong.trim().isEmpty() ? loaiHoatDong.trim() : "CUOC_GOI");
        hd.setTieuDe(tieuDe.trim());
        hd.setNoiDung(noiDung != null ? noiDung.trim() : "");
        hd.setThoiGianBatDau(new Timestamp(System.currentTimeMillis()));
        hd.setTrangThai("HOAN_THANH");
        hd.setLaGhiNhanNhanh(true);

        try {
            Long newId = dao.themHoatDong(hd);
            if (newId != null) {
                hd.setId(newId);
                return hd;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu hoạt động vào DB: " + e.getMessage(), e);
            throw new RuntimeException("Không thể ghi nhận hoạt động vào cơ sở dữ liệu.", e);
        }

        return hd;
    }
}
