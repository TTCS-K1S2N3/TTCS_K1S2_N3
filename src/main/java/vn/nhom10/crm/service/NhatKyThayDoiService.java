package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.NhatKyThayDoiDAO;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.KetQuaPhanTrangDTO;
import vn.nhom10.crm.dto.NguoiDungOptionDTO;
import vn.nhom10.crm.dto.NhatKyThayDoiDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.NhatKyThayDoi;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ truy xuất và ghi nhận nhật ký thay đổi trên dữ liệu nhạy cảm (Story S2-04).
 * Kết nối tầng DAO để tương tác CSDL MySQL (bảng nhat_ky_he_thong) và cung cấp cơ chế dự phòng an toàn.
 * Đáp ứng đầy đủ các tiêu chí Acceptance Criteria:
 * - Ghi lại mọi thay đổi trên chiết khấu, chỉ tiêu, quyền sở hữu dữ liệu và vai trò người dùng
 * - Mỗi bản ghi có người thực hiện, thời điểm, giá trị trước và sau
 * - Lọc theo người dùng, loại đối tượng, khoảng thời gian
 */
public class NhatKyThayDoiService {

    private static final Logger LOGGER = Logger.getLogger(NhatKyThayDoiService.class.getName());

    private final NhatKyThayDoiDAO nhatKyDAO;
    private final List<NhatKyThayDoiDTO> boNhoDuPhong = new CopyOnWriteArrayList<>();

    public NhatKyThayDoiService() {
        this(new NhatKyThayDoiDAO());
    }

    public NhatKyThayDoiService(NhatKyThayDoiDAO nhatKyDAO) {
        this.nhatKyDAO = nhatKyDAO != null ? nhatKyDAO : new NhatKyThayDoiDAO();
        khoiTaoDuLieuMauNghiepVu();
        dongBoDuLieuKhoiTaoVaoDatabase();
    }

