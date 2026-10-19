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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ truy xuất và ghi nhận nhật ký thay đổi trên dữ liệu nhạy cảm (Story S2-04).
 * Kết nối tầng DAO để tương tác trực tiếp CSDL MySQL (bảng nhat_ky_he_thong).
 * Tuyệt đối không tự nạp log mẫu, không dùng bộ nhớ dự phòng giả lập, audit log là bất biến.
 *
 * Đáp ứng đầy đủ các tiêu chí Acceptance Criteria:
 * - Ghi lại mọi thay đổi trên chiết khấu, chỉ tiêu, quyền sở hữu dữ liệu và vai trò người dùng
 * - Mỗi bản ghi có người thực hiện, thời điểm, giá trị trước và sau
 * - Lọc theo người dùng, loại đối tượng, khoảng thời gian
 */
public class NhatKyThayDoiService {

    private static final Logger LOGGER = Logger.getLogger(NhatKyThayDoiService.class.getName());

    private final NhatKyThayDoiDAO nhatKyDAO;

    public NhatKyThayDoiService() {
        this(new NhatKyThayDoiDAO());
    }

    public NhatKyThayDoiService(NhatKyThayDoiDAO nhatKyDAO) {
        this.nhatKyDAO = nhatKyDAO != null ? nhatKyDAO : new NhatKyThayDoiDAO();
    }

    /**
     * Tìm kiếm và phân trang nhật ký thay đổi theo bộ lọc đa tiêu chí (AC 3).
     * Khi CSDL trống -> trả về empty state chuẩn.
     * Khi CSDL lỗi -> fail-safe trả về danh sách rỗng, tuyệt đối không trả log giả.
     *
     * @param boLoc Điều kiện lọc
     * @return KetQuaPhanTrangDTO chứa danh sách NhatKyThayDoiDTO và thông số phân trang
     */
    public KetQuaPhanTrangDTO<NhatKyThayDoiDTO> timKiemNhatKy(BoLocNhatKyDTO boLoc) {
        if (boLoc == null) {
            boLoc = new BoLocNhatKyDTO();
        }

        int trang = boLoc.getTrang() > 0 ? boLoc.getTrang() : 1;
        int soBanGhi = boLoc.getSoBanGhiTrenTrang() > 0 ? boLoc.getSoBanGhiTrenTrang() : 10;

        try {
            long tongSoBanGhi = nhatKyDAO.demTongSoBanGhi(boLoc);
            if (tongSoBanGhi <= 0) {
                return new KetQuaPhanTrangDTO<>(Collections.emptyList(), trang, soBanGhi, 0);
            }

            List<NhatKyThayDoi> models = nhatKyDAO.layDanhSach(boLoc);
            if (models != null && !models.isEmpty()) {
                List<NhatKyThayDoiDTO> dtoList = models.stream()
                        .map(NhatKyThayDoi::toDTO)
                        .collect(Collectors.toList());
                return new KetQuaPhanTrangDTO<>(dtoList, trang, soBanGhi, tongSoBanGhi);
            }
            return new KetQuaPhanTrangDTO<>(Collections.emptyList(), trang, soBanGhi, tongSoBanGhi);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách nhật ký từ CSDL: " + e.getMessage(), e);
            return new KetQuaPhanTrangDTO<>(Collections.emptyList(), trang, soBanGhi, 0);
        }
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
            if (thongKe != null) {
                return thongKe;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy thống kê từ DAO: " + e.getMessage(), e);
        }

        return new ThongKeNhatKyDTO(0, 0, 0, 0, 0, 0);
    }

