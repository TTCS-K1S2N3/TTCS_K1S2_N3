package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử KhachHangDAO - Lọc đa điều kiện, tìm kiếm tên/MST/SĐT và Data Scope (Story S3-07)")
class KhachHangDAOTest {

    private static Connection h2Connection;
    private static KhachHangDAO khachHangDAO;
    private static NguoiLienHeDAO nguoiLienHeDAO;

    private static NguoiDungDTO salesA;
    private static NguoiDungDTO salesB;
    private static NguoiDungDTO teamLeadBac;

    @BeforeAll
    static void setUp() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_s3_07_kh_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:crm_s3_07_kh_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {}
            return h2Connection;
        });

        khachHangDAO = new KhachHangDAO();
        nguoiLienHeDAO = new NguoiLienHeDAO();

        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nganh_nghe (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_nganh VARCHAR(50), ten_nganh VARCHAR(150), thu_tu_hien_thi INT, hoat_dong INT, created_at TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS quy_mo_doanh_nghiep (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_quy_mo VARCHAR(50), ten_quy_mo VARCHAR(150), thu_tu_hien_thi INT, hoat_dong INT, created_at TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(150), loai_khu_vuc VARCHAR(50), khu_vuc_cha_id BIGINT, thu_tu_hien_thi INT, hoat_dong INT, created_at TIMESTAMP, updated_at TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_nhom VARCHAR(50), ten_nhom VARCHAR(150), mo_ta TEXT, khu_vuc_id BIGINT, hoat_dong INT, created_at TIMESTAMP)");
            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (id BIGINT AUTO_INCREMENT PRIMARY KEY, ho_ten VARCHAR(150), email VARCHAR(200), nhom_kinh_doanh_id BIGINT, trang_thai VARCHAR(30))");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50), ten_cong_ty VARCHAR(255), ten_chuan_hoa VARCHAR(255), ma_so_thue VARCHAR(50), " +
                    "nganh_nghe_id BIGINT, quy_mo_id BIGINT, website VARCHAR(255), website_chuan_hoa VARCHAR(255), dia_chi VARCHAR(500), " +
                    "khu_vuc_id BIGINT, nguoi_so_huu_id BIGINT NOT NULL, nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0.00, cong_ty_me_id BIGINT, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', co_rui_ro INT DEFAULT 0, rui_ro_cap_nhat_luc TIMESTAMP, " +
                    "lan_tuong_tac_cuoi TIMESTAMP, gop_vao_khach_hang_id BIGINT, mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE DEFAULT CURRENT_DATE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, ho_ten VARCHAR(150) NOT NULL, chuc_danh VARCHAR(150), " +
                    "email VARCHAR(255), so_dien_thoai VARCHAR(20), vai_tro_quyet_dinh VARCHAR(50), " +
                    "la_dau_moi_chinh INT DEFAULT 0, trang_thai VARCHAR(30) DEFAULT 'DANG_HOAT_DONG', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Dữ liệu mẫu danh mục
            st.execute("INSERT INTO nganh_nghe (id, ma_nganh, ten_nganh) VALUES (1, 'CNTT', 'Công nghệ thông tin'), (2, 'BAN_LE', 'Bán lẻ & Thương mại')");
            st.execute("INSERT INTO quy_mo_doanh_nghiep (id, ma_quy_mo, ten_quy_mo) VALUES (1, 'NHO', 'Dưới 50 nhân sự'), (2, 'LON', 'Trên 500 nhân sự')");
            st.execute("INSERT INTO khu_vuc_dia_ly (id, ma_khu_vuc, ten_khu_vuc) VALUES (1, 'MB', 'Miền Bắc'), (2, 'MN', 'Miền Nam')");
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'NHOM_BAC', 'Nhóm Miền Bắc'), (2, 'NHOM_NAM', 'Nhóm Miền Nam')");

            // Người dùng
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id, trang_thai) VALUES " +
                    "(101, 'Nguyễn Văn A', 'sales.a@crm.vn', 1, 'HOAT_DONG'), " +
                    "(102, 'Trần Văn B', 'sales.b@crm.vn', 1, 'HOAT_DONG'), " +
                    "(100, 'Lê Thị Lead', 'lead.bac@crm.vn', 1, 'HOAT_DONG')");

            // Khách hàng mẫu
            // KH 1 của A: CNTT, Lớn, Miền Bắc, Tiềm năng, MST 010111222, SĐT 0912345678
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, trang_thai) " +
                    "VALUES (1, 'KH-001', 'Tập đoàn FPT', 'tap doan fpt', '010111222', 1, 2, 1, 101, 1, 'TIEM_NANG')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, so_dien_thoai, la_dau_moi_chinh) VALUES (1, 1, 'Trương Gia Bình', '0912345678', 1)");

            // KH 2 của A: Bán lẻ, Nhỏ, Miền Bắc, Đang giao dịch, MST 010333444, SĐT 0988776655
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, trang_thai) " +
                    "VALUES (2, 'KH-002', 'Chuỗi Cửa Hàng WinMart', 'chuoi cua hang winmart', '010333444', 2, 1, 1, 101, 1, 'DANG_GIAO_DICH')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, so_dien_thoai, la_dau_moi_chinh) VALUES (2, 2, 'Nguyễn Thị Thu', '0988776655', 1)");

            // KH 3 của B: CNTT, Lớn, Miền Bắc, Khách hàng, MST 010555666, SĐT 0905123456
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, trang_thai) " +
                    "VALUES (3, 'KH-003', 'Tập đoàn Viettel', 'tap doan viettel', '010555666', 1, 2, 1, 102, 1, 'KHACH_HANG')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, so_dien_thoai, la_dau_moi_chinh) VALUES (3, 3, 'Tào Đức Thắng', '0905123456', 1)");
        }

        // Tạo UserDTO cho các test
        NguoiDung uA = new NguoiDung(101L, "Nguyễn Văn A", "sales.a@crm.vn");
        uA.setNhomKinhDoanhId(1);
        uA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));
        salesA = NguoiDungDTO.tuNguoiDung(uA);

        NguoiDung uB = new NguoiDung(102L, "Trần Văn B", "sales.b@crm.vn");
        uB.setNhomKinhDoanhId(1);
        uB.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));
        salesB = NguoiDungDTO.tuNguoiDung(uB);

        NguoiDung uLead = new NguoiDung(100L, "Lê Thị Lead", "lead.bac@crm.vn");
        uLead.setNhomKinhDoanhId(1);
        uLead.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.TEAM_LEAD, PhamViDuLieu.NHOM)));
        teamLeadBac = NguoiDungDTO.tuNguoiDung(uLead);
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @Test
    @DisplayName("AC1: Lọc theo trạng thái (TIEM_NANG)")
    void testLoc_TheoTrangThai() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTrangThai("TIEM_NANG");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC1: Lọc theo ngành nghề (Bán lẻ ID=2)")
    void testLoc_TheoNganhNghe() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setNganhNgheId(2L);

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Chuỗi Cửa Hàng WinMart", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC1: Lọc theo quy mô và khu vực")
    void testLoc_TheoQuyMoVaKhuVuc() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setQuyMoId(2L);   // Lớn
        boLoc.setKhuVucId(1L);  // Miền Bắc

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC2: Tìm theo tên công ty")
    void testTim_TheoTenCongTy() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTenCongTy("WinMart");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Chuỗi Cửa Hàng WinMart", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC2: Tìm theo mã số thuế")
    void testTim_TheoMaSoThue() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setMaSoThue("010111222");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC2: Tìm theo số điện thoại người liên hệ")
    void testTim_TheoSoDienThoaiLienHe() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setSoDienThoai("0988776655");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Chuỗi Cửa Hàng WinMart", ketQua.get(0).getTenCongTy());
        assertEquals("Nguyễn Thị Thu", ketQua.get(0).getTenNguoiLienHeChinh());
        assertEquals("0988776655", ketQua.get(0).getSoDienThoaiLienHe());
    }

    @Test
    @DisplayName("AC2: Tìm kiếm tổng hợp theo từ khóa (khớp SĐT)")
    void testTim_TuKhoaTongHop() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTuKhoa("0912345678");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("Data Scope & AC1: Sales Rep A (CA_NHAN) không thấy khách của Sales Rep B")
    void testDataScope_SalesRepA_KhongThayKhachCuaB() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setPhamVi(PhamViDuLieu.CA_NHAN);

        List<KhachHang> dsA = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(2, dsA.size());
        for (KhachHang kh : dsA) {
            assertEquals(101L, kh.getNguoiSoHuuId(), "Chỉ thấy khách do A sở hữu");
        }
    }

    @Test
    @DisplayName("Data Scope & AC1: Trưởng nhóm (NHOM) thấy cả khách của A và B trong nhóm Miền Bắc")
    void testDataScope_TeamLead_ThayToanBoNhom() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setPhamVi(PhamViDuLieu.NHOM);

        List<KhachHang> dsLead = khachHangDAO.timKiemVaLoc(teamLeadBac, boLoc);
        assertEquals(3, dsLead.size());
    }

    @Test
    @DisplayName("Đếm số lượng khách hàng theo bộ lọc")
    void testDemSoLuong() {
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTrangThai("TIEM_NANG");

        int count = khachHangDAO.demSoLuong(salesA, boLoc);
        assertEquals(1, count);
    }
}
