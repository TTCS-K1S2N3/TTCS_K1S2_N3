package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.BanGiaoDuLieuDAO;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO;
import vn.nhom10.crm.dto.ThongTinBanGiaoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyBanGiao;
import vn.nhom10.crm.util.ActiveSessionManager;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý nghiệp vụ khoá tài khoản và bàn giao dữ liệu khi nhân viên nghỉ (Story S1-10).
 * Đảm bảo:
 * 1. Bắt buộc chọn người tiếp nhận trước khi khoá.
 * 2. Thực hiện chuyển giao khách hàng, cơ hội và ghi nhật ký trong 1 TRANSACTION duy nhất.
 * 3. Thu hồi toàn bộ phiên làm việc (HTTP Session) đang mở của tài khoản bị khoá.
 */
public class KhoaTaiKhoanService {

    private static final Logger LOGGER = Logger.getLogger(KhoaTaiKhoanService.class.getName());

    private final NguoiDungDAO nguoiDungDAO;
    private final BanGiaoDuLieuDAO banGiaoDAO;
    private final ActiveSessionManager sessionManager;

    public KhoaTaiKhoanService() {
        this.nguoiDungDAO = new NguoiDungDAO();
        this.banGiaoDAO = new BanGiaoDuLieuDAO();
        this.sessionManager = ActiveSessionManager.getInstance();
    }

    public KhoaTaiKhoanService(NguoiDungDAO nguoiDungDAO, BanGiaoDuLieuDAO banGiaoDAO, ActiveSessionManager sessionManager) {
        this.nguoiDungDAO = nguoiDungDAO;
        this.banGiaoDAO = banGiaoDAO;
        this.sessionManager = sessionManager != null ? sessionManager : ActiveSessionManager.getInstance();
    }

    /**
     * Lấy thông tin thống kê dữ liệu cần bàn giao và danh sách người tiếp nhận khả dụng.
     */
    public ThongTinBanGiaoDTO layThongTinBanGiao(int nguoiBiKhoaId) {
        NguoiDung nguoiBiKhoa = nguoiDungDAO.timTheoId(nguoiBiKhoaId);
        if (nguoiBiKhoa == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID=" + nguoiBiKhoaId);
        }

        int soKH = banGiaoDAO.demKhachHangCuaUser(nguoiBiKhoaId);
        int soCH = banGiaoDAO.demCoHoiCuaUser(nguoiBiKhoaId);
        List<NguoiDung> dsNguoiNhan = nguoiDungDAO.layDanhSachNguoiDungKhaDungTiepNhan(nguoiBiKhoaId);

        return new ThongTinBanGiaoDTO(nguoiBiKhoa, soKH, soCH, dsNguoiNhan);
    }

