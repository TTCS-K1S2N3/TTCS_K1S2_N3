package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.HopDong;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tầng DAO cho HopDongDAO với H2 in-memory.
 * Kiểm tra các tính năng của Story S3-05:
 * - Tính tổng giá trị hợp đồng của một khách hàng
 * - Tính tổng giá trị hợp đồng của cả nhóm công ty (công ty mẹ + công ty con)
 * - Loại trừ các hợp đồng đã hủy (DA_HUY)
 */
public class HopDongDAOTest {

    private static Connection h2Connection;
    private HopDongDAO hopDongDAO;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_hopdong;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS hop_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "so_hop_dong VARCHAR(80) NOT NULL UNIQUE, " +
                    "bao_gia_id BIGINT NOT NULL, " +
                    "phien_ban_bao_gia_id BIGINT NOT NULL, " +
                    "co_hoi_id BIGINT NOT NULL, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ngay_ky DATE NOT NULL, " +
                    "ngay_hieu_luc DATE NOT NULL, " +
                    "ngay_het_han DATE NULL, " +
                    "gia_tri_hop_dong DECIMAL(18,2) NOT NULL, " +
                    "dieu_khoan_thanh_toan TEXT NULL, " +
                    "trang_thai VARCHAR(30) NOT NULL DEFAULT 'DA_KY', " +
                    "hop_dong_goc_id BIGINT NULL, " +
                    "co_hoi_gia_han_id BIGINT NULL, " +
                    "created_by BIGINT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty) VALUES (1, 'KH-001', 'Tập Đoàn Viettel')");
            stmt.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty) VALUES (2, 'KH-002', 'Viettel Telecom')");
            stmt.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty) VALUES (3, 'KH-003', 'Viettel Post')");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_hopdong;MODE=MySQL;DB_CLOSE_DELAY=-1");
            } catch (Exception e) {
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
    public void resetData() throws Exception {
        hopDongDAO = new HopDongDAO();
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM hop_dong");
        }
    }

    @Test
    @DisplayName("AC2: Tính tổng giá trị hợp đồng của 1 khách hàng cụ thể")
    void testTinhTongGiaTriHopDongTheoKhachHang() throws Exception {
        HopDong hd1 = new HopDong();
        hd1.setSoHopDong("HD-2026-001");
        hd1.setKhachHangId(1L);
        hd1.setGiaTriHopDong(new BigDecimal("500000000.00")); // 500 triệu
        hd1.setTrangThai("DA_KY");
        hopDongDAO.themMoi(hd1);

        HopDong hd2 = new HopDong();
        hd2.setSoHopDong("HD-2026-002");
        hd2.setKhachHangId(1L);
        hd2.setGiaTriHopDong(new BigDecimal("300000000.00")); // 300 triệu
        hd2.setTrangThai("HIEU_LUC");
        hopDongDAO.themMoi(hd2);

        BigDecimal tong = hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(1L);
        assertEquals(0, new BigDecimal("800000000.00").compareTo(tong), "Tổng giá trị hợp đồng phải là 800 triệu");
    }

    @Test
    @DisplayName("AC2: Bỏ qua hợp đồng trạng thái DA_HUY khi tính tổng giá trị")
    void testTinhTongGiaTriHopDong_BoQuaDaHuy() throws Exception {
        HopDong hd1 = new HopDong();
        hd1.setSoHopDong("HD-VALID");
        hd1.setKhachHangId(1L);
        hd1.setGiaTriHopDong(new BigDecimal("200000000.00"));
        hd1.setTrangThai("DA_KY");
        hopDongDAO.themMoi(hd1);

        HopDong hdHuy = new HopDong();
        hdHuy.setSoHopDong("HD-CANCELLED");
        hdHuy.setKhachHangId(1L);
        hdHuy.setGiaTriHopDong(new BigDecimal("999000000.00"));
        hdHuy.setTrangThai("DA_HUY"); // Hợp đồng bị hủy
        hopDongDAO.themMoi(hdHuy);

        BigDecimal tong = hopDongDAO.tinhTongGiaTriHopDongTheoKhachHang(1L);
        assertEquals(0, new BigDecimal("200000000.00").compareTo(tong), "Hợp đồng DA_HUY không được tính");
    }

    @Test
    @DisplayName("AC2: Tính tổng giá trị hợp đồng của cả nhóm công ty (Mẹ + các Công ty con)")
    void testTinhTongGiaTriHopDongNhomCongTy() throws Exception {
        // Hợp đồng của công ty mẹ (ID 1)
        HopDong hdMe = new HopDong();
        hdMe.setSoHopDong("HD-ME-01");
        hdMe.setKhachHangId(1L);
        hdMe.setGiaTriHopDong(new BigDecimal("1000000000.00")); // 1 tỷ
        hdMe.setTrangThai("DA_KY");
        hopDongDAO.themMoi(hdMe);

        // Hợp đồng của công ty con 1 (ID 2)
        HopDong hdCon1 = new HopDong();
        hdCon1.setSoHopDong("HD-CON-01");
        hdCon1.setKhachHangId(2L);
        hdCon1.setGiaTriHopDong(new BigDecimal("500000000.00")); // 500 triệu
        hdCon1.setTrangThai("DA_KY");
        hopDongDAO.themMoi(hdCon1);

        // Hợp đồng của công ty con 2 (ID 3)
        HopDong hdCon2 = new HopDong();
        hdCon2.setSoHopDong("HD-CON-02");
        hdCon2.setKhachHangId(3L);
        hdCon2.setGiaTriHopDong(new BigDecimal("250000000.00")); // 250 triệu
        hdCon2.setTrangThai("DA_KY");
        hopDongDAO.themMoi(hdCon2);

        // Tính tổng toàn nhóm (Mẹ 1 tỷ + Con1 500tr + Con2 250tr = 1.75 tỷ)
        BigDecimal tongNhom = hopDongDAO.tinhTongGiaTriHopDongNhomCongTy(1L, Arrays.asList(2L, 3L));
        assertEquals(0, new BigDecimal("1750000000.00").compareTo(tongNhom), "Tổng giá trị nhóm phải là 1.75 tỷ");
    }
}
