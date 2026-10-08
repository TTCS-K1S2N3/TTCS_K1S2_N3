package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import org.junit.jupiter.api.DisplayName;
import vn.nhom10.crm.dto.BoLocKhachHangDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiKhachHangEnum;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp toàn diện cho KhachHangDAO:
 * - Story S3-01: AC1 đầy đủ trường, AC2 duy nhất MST, AC3 4 trạng thái chuẩn, AC4 Data Scope.
 * - Story S3-05: Quan hệ công ty mẹ - con và kiểm tra chống vòng lặp.
 * - Story S3-08: Gắn và gỡ cờ rủi ro rời bỏ.
 */
public class KhachHangDAOTest {

    private static final String H2_URL = "jdbc:h2:mem:crm_test_khachhang;MODE=MySQL;DB_CLOSE_DELAY=-1";
    private static Connection rootConnection;
    private KhachHangDAO khachHangDAO;
    private KhachHangDAO dao;

    private static NguoiDungDTO salesA;
    private static NguoiDungDTO salesB;
    private static NguoiDungDTO teamLeadBac;


    @BeforeAll
    public static void setUpDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        rootConnection = DriverManager.getConnection(H2_URL, "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection(H2_URL, "sa", "");
            } catch (SQLException e) {
                return null;
            }
        });

        try (Statement st = rootConnection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS nganh_nghe (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nganh VARCHAR(50), ten_nganh VARCHAR(150), thu_tu_hien_thi INT, hoat_dong TINYINT, created_at DATETIME)");

            st.execute("CREATE TABLE IF NOT EXISTS quy_mo_doanh_nghiep (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_quy_mo VARCHAR(50), ten_quy_mo VARCHAR(150), thu_tu_hien_thi INT, hoat_dong TINYINT, created_at DATETIME)");

            st.execute("CREATE TABLE IF NOT EXISTS khu_vuc_dia_ly (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(150))");

            st.execute("CREATE TABLE IF NOT EXISTS nhom_kinh_doanh (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50), ten_nhom VARCHAR(150), nhom_cha_id BIGINT)");

            st.execute("CREATE TABLE IF NOT EXISTS nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), email VARCHAR(200) UNIQUE, nhom_kinh_doanh_id BIGINT)");


            st.execute("CREATE TABLE IF NOT EXISTS nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT, " +
                    "ho_ten VARCHAR(150), " +
                    "so_dien_thoai VARCHAR(50), " +
                    "email VARCHAR(150), " +
                    "chuc_danh VARCHAR(150), " +
                    "vai_tro_quyet_dinh VARCHAR(50), " +
                    "la_dau_moi_chinh TINYINT DEFAULT 0, " +
                    "trang_thai VARCHAR(50) DEFAULT 'DANG_HOAT_DONG', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS khach_hang (" +
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

            // Nạp dữ liệu danh mục & người dùng dùng chung
            st.execute("MERGE INTO nganh_nghe (id, ma_nganh, ten_nganh, thu_tu_hien_thi, hoat_dong) KEY(id) " +
                    "VALUES (1, 'CNTT', 'Công nghệ thông tin', 1, 1)");

            st.execute("MERGE INTO quy_mo_doanh_nghiep (id, ma_quy_mo, ten_quy_mo, thu_tu_hien_thi, hoat_dong) KEY(id) " +
                    "VALUES (1, '100_500', '100 - 500 nhân sự', 1, 1), (2, 'LON', 'Trên 500 người', 2, 1)");

            st.execute("MERGE INTO khu_vuc_dia_ly (id, ma_khu_vuc, ten_khu_vuc) KEY(id) " +
                    "VALUES (1, 'MB', 'Miền Bắc'), (2, 'MN', 'Miền Nam')");

            st.execute("MERGE INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom, nhom_cha_id) KEY(id) " +
                    "VALUES (1, 'NHOM_BAC', 'Nhóm Miền Bắc', NULL)");
            st.execute("MERGE INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom, nhom_cha_id) KEY(id) " +
                    "VALUES (2, 'NHOM_NAM', 'Nhóm Miền Nam', NULL)");

            st.execute("MERGE INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id) KEY(id) " +
                    "VALUES (1, 'Nguyễn Văn Sales', 'sales@crm.vn', 1), " +
                    "(100, 'Lê Thị Lead', 'lead.bac@crm.vn', 1), " +
                    "(101, 'Sales A', 'sales.a@crm.vn', 1), " +
                    "(102, 'Sales B', 'sales.b@crm.vn', 2), " +
                    "(201, 'Lead Bac Old', 'lead.bac.old@crm.vn', 1)");
        }

        NguoiDung uA = new NguoiDung(101L, "Nguyễn Văn A", "sales.a@crm.vn");
        uA.setNhomKinhDoanhId(1);
        uA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));
        salesA = NguoiDungDTO.tuNguoiDung(uA);

        NguoiDung uB = new NguoiDung(102L, "Trần Văn B", "sales.b@crm.vn");
        uB.setNhomKinhDoanhId(2);
        uB.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));
        salesB = NguoiDungDTO.tuNguoiDung(uB);

        NguoiDung uLead = new NguoiDung(100L, "Lê Thị Lead", "lead.bac@crm.vn");
        uLead.setNhomKinhDoanhId(1);
        uLead.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.TEAM_LEAD, PhamViDuLieu.NHOM)));
        teamLeadBac = NguoiDungDTO.tuNguoiDung(uLead);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (rootConnection != null && !rootConnection.isClosed()) {
            rootConnection.close();
        }
    }

    @BeforeEach
    public void resetData() throws Exception {
        khachHangDAO = new KhachHangDAO();
        dao = khachHangDAO;
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM khach_hang");
        }
    }

    // =========================================================================
    // TESTS CHO STORY S3-05: CÔNG TY MẸ - CON
    // =========================================================================

    @Test
    void testGanCongTyMe_ThanhCong() throws Exception {
        KhachHang me = new KhachHang("Tập đoàn Alpha", 1L);
        KhachHang con = new KhachHang("Công ty TNHH Alpha Beta", 1L);

        Long meId = khachHangDAO.themMoi(me);
        Long conId = khachHangDAO.themMoi(con);

        boolean ganThanhCong = khachHangDAO.ganCongTyMe(conId, meId);
        assertTrue(ganThanhCong, "Gán công ty mẹ phải thành công");

        KhachHang conSauKhiGan = khachHangDAO.timTheoId(conId);
        assertNotNull(conSauKhiGan);
        assertEquals(meId, conSauKhiGan.getCongTyMeId());
        assertEquals("Tập đoàn Alpha", conSauKhiGan.getTenCongTyMe());
    }

    @Test
    void testLayDanhSachCongTyCon() throws Exception {
        KhachHang me = new KhachHang("Tổng công ty Hàng Hải", 1L);
        Long meId = khachHangDAO.themMoi(me);

        KhachHang con1 = new KhachHang("Chi nhánh Hải Phòng", 1L);
        con1.setCongTyMeId(meId);
        khachHangDAO.themMoi(con1);

        KhachHang con2 = new KhachHang("Chi nhánh Đà Nẵng", 1L);
        con2.setCongTyMeId(meId);
        khachHangDAO.themMoi(con2);

        List<KhachHang> dsCon = khachHangDAO.layDanhSachCongTyCon(meId);
        assertEquals(2, dsCon.size(), "Công ty mẹ phải có 2 công ty con");
    }

    @Test
    void testGoCongTyMe_ThanhCong() throws Exception {
        KhachHang me = new KhachHang("Tập đoàn Viễn Thông", 1L);
        Long meId = khachHangDAO.themMoi(me);

        KhachHang con = new KhachHang("Công ty Phần mềm Viễn Thông", 1L);
        con.setCongTyMeId(meId);
        Long conId = khachHangDAO.themMoi(con);

        boolean goThanhCong = khachHangDAO.ganCongTyMe(conId, null);
        assertTrue(goThanhCong, "Gỡ công ty mẹ phải thành công");

        KhachHang conSauGo = khachHangDAO.timTheoId(conId);
        assertNotNull(conSauGo);
        assertNull(conSauGo.getCongTyMeId(), "Công ty con sau khi gỡ không được còn công ty mẹ");
    }

    @Test
    void testKiemTraVongLap_ChinhMinh() throws Exception {
        boolean vongLap = khachHangDAO.kiemTraVongLapCongTyMeCon(10L, 10L);
        assertTrue(vongLap, "Tự gán chính mình làm công ty mẹ phải phát hiện vòng lặp");
    }

    @Test
    void testKiemTraVongLap_HaiChieu() throws Exception {
        KhachHang ctyA = new KhachHang("Công ty A", 1L);
        Long idA = khachHangDAO.themMoi(ctyA);

        KhachHang ctyB = new KhachHang("Công ty B", 1L);
        ctyB.setCongTyMeId(idA);
        Long idB = khachHangDAO.themMoi(ctyB);

        boolean vongLap = khachHangDAO.kiemTraVongLapCongTyMeCon(idA, idB);
        assertTrue(vongLap, "Gán B làm mẹ của A trong khi A đang là mẹ của B phải phát hiện vòng lặp");
    }

    @Test
    void testKiemTraVongLap_NhieuCap() throws Exception {
        KhachHang ctyA = new KhachHang("Công ty A (Ông nội)", 1L);
        Long idA = khachHangDAO.themMoi(ctyA);

        KhachHang ctyB = new KhachHang("Công ty B (Cha)", 1L);
        ctyB.setCongTyMeId(idA);
        Long idB = khachHangDAO.themMoi(ctyB);

        KhachHang ctyC = new KhachHang("Công ty C (Con)", 1L);
        ctyC.setCongTyMeId(idB);
        Long idC = khachHangDAO.themMoi(ctyC);

        boolean vongLap = khachHangDAO.kiemTraVongLapCongTyMeCon(idA, idC);
        assertTrue(vongLap, "Gán C làm mẹ của A phải phát hiện vòng lặp chu kỳ 3 cấp");
    }

    // =========================================================================
    // TESTS CHO STORY S3-08: CỜ RỦI RO RỜI BỎ
    // =========================================================================

    @Test
    public void testThemVaCapNhatCoRuiRo() throws Exception {
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

        boolean updated = dao.capNhatCoRuiRo(id, true);
        assertTrue(updated);
        assertTrue(dao.kiemTraCoRuiRo(id));

        List<KhachHang> dsRuiRo = dao.layDanhSachKhachHangRuiRo(1L);
        assertEquals(1, dsRuiRo.size());
        assertEquals(id, dsRuiRo.get(0).getId());

        boolean unflagged = dao.capNhatCoRuiRo(id, false);
        assertTrue(unflagged);
        assertFalse(dao.kiemTraCoRuiRo(id));
    }

    // =========================================================================
    // TESTS CHO STORY S3-01: HỒ SƠ KHÁCH HÀNG DOANH NGHIỆP
    // =========================================================================

    @Test
    void testThemVaLayChiTiet_DayDuTruongAC1() throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy("Công ty Cổ phần Giải pháp Phần mềm ABC");
        kh.setMaSoThue("0109988776");
        kh.setNganhNgheId(1L);
        kh.setQuyMoId(1L);
        kh.setWebsite("https://abcsoftware.vn");
        kh.setDiaChi("Tầng 5, Tòa nhà FPT, Cầu Giấy, Hà Nội");
        kh.setNguoiSoHuuId(101L);
        kh.setNhomKinhDoanhId(1L);
        kh.setDoanhThuUocTinh(new BigDecimal("500000000.00"));
        kh.setTrangThai(TrangThaiKhachHangEnum.TIEM_NANG.getMa());
        kh.setMoTaChiTiet("Khách hàng tiềm năng khối Enterprise");

        Long id = dao.themKhachHang(kh);
        assertNotNull(id, "ID khách hàng tạo mới phải khác null");

        KhachHang timThay = dao.timTheoId(id);
        assertNotNull(timThay);
        assertEquals("Công ty Cổ phần Giải pháp Phần mềm ABC", timThay.getTenCongTy());
        assertEquals("0109988776", timThay.getMaSoThue());
        assertEquals(1L, timThay.getNganhNgheId());
        assertEquals("Công nghệ thông tin", timThay.getTenNganhNghe());
        assertEquals(1L, timThay.getQuyMoId());
        assertEquals("100 - 500 nhân sự", timThay.getTenQuyMo());
        assertEquals("https://abcsoftware.vn", timThay.getWebsite());
        assertEquals("Tầng 5, Tòa nhà FPT, Cầu Giấy, Hà Nội", timThay.getDiaChi());
        assertEquals(101L, timThay.getNguoiSoHuuId());
        assertEquals("Sales A", timThay.getTenNguoiSoHuu());
        assertEquals(1L, timThay.getNhomKinhDoanhId());
        assertEquals("Nhóm Miền Bắc", timThay.getTenNhomKinhDoanh());
        assertNotNull(timThay.getMaKhachHang(), "Mã KH phải được tự động sinh");
    }

    @Test
    void testKiemTraTrungMaSoThue_PhatHienTrung() throws SQLException {
        KhachHang kh1 = new KhachHang();
        kh1.setTenCongTy("Công ty A");
        kh1.setMaSoThue("0101234567");
        kh1.setNguoiSoHuuId(101L);
        Long id1 = dao.themKhachHang(kh1);

        assertTrue(dao.kiemTraTrungMaSoThue("0101234567", null), "MST đã có phải báo trùng");
        assertFalse(dao.kiemTraTrungMaSoThue("0101234567", id1), "Chính bản ghi đó cập nhật MST của nó thì không báo trùng");
        assertFalse(dao.kiemTraTrungMaSoThue("0999999999", null), "MST chưa có không được báo trùng");
    }

    @Test
    void testMaSoThueRong_KhongViPhamUnique() throws SQLException {
        KhachHang kh1 = new KhachHang();
        kh1.setTenCongTy("Công ty Không MST 1");
        kh1.setMaSoThue(null);
        kh1.setNguoiSoHuuId(101L);
        Long id1 = dao.themKhachHang(kh1);

        KhachHang kh2 = new KhachHang();
        kh2.setTenCongTy("Công ty Không MST 2");
        kh2.setMaSoThue("");
        kh2.setNguoiSoHuuId(101L);
        Long id2 = dao.themKhachHang(kh2);

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    void testTrangThaiKhachHang_BonTrangThaiChuan() throws SQLException {
        taoKhachHangMau("Khách 1", "010001", TrangThaiKhachHangEnum.TIEM_NANG.getMa(), 101L, 1L);
        taoKhachHangMau("Khách 2", "010002", TrangThaiKhachHangEnum.DANG_GIAO_DICH.getMa(), 101L, 1L);
        taoKhachHangMau("Khách 3", "010003", TrangThaiKhachHangEnum.KHACH_HANG.getMa(), 101L, 1L);
        taoKhachHangMau("Khách 4", "010004", TrangThaiKhachHangEnum.NGUNG_HOP_TAC.getMa(), 101L, 1L);

        List<KhachHang> dsTiemNang = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "TIEM_NANG", null, null, 0, 10);
        assertEquals(1, dsTiemNang.size());
        assertEquals("Khách 1", dsTiemNang.get(0).getTenCongTy());

        List<KhachHang> dsDangGD = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "Đang giao dịch", null, null, 0, 10);
        assertEquals(1, dsDangGD.size());
        assertEquals("Khách 2", dsDangGD.get(0).getTenCongTy());

        List<KhachHang> dsKhachHang = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "Khách hàng", null, null, 0, 10);
        assertEquals(1, dsKhachHang.size());
        assertEquals("Khách 3", dsKhachHang.get(0).getTenCongTy());

        List<KhachHang> dsNgungHT = dao.layDanhSach(101L, null, PhamViDuLieu.TOAN_BO, null, "Ngừng hợp tác", null, null, 0, 10);
        assertEquals(1, dsNgungHT.size());
        assertEquals("Khách 4", dsNgungHT.get(0).getTenCongTy());
    }

    @Test
    void testDataScope_NhanVienVaTruongNhom() throws SQLException {
        taoKhachHangMau("Khách của Sales A", "011001", "TIEM_NANG", 101L, 1L);
        taoKhachHangMau("Khách của Sales B", "011002", "TIEM_NANG", 102L, 2L);

        // Sales A chỉ xem CA_NHAN -> chỉ thấy 1 khách
        List<KhachHang> dsA = dao.layDanhSach(101L, Collections.singleton(1L), PhamViDuLieu.CA_NHAN, null, null, null, null, 0, 10);
        assertEquals(1, dsA.size());
        assertEquals("Khách của Sales A", dsA.get(0).getTenCongTy());

        // Sales B xem CA_NHAN -> chỉ thấy khách của B
        List<KhachHang> dsB = dao.layDanhSach(102L, Collections.singleton(2L), PhamViDuLieu.CA_NHAN, null, null, null, null, 0, 10);
        assertEquals(1, dsB.size());
        assertEquals("Khách của Sales B", dsB.get(0).getTenCongTy());

        // Trưởng nhóm Miền Bắc xem NHOM -> thấy khách của A (thuộc nhóm 1), không thấy B (nhóm 2)
        List<KhachHang> dsLeadBac = dao.layDanhSach(201L, Collections.singleton(1L), PhamViDuLieu.NHOM, null, null, null, null, 0, 10);
        assertEquals(1, dsLeadBac.size());
        assertEquals("Khách của Sales A", dsLeadBac.get(0).getTenCongTy());

        // Admin xem TOAN_BO -> thấy cả 2
        List<KhachHang> dsToanBo = dao.layDanhSach(1L, null, PhamViDuLieu.TOAN_BO, null, null, null, null, 0, 10);
        assertEquals(2, dsToanBo.size());
    }

    private void taoKhachHangMau(String ten, String mst, String trangThai, Long nguoiSoHuuId, Long nhomId) throws SQLException {
        KhachHang kh = new KhachHang();
        kh.setTenCongTy(ten);
        kh.setMaSoThue(mst);
        kh.setTrangThai(trangThai);
        kh.setNguoiSoHuuId(nguoiSoHuuId);
        kh.setNhomKinhDoanhId(nhomId);
        dao.themKhachHang(kh);
    }


    // =========================================================================
    // TESTS CHO STORY S3-07: TÌM KIẾM VÀ LỌC KHÁCH HÀNG
    // =========================================================================

    private void napDuLieuMauS307() throws Exception {
        try (Statement st = rootConnection.createStatement()) {
            st.execute("DELETE FROM nguoi_lien_he");
            st.execute("DELETE FROM khach_hang");

            st.execute("MERGE INTO nganh_nghe (id, ma_nganh, ten_nganh, thu_tu_hien_thi, hoat_dong) KEY(id) VALUES (1, 'CNTT', 'Công nghệ thông tin', 1, 1), (2, 'BAN_LE', 'Bán lẻ', 2, 1)");
            st.execute("MERGE INTO quy_mo_doanh_nghiep (id, ma_quy_mo, ten_quy_mo, thu_tu_hien_thi, hoat_dong) KEY(id) VALUES (1, '100_500', '100 - 500 nhân sự', 1, 1), (2, 'LON', 'Trên 500 người', 2, 1)");
            st.execute("MERGE INTO khu_vuc_dia_ly (id, ma_khu_vuc, ten_khu_vuc) KEY(id) VALUES (1, 'MB', 'Miền Bắc'), (2, 'MN', 'Miền Nam')");
            st.execute("MERGE INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) KEY(id) VALUES (1, 'NHOM_BAC', 'Nhóm Miền Bắc'), (2, 'NHOM_NAM', 'Nhóm Miền Nam')");

            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, trang_thai) " +
                    "VALUES (1, 'KH-001', 'Tập đoàn FPT', 'tap doan fpt', '010111222', 1, 2, 1, 101, 1, 'TIEM_NANG')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, so_dien_thoai, la_dau_moi_chinh) VALUES (1, 1, 'Trương Gia Bình', '0912345678', 1)");

            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, trang_thai) " +
                    "VALUES (2, 'KH-002', 'Chuỗi Cửa Hàng WinMart', 'chuoi cua hang winmart', '010333444', 2, 1, 1, 101, 1, 'DANG_GIAO_DICH')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, so_dien_thoai, la_dau_moi_chinh) VALUES (2, 2, 'Nguyễn Thị Thu', '0988776655', 1)");

            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, ten_chuan_hoa, ma_so_thue, nganh_nghe_id, quy_mo_id, khu_vuc_id, nguoi_so_huu_id, nhom_kinh_doanh_id, trang_thai) " +
                    "VALUES (3, 'KH-003', 'Tập đoàn Viettel', 'tap doan viettel', '010555666', 1, 2, 1, 102, 1, 'KHACH_HANG')");
            st.execute("INSERT INTO nguoi_lien_he (id, khach_hang_id, ho_ten, so_dien_thoai, la_dau_moi_chinh) VALUES (3, 3, 'Tào Đức Thắng', '0905123456', 1)");
        }
    }

    @Test
    @DisplayName("AC1: Lọc theo trạng thái (TIEM_NANG)")
    void testLoc_TheoTrangThai() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTrangThai("TIEM_NANG");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC1: Lọc theo ngành nghề (Bán lẻ ID=2)")
    void testLoc_TheoNganhNghe() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setNganhNgheId(2L);

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Chuỗi Cửa Hàng WinMart", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC1: Lọc theo quy mô và khu vực")
    void testLoc_TheoQuyMoVaKhuVuc() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setQuyMoId(2L);   // Lớn
        boLoc.setKhuVucId(1L);  // Miền Bắc

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC2: Tìm theo tên công ty")
    void testTim_TheoTenCongTy() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTenCongTy("WinMart");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Chuỗi Cửa Hàng WinMart", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC2: Tìm theo mã số thuế")
    void testTim_TheoMaSoThue() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setMaSoThue("010111222");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("AC2: Tìm theo số điện thoại người liên hệ")
    void testTim_TheoSoDienThoaiLienHe() throws Exception {
        napDuLieuMauS307();
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
    void testTim_TuKhoaTongHop() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTuKhoa("0912345678");

        List<KhachHang> ketQua = khachHangDAO.timKiemVaLoc(salesA, boLoc);
        assertEquals(1, ketQua.size());
        assertEquals("Tập đoàn FPT", ketQua.get(0).getTenCongTy());
    }

    @Test
    @DisplayName("Data Scope & AC1: Sales Rep A (CA_NHAN) không thấy khách của Sales Rep B")
    void testDataScope_SalesRepA_KhongThayKhachCuaB() throws Exception {
        napDuLieuMauS307();
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
    void testDataScope_TeamLead_ThayToanBoNhom() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setPhamVi(PhamViDuLieu.NHOM);

        List<KhachHang> dsLead = khachHangDAO.timKiemVaLoc(teamLeadBac, boLoc);
        assertEquals(3, dsLead.size());
    }

    @Test
    @DisplayName("Đếm số lượng khách hàng theo bộ lọc")
    void testDemSoLuong() throws Exception {
        napDuLieuMauS307();
        BoLocKhachHangDTO boLoc = new BoLocKhachHangDTO();
        boLoc.setTrangThai("TIEM_NANG");

        int count = khachHangDAO.demSoLuong(salesA, boLoc);
        assertEquals(1, count);
    }

}
