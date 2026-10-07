package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.model.KhachHang;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tầng DAO cho KhachHangDAO với H2 in-memory.
 * Kiểm tra các tính năng của Story S3-05:
 * - Gắn khách hàng làm công ty con
 * - Gỡ bỏ công ty mẹ
 * - Phát hiện vòng lặp chu kỳ mẹ con
 * - Lấy danh sách công ty con
 * Kiểm tra các tính năng của Story S3-08:
 * - Thêm khách hàng
 * - Cập nhật và kiểm tra cờ rủi ro rời bỏ
 * - Lấy danh sách khách hàng rủi ro
 */
public class KhachHangDAOTest {

    private static Connection h2Connection;
    private KhachHangDAO khachHangDAO;
    private KhachHangDAO dao;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_test_khachhang;MODE=MySQL;DB_CLOSE_DELAY=-1");

        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL UNIQUE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ten_nhom VARCHAR(255) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NULL, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255) NULL, " +
                    "ma_so_thue VARCHAR(50) NULL, " +
                    "nganh_nghe_id BIGINT NULL, " +
                    "quy_mo_id BIGINT NULL, " +
                    "website VARCHAR(255) NULL, " +
                    "website_chuan_hoa VARCHAR(255) NULL, " +
                    "dia_chi VARCHAR(500) NULL, " +
                    "khu_vuc_id BIGINT NULL, " +
                    "nguoi_so_huu_id BIGINT NOT NULL, " +
                    "nhom_kinh_doanh_id BIGINT NULL, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, " +
                    "cong_ty_me_id BIGINT NULL, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro TINYINT(1) DEFAULT 0, " +
                    "rui_ro_cap_nhat_luc TIMESTAMP NULL, " +
                    "lan_tuong_tac_cuoi TIMESTAMP NULL, " +
                    "gop_vao_khach_hang_id BIGINT NULL, " +
                    "mo_ta_chi_tiet TEXT NULL, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES (1, 'Nguyễn Văn Sales', 'sales@crm.vn')");
            stmt.execute("INSERT INTO nhom_kinh_doanh (id, ten_nhom) VALUES (1, 'Nhóm Miền Bắc')");
        }

        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:crm_test_khachhang;MODE=MySQL;DB_CLOSE_DELAY=-1");
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
        khachHangDAO = new KhachHangDAO();
        dao = khachHangDAO;
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("DELETE FROM khach_hang");
        }
    }

    @Test
    @DisplayName("AC1: Gắn một khách hàng làm công ty con của khách hàng khác thành công")
    void testGanCongTyMe_ThanhCong() throws Exception {
        KhachHang me = new KhachHang();
        me.setTenCongTy("Tập Đoàn VinGroup");
        me.setMaKhachHang("KH-VIC");
        me.setNguoiSoHuuId(1L);
        me.setNhomKinhDoanhId(1L);
        Long meId = khachHangDAO.themMoi(me);

        KhachHang con = new KhachHang();
        con.setTenCongTy("Công ty CP VinFast");
        con.setMaKhachHang("KH-VFS");
        con.setNguoiSoHuuId(1L);
        con.setNhomKinhDoanhId(1L);
        Long conId = khachHangDAO.themMoi(con);

        boolean ketQua = khachHangDAO.ganCongTyMe(conId, meId);
        assertTrue(ketQua, "Gắn công ty con phải thành công");

        KhachHang conSauKhiGan = khachHangDAO.timTheoId(conId);
        assertNotNull(conSauKhiGan);
        assertEquals(meId, conSauKhiGan.getCongTyMeId());
        assertEquals("Tập Đoàn VinGroup", conSauKhiGan.getTenCongTyMe());
    }

    @Test
    @DisplayName("AC1: Lấy danh sách công ty con của một công ty mẹ")
    void testLayDanhSachCongTyCon() throws Exception {
        KhachHang me = new KhachHang();
        me.setTenCongTy("Tập Đoàn FPT");
        me.setMaKhachHang("KH-FPT");
        me.setNguoiSoHuuId(1L);
        Long meId = khachHangDAO.themMoi(me);

        KhachHang con1 = new KhachHang();
        con1.setTenCongTy("FPT Software");
        con1.setMaKhachHang("KH-FSOFT");
        con1.setNguoiSoHuuId(1L);
        con1.setCongTyMeId(meId);
        khachHangDAO.themMoi(con1);

        KhachHang con2 = new KhachHang();
        con2.setTenCongTy("FPT Telecom");
        con2.setMaKhachHang("KH-FTEL");
        con2.setNguoiSoHuuId(1L);
        con2.setCongTyMeId(meId);
        khachHangDAO.themMoi(con2);

        List<KhachHang> dsCon = khachHangDAO.layDanhSachCongTyCon(meId);
        assertEquals(2, dsCon.size(), "Công ty mẹ FPT phải có 2 công ty con");
        assertEquals("FPT Software", dsCon.get(0).getTenCongTy());
        assertEquals("FPT Telecom", dsCon.get(1).getTenCongTy());
    }

    @Test
    @DisplayName("AC1: Gỡ bỏ quan hệ công ty con (set cong_ty_me_id = NULL)")
    void testGoCongTyMe_ThanhCong() throws Exception {
        KhachHang me = new KhachHang();
        me.setTenCongTy("Tập Đoàn Masan");
        me.setNguoiSoHuuId(1L);
        Long meId = khachHangDAO.themMoi(me);

        KhachHang con = new KhachHang();
        con.setTenCongTy("Masan Consumer");
        con.setNguoiSoHuuId(1L);
        con.setCongTyMeId(meId);
        Long conId = khachHangDAO.themMoi(con);

        // Gỡ bỏ quan hệ
        boolean ketQua = khachHangDAO.ganCongTyMe(conId, null);
        assertTrue(ketQua);

        KhachHang conSauGo = khachHangDAO.timTheoId(conId);
        assertNull(conSauGo.getCongTyMeId());
    }

    @Test
    @DisplayName("AC1 Validation: Phát hiện vòng lặp tự làm mẹ của chính mình")
    void testKiemTraVongLap_ChinhMinh() throws Exception {
        boolean vongLap = khachHangDAO.kiemTraVongLapCongTyMeCon(10L, 10L);
        assertTrue(vongLap, "Tự gán chính mình làm mẹ phải bị coi là vòng lặp");
    }

    @Test
    @DisplayName("AC1 Validation: Phát hiện vòng lặp trực tiếp 2 chiều (A -> B -> A)")
    void testKiemTraVongLap_HaiChieu() throws Exception {
        KhachHang ctyA = new KhachHang();
        ctyA.setTenCongTy("Công ty A");
        ctyA.setNguoiSoHuuId(1L);
        Long idA = khachHangDAO.themMoi(ctyA);

        KhachHang ctyB = new KhachHang();
        ctyB.setTenCongTy("Công ty B");
        ctyB.setNguoiSoHuuId(1L);
        ctyB.setCongTyMeId(idA); // B là con của A
        Long idB = khachHangDAO.themMoi(ctyB);

        // Kiểm tra xem gán B làm mẹ của A có bị phát hiện vòng lặp không
        boolean vongLap = khachHangDAO.kiemTraVongLapCongTyMeCon(idA, idB);
        assertTrue(vongLap, "A là mẹ B thì không thể chọn B làm mẹ A");
    }

    @Test
    @DisplayName("AC1 Validation: Phát hiện vòng lặp nhiều cấp (A -> B -> C -> A)")
    void testKiemTraVongLap_NhieuCap() throws Exception {
        KhachHang ctyA = new KhachHang();
        ctyA.setTenCongTy("Công ty A");
        ctyA.setNguoiSoHuuId(1L);
        Long idA = khachHangDAO.themMoi(ctyA);

        KhachHang ctyB = new KhachHang();
        ctyB.setTenCongTy("Công ty B");
        ctyB.setNguoiSoHuuId(1L);
        ctyB.setCongTyMeId(idA); // B là con của A
        Long idB = khachHangDAO.themMoi(ctyB);

        KhachHang ctyC = new KhachHang();
        ctyC.setTenCongTy("Công ty C");
        ctyC.setNguoiSoHuuId(1L);
        ctyC.setCongTyMeId(idB); // C là con của B
        Long idC = khachHangDAO.themMoi(ctyC);

        // Gán C làm mẹ của A -> A -> B -> C -> A (vòng lặp!)
        boolean vongLap = khachHangDAO.kiemTraVongLapCongTyMeCon(idA, idC);
        assertTrue(vongLap, "Phải phát hiện vòng lặp nhiều cấp");
    }

    @Test
    @DisplayName("S3-08: Thêm mới khách hàng và cập nhật cờ rủi ro rời bỏ")
    public void testThemVaCapNhatCoRuiRo() {
        KhachHang kh = new KhachHang("Tập đoàn Công nghệ Demo", 1L);
        kh.setMaKhachHang("KH-DEMO-01");
        kh.setDoanhThuUocTinh(new BigDecimal("500000000.00"));
        kh.setNhomKinhDoanhId(1L);

        Long id = dao.themKhachHang(kh);
        assertNotNull(id);
        assertTrue(id > 0);

        KhachHang timDuoc = dao.timTheoId(id);
        assertNotNull(timDuoc);
        assertEquals("Tập đoàn Công nghệ Demo", timDuoc.getTenCongTy());
        assertEquals("Nguyễn Văn Sales", timDuoc.getTenNguoiSoHuu());
        assertFalse(timDuoc.isCoRuiRo());

        // Gắn cờ rủi ro rời bỏ
        boolean updated = dao.capNhatCoRuiRo(id, true);
        assertTrue(updated);
        assertTrue(dao.kiemTraCoRuiRo(id));

        List<KhachHang> dsRuiRo = dao.layDanhSachKhachHangRuiRo(1L);
        assertEquals(1, dsRuiRo.size());
        assertEquals(id, dsRuiRo.get(0).getId());

        // Gỡ cờ rủi ro
        boolean unflagged = dao.capNhatCoRuiRo(id, false);
        assertTrue(unflagged);
        assertFalse(dao.kiemTraCoRuiRo(id));
    }
}
