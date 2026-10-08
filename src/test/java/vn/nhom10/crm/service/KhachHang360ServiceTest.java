package vn.nhom10.crm.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.dao.KhachHang360DAO;
import vn.nhom10.crm.dao.PhanQuyenDuLieuDAO;
import vn.nhom10.crm.dto.KhachHang360DTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.model.HoatDong;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.util.LoiPhanQuyenException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử KhachHang360Service với phân quyền Data Scope & nghiệp vụ 360")
class KhachHang360ServiceTest {

    private Connection connection;
    private KhachHang360Service service;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:test_khach_hang_service;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                return DriverManager.getConnection("jdbc:h2:mem:test_khach_hang_service;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        try (Statement st = connection.createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("CREATE TABLE nhom_kinh_doanh (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_nhom VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_nhom VARCHAR(255) NOT NULL" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "email VARCHAR(200) NOT NULL, " +
                    "nhom_kinh_doanh_id INT" +
                    ")");

            st.execute("CREATE TABLE vai_tro (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL" +
                    ")");

            st.execute("CREATE TABLE nguoi_dung_vai_tro (" +
                    "nguoi_dung_id INT NOT NULL, " +
                    "vai_tro_id INT NOT NULL, " +
                    "PRIMARY KEY (nguoi_dung_id, vai_tro_id)" +
                    ")");

            st.execute("CREATE TABLE phan_quyen_du_lieu (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "vai_tro_id INT NOT NULL, " +
                    "module VARCHAR(50) NOT NULL, " +
                    "pham_vi VARCHAR(50) NOT NULL" +
                    ")");

            st.execute("CREATE TABLE nganh_nghe (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_nganh VARCHAR(50), ten_nganh VARCHAR(255))");
            st.execute("CREATE TABLE quy_mo_doanh_nghiep (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_quy_mo VARCHAR(50), ten_quy_mo VARCHAR(255))");
            st.execute("CREATE TABLE khu_vuc_dia_ly (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_khu_vuc VARCHAR(50), ten_khu_vuc VARCHAR(255))");
            st.execute("CREATE TABLE giai_doan_pipeline (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_giai_doan VARCHAR(50), ten_giai_doan VARCHAR(255))");
            st.execute("CREATE TABLE ly_do_thang_thua (id BIGINT AUTO_INCREMENT PRIMARY KEY, ma_ly_do VARCHAR(50), ten_ly_do VARCHAR(255))");

            st.execute("CREATE TABLE khach_hang (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_khach_hang VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_cong_ty VARCHAR(255) NOT NULL, " +
                    "ten_chuan_hoa VARCHAR(255), " +
                    "ma_so_thue VARCHAR(50), " +
                    "nganh_nghe_id BIGINT, " +
                    "quy_mo_id BIGINT, " +
                    "website VARCHAR(255), " +
                    "website_chuan_hoa VARCHAR(255), " +
                    "dia_chi TEXT, " +
                    "khu_vuc_id BIGINT, " +
                    "nguoi_so_huu_id BIGINT, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "doanh_thu_uoc_tinh DECIMAL(18,2) DEFAULT 0, " +
                    "cong_ty_me_id BIGINT, " +
                    "trang_thai VARCHAR(50) DEFAULT 'TIEM_NANG', " +
                    "co_rui_ro BOOLEAN DEFAULT FALSE, " +
                    "rui_ro_cap_nhat_luc TIMESTAMP, " +
                    "lan_tuong_tac_cuoi TIMESTAMP, " +
                    "gop_vao_khach_hang_id BIGINT, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE nguoi_lien_he (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ho_ten VARCHAR(150) NOT NULL, " +
                    "chuc_danh VARCHAR(100), " +
                    "email VARCHAR(150), " +
                    "so_dien_thoai VARCHAR(50), " +
                    "vai_tro_quyet_dinh VARCHAR(50) DEFAULT 'NGUOI_ANH_HUONG', " +
                    "la_dau_moi_chinh BOOLEAN DEFAULT FALSE, " +
                    "trang_thai VARCHAR(50) DEFAULT 'DANG_HOAT_DONG', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE co_hoi (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_co_hoi VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_co_hoi VARCHAR(255) NOT NULL, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "nguoi_lien_he_chinh_id BIGINT, " +
                    "lead_id BIGINT, " +
                    "chien_dich_id BIGINT, " +
                    "nguon_lead_id BIGINT, " +
                    "giai_doan_id BIGINT, " +
                    "nguoi_phu_trach_id BIGINT, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "gia_tri_du_kien DECIMAL(18,2) DEFAULT 0, " +
                    "xac_suat INT DEFAULT 50, " +
                    "ly_do_sua_xac_suat VARCHAR(255), " +
                    "ngay_chot_du_kien DATE, " +
                    "trang_thai VARCHAR(50) DEFAULT 'MO', " +
                    "gia_tri_chot_thuc_te DECIMAL(18,2) DEFAULT 0, " +
                    "ngay_ky DATE, " +
                    "ly_do_thang_thua_id BIGINT, " +
                    "doi_thu_id BIGINT, " +
                    "ngay_hoat_dong_cuoi TIMESTAMP, " +
                    "bi_dinh_tre BOOLEAN DEFAULT FALSE, " +
                    "mo_ta_chi_tiet TEXT, " +
                    "ngay_tao DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE hop_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "so_hop_dong VARCHAR(100) NOT NULL UNIQUE, " +
                    "bao_gia_id BIGINT, " +
                    "phien_ban_bao_gia_id BIGINT, " +
                    "co_hoi_id BIGINT, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "ngay_ky DATE, " +
                    "ngay_hieu_luc DATE, " +
                    "ngay_het_han DATE, " +
                    "gia_tri_hop_dong DECIMAL(18,2) NOT NULL, " +
                    "dieu_khoan_thanh_toan VARCHAR(255), " +
                    "trang_thai VARCHAR(50) DEFAULT 'DA_KY', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE hoat_dong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_hoat_dong VARCHAR(50) NOT NULL UNIQUE, " +
                    "tieu_de VARCHAR(255) NOT NULL, " +
                    "loai_hoat_dong VARCHAR(50) NOT NULL, " +
                    "lead_id BIGINT, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "nguoi_lien_he_id BIGINT, " +
                    "co_hoi_id BIGINT, " +
                    "nguoi_phu_trach_id BIGINT, " +
                    "nhom_kinh_doanh_id BIGINT, " +
                    "chi_phi DECIMAL(18,2) DEFAULT 0, " +
                    "thoi_gian_bat_dau TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "thoi_gian_ket_thuc TIMESTAMP, " +
                    "trang_thai VARCHAR(50) DEFAULT 'HOAN_THANH', " +
                    "mo_ta_chi_tiet TEXT, " +
                    "noi_dung TEXT, " +
                    "ket_qua TEXT, " +
                    "thoi_luong_phut INT, " +
                    "la_ghi_nhan_qua_khu BOOLEAN DEFAULT FALSE, " +
                    "la_ghi_nhan_nhanh BOOLEAN DEFAULT FALSE, " +
                    "ngay_tao DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            st.execute("CREATE TABLE tep_dinh_kem (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "khach_hang_id BIGINT NOT NULL, " +
                    "co_hoi_id BIGINT, " +
                    "hoat_dong_id BIGINT, " +
                    "bao_gia_id BIGINT, " +
                    "hop_dong_id BIGINT, " +
                    "loai_tep VARCHAR(50), " +
                    "ten_file_goc VARCHAR(255) NOT NULL, " +
                    "ten_file_luu VARCHAR(255), " +
                    "duong_dan VARCHAR(500) NOT NULL, " +
                    "mime_type VARCHAR(100), " +
                    "kich_thuoc_byte BIGINT DEFAULT 0, " +
                    "nguoi_tai_len_id BIGINT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Seed master data
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (1, 'KD_BAC', 'Kinh Doanh Miền Bắc')");
            st.execute("INSERT INTO nhom_kinh_doanh (id, ma_nhom, ten_nhom) VALUES (2, 'KD_NAM', 'Kinh Doanh Miền Nam')");

            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id) VALUES (1, 'Sales A', 'sales.a@crm.vn', 1)");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id) VALUES (2, 'Sales B', 'sales.b@crm.vn', 2)");
            st.execute("INSERT INTO nguoi_dung (id, ho_ten, email, nhom_kinh_doanh_id) VALUES (3, 'Admin', 'admin@crm.vn', 1)");

            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro) VALUES (1, 'ADMIN', 'Quản trị viên')");
            st.execute("INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro) VALUES (4, 'SALES_REP', 'Nhân viên kinh doanh')");

            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (1, 4)");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (2, 4)");
            st.execute("INSERT INTO nguoi_dung_vai_tro (nguoi_dung_id, vai_tro_id) VALUES (3, 1)");

            // Phân quyền: SALES_REP chỉ xem CA_NHAN module KHACH_HANG
            st.execute("INSERT INTO phan_quyen_du_lieu (vai_tro_id, module, pham_vi) VALUES (4, 'KHACH_HANG', 'CA_NHAN')");
            st.execute("INSERT INTO phan_quyen_du_lieu (vai_tro_id, module, pham_vi) VALUES (1, 'KHACH_HANG', 'TOAN_QUYEN')");

            // Khách hàng thuộc sở hữu của Sales A (id = 1)
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id) " +
                    "VALUES (10, 'KH-0010', 'Công ty Khách Hàng Sales A', 1, 1)");

            // Khách hàng thuộc sở hữu của Sales B (id = 2)
            st.execute("INSERT INTO khach_hang (id, ma_khach_hang, ten_cong_ty, nguoi_so_huu_id, nhom_kinh_doanh_id) " +
                    "VALUES (20, 'KH-0020', 'Công ty Khách Hàng Sales B', 2, 2)");
        }

        KhachHang360DAO dao = new KhachHang360DAO();
        PhanQuyenDuLieuService pqService = new PhanQuyenDuLieuService(new PhanQuyenDuLieuDAO());
        service = new KhachHang360Service(dao, pqService);
    }

    @AfterEach
    void tearDown() throws Exception {
        DatabaseConfig.resetConnectionSupplier();
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private NguoiDungDTO taoUser(Long id, String hoTen, Long nhomId, VaiTroEnum vaiTro, PhamViDuLieu phamVi) {
        return new NguoiDungDTO(id, hoTen, hoTen + "@crm.vn", vaiTro, nhomId, "Nhóm", phamVi);
    }

    @Test
    @DisplayName("Data Scope: Sales Rep được phép truy cập khách hàng do chính mình phụ trách")
    void testSalesAccessCustomerOwn() {
        NguoiDungDTO salesA = taoUser(1L, "Sales A", 1L, VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN);

        KhachHang360DTO dto = service.layThongTin360(10L, salesA);
        assertNotNull(dto);
        assertEquals("KH-0010", dto.getKhachHang().getMaKhachHang());
    }

    @Test
    @DisplayName("Data Scope: Sales Rep BỊ TỪ CHỐI truy cập khách hàng của nhân viên khác (Ngoại phạm vi)")
    void testSalesBlockedCustomerOther() {
        NguoiDungDTO salesA = taoUser(1L, "Sales A", 1L, VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN);

        // Khách hàng 20 thuộc sở hữu của Sales B (id=2)
        assertThrows(LoiPhanQuyenException.class, () -> {
            service.layThongTin360(20L, salesA);
        });
    }

    @Test
    @DisplayName("Data Scope: Admin có quyền xem tất cả khách hàng (TOAN_BO)")
    void testAdminAccessAllCustomers() {
        NguoiDungDTO admin = taoUser(3L, "Admin", 1L, VaiTroEnum.ADMIN, PhamViDuLieu.TOAN_BO);

        KhachHang360DTO dto10 = service.layThongTin360(10L, admin);
        assertNotNull(dto10);
        KhachHang360DTO dto20 = service.layThongTin360(20L, admin);
        assertNotNull(dto20);
    }

    @Test
    @DisplayName("Thêm hoạt động mới thành công và lấy danh sách hoạt động")
    void testThemHoatDongHopLe() {
        HoatDong hd = service.themHoatDong(10L, 1L, 1L, "CUOC_GOI", "Trao đổi tiến độ hợp đồng", "Đã thống nhất giá");
        assertNotNull(hd);
        assertNotNull(hd.getId());
        assertEquals("Trao đổi tiến độ hợp đồng", hd.getTieuDe());

        List<HoatDong> list = service.layDsHoatDong(10L, 10, 0);
        assertEquals(1, list.size());
        assertEquals("Trao đổi tiến độ hợp đồng", list.get(0).getTieuDe());
    }

    @Test
    @DisplayName("Thêm hoạt động với đầu vào không hợp lệ ném ngoại lệ đúng")
    void testThemHoatDongKhongHopLe() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.themHoatDong(null, 1L, 1L, "CUOC_GOI", "Hợp lệ", "Nội dung");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            service.themHoatDong(10L, 1L, 1L, "CUOC_GOI", "", "Nội dung");
        });
    }
}
