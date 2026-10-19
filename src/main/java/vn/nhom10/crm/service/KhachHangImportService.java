package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.KhachHangImportDAO;
import vn.nhom10.crm.dto.BaoCaoNhapKhachHangExcelDTO;
import vn.nhom10.crm.dto.DongExcelKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.util.DatabaseConnection;
import vn.nhom10.crm.util.ExcelKhachHangUtil;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service xử lý toàn diện tiến trình nhập danh sách khách hàng hàng loạt từ tệp Excel (Story S3-06).
 * Đảm bảo:
 * - AC1: Đọc tệp mẫu, thẩm định lỗi theo từng dòng
 * - AC2: Đánh dấu rõ ràng bản ghi trùng, hỗ trợ chọn BỎ QUA hoặc CẬP NHẬT
 * - Phân quyền và Data Scope: Khách hàng mới được gắn cho người dùng đăng nhập hiện tại
 * - Transaction thực tế: Rollback an toàn khi thao tác DB gặp lỗi
 */
public class KhachHangImportService {

    private static final Logger LOGGER = Logger.getLogger(KhachHangImportService.class.getName());

    private final KhachHangImportDAO importDAO;

    public KhachHangImportService() {
        this.importDAO = new KhachHangImportDAO();
    }

    public KhachHangImportService(KhachHangImportDAO importDAO) {
        this.importDAO = importDAO != null ? importDAO : new KhachHangImportDAO();
    }

