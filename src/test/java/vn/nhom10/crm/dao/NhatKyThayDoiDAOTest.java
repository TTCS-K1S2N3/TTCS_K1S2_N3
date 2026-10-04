package vn.nhom10.crm.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dto.BoLocNhatKyDTO;
import vn.nhom10.crm.dto.NguoiDungOptionDTO;
import vn.nhom10.crm.dto.ThongKeNhatKyDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.NhatKyThayDoi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử NhatKyThayDoiDAO với H2 in-memory trên bảng nhat_ky_he_thong (Story S2-04)")
class NhatKyThayDoiDAOTest {

    private Connection connection;
    private NhatKyThayDoiDAO dao;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_crm_audit;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_crm_audit;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE nguoi_dung (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150), " +
                    "email VARCHAR(150)" +
                    ")");

            st.execute("CREATE TABLE nhat_ky_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_thuc_hien_id BIGINT NULL, " +
                    "hanh_dong VARCHAR(80) NOT NULL, " +
                    "loai_doi_tuong VARCHAR(80) NOT NULL, " +
                    "doi_tuong_id BIGINT NULL, " +
                    "gia_tri_truoc_json VARCHAR(2000) NULL, " +
                    "gia_tri_sau_json VARCHAR(2000) NULL, " +
                    "ly_do VARCHAR(1000) NULL, " +
                    "dia_chi_ip VARCHAR(45) NULL, " +
                    "thong_tin_thiet_bi VARCHAR(500) NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL, " +
                    "mo_ta VARCHAR(255) NULL" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung_vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "nguoi_dung_id BIGINT NOT NULL, " +
                    "vai_tro_id BIGINT NOT NULL" +
                    ")");

            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro) VALUES " +
                    "(1, 'ADMIN', 'Quản trị hệ thống'), " +
                    "(2, 'SALES_REP', 'Nhân viên kinh doanh'), " +
                    "(3, 'MARKETING', 'Nhân viên Marketing')");

            // Nạp người dùng mẫu
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email) VALUES " +
                    "(101, 'Lê Minh Tuấn', 'tuan.lm@crm.vn'), " +
                    "(102, 'Phạm Hoàng Long', 'long.ph@crm.vn'), " +
                    "(103, 'Nguyễn Thị Thu Hà', 'ha.ntt@crm.vn')");

            // 101 có 1 vai trò Admin
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (101, 1)");

            // 102 có 2 vai trò: SALES_REP + MARKETING
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (102, 2), (102, 3)");

            // Nạp dữ liệu mẫu
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id, " +
                    "gia_tri_truoc_json, gia_tri_sau_json, ly_do, dia_chi_ip, thong_tin_thiet_bi, created_at) VALUES " +
                    "(1, 101, 'CAP_NHAT', 'CHI_TIEU', 1, " +
                    "'{\"maDoiTuong\":\"KPI-001\",\"truongThayDoi\":\"Chỉ tiêu\",\"giaTri\":\"500tr\"}', " +
                    "'{\"maDoiTuong\":\"KPI-001\",\"truongThayDoi\":\"Chỉ tiêu\",\"giaTri\":\"350tr\"}', " +
                    "'Rà soát cuối quý số liệu không khớp', '192.168.1.105', 'Chrome/Win11', '2026-09-30 08:45:12'), " +
                    "(2, 102, 'CAP_NHAT', 'CHIET_KHAU', 2, " +
                    "'{\"maDoiTuong\":\"BG-088\",\"truongThayDoi\":\"Chiết khấu\",\"giaTri\":\"10%\"}', " +
                    "'{\"maDoiTuong\":\"BG-088\",\"truongThayDoi\":\"Chiết khấu\",\"giaTri\":\"25%\"}', " +
                    "'Ưu đãi cuối quý', '192.168.1.112', 'Firefox/Mac', '2026-09-29 16:20:45'), " +
                    "(3, 101, 'CHUYEN_QUYEN', 'QUYEN_SO_HUU', 3, " +
                    "'{\"maDoiTuong\":\"KH-009\",\"truongThayDoi\":\"Owner\",\"giaTri\":\"Nguyễn Văn An\"}', " +
                    "'{\"maDoiTuong\":\"KH-009\",\"truongThayDoi\":\"Owner\",\"giaTri\":\"Trần Thị Mai\"}', " +
                    "'Tái phân bổ khách hàng', '192.168.1.105', 'Chrome/Win11', '2026-09-29 11:10:00'), " +
                    "(4, 103, 'THEM_MOI', 'VAI_TRO_NGUOI_DUNG', 4, " +
                    "'{\"maDoiTuong\":\"ND-012\",\"truongThayDoi\":\"Quyền\",\"giaTri\":\"Xem báo cáo\"}', " +
                    "'{\"maDoiTuong\":\"ND-012\",\"truongThayDoi\":\"Quyền\",\"giaTri\":\"Xem báo cáo, Phê duyệt chiết khấu\"}', " +
                    "'Cấp quyền mới', '14.162.145.22', 'Edge/Win11', '2026-09-28 19:05:30')");
        }

        dao = new NhatKyThayDoiDAO();
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("AC1 & AC2: Ghi nhận nhật ký thay đổi mới thành công vào bảng nhat_ky_he_thong")
    void testGhiNhatKyThanhCong() {
        NhatKyThayDoi nk = new NhatKyThayDoi();
        nk.setNguoiThucHienId(101L);
        nk.setHanhDong(HanhDongThayDoi.CAP_NHAT);
        nk.setLoaiDoiTuong(LoaiDoiTuongNhayCam.CHIET_KHAU);
        nk.setMaDoiTuong("BG-2026-099");
        nk.setTenDoiTuong("Báo giá Thang máy Mitsubishi");
        nk.setTruongThayDoi("Chiết khấu thanh toán");
        nk.setGiaTriTruoc("5%");
        nk.setGiaTriSau("15%");
        nk.setLyDoThayDoi("Đàm phán chốt hợp đồng");
        nk.setDiaChiIp("10.0.0.1");
        nk.setThietBi("Safari/iOS");
        nk.setCreatedAt(LocalDateTime.now());

        long generatedId = dao.ghiNhatKy(nk);
        assertTrue(generatedId > 0, "ID sinh ra phải lớn hơn 0");

        NhatKyThayDoi timKiem = dao.timTheoId(generatedId);
        assertNotNull(timKiem, "Phải tìm thấy bản ghi vừa chèn");
        assertEquals(101L, timKiem.getNguoiThucHienId());
        assertEquals("Lê Minh Tuấn", timKiem.getTenNguoiThucHien());
        assertEquals(LoaiDoiTuongNhayCam.CHIET_KHAU, timKiem.getLoaiDoiTuong());
        assertEquals(HanhDongThayDoi.CAP_NHAT, timKiem.getHanhDong());
        assertEquals("5%", timKiem.getGiaTriTruoc());
        assertEquals("15%", timKiem.getGiaTriSau());
    }

    @Test
    @DisplayName("AC3: Lọc nhật ký theo người dùng")
    void testLocTheoNguoiDung() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setNguoiDungId(101L);

        List<NhatKyThayDoi> ds = dao.layDanhSach(boLoc);
        assertEquals(2, ds.size(), "User 101 có đúng 2 bản ghi");
        for (NhatKyThayDoi nk : ds) {
            assertEquals(101L, nk.getNguoiThucHienId());
        }

        long count = dao.demTongSoBanGhi(boLoc);
        assertEquals(2, count);
    }

    @Test
    @DisplayName("AC3: Lọc nhật ký theo loại đối tượng nhạy cảm")
    void testLocTheoLoaiDoiTuong() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setLoaiDoiTuong(LoaiDoiTuongNhayCam.CHI_TIEU.getMa());

        List<NhatKyThayDoi> ds = dao.layDanhSach(boLoc);
        assertEquals(1, ds.size(), "Chỉ có 1 bản ghi CHI_TIEU");
        assertEquals(LoaiDoiTuongNhayCam.CHI_TIEU, ds.get(0).getLoaiDoiTuong());
    }

    @Test
    @DisplayName("AC3: Lọc nhật ký theo khoảng thời gian")
    void testLocTheoKhoangThoiGian() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setTuNgay(LocalDate.of(2026, 9, 29));
        boLoc.setDenNgay(LocalDate.of(2026, 9, 30));

        List<NhatKyThayDoi> ds = dao.layDanhSach(boLoc);
        assertEquals(3, ds.size(), "Có 3 bản ghi trong khoảng 29-30/09/2026");
    }

    @Test
    @DisplayName("Lọc theo từ khóa tìm kiếm")
    void testLocTheoTuKhoa() {
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setTuKhoa("lệch");

        List<NhatKyThayDoi> ds = dao.layDanhSach(boLoc);
        // Kiểm tra từ khóa trong lý do
        boLoc.setTuKhoa("số liệu không khớp");
        ds = dao.layDanhSach(boLoc);
        assertEquals(1, ds.size(), "Tìm thấy 1 bản ghi có lý do 'số liệu không khớp'");
    }

    @Test
    @DisplayName("Thống kê số lượng thay đổi theo từng loại đối tượng")
    void testLayThongKe() {
        ThongKeNhatKyDTO tk = dao.layThongKe(null);
        assertNotNull(tk);
        assertEquals(4, tk.getTongSoBanGhi());
        assertEquals(1, tk.getSoThayDoiChiTieu());
        assertEquals(1, tk.getSoThayDoiChietKhau());
        assertEquals(1, tk.getSoThayDoiQuyenSoHuu());
        assertEquals(1, tk.getSoThayDoiVaiTro());
        assertEquals(3, tk.getSoNguoiThucHien());
    }

    @Test
    @DisplayName("Lấy danh sách người dùng option phục vụ dropdown")
    void testLayDanhSachNguoiDungOption() {
        List<NguoiDungOptionDTO> users = dao.layDanhSachNguoiDungOption();
        assertNotNull(users);
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("S2-04 Actor Role: Người dùng có 1 vai trò (Admin) hiển thị đúng tên vai trò")
    void testActorRole_DonVaiTro() {
        NhatKyThayDoi nk = dao.timTheoId(1L); // Actor là 101 (Admin)
        assertNotNull(nk);
        assertEquals("Quản trị hệ thống", nk.getVaiTroNguoiThucHien());
    }

    @Test
    @DisplayName("S2-04 Actor Role: Người dùng có nhiều vai trò hiển thị đầy đủ, không nhân bản audit row")
    void testActorRole_DaVaiTro_KhongDuplicateRow() {
        // Actor 102 có 2 vai trò: SALES_REP + MARKETING
        NhatKyThayDoi nk = dao.timTheoId(2L);
        assertNotNull(nk);
        assertEquals("Nhân viên kinh doanh, Nhân viên Marketing", nk.getVaiTroNguoiThucHien());

        // Kiểm tra danh sách không bị nhân bản row khi user có nhiều vai trò
        BoLocNhatKyDTO boLoc = new BoLocNhatKyDTO();
        boLoc.setNguoiDungId(102L);
        List<NhatKyThayDoi> ds = dao.layDanhSach(boLoc);
        assertEquals(1, ds.size(), "Bản ghi của actor 102 phải đúng 1 dòng, không bị duplicate");
        assertEquals("Nhân viên kinh doanh, Nhân viên Marketing", ds.get(0).getVaiTroNguoiThucHien());
    }

    @Test
    @DisplayName("S2-04 Actor Role: Actor NULL thì fallback 'Không xác định', không có literal null")
    void testActorRole_ActorNull_FallbackKhongXacDinh() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute("INSERT INTO nhat_ky_he_thong (id, nguoi_thuc_hien_id, hanh_dong, loai_doi_tuong, doi_tuong_id) " +
                    "VALUES (99, NULL, 'CAP_NHAT', 'CHIET_KHAU', 1)");
        }

        NhatKyThayDoi nk = dao.timTheoId(99L);
        assertNotNull(nk);
        assertEquals("Không xác định", nk.getVaiTroNguoiThucHien());
        assertEquals("Hệ thống", nk.getTenNguoiThucHien());
        assertEquals("-", nk.getEmailNguoiThucHien());
    }
}