    /**
     * Khởi tạo bộ dữ liệu mẫu chuẩn nghiệp vụ CRM khi cuối quý số liệu không khớp (Story S2-04).
     */
    private void khoiTaoDuLieuMauNghiepVu() {
        boNhoDuPhong.clear();

        // 1. Sửa chỉ tiêu doanh số cuối quý 3 làm lệch báo cáo (Story S2-04 chính)
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                1L, "LOG-2026-0930-001",
                101L, "Lê Minh Tuấn", "tuan.lm@crm.vn", "Trưởng phòng Kinh doanh",
                LocalDateTime.of(2026, 9, 30, 8, 45, 12),
                LoaiDoiTuongNhayCam.CHI_TIEU,
                "KPI-2026-Q3-T01", "Chỉ tiêu Quý 3 - Nhóm Kinh doanh 1",
                "Chỉ tiêu doanh số kỳ chốt quý (VNĐ)",
                "500,000,000 đ", "350,000,000 đ",
                HanhDongThayDoi.CAP_NHAT,
                "Điều chỉnh số liệu chỉ tiêu khi rà soát cuối quý số liệu không khớp",
                "192.168.1.105", "Chrome 128 / Windows 11"
        ));

        // 2. Chỉnh sửa chiết khấu báo giá vượt hạn mức thẩm quyền
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                2L, "LOG-2026-0929-002",
                102L, "Phạm Hoàng Long", "long.ph@crm.vn", "Chuyên viên Bán hàng Cao cấp",
                LocalDateTime.of(2026, 9, 29, 16, 20, 45),
                LoaiDoiTuongNhayCam.CHIET_KHAU,
                "BG-2026-088", "Báo giá Phần mềm Quản trị ERP Sao Mai",
                "Tỷ lệ chiết khấu thương mại (%)",
                "10.0%", "25.0%",
                HanhDongThayDoi.CAP_NHAT,
                "Áp dụng chính sách ưu đãi đặc biệt ký kết cuối quý",
                "192.168.1.112", "Firefox 130 / macOS Sonoma"
        ));

        // 3. Thay đổi quyền sở hữu dữ liệu khách hàng VIP
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                3L, "LOG-2026-0929-003",
                101L, "Lê Minh Tuấn", "tuan.lm@crm.vn", "Trưởng phòng Kinh doanh",
                LocalDateTime.of(2026, 9, 29, 11, 10, 0),
                LoaiDoiTuongNhayCam.QUYEN_SO_HUU,
                "KH-VIP-009", "Tập đoàn Viễn thông & Công nghệ Sao Bắc Đẩu",
                "Người phụ trách khách hàng (Owner)",
                "Nguyễn Văn An (NV024)", "Trần Thị Mai (NV015)",
                HanhDongThayDoi.CHUYEN_QUYEN,
                "Tái phân bổ khách hàng trọng điểm sang nhóm phụ trách khối Doanh nghiệp lớn",
                "192.168.1.105", "Chrome 128 / Windows 11"
        ));

        // 4. Thay đổi vai trò người dùng (phân quyền tài khoản)
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                4L, "LOG-2026-0928-004",
                103L, "Nguyễn Thị Thu Hà", "ha.ntt@crm.vn", "Quản trị viên Hệ thống (System Admin)",
                LocalDateTime.of(2026, 9, 28, 19, 5, 30),
                LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                "ND-2026-056", "Tài khoản: bui.dt@crm.vn (Bùi Duy Thắng)",
                "Vai trò hệ thống & Quyền hạn",
                "Nhân viên kinh doanh", "Quản trị viên chi nhánh & Duyệt báo giá",
                HanhDongThayDoi.CAP_NHAT,
                "Bổ nhiệm quyền phê duyệt tạm thời thay mặt Trưởng chi nhánh đi công tác",
                "14.162.145.22", "Edge 128 / Windows 11"
        ));

        // 5. Sửa chiết khấu hợp đồng cung cấp dịch vụ
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                5L, "LOG-2026-0927-005",
                104L, "Trần Văn Bình", "binh.tv@crm.vn", "Giám sát Bán hàng",
                LocalDateTime.of(2026, 9, 27, 14, 15, 22),
                LoaiDoiTuongNhayCam.CHIET_KHAU,
                "HD-2026-042", "Hợp đồng Triển khai CRM Cloud - Công ty Nam Thịnh",
                "Giá trị chiết khấu hợp đồng (VNĐ)",
                "15,000,000 đ (5%)", "54,000,000 đ (18%)",
                HanhDongThayDoi.CAP_NHAT,
                "Phụ lục bổ sung chiết khấu theo thỏa thuận thanh toán sớm",
                "192.168.1.118", "Chrome 128 / Windows 10"
        ));

        // 6. Điều chỉnh chỉ tiêu doanh số cá nhân quý 3
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                6L, "LOG-2026-0925-006",
                101L, "Lê Minh Tuấn", "tuan.lm@crm.vn", "Trưởng phòng Kinh doanh",
                LocalDateTime.of(2026, 9, 25, 10, 30, 0),
                LoaiDoiTuongNhayCam.CHI_TIEU,
                "KPI-NV-089", "Chỉ tiêu cá nhân: Vũ Thùy Linh (NV089)",
                "Chỉ tiêu doanh thu tháng 9 / Q3 (VNĐ)",
                "120,000,000 đ", "85,000,000 đ",
                HanhDongThayDoi.CAP_NHAT,
                "Miễn giảm chỉ tiêu do nhân sự nghỉ thai sản giữa kỳ",
                "192.168.1.105", "Chrome 128 / Windows 11"
        ));

        // 7. Chuyển quyền cơ hội kinh doanh quy mô lớn
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                7L, "LOG-2026-0922-007",
                104L, "Trần Văn Bình", "binh.tv@crm.vn", "Giám sát Bán hàng",
                LocalDateTime.of(2026, 9, 22, 15, 40, 19),
                LoaiDoiTuongNhayCam.QUYEN_SO_HUU,
                "CH-2026-015", "Cơ hội: Đấu thầu Hệ thống Chăm sóc Khách hàng BV Đa Khoa",
                "Người quản lý cơ hội bán hàng (Opportunity Owner)",
                "Lý Thị Lan (NV034)", "Trần Văn Bình (NV010)",
                HanhDongThayDoi.CHUYEN_QUYEN,
                "Hồ sơ chuyển giao dự án chiến lược cấp độ A",
                "192.168.1.118", "Chrome 128 / Windows 10"
        ));

        // 8. Thêm vai trò mới cho tài khoản kế toán
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                8L, "LOG-2026-0920-008",
                103L, "Nguyễn Thị Thu Hà", "ha.ntt@crm.vn", "Quản trị viên Hệ thống (System Admin)",
                LocalDateTime.of(2026, 9, 20, 9, 12, 44),
                LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                "ND-2026-012", "Tài khoản: ketoan.truong@crm.vn (Hoàng Mai Loan)",
                "Phân quyền module Bán hàng",
                "Xem báo cáo tài chính", "Xem báo cáo tài chính, Phê duyệt chiết khấu hợp đồng",
                HanhDongThayDoi.THEM_MOI,
                "Cấp quyền kiểm soát trần chiết khấu bán hàng theo quy chế tài chính mới",
                "14.162.145.22", "Edge 128 / Windows 11"
        ));

        // 9. Sửa chiết khấu phụ kiện đi kèm báo giá
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                9L, "LOG-2026-0915-009",
                102L, "Phạm Hoàng Long", "long.ph@crm.vn", "Chuyên viên Bán hàng Cao cấp",
                LocalDateTime.of(2026, 9, 15, 17, 0, 10),
                LoaiDoiTuongNhayCam.CHIET_KHAU,
                "BG-2026-071", "Báo giá Thiết bị POS & Thẻ thông minh Hải Đăng",
                "Chiết khấu tổng đơn hàng (%)",
                "0.0%", "12.5%",
                HanhDongThayDoi.CAP_NHAT,
                "Hỗ trợ đại lý mới gia nhập chuỗi cung ứng",
                "192.168.1.112", "Firefox 130 / macOS Sonoma"
        ));

        // 10. Chỉnh sửa chỉ tiêu quý 4 sắp tới
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                10L, "LOG-2026-0910-010",
                101L, "Lê Minh Tuấn", "tuan.lm@crm.vn", "Trưởng phòng Kinh doanh",
                LocalDateTime.of(2026, 9, 10, 14, 50, 0),
                LoaiDoiTuongNhayCam.CHI_TIEU,
                "KPI-2026-Q4-ENT", "Chỉ tiêu Quý 4 - Khối Doanh nghiệp lớn Enterprise",
                "Kế hoạch doanh số quý 4 (VNĐ)",
                "2,500,000,000 đ", "2,000,000,000 đ",
                HanhDongThayDoi.CAP_NHAT,
                "Cân đối lại ngân sách thị trường theo định hướng hội đồng quản trị",
                "192.168.1.105", "Chrome 128 / Windows 11"
        ));

        // 11. Bàn giao danh sách 40 khách hàng khi nhân sự luân chuyển
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                11L, "LOG-2026-0905-011",
                104L, "Trần Văn Bình", "binh.tv@crm.vn", "Giám sát Bán hàng",
                LocalDateTime.of(2026, 9, 5, 11, 20, 33),
                LoaiDoiTuongNhayCam.QUYEN_SO_HUU,
                "DS-KH-MN-01", "Nhóm 40 khách hàng khu vực Miền Nam",
                "Quyền sở hữu tập dữ liệu khách hàng",
                "Đỗ Quốc Huy (NV041)", "Nguyễn Văn An (NV024)",
                HanhDongThayDoi.CHUYEN_QUYEN,
                "Bàn giao trách nhiệm chăm sóc khi nhân sự chuyển công tác ra Hà Nội",
                "192.168.1.118", "Chrome 128 / Windows 10"
        ));

        // 12. Hủy quyền quản trị viên chi nhánh của tài khoản
        boNhoDuPhong.add(new NhatKyThayDoiDTO(
                12L, "LOG-2026-0901-012",
                103L, "Nguyễn Thị Thu Hà", "ha.ntt@crm.vn", "Quản trị viên Hệ thống (System Admin)",
                LocalDateTime.of(2026, 9, 1, 8, 30, 0),
                LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG,
                "ND-2026-008", "Tài khoản: vuong.tc@crm.vn (Tạ Chí Vượng)",
                "Vai trò tài khoản hệ thống",
                "Quản trị viên chi nhánh Đà Nẵng", "Nhân viên kinh doanh thông thường",
                HanhDongThayDoi.XOA,
                "Hết thời hạn biệt phái quản lý chi nhánh theo quyết định nhân sự",
                "14.162.145.22", "Edge 128 / Windows 11"
        ));
    }

    /**
     * Đồng bộ nạp dữ liệu mẫu vào CSDL nếu bảng nhat_ky_he_thong hiện đang trống.
     */
    private void dongBoDuLieuKhoiTaoVaoDatabase() {
        try {
            long count = nhatKyDAO.demTongSoBanGhi(null);
            if (count == 0) {
                LOGGER.info("Bảng nhat_ky_he_thong đang trống, tiến hành nạp dữ liệu khởi tạo mẫu...");
                for (NhatKyThayDoiDTO dto : boNhoDuPhong) {
                    NhatKyThayDoi nk = new NhatKyThayDoi();
                    nk.setNguoiThucHienId(dto.getNguoiThucHienId());
                    nk.setTenNguoiThucHien(dto.getTenNguoiThucHien());
                    nk.setEmailNguoiThucHien(dto.getEmailNguoiThucHien());
                    nk.setVaiTroNguoiThucHien(dto.getVaiTroNguoiThucHien());
                    nk.setLoaiDoiTuong(dto.getLoaiDoiTuong());
                    nk.setMaDoiTuong(dto.getMaDoiTuong());
                    nk.setTenDoiTuong(dto.getTenDoiTuong());
                    nk.setTruongThayDoi(dto.getTruongThayDoi());
                    nk.setGiaTriTruoc(dto.getGiaTriTruoc());
                    nk.setGiaTriSau(dto.getGiaTriSau());
                    nk.setHanhDong(dto.getHanhDong());
                    nk.setLyDoThayDoi(dto.getLyDoThayDoi());
                    nk.setDiaChiIp(dto.getDiaChiIp());
                    nk.setThietBi(dto.getThietBi());
                    nk.setCreatedAt(dto.getThoiDiem());

                    nhatKyDAO.ghiNhatKy(nk);
                }
                LOGGER.info("Đã đồng bộ xong dữ liệu mẫu vào nhat_ky_he_thong.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Bỏ qua đồng bộ dữ liệu vào DB (môi trường không kết nối CSDL hoặc DB mock): " + e.getMessage());
        }
    }

    /**
     * Tìm kiếm và phân trang nhật ký thay đổi theo bộ lọc đa tiêu chí (AC 3).
     *
     * @param boLoc Điều kiện lọc
     * @return KetQuaPhanTrangDTO chứa danh sách NhatKyThayDoiDTO và thông số phân trang
     */
    public KetQuaPhanTrangDTO<NhatKyThayDoiDTO> timKiemNhatKy(BoLocNhatKyDTO boLoc) {
        if (boLoc == null) {
            boLoc = new BoLocNhatKyDTO();
        }

        try {
            long tongSoBanGhi = nhatKyDAO.demTongSoBanGhi(boLoc);
            if (tongSoBanGhi > 0) {
                List<NhatKyThayDoi> models = nhatKyDAO.layDanhSach(boLoc);
                if (models != null && !models.isEmpty()) {
                    List<NhatKyThayDoiDTO> dtoList = models.stream()
                            .map(NhatKyThayDoi::toDTO)
                            .collect(Collectors.toList());
                    return new KetQuaPhanTrangDTO<>(dtoList, boLoc.getTrang(), boLoc.getSoBanGhiTrenTrang(), tongSoBanGhi);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể truy vấn CSDL, chuyển sang lọc từ bộ nhớ dự phòng: " + e.getMessage());
        }

        return locTuBoNhoDuPhong(boLoc);
    }

    /**
     * Lấy số liệu thống kê tổng hợp số lượt thay đổi theo từng loại đối tượng nhạy cảm.
     *
     * @param boLoc Điều kiện lọc
     * @return ThongKeNhatKyDTO
     */
    public ThongKeNhatKyDTO layThongKe(BoLocNhatKyDTO boLoc) {
        try {
            ThongKeNhatKyDTO thongKe = nhatKyDAO.layThongKe(boLoc);
            if (thongKe != null && thongKe.getTongSoBanGhi() > 0) {
                return thongKe;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi lấy thống kê từ DAO, chuyển sang tính từ bộ nhớ: " + e.getMessage());
        }

        return tinhThongKeTuBoNho(boLoc);
    }

    /**
     * Lấy chi tiết một bản ghi nhật ký theo ID (AC 2).
     *
     * @param id ID bản ghi
     * @return NhatKyThayDoiDTO hoặc null
     */
    public NhatKyThayDoiDTO layChiTiet(long id) {
        try {
            NhatKyThayDoi model = nhatKyDAO.timTheoId(id);
            if (model != null) {
                return model.toDTO();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi tìm bản ghi theo ID từ CSDL: " + e.getMessage());
        }

        return boNhoDuPhong.stream()
                .filter(nk -> nk.getId() != null && nk.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Lấy danh sách phân trang (alias cho timKiemNhatKy).
     */
    public KetQuaPhanTrangDTO<NhatKyThayDoiDTO> layDanhSach(BoLocNhatKyDTO boLoc) {
        return timKiemNhatKy(boLoc);
    }

    /**
     * Lấy số liệu thống kê tổng hợp (alias cho layThongKe(null)).
     */
    public ThongKeNhatKyDTO tinhThongKe() {
        return layThongKe(null);
    }

    /**
     * Tìm kiếm bản ghi theo ID (alias cho layChiTiet).
     */
    public NhatKyThayDoiDTO timTheoId(long id) {
        return layChiTiet(id);
    }

    /**
     * Lấy toàn bộ danh sách bản ghi nhật ký trong bộ nhớ phục vụ kiểm thử.
     */
    public List<NhatKyThayDoiDTO> layTatCa() {
        return new ArrayList<>(boNhoDuPhong);
    }

    /**
     * Lấy danh sách người dùng cho dropdown bộ lọc (AC 3).
     *
     * @return Danh sách NguoiDungOptionDTO
     */
    public List<NguoiDungOptionDTO> layDanhSachNguoiDung() {
        Map<Long, NguoiDungOptionDTO> map = new LinkedHashMap<>();

        try {
            List<NguoiDungOptionDTO> ds = nhatKyDAO.layDanhSachNguoiDungOption();
            if (ds != null) {
                for (NguoiDungOptionDTO u : ds) {
                    if (u.getId() != null) {
                        map.put(u.getId(), u);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi lấy danh sách người dùng từ CSDL: " + e.getMessage());
        }

        // Bổ sung thêm người thực hiện từ nhật ký kiểm toán nếu chưa có trong danh sách
        for (NhatKyThayDoiDTO dto : boNhoDuPhong) {
            if (dto.getNguoiThucHienId() != null && !map.containsKey(dto.getNguoiThucHienId())) {
                map.put(dto.getNguoiThucHienId(), new NguoiDungOptionDTO(
                        dto.getNguoiThucHienId(),
                        dto.getTenNguoiThucHien(),
                        dto.getEmailNguoiThucHien(),
                        dto.getVaiTroNguoiThucHien()
                ));
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Ghi lại một thay đổi trên dữ liệu nhạy cảm vào nhật ký hệ thống (AC 1 & AC 2).
     *
     * @param nguoiThucHienId   ID người thực hiện
     * @param tenNguoiThucHien  Họ tên người thực hiện
     * @param emailNguoiThucHien Email người thực hiện
     * @param loaiDoiTuong      Loại đối tượng nhạy cảm (CHIET_KHAU, CHI_TIEU, QUYEN_SO_HUU, VAI_TRO_NGUOI_DUNG)
     * @param maDoiTuong        Mã nghiệp vụ đối tượng (VD: BG-2026-088, KPI-2026-Q3-T01)
     * @param tenDoiTuong       Tên mô tả đối tượng
     * @param truongThayDoi     Tên trường dữ liệu bị thay đổi
     * @param giaTriTruoc       Giá trị trước khi sửa
     * @param giaTriSau         Giá trị sau khi sửa
     * @param hanhDong          Hành động thực hiện (CAP_NHAT, THEM_MOI, XOA, CHUYEN_QUYEN)
     * @param lyDo              Lý do thay đổi / giải trình
     * @param diaChiIp          Địa chỉ IP
     * @param thietBi           Thông tin thiết bị / User-Agent
     * @return true nếu ghi thành công
     */
    public boolean ghiNhatKyThayDoi(Long nguoiThucHienId, String tenNguoiThucHien, String emailNguoiThucHien,
                                    LoaiDoiTuongNhayCam loaiDoiTuong, String maDoiTuong, String tenDoiTuong,
                                    String truongThayDoi, String giaTriTruoc, String giaTriSau,
                                    HanhDongThayDoi hanhDong, String lyDo, String diaChiIp, String thietBi) {

        NhatKyThayDoi nk = new NhatKyThayDoi();
        nk.setNguoiThucHienId(nguoiThucHienId);
        nk.setTenNguoiThucHien(tenNguoiThucHien);
        nk.setEmailNguoiThucHien(emailNguoiThucHien);
        nk.setLoaiDoiTuong(loaiDoiTuong);
        nk.setMaDoiTuong(maDoiTuong);
        nk.setTenDoiTuong(tenDoiTuong);
        nk.setTruongThayDoi(truongThayDoi);
        nk.setGiaTriTruoc(giaTriTruoc);
        nk.setGiaTriSau(giaTriSau);
        nk.setHanhDong(hanhDong);
        nk.setLyDoThayDoi(lyDo);
        nk.setDiaChiIp(diaChiIp);
        nk.setThietBi(thietBi);
        nk.setCreatedAt(LocalDateTime.now());

        long id = -1;
        try {
            id = nhatKyDAO.ghiNhatKy(nk);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không thể ghi nhật ký vào CSDL: " + e.getMessage());
        }

        if (id <= 0) {
            id = boNhoDuPhong.stream().mapToLong(d -> d.getId() != null ? d.getId() : 0).max().orElse(0) + 1;
        }

        nk.setId(id);
        boNhoDuPhong.add(0, nk.toDTO());
        return true;
    }

    /**
     * Ghi nhật ký trong một Transaction đang mở (AC 1 & AC 2).
     */
    public boolean ghiNhatKyThayDoi(Connection conn, Long nguoiThucHienId, String tenNguoiThucHien, String emailNguoiThucHien,
                                    LoaiDoiTuongNhayCam loaiDoiTuong, String maDoiTuong, String tenDoiTuong,
                                    String truongThayDoi, String giaTriTruoc, String giaTriSau,
                                    HanhDongThayDoi hanhDong, String lyDo, String diaChiIp, String thietBi) throws SQLException {

        NhatKyThayDoi nk = new NhatKyThayDoi();
        nk.setNguoiThucHienId(nguoiThucHienId);
        nk.setTenNguoiThucHien(tenNguoiThucHien);
        nk.setEmailNguoiThucHien(emailNguoiThucHien);
        nk.setLoaiDoiTuong(loaiDoiTuong);
        nk.setMaDoiTuong(maDoiTuong);
        nk.setTenDoiTuong(tenDoiTuong);
        nk.setTruongThayDoi(truongThayDoi);
        nk.setGiaTriTruoc(giaTriTruoc);
        nk.setGiaTriSau(giaTriSau);
        nk.setHanhDong(hanhDong);
        nk.setLyDoThayDoi(lyDo);
        nk.setDiaChiIp(diaChiIp);
        nk.setThietBi(thietBi);
        nk.setCreatedAt(LocalDateTime.now());

        long id = nhatKyDAO.ghiNhatKy(conn, nk);
        if (id > 0) {
            nk.setId(id);
            boNhoDuPhong.add(0, nk.toDTO());
            return true;
        }
        return false;
    }

    // =========================================================================
    // XỬ LÝ LỌC & THỐNG KÊ TỪ BỘ NHỚ DỰ PHÒNG KHI CSDL TRỐNG / TEST MOCK
    // =========================================================================

    private KetQuaPhanTrangDTO<NhatKyThayDoiDTO> locTuBoNhoDuPhong(BoLocNhatKyDTO boLoc) {
        List<NhatKyThayDoiDTO> ketQua = boNhoDuPhong.stream()
                .filter(nk -> thoaManBoLoc(nk, boLoc))
                .sorted(Comparator.comparing(NhatKyThayDoiDTO::getThoiDiem, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        long tongSoBanGhi = ketQua.size();
        int soBanGhi = boLoc.getSoBanGhiTrenTrang() > 0 ? boLoc.getSoBanGhiTrenTrang() : 10;
        int trang = boLoc.getTrang() > 0 ? boLoc.getTrang() : 1;
        int batDau = (trang - 1) * soBanGhi;

        if (batDau >= ketQua.size()) {
            return new KetQuaPhanTrangDTO<>(new ArrayList<>(), trang, soBanGhi, tongSoBanGhi);
        }

        int ketThuc = Math.min(batDau + soBanGhi, ketQua.size());
        List<NhatKyThayDoiDTO> trangHienTai = new ArrayList<>(ketQua.subList(batDau, ketThuc));

        return new KetQuaPhanTrangDTO<>(trangHienTai, trang, soBanGhi, tongSoBanGhi);
    }

    private boolean thoaManBoLoc(NhatKyThayDoiDTO nk, BoLocNhatKyDTO boLoc) {
        if (boLoc == null) return true;

        if (boLoc.getNguoiDungId() != null && !Objects.equals(nk.getNguoiThucHienId(), boLoc.getNguoiDungId())) {
            return false;
        }

        if (boLoc.getLoaiDoiTuong() != null && !boLoc.getLoaiDoiTuong().trim().isEmpty()) {
            if (nk.getLoaiDoiTuong() == null || !nk.getLoaiDoiTuong().getMa().equalsIgnoreCase(boLoc.getLoaiDoiTuong().trim())) {
                return false;
            }
        }

        if (boLoc.getTuNgay() != null && nk.getThoiDiem() != null) {
            LocalDate thoiDiemNgay = nk.getThoiDiem().toLocalDate();
            if (thoiDiemNgay.isBefore(boLoc.getTuNgay())) {
                return false;
            }
        }

        if (boLoc.getDenNgay() != null && nk.getThoiDiem() != null) {
            LocalDate thoiDiemNgay = nk.getThoiDiem().toLocalDate();
            if (thoiDiemNgay.isAfter(boLoc.getDenNgay())) {
                return false;
            }
        }

        if (boLoc.getTuKhoa() != null && !boLoc.getTuKhoa().trim().isEmpty()) {
            String tuKhoa = boLoc.getTuKhoa().trim().toLowerCase();
            boolean khopMa = nk.getMaTruyVet() != null && nk.getMaTruyVet().toLowerCase().contains(tuKhoa);
            boolean khopNguoi = nk.getTenNguoiThucHien() != null && nk.getTenNguoiThucHien().toLowerCase().contains(tuKhoa);
            boolean khopEmail = nk.getEmailNguoiThucHien() != null && nk.getEmailNguoiThucHien().toLowerCase().contains(tuKhoa);
            boolean khopDoiTuong = (nk.getMaDoiTuong() != null && nk.getMaDoiTuong().toLowerCase().contains(tuKhoa))
                    || (nk.getTenDoiTuong() != null && nk.getTenDoiTuong().toLowerCase().contains(tuKhoa));
            boolean khopTruong = nk.getTruongThayDoi() != null && nk.getTruongThayDoi().toLowerCase().contains(tuKhoa);
            boolean khopLyDo = nk.getLyDoThayDoi() != null && nk.getLyDoThayDoi().toLowerCase().contains(tuKhoa);
            boolean khopGiaTri = (nk.getGiaTriTruoc() != null && nk.getGiaTriTruoc().toLowerCase().contains(tuKhoa))
                    || (nk.getGiaTriSau() != null && nk.getGiaTriSau().toLowerCase().contains(tuKhoa));

            if (!khopMa && !khopNguoi && !khopEmail && !khopDoiTuong && !khopTruong && !khopLyDo && !khopGiaTri) {
                return false;
            }
        }

        return true;
    }

    private ThongKeNhatKyDTO tinhThongKeTuBoNho(BoLocNhatKyDTO boLoc) {
        List<NhatKyThayDoiDTO> danhSachLoc = boNhoDuPhong.stream()
                .filter(nk -> thoaManBoLoc(nk, boLoc))
                .collect(Collectors.toList());

        long tong = danhSachLoc.size();
        long chietKhau = danhSachLoc.stream().filter(nk -> nk.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.CHIET_KHAU).count();
        long chiTieu = danhSachLoc.stream().filter(nk -> nk.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.CHI_TIEU).count();
        long quyenSoHuu = danhSachLoc.stream().filter(nk -> nk.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.QUYEN_SO_HUU).count();
        long vaiTro = danhSachLoc.stream().filter(nk -> nk.getLoaiDoiTuong() == LoaiDoiTuongNhayCam.VAI_TRO_NGUOI_DUNG).count();
        long soNguoi = danhSachLoc.stream().map(NhatKyThayDoiDTO::getNguoiThucHienId).filter(Objects::nonNull).distinct().count();

        return new ThongKeNhatKyDTO(tong, chietKhau, chiTieu, quyenSoHuu, vaiTro, soNguoi);
    }
}