    /**
     * Thực hiện khoá tài khoản và bàn giao dữ liệu với ràng buộc nghiệp vụ và Transaction an toàn.
     */
    public KetQuaKhoaVaBanGiaoDTO khoaVaBanGiao(int nguoiBiKhoaId, Integer nguoiTiepNhanId, int nguoiThucHienId, String lyDo) {
        // 1. Kiểm tra quyền và logic tự khoá
        if (nguoiBiKhoaId == nguoiThucHienId) {
            return new KetQuaKhoaVaBanGiaoDTO(false, "Không thể tự khoá tài khoản quản trị của chính mình.");
        }

        // 2. Kiểm tra bắt buộc chọn người tiếp nhận
        if (nguoiTiepNhanId == null || nguoiTiepNhanId <= 0) {
            return new KetQuaKhoaVaBanGiaoDTO(false, "Bắt buộc phải chọn người tiếp nhận toàn bộ dữ liệu trước khi khoá tài khoản.");
        }

        if (nguoiTiepNhanId.intValue() == nguoiBiKhoaId) {
            return new KetQuaKhoaVaBanGiaoDTO(false, "Người tiếp nhận không được trùng với tài khoản sắp bị khoá.");
        }

        // 3. Kiểm tra thông tin người bị khoá
        NguoiDung nguoiBiKhoa = nguoiDungDAO.timTheoId(nguoiBiKhoaId);
        if (nguoiBiKhoa == null) {
            return new KetQuaKhoaVaBanGiaoDTO(false, "Tài khoản nhân viên cần khoá không tồn tại trong hệ thống.");
        }

        if (NguoiDung.TRANG_THAI_KHOA.equalsIgnoreCase(nguoiBiKhoa.getTrangThai())) {
            return new KetQuaKhoaVaBanGiaoDTO(false, "Tài khoản này đã bị khoá trước đó.");
        }

        // 4. Kiểm tra người tiếp nhận
        NguoiDung nguoiTiepNhan = nguoiDungDAO.timTheoId(nguoiTiepNhanId);
        if (nguoiTiepNhan == null || !nguoiTiepNhan.dangHoatDong()) {
            return new KetQuaKhoaVaBanGiaoDTO(false, "Người tiếp nhận không hợp lệ hoặc tài khoản không ở trạng thái hoạt động.");
        }

        // 5. Thực thi Transaction chuyển giao & khoá tài khoản
        Connection conn = null;
        try {
            conn = DatabaseConnection.layKetNoi();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 5.1. Đếm số lượng khách hàng và cơ hội thực tế
            int soKH = banGiaoDAO.demKhachHangCuaUser(conn, nguoiBiKhoaId);
            int soCH = banGiaoDAO.demCoHoiCuaUser(conn, nguoiBiKhoaId);

            // 5.2. Chuyển quyền sở hữu khách hàng
            if (soKH > 0) {
                banGiaoDAO.chuyenKhachHang(conn, nguoiBiKhoaId, nguoiTiepNhanId);
            }

            // 5.3. Chuyển quyền phụ trách cơ hội
            if (soCH > 0) {
                banGiaoDAO.chuyenCoHoi(conn, nguoiBiKhoaId, nguoiTiepNhanId);
            }

            // 5.4. Ghi nhật ký bàn giao
            String lyDoGhiNhan = (lyDo != null && !lyDo.isBlank()) ? lyDo.trim() : "Bàn giao dữ liệu khi nhân viên nghỉ việc";
            NhatKyBanGiao nk = new NhatKyBanGiao(nguoiBiKhoaId, nguoiTiepNhanId, nguoiThucHienId, soKH, soCH, lyDoGhiNhan);
            int nhatKyId = banGiaoDAO.ghiNhatKyBanGiao(conn, nk);

            // 5.5. Cập nhật trạng thái người dùng thành KHOA
            nguoiDungDAO.khoaTaiKhoan(conn, nguoiBiKhoaId);

            // 5.6. Commit Transaction thành công
            conn.commit();

            // 6. Thu hồi toàn bộ session đang mở của người dùng bị khoá
            int soSessionThuHoi = sessionManager.thuHoiTatCaSessionCuaUser(nguoiBiKhoaId);
            LOGGER.info("Đã khoá tài khoản ID=" + nguoiBiKhoaId + ", bàn giao " + soKH + " khách hàng, " + soCH +
                    " cơ hội sang ID=" + nguoiTiepNhanId + ", thu hồi " + soSessionThuHoi + " session.");

            String thongBao = String.format("Khoá tài khoản thành công! Đã bàn giao %d khách hàng và %d cơ hội sang %s.",
                    soKH, soCH, nguoiTiepNhan.getHoTen());

            return new KetQuaKhoaVaBanGiaoDTO(true, thongBao, soKH, soCH, nhatKyId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi transaction khi bàn giao & khoá tài khoản: " + e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.warning("Đã rollback transaction bàn giao dữ liệu.");
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi khi rollback: " + ex.getMessage(), ex);
                }
            }
            return new KetQuaKhoaVaBanGiaoDTO(false, "Lỗi hệ thống khi bàn giao dữ liệu: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Lỗi đóng connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Lấy lịch sử bàn giao để xem nhật ký.
     */
    public List<NhatKyBanGiao> layLichSuBanGiao(int limit) {
        return banGiaoDAO.layLichSuBanGiao(limit);
    }
}