    /**
     * Lấy chi tiết một bản ghi nhật ký theo ID (AC 2).
     *
     * @param id ID bản ghi
     * @return NhatKyThayDoiDTO hoặc null nếu không tồn tại
     */
    public NhatKyThayDoiDTO layChiTiet(long id) {
        if (id <= 0) {
            return null;
        }
        try {
            NhatKyThayDoi model = nhatKyDAO.timTheoId(id);
            if (model != null) {
                return model.toDTO();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi tìm bản ghi nhật ký theo ID: " + id, e);
        }

        return null;
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
     * Lấy danh sách người dùng cho dropdown bộ lọc (AC 3).
     *
     * @return Danh sách NguoiDungOptionDTO từ CSDL thật
     */
    public List<NguoiDungOptionDTO> layDanhSachNguoiDung() {
        try {
            List<NguoiDungOptionDTO> ds = nhatKyDAO.layDanhSachNguoiDungOption();
            if (ds != null) {
                return ds;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách người dùng cho bộ lọc từ CSDL: " + e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * Ghi lại một thay đổi trên dữ liệu nhạy cảm vào nhật ký hệ thống (AC 1 & AC 2).
     * Dữ liệu nhạy cảm như password/token sẽ được tự động làm mờ (redact/mask).
     *
     * @param nguoiThucHienId    ID người thực hiện
     * @param tenNguoiThucHien   Họ tên người thực hiện
     * @param emailNguoiThucHien Email người thực hiện
     * @param loaiDoiTuong       Loại đối tượng nhạy cảm (CHIET_KHAU, CHI_TIEU, QUYEN_SO_HUU, VAI_TRO_NGUOI_DUNG)
     * @param maDoiTuong         Mã nghiệp vụ đối tượng (VD: BG-2026-088, KPI-2026-Q3-T01, ND-102)
     * @param tenDoiTuong        Tên mô tả đối tượng
     * @param truongThayDoi      Tên trường dữ liệu bị thay đổi
     * @param giaTriTruoc        Giá trị trước khi sửa
     * @param giaTriSau          Giá trị sau khi sửa
     * @param hanhDong           Hành động thực hiện (CAP_NHAT, THEM_MOI, XOA, CHUYEN_QUYEN)
     * @param lyDo               Lý do thay đổi / giải trình
     * @param diaChiIp           Địa chỉ IP
     * @param thietBi            Thông tin thiết bị / User-Agent
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

        try {
            long id = nhatKyDAO.ghiNhatKy(nk);
            return id > 0;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Không thể ghi nhật ký vào CSDL: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Ghi nhật ký trong một Transaction đang mở (AC 1 & AC 2).
     * Ném SQLException để Caller Transaction quản lý rollback fail-closed chuẩn xác.
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
        if (id <= 0) {
            throw new SQLException("Không thể ghi nhật ký hệ thống vào bảng nhat_ky_he_thong");
        }
        return true;
    }

    /**
     * Ghi nhật ký trong một Transaction đang mở có đầy đủ doiTuongId, giaTriTruocJson, giaTriSauJson.
     * Ném SQLException để Caller Transaction quản lý rollback fail-closed chuẩn xác.
     */
    public boolean ghiNhatKyThayDoi(Connection conn, Long nguoiThucHienId, String tenNguoiThucHien, String emailNguoiThucHien,
                                     LoaiDoiTuongNhayCam loaiDoiTuong, Long doiTuongId, String maDoiTuong, String tenDoiTuong,
                                     String truongThayDoi, String giaTriTruocJson, String giaTriSauJson,
                                     HanhDongThayDoi hanhDong, String lyDo, String diaChiIp, String thietBi) throws SQLException {

        NhatKyThayDoi nk = new NhatKyThayDoi();
        nk.setNguoiThucHienId(nguoiThucHienId);
        nk.setTenNguoiThucHien(tenNguoiThucHien);
        nk.setEmailNguoiThucHien(emailNguoiThucHien);
        nk.setLoaiDoiTuong(loaiDoiTuong);
        nk.setDoiTuongId(doiTuongId);
        nk.setMaDoiTuong(maDoiTuong != null ? maDoiTuong : (doiTuongId != null ? "ND-" + doiTuongId : null));
        nk.setTenDoiTuong(tenDoiTuong);
        nk.setTruongThayDoi(truongThayDoi);
        nk.setGiaTriTruocJson(giaTriTruocJson);
        nk.setGiaTriSauJson(giaTriSauJson);
        nk.setHanhDong(hanhDong != null ? hanhDong : HanhDongThayDoi.CAP_NHAT);
        nk.setLyDoThayDoi(lyDo);
        nk.setDiaChiIp(diaChiIp);
        nk.setThietBi(thietBi);
        nk.setCreatedAt(LocalDateTime.now());

        long id = nhatKyDAO.ghiNhatKy(conn, nk);
        if (id <= 0) {
            throw new SQLException("Không thể ghi nhật ký hệ thống vào bảng nhat_ky_he_thong");
        }
        return true;
    }
}
