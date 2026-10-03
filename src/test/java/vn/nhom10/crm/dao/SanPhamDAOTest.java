package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.LoaiSanPhamEnum;
import vn.nhom10.crm.model.SanPham;
import vn.nhom10.crm.model.TrangThaiSanPhamEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tầng DAO cho SanPhamDAO sử dụng H2 In-Memory (MySQL Mode).
 */
public class SanPhamDAOTest {

    private static Connection h2Connection;
    private SanPhamDAO sanPhamDAO;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_dao;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS san_pham (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_san_pham VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_san_pham VARCHAR(255) NOT NULL, " +
                    "loai_san_pham VARCHAR(50) NOT NULL, " +
                    "don_vi_tinh VARCHAR(50) NOT NULL, " +
                    "gia_niem_yet DECIMAL(15, 2) NOT NULL, " +
                    "gia_san DECIMAL(15, 2) NOT NULL, " +
                    "gia_von DECIMAL(15, 2) NULL, " +
                    "mo_ta TEXT NULL, " +
                    "trang_thai VARCHAR(30) NOT NULL DEFAULT 'DANG_KINH_DOANH', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS bao_gia (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_bao_gia VARCHAR(50) NOT NULL UNIQUE, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "tong_tien DECIMAL(15, 2) NOT NULL DEFAULT 0.00, " +
                    "trang_thai VARCHAR(50) NOT NULL DEFAULT 'CHO_DUYET', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS bao_gia_chi_tiet (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "bao_gia_id INT NOT NULL, " +
                    "san_pham_id INT NOT NULL, " +
                    "so_luong INT NOT NULL DEFAULT 1, " +
                    "don_gia DECIMAL(15, 2) NOT NULL, " +
                    "thanh_tien DECIMAL(15, 2) NOT NULL)");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_dao;MODE=MySQL;DB_CLOSE_DELAY=-1");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
    }

    @BeforeEach
    public void clearData() throws Exception {
        sanPhamDAO = new SanPhamDAO();
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM bao_gia_chi_tiet");
            stmt.execute("DELETE FROM bao_gia");
            stmt.execute("DELETE FROM san_pham");
        }
    }

    @Test
    public void testThemVaTimTheoId() throws Exception {
        SanPham sp = new SanPham();
        sp.setMaSanPham("TEST-SP-01");
        sp.setTenSanPham("Phần mềm Kiểm thử");
        sp.setLoai(LoaiSanPhamEnum.DICH_VU_THUE_BAO);
        sp.setDonViTinh("Tháng");
        sp.setGiaNiemYet(new BigDecimal("1000000.00"));
        sp.setGiaSan(new BigDecimal("800000.00"));
        sp.setGiaVon(new BigDecimal("500000.00"));
        sp.setMoTa("Mô tả kiểm thử");

        int id = sanPhamDAO.themSanPham(sp, true);
        assertTrue(id > 0);

        // Kiểm tra tìm kiếm khi có quyền giá vốn
        SanPham timThayCoQuyen = sanPhamDAO.timTheoId(id, true);
        assertNotNull(timThayCoQuyen);
        assertEquals("TEST-SP-01", timThayCoQuyen.getMaSanPham());
        assertEquals(0, new BigDecimal("500000.00").compareTo(timThayCoQuyen.getGiaVon()));

        // Kiểm tra tìm kiếm khi KHÔNG có quyền giá vốn (AC: Giá vốn chỉ Giám đốc xem được)
        SanPham timThayKhongQuyen = sanPhamDAO.timTheoId(id, false);
        assertNotNull(timThayKhongQuyen);
        assertNull(timThayKhongQuyen.getGiaVon(), "Giá vốn phải là null đối với người không có quyền Giám đốc");
    }

    @Test
    public void testKiemTraMaTonTai() throws Exception {
        SanPham sp = new SanPham();
        sp.setMaSanPham("DUPLICATE-01");
        sp.setTenSanPham("Sản phẩm mẫu");
        sp.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        sp.setDonViTinh("Bộ");
        sp.setGiaNiemYet(new BigDecimal("200000.00"));
        sp.setGiaSan(new BigDecimal("150000.00"));

        int id = sanPhamDAO.themSanPham(sp, false);

        assertTrue(sanPhamDAO.kiemTraMaTonTai("DUPLICATE-01", null));
        assertTrue(sanPhamDAO.kiemTraMaTonTai("duplicate-01", null), "Phải không phân biệt hoa thường");
        assertFalse(sanPhamDAO.kiemTraMaTonTai("DUPLICATE-01", id), "Trùng chính nó khi cập nhật thì trả về false");
        assertFalse(sanPhamDAO.kiemTraMaTonTai("MA-KHAC", null));
    }

    @Test
    public void testKiemTraXuatHienTrongBaoGia() throws Exception {
        SanPham sp = new SanPham();
        sp.setMaSanPham("BG-SP-01");
        sp.setTenSanPham("Sản phẩm có trong báo giá");
        sp.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        sp.setDonViTinh("Cái");
        sp.setGiaNiemYet(new BigDecimal("500000.00"));
        sp.setGiaSan(new BigDecimal("400000.00"));

        int spId = sanPhamDAO.themSanPham(sp, false);

        // Ban đầu chưa có trong báo giá
        assertFalse(sanPhamDAO.kiemTraXuatHienTrongBaoGia(spId));

        // Thêm vào bảng bao_gia và bao_gia_chi_tiet
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("INSERT INTO bao_gia (id, ma_bao_gia, tieu_de, tong_tien) VALUES (1, 'BG-001', 'Báo giá 1', 500000.00)");
            stmt.execute("INSERT INTO bao_gia_chi_tiet (id, bao_gia_id, san_pham_id, so_luong, don_gia, thanh_tien) VALUES (1, 1, " + spId + ", 1, 500000.00, 500000.00)");
        }

        // Sau khi đã gắn vào báo giá
        assertTrue(sanPhamDAO.kiemTraXuatHienTrongBaoGia(spId), "Sản phẩm phải được ghi nhận đã xuất hiện trong báo giá");
    }

    @Test
    public void testCapNhatTrangThai() throws Exception {
        SanPham sp = new SanPham();
        sp.setMaSanPham("STATUS-01");
        sp.setTenSanPham("Sản phẩm đổi trạng thái");
        sp.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        sp.setDonViTinh("Gói");
        sp.setGiaNiemYet(new BigDecimal("300000.00"));
        sp.setGiaSan(new BigDecimal("250000.00"));

        int id = sanPhamDAO.themSanPham(sp, false);

        boolean capNhatOk = sanPhamDAO.capNhatTrangThai(id, TrangThaiSanPhamEnum.NGUNG_KINH_DOANH);
        assertTrue(capNhatOk);

        SanPham spSauCapNhat = sanPhamDAO.timTheoId(id, false);
        assertEquals(TrangThaiSanPhamEnum.NGUNG_KINH_DOANH, spSauCapNhat.getTrangThai());
    }

    @Test
    public void testTimKiemVaPhanTrang() throws Exception {
        for (int i = 1; i <= 5; i++) {
            SanPham sp = new SanPham();
            sp.setMaSanPham("PAGINATION-" + i);
            sp.setTenSanPham("Sản phẩm số " + i);
            sp.setLoai(i % 2 == 0 ? LoaiSanPhamEnum.DICH_VU_THUE_BAO : LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
            sp.setDonViTinh("Cái");
            sp.setGiaNiemYet(new BigDecimal("100000.00"));
            sp.setGiaSan(new BigDecimal("80000.00"));
            sanPhamDAO.themSanPham(sp, false);
        }

        int tong = sanPhamDAO.demSoLuong("PAGINATION", null, null);
        assertEquals(5, tong);

        List<SanPham> danhSach = sanPhamDAO.timKiemVaPhanTrang("PAGINATION", null, null, 2, 0, false);
        assertEquals(2, danhSach.size());
    }

    @Test
    public void testCapNhatSanPhamKhongQuyenGiaVonKhongDoiGiaVonTrongDB() throws Exception {
        SanPham sp = new SanPham();
        sp.setMaSanPham("SECURITY-01");
        sp.setTenSanPham("Sản phẩm bảo mật giá");
        sp.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        sp.setDonViTinh("Gói");
        sp.setGiaNiemYet(new BigDecimal("1000000.00"));
        sp.setGiaSan(new BigDecimal("800000.00"));
        sp.setGiaVon(new BigDecimal("500000.00")); // Giá vốn thiết lập ban đầu bởi Giám đốc

        int id = sanPhamDAO.themSanPham(sp, true);
        assertTrue(id > 0);

        // User không có quyền giá vốn (coQuyenGiaVon = false) cập nhật tên và cố tình sửa giá vốn thành 100,000
        SanPham spUpdate = new SanPham();
        spUpdate.setId(id);
        spUpdate.setMaSanPham("SECURITY-01");
        spUpdate.setTenSanPham("Sản phẩm đã đổi tên");
        spUpdate.setLoai(LoaiSanPhamEnum.SAN_PHAM_MOT_LAN);
        spUpdate.setDonViTinh("Gói");
        spUpdate.setGiaNiemYet(new BigDecimal("1000000.00"));
        spUpdate.setGiaSan(new BigDecimal("800000.00"));
        spUpdate.setGiaVon(new BigDecimal("100000.00")); // Cố tình đổi

        boolean updateOk = sanPhamDAO.capNhatSanPham(spUpdate, false);
        assertTrue(updateOk);

        // Đọc lại từ DB với quyền Giám đốc -> Giá vốn trong DB vẫn phải là 500,000!
        SanPham spDb = sanPhamDAO.timTheoId(id, true);
        assertEquals("Sản phẩm đã đổi tên", spDb.getTenSanPham());
        assertEquals(0, new BigDecimal("500000.00").compareTo(spDb.getGiaVon()), "Giá vốn trong database không được thay đổi!");
    }
}