    /**
     * Lấy danh sách khách hàng hiện có trong hệ thống để đối chiếu trùng lặp tuân theo Data Scope.
     */
    public List<KhachHang> layDanhSachKhachHangDoiChieu(NguoiDungDTO user) {
        if (user == null) {
            return List.of();
        }
        PhamViDuLieu phamVi = user.getPhamViHienTai() != null ? user.getPhamViHienTai() : user.getPhamViToiDa();
        if (phamVi == null) {
            phamVi = PhamViDuLieu.CA_NHAN;
        }

        try {
            return importDAO.layDanhSachDoiChieuTrung(user.getId(), user.getNhomKinhDoanhId(), phamVi);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi truy vấn danh sách khách hàng để đối chiếu trùng: " + e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Thẩm định tệp Excel tải lên và sinh báo cáo xem trước (AC 1 & AC 2).
     */
    public BaoCaoNhapKhachHangExcelDTO thamDinhTepExcel(InputStream is, String tenTep, NguoiDungDTO user) throws IOException {
        List<KhachHang> danhSachHienCo = layDanhSachKhachHangDoiChieu(user);
        List<DongExcelKhachHangDTO> danhSachDong = ExcelKhachHangUtil.docDanhSachKhachHangTuEntities(is, danhSachHienCo);

        BaoCaoNhapKhachHangExcelDTO baoCao = new BaoCaoNhapKhachHangExcelDTO(tenTep);
        baoCao.napDanhSachDong(danhSachDong);
        return baoCao;
    }

    /**
     * Thực hiện nhập dữ liệu thật vào cơ sở dữ liệu dựa trên lựa chọn xử lý của người dùng.
     */
    public void thucHienNhapDuLieu(
            BaoCaoNhapKhachHangExcelDTO baoCao,
            Map<Integer, String> luaChonTungDong,
            String xuLyTrungLapChung,
            NguoiDungDTO user) {

        if (baoCao == null || user == null) {
            return;
        }

        baoCao.setDaThucHienNhap(true);
        String actionChung = (xuLyTrungLapChung != null && !xuLyTrungLapChung.isBlank())
                ? xuLyTrungLapChung : "BO_QUA";

        for (DongExcelKhachHangDTO dong : baoCao.getDanhSachTatCaDong()) {
            if (!dong.isHopLe()) {
                dong.setDaNhap(false);
                dong.setThanhCong(false);
                dong.setGhiChuKetQua("Bỏ qua do dữ liệu không hợp lệ: " + dong.getChuoiLoi());
                baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
                continue;
            }

            // Đọc lựa chọn xử lý cho dòng này
            String luaChon = luaChonTungDong != null ? luaChonTungDong.get(dong.getSoDong()) : null;
            if (luaChon == null || luaChon.isBlank()) {
                luaChon = actionChung;
            }
            dong.setLuaChonXuLy(luaChon);

            if (dong.isBiTrung()) {
                if ("BO_QUA".equalsIgnoreCase(luaChon)) {
                    // Người dùng chọn BỎ QUA bản ghi trùng
                    dong.setDaNhap(true);
                    dong.setThanhCong(true);
                    dong.setGhiChuKetQua("Đã bỏ qua bản ghi trùng theo lựa chọn của người dùng");
                    baoCao.setSoDongBoQua(baoCao.getSoDongBoQua() + 1);
                    continue;
                } else if ("CAP_NHAT".equalsIgnoreCase(luaChon)) {
                    // Người dùng chọn CẬP NHẬT bản ghi trùng
                    xuLyCapNhatBanGhiTrung(dong, baoCao);
                    continue;
                }
            }

            // Trường hợp khách hàng mới hợp lệ: thêm mới vào hệ thống
            xuLyThemMoiKhachHang(dong, baoCao, user);
        }

        baoCao.setThongDiep(String.format(
                "Đã hoàn tất tiến trình nhập dữ liệu khách hàng từ tệp Excel: %d thêm mới, %d cập nhật, %d bỏ qua, %d lỗi.",
                baoCao.getSoDongThanhCong(),
                baoCao.getSoDongCapNhat(),
                baoCao.getSoDongBoQua(),
                baoCao.getSoDongThatBai()
        ));
    }

    /**
     * Xử lý cập nhật thông tin khách hàng bị trùng vào database kèm transaction.
     */
    private void xuLyCapNhatBanGhiTrung(DongExcelKhachHangDTO dong, BaoCaoNhapKhachHangExcelDTO baoCao) {
        if (dong.getIdKhachHangTrung() == null) {
            dong.setDaNhap(true);
            dong.setThanhCong(true);
            dong.setGhiChuKetQua("Đã ghi nhận yêu cầu cập nhật");
            baoCao.setSoDongCapNhat(baoCao.getSoDongCapNhat() + 1);
            return;
        }

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            try {
                KhachHang kh = importDAO.timTheoId(conn, dong.getIdKhachHangTrung());
                if (kh != null) {
                    if (dong.getTenCongTy() != null && !dong.getTenCongTy().isBlank()) {
                        kh.setTenCongTy(dong.getTenCongTy().trim());
                    }
                    if (dong.getMaSoThue() != null && !dong.getMaSoThue().isBlank()) {
                        kh.setMaSoThue(dong.getMaSoThue().trim());
                    }
                    if (dong.getWebsite() != null && !dong.getWebsite().isBlank()) {
                        kh.setWebsite(dong.getWebsite().trim());
                    }
                    if (dong.getDiaChi() != null && !dong.getDiaChi().isBlank()) {
                        kh.setDiaChi(dong.getDiaChi().trim());
                    }
                    if (dong.getDoanhThuUocTinh() != null && !dong.getDoanhThuUocTinh().isBlank()) {
                        kh.setDoanhThuUocTinh(parseDoanhThu(dong.getDoanhThuUocTinh()));
                    }
                    if (dong.getTrangThai() != null && !dong.getTrangThai().isBlank()) {
                        kh.setTrangThai(dong.getTrangThai().trim());
                    }
                    if (dong.getMoTaChiTiet() != null && !dong.getMoTaChiTiet().isBlank()) {
                        kh.setMoTaChiTiet(dong.getMoTaChiTiet().trim());
                    }

                    boolean ok = importDAO.capNhatKhachHang(conn, kh);
                    if (ok) {
                        conn.commit();
                        dong.setDaNhap(true);
                        dong.setThanhCong(true);
                        dong.setGhiChuKetQua("Đã cập nhật thông tin khách hàng thành công (ID: " + kh.getId() + ")");
                        baoCao.setSoDongCapNhat(baoCao.getSoDongCapNhat() + 1);
                    } else {
                        conn.rollback();
                        dong.setDaNhap(false);
                        dong.setThanhCong(false);
                        dong.setGhiChuKetQua("Không thể cập nhật bản ghi trong cơ sở dữ liệu");
                        baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
                    }
                } else {
                    conn.rollback();
                    dong.setDaNhap(false);
                    dong.setThanhCong(false);
                    dong.setGhiChuKetQua("Không tìm thấy bản ghi khách hàng ID " + dong.getIdKhachHangTrung() + " để cập nhật");
                    baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi cập nhật khách hàng trùng dòng " + dong.getSoDong() + ": " + e.getMessage(), e);
            dong.setDaNhap(false);
            dong.setThanhCong(false);
            dong.setGhiChuKetQua("Lỗi cơ sở dữ liệu khi cập nhật: " + e.getMessage());
            baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
        }
    }

    /**
     * Xử lý thêm mới khách hàng vào database kèm transaction.
     */
    private void xuLyThemMoiKhachHang(DongExcelKhachHangDTO dong, BaoCaoNhapKhachHangExcelDTO baoCao, NguoiDungDTO user) {
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            try {
                KhachHang kh = new KhachHang();
                String ma = dong.getMaKhachHang();
                if (ma == null || ma.isBlank()) {
                    ma = "KH-" + System.currentTimeMillis() % 1000000;
                }
                kh.setMaKhachHang(ma.trim());
                kh.setTenCongTy(dong.getTenCongTy().trim());
                kh.setMaSoThue(dong.getMaSoThue() != null ? dong.getMaSoThue().trim() : null);
                kh.setWebsite(dong.getWebsite() != null ? dong.getWebsite().trim() : null);
                kh.setDiaChi(dong.getDiaChi() != null ? dong.getDiaChi().trim() : null);
                kh.setDoanhThuUocTinh(parseDoanhThu(dong.getDoanhThuUocTinh()));
                kh.setTrangThai(dong.getTrangThai() != null && !dong.getTrangThai().isBlank() ? dong.getTrangThai().trim() : "TIEM_NANG");
                kh.setMoTaChiTiet(dong.getMoTaChiTiet() != null ? dong.getMoTaChiTiet().trim() : null);

                // Tuân thủ Data Scope (S1-05): gắn quyền sở hữu cho người dùng thực hiện
                kh.setNguoiSoHuuId(user.getId());
                kh.setNhomKinhDoanhId(user.getNhomKinhDoanhId());
                kh.setNgayTao(LocalDate.now());

                Long newId = importDAO.themKhachHang(conn, kh);
                if (newId != null) {
                    conn.commit();
                    dong.setDaNhap(true);
                    dong.setThanhCong(true);
                    dong.setGhiChuKetQua("Thêm mới thành công (Mã KH: " + kh.getMaKhachHang() + ")");
                    baoCao.setSoDongThanhCong(baoCao.getSoDongThanhCong() + 1);
                } else {
                    conn.rollback();
                    dong.setDaNhap(false);
                    dong.setThanhCong(false);
                    dong.setGhiChuKetQua("Không thể lưu khách hàng vào cơ sở dữ liệu");
                    baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi thêm mới khách hàng dòng " + dong.getSoDong() + ": " + e.getMessage(), e);
            dong.setDaNhap(false);
            dong.setThanhCong(false);
            dong.setGhiChuKetQua("Lỗi khi thêm mới: " + e.getMessage());
            baoCao.setSoDongThatBai(baoCao.getSoDongThatBai() + 1);
        }
    }

    private BigDecimal parseDoanhThu(String val) {
        if (val == null || val.isBlank()) return BigDecimal.ZERO;
        try {
            String clean = val.replace(",", "").replace(".", "").replace("đ", "").replace("VND", "").trim();
            return new BigDecimal(clean);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
