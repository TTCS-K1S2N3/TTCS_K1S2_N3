package vn.nhom10.crm.service;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.nhom10.crm.config.DatabaseConfig;
import vn.nhom10.crm.controller.DanhMucBanHangServlet;
import vn.nhom10.crm.controller.KhachHangServlet;
import vn.nhom10.crm.controller.NhatKyThayDoiServlet;
import vn.nhom10.crm.controller.PhanQuyenVaiTroServlet;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dao.VaiTroModuleDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.dto.MucMenuDTO;
import vn.nhom10.crm.model.MucQuyen;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.model.VaiTroModule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("13 Kiểm thử bắt buộc: Hệ thống phân quyền DB-backed, Multi-role an toàn và Regression Sprint 3")
public class PermissionMatrixSpikeTest {

    private static Connection h2Connection;
    private PermissionService permissionService;
    private MenuService menuService;
    private VaiTroModuleDAO vaiTroModuleDAO;
    private VaiTroDAO vaiTroDAO;

    @BeforeAll
    static void initDatabase() throws Exception {
        h2Connection = DriverManager.getConnection("jdbc:h2:mem:spike_permission_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        DatabaseConfig.setConnectionSupplier(() -> {
            try {
                if (h2Connection.isClosed()) {
                    h2Connection = DriverManager.getConnection("jdbc:h2:mem:spike_permission_test;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
                }
            } catch (SQLException ignored) {
            }
            return h2Connection;
        });

        // Nạp schema bảng vai_tro, module_he_thong, vai_tro_module vào H2
        try (Statement st = h2Connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS vai_tro (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_vai_tro VARCHAR(50) NOT NULL UNIQUE, " +
                    "ten_vai_tro VARCHAR(100) NOT NULL, " +
                    "mo_ta VARCHAR(500) NULL, " +
                    "pham_vi_toi_da VARCHAR(50) NOT NULL DEFAULT 'CA_NHAN', " +
                    "hoat_dong TINYINT NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            st.execute("CREATE TABLE IF NOT EXISTS module_he_thong (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "ma_module VARCHAR(60) NOT NULL UNIQUE, " +
                    "ten_module VARCHAR(150) NOT NULL, " +
                    "mo_ta VARCHAR(500) NULL, " +
                    "thu_tu_hien_thi INT NOT NULL DEFAULT 0, " +
                    "hien_thi_menu TINYINT NOT NULL DEFAULT 1, " +
                    "hoat_dong TINYINT NOT NULL DEFAULT 1)");

            st.execute("CREATE TABLE IF NOT EXISTS vai_tro_module (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "vai_tro_id BIGINT NOT NULL, " +
                    "module_id BIGINT NOT NULL, " +
                    "muc_quyen VARCHAR(20) NOT NULL, " +
                    "pham_vi_du_lieu VARCHAR(20) NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // Nạp 7 vai trò
            st.execute("MERGE INTO vai_tro (id, ma_vai_tro, ten_vai_tro, mo_ta, pham_vi_toi_da) KEY(id) VALUES " +
                    "(1, 'ADMIN', 'Quản trị hệ thống', 'Quản trị toàn hệ thống', 'TOAN_BO'), " +
                    "(2, 'DIRECTOR', 'Giám đốc kinh doanh', 'Quản lý toàn bộ dữ liệu', 'TOAN_BO'), " +
                    "(3, 'TEAM_LEAD', 'Trưởng nhóm kinh doanh', 'Quản lý nhóm', 'NHOM'), " +
                    "(4, 'SALES_REP', 'Nhân viên kinh doanh', 'Quản lý cá nhân', 'CA_NHAN'), " +
                    "(5, 'MARKETING', 'Nhân viên Marketing', 'Quản lý lead', 'CA_NHAN'), " +
                    "(6, 'CUST_SUCCESS', 'Chăm sóc khách hàng', 'Chăm sóc sau bán', 'CA_NHAN'), " +
                    "(7, 'ACCOUNTANT', 'Kế toán', 'Quản lý tài chính', 'TOAN_BO')");

            // Nạp 10 module canonical
            st.execute("MERGE INTO module_he_thong (id, ma_module, ten_module, thu_tu_hien_thi, hien_thi_menu) KEY(id) VALUES " +
                    "(1, 'DANH_MUC', 'Danh mục & cấu hình bán hàng', 10, 1), " +
                    "(2, 'KHACH_HANG', 'Khách hàng & liên hệ', 20, 1), " +
                    "(3, 'LEAD', 'Lead & phân bổ', 30, 1), " +
                    "(4, 'CO_HOI', 'Cơ hội & pipeline', 40, 1), " +
                    "(5, 'HOAT_DONG', 'Hoạt động & lịch làm việc', 50, 1), " +
                    "(6, 'BAO_GIA_HOP_DONG', 'Báo giá & hợp đồng', 60, 1), " +
                    "(7, 'KPI', 'Chỉ tiêu & KPI', 70, 1), " +
                    "(8, 'BAO_CAO', 'Báo cáo & dashboard', 80, 1), " +
                    "(9, 'TU_DONG_HOA', 'Tự động hoá & thông báo', 90, 1), " +
                    "(10, 'NGUOI_DUNG_NHAT_KY', 'Người dùng & nhật ký', 100, 1)");

            reseedMatrix();
        }
    }

    private static void reseedMatrix() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM vai_tro_module");
            st.execute("INSERT INTO vai_tro_module (vai_tro_id, module_id, muc_quyen, pham_vi_du_lieu) VALUES " +
                    // Admin (1)
                    "(1,1,'FULL','TOAN_BO'),(1,2,'FULL','TOAN_BO'),(1,3,'FULL','TOAN_BO'),(1,4,'FULL','TOAN_BO')," +
                    "(1,5,'FULL','TOAN_BO'),(1,6,'FULL','TOAN_BO'),(1,7,'FULL','TOAN_BO'),(1,8,'FULL','TOAN_BO')," +
                    "(1,9,'FULL','TOAN_BO'),(1,10,'FULL','TOAN_BO')," +
                    // Director (2)
                    "(2,1,'FULL','TOAN_BO'),(2,2,'FULL','TOAN_BO'),(2,3,'FULL','TOAN_BO'),(2,4,'FULL','TOAN_BO')," +
                    "(2,5,'FULL','TOAN_BO'),(2,6,'FULL','TOAN_BO'),(2,7,'FULL','TOAN_BO'),(2,8,'FULL','TOAN_BO')," +
                    "(2,9,'FULL','TOAN_BO'),(2,10,'READ','TOAN_BO')," +
                    // Team Lead (3)
                    "(3,1,'READ',NULL),(3,2,'FULL','NHOM'),(3,3,'FULL','NHOM'),(3,4,'FULL','NHOM')," +
                    "(3,5,'FULL','NHOM'),(3,6,'WRITE','NHOM'),(3,7,'WRITE','NHOM'),(3,8,'READ','NHOM')," +
                    "(3,9,'READ','NHOM'),(3,10,'NONE',NULL)," +
                    // Sales Rep (4)
                    "(4,1,'READ',NULL),(4,2,'WRITE','CA_NHAN'),(4,3,'WRITE','CA_NHAN'),(4,4,'WRITE','CA_NHAN')," +
                    "(4,5,'WRITE','CA_NHAN'),(4,6,'WRITE','CA_NHAN'),(4,7,'READ','CA_NHAN'),(4,8,'READ','CA_NHAN')," +
                    "(4,9,'READ',NULL),(4,10,'NONE',NULL)," +
                    // Marketing (5)
                    "(5,1,'READ',NULL),(5,2,'WRITE','TOAN_BO'),(5,3,'FULL','TOAN_BO'),(5,4,'READ','TOAN_BO')," +
                    "(5,5,'WRITE','TOAN_BO'),(5,6,'NONE',NULL),(5,7,'NONE',NULL),(5,8,'READ','CA_NHAN')," +
                    "(5,9,'WRITE','TOAN_BO'),(5,10,'NONE',NULL)," +
                    // Customer Success (6)
                    "(6,1,'READ',NULL),(6,2,'WRITE','CA_NHAN'),(6,3,'NONE',NULL),(6,4,'READ','CA_NHAN')," +
                    "(6,5,'WRITE','CA_NHAN'),(6,6,'READ','CA_NHAN'),(6,7,'NONE',NULL),(6,8,'READ','CA_NHAN')," +
                    "(6,9,'READ',NULL),(6,10,'NONE',NULL)," +
                    // Accountant (7)
                    "(7,1,'READ',NULL),(7,2,'READ','TOAN_BO'),(7,3,'NONE',NULL),(7,4,'READ','TOAN_BO')," +
                    "(7,5,'NONE',NULL),(7,6,'WRITE','TOAN_BO'),(7,7,'READ','TOAN_BO'),(7,8,'READ','TOAN_BO')," +
                    "(7,9,'NONE',NULL),(7,10,'NONE',NULL)");
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        if (h2Connection != null && !h2Connection.isClosed()) {
            h2Connection.close();
        }
        DatabaseConfig.resetConnectionSupplier();
    }

    @BeforeEach
    void setUp() throws Exception {
        reseedMatrix();
        vaiTroModuleDAO = new VaiTroModuleDAO();
        vaiTroDAO = new VaiTroDAO();
        permissionService = new PermissionService(vaiTroModuleDAO, vaiTroDAO);
        permissionService.xoaCache();
        menuService = new MenuService(permissionService);
    }

    private NguoiDung taoUser(int id, String hoTen, String email, VaiTroEnum... vaiTros) {
        NguoiDung nd = new NguoiDung(id, hoTen, email);
        nd.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
        for (VaiTroEnum vt : vaiTros) {
            PhamViDuLieu pv = PhamViDuLieu.CA_NHAN;
            if (vt == VaiTroEnum.ADMIN || vt == VaiTroEnum.DIRECTOR) {
                pv = PhamViDuLieu.TOAN_BO;
            } else if (vt == VaiTroEnum.TEAM_LEAD) {
                pv = PhamViDuLieu.NHOM;
            }
            nd.themVaiTro(new VaiTro(vt, pv));
        }
        return nd;
    }

    @Test
    @DisplayName("Test 1: NONE - Menu bị ẩn và URL module bị chặn 403")
    void test1_NoneMenuAnVaUrl403() {
        // Sales Rep có quyền NONE đối với NGUOI_DUNG_NHAT_KY (module NGUOI_DUNG)
        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);

        List<MucMenuDTO> menus = menuService.layDanhSachMenuChoNguoiDung(sales, "/khach-hang");
        Set<String> menuCodes = menus.stream().map(MucMenuDTO::getMaModule).collect(Collectors.toSet());

        assertFalse(menuCodes.contains("NGUOI_DUNG"), "Menu NGUOI_DUNG phải bị ẩn đối với Sales Rep");
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(sales, "/nguoi-dung"), "URL /nguoi-dung phải trả về false (403)");
    }

    @Test
    @DisplayName("Test 2: READ - VIEW được, CREATE/UPDATE/DELETE bị chặn")
    void test2_ReadViewDuocMutationBiChan() {
        // Sales Rep có quyền READ trên KPI (CHI_TIEU)
        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);

        assertTrue(permissionService.coQuyen(sales, "KPI", MucQuyen.READ), "READ: xem được");
        assertFalse(permissionService.coQuyen(sales, "KPI", MucQuyen.WRITE), "READ: không được tạo/sửa (WRITE)");
        assertFalse(permissionService.coQuyen(sales, "KPI", MucQuyen.FULL), "READ: không được xóa (FULL)");
    }

    @Test
    @DisplayName("Test 3: WRITE - VIEW/CREATE/UPDATE được, DELETE bị chặn")
    void test3_WriteCrudPhanCap() {
        // Sales Rep có quyền WRITE trên KHACH_HANG
        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);

        assertTrue(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.READ), "WRITE bao gồm READ (xem)");
        assertTrue(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.WRITE), "WRITE cho phép tạo/sửa");
        assertFalse(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.FULL), "WRITE không bao gồm FULL (xóa)");
    }

    @Test
    @DisplayName("Test 4: FULL - Toàn quyền CRUD (Xem, Tạo, Sửa, Xóa)")
    void test4_FullCrud() {
        // Team Lead có quyền FULL trên KHACH_HANG
        NguoiDung leader = taoUser(3, "Team Lead", "lead@crm.vn", VaiTroEnum.TEAM_LEAD);

        assertTrue(permissionService.coQuyen(leader, "KHACH_HANG", MucQuyen.READ));
        assertTrue(permissionService.coQuyen(leader, "KHACH_HANG", MucQuyen.WRITE));
        assertTrue(permissionService.coQuyen(leader, "KHACH_HANG", MucQuyen.FULL));
    }

    @Test
    @DisplayName("Test 5: Admin - Được phép GET và POST permission matrix")
    void test5_AdminGetVaPostPermissionMatrix() throws Exception {
        PhanQuyenVaiTroServlet servlet = new PhanQuyenVaiTroServlet(permissionService, vaiTroDAO);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        NguoiDung admin = taoUser(1, "Admin", "admin@crm.vn", VaiTroEnum.ADMIN);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(admin);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);
        when(req.getParameter("vaiTro")).thenReturn("SALES_REP");

        // GET
        invokeDoGet(servlet, req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(req).getRequestDispatcher("/WEB-INF/views/nguoi-dung/phan-quyen-vai-tro.jsp");

        // POST cập nhật quyền hợp lệ cho SALES_REP
        when(req.getParameter("maVaiTro")).thenReturn("SALES_REP");
        when(req.getParameter("mucQuyen_2")).thenReturn("READ"); // KHACH_HANG -> READ
        when(req.getParameter("phamVi_2")).thenReturn("CA_NHAN");
        when(req.getRemoteAddr()).thenReturn("127.0.0.1");

        invokeDoPost(servlet, req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Test 6: Director - Được phép GET, nhưng POST bị chặn 403 / read-only")
    void test6_DirectorGetDuocPostBiChan() throws Exception {
        PhanQuyenVaiTroServlet servlet = new PhanQuyenVaiTroServlet(permissionService, vaiTroDAO);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        NguoiDung director = taoUser(2, "Director", "director@crm.vn", VaiTroEnum.DIRECTOR);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(director);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);

        // GET -> cho phép xem
        invokeDoGet(servlet, req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(req).setAttribute(eq("isReadOnly"), eq(true));

        // POST -> bị từ chối 403
        when(req.getParameter("maVaiTro")).thenReturn("SALES_REP");
        invokeDoPost(servlet, req, resp);
        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Test 7: Role khác (Sales, Mkt,...) - Truy cập permission matrix bị 403")
    void test7_RoleKhacBiChan403() throws Exception {
        PhanQuyenVaiTroServlet servlet = new PhanQuyenVaiTroServlet(permissionService, vaiTroDAO);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        NguoiDung sales = taoUser(4, "Sales", "sales@crm.vn", VaiTroEnum.SALES_REP);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(sales);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoGet(servlet, req, resp);
        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Test 8: Admin row - Không thể sửa đổi hoặc hạ quyền Admin")
    void test8_AdminRowBaoVeTuyetDoi() {
        List<VaiTroModule> list = new ArrayList<>();
        VaiTroModule vtm = new VaiTroModule(1, 1, MucQuyen.NONE, null);
        list.add(vtm);

        boolean kq = permissionService.capNhatMatrixChoVaiTro("ADMIN", list, 1, "127.0.0.1", "Chrome");
        assertFalse(kq, "Cập nhật hạ quyền vai trò ADMIN phải bị từ chối");
    }

    @Test
    @DisplayName("Test 9: Multi-role chống Privilege Escalation (Role A: WRITE+CA_NHAN, Role B: READ+TOAN_BO)")
    void test9_MultiRoleChongPrivilegeEscalation() throws Exception {
        // Giả lập cấu hình DB tạm:
        // Cập nhật CUST_SUCCESS: KHACH_HANG = READ, TOAN_BO (ở vai trò gốc là WRITE, CA_NHAN)
        // Tạo user kết hợp: SALES_REP (WRITE, CA_NHAN) + ACCOUNTANT (READ, TOAN_BO)
        // SALES_REP: KHACH_HANG = WRITE, CA_NHAN
        // ACCOUNTANT: KHACH_HANG = READ, TOAN_BO
        NguoiDung multiUser = taoUser(99, "Multi User", "multi@crm.vn", VaiTroEnum.SALES_REP, VaiTroEnum.ACCOUNTANT);

        // 1. VIEW cần READ:
        // Cả 2 role đều đạt >= READ. Scope xét: CA_NHAN và TOAN_BO.
        // Expected: VIEW scope = TOAN_BO.
        assertTrue(permissionService.coQuyen(multiUser, "KHACH_HANG", MucQuyen.READ));
        assertEquals(PhamViDuLieu.TOAN_BO, permissionService.layPhamViHieuLuc(multiUser, "KHACH_HANG", MucQuyen.READ));

        // 2. CREATE/UPDATE cần WRITE:
        // Chỉ SALES_REP đạt WRITE (CA_NHAN). ACCOUNTANT chỉ có READ nên BỊ LOẠI KHỎI TÍNH TOÁN!
        // Expected: WRITE scope = CA_NHAN (TUYỆT ĐỐI KHÔNG ĐƯỢC LEO QUYỀN LÊN TOAN_BO!).
        assertTrue(permissionService.coQuyen(multiUser, "KHACH_HANG", MucQuyen.WRITE));
        assertEquals(PhamViDuLieu.CA_NHAN, permissionService.layPhamViHieuLuc(multiUser, "KHACH_HANG", MucQuyen.WRITE),
                "Chống Privilege Escalation: WRITE chỉ được hưởng phạm vi CA_NHAN, không được leo quyền lên TOAN_BO!");

        // 3. DELETE cần FULL:
        // Cả 2 role đều không có FULL.
        assertFalse(permissionService.coQuyen(multiUser, "KHACH_HANG", MucQuyen.FULL));
        assertNull(permissionService.layPhamViHieuLuc(multiUser, "KHACH_HANG", MucQuyen.FULL));
    }

    @Test
    @DisplayName("Test 10: Runtime DB-backed - Thay đổi vai_tro_module cập nhật tức thì menu và gate")
    void test10_RuntimeDbBackedMenuVaGateThayDoiThat() throws Exception {
        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);

        // Ban đầu Sales Rep có WRITE trên KHACH_HANG (id=2)
        assertTrue(menuService.kiemTraQuyenTruyCapUrl(sales, "/khach-hang"));
        assertTrue(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.WRITE));

        // Sửa quyền trong DB sang NONE
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("UPDATE vai_tro_module SET muc_quyen = 'NONE' WHERE vai_tro_id = 4 AND module_id = 2");
        }
        permissionService.xoaCache();

        // Kiểm tra tức thì: Quyền thành false, URL bị chặn
        assertFalse(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.READ), "Sau khi sửa DB, quyền READ phải bị mất");
        assertFalse(menuService.kiemTraQuyenTruyCapUrl(sales, "/khach-hang"), "Sau khi sửa DB, URL /khach-hang phải bị chặn 403");

        // Khôi phục lại quyền WRITE cho Sales Rep để bảo đảm an toàn dữ liệu
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("UPDATE vai_tro_module SET muc_quyen = 'WRITE', pham_vi_du_lieu = 'CA_NHAN' WHERE vai_tro_id = 4 AND module_id = 2");
        }
        permissionService.xoaCache();
        assertTrue(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.WRITE));
    }

    @Test
    @DisplayName("Test 11: Data Scope regression - Team Lead FULL+NHOM không đọc/sửa ngoài NHOM")
    void test11_DataScopeRegressionTeamLeadKhongVuotNhom() {
        NguoiDung leader = taoUser(3, "Team Lead", "lead@crm.vn", VaiTroEnum.TEAM_LEAD);
        leader.setNhomKinhDoanhId(2);

        // Phạm vi hiệu lực cho KHACH_HANG ở mức WRITE là NHOM
        PhamViDuLieu scope = permissionService.layPhamViHieuLuc(leader, "KHACH_HANG", MucQuyen.WRITE);
        assertEquals(PhamViDuLieu.NHOM, scope);
        assertNotEquals(PhamViDuLieu.TOAN_BO, scope, "FULL trên module không được tự nâng thành TOAN_BO");

        // Thử yêu cầu phạm vi TOAN_BO -> hệ thống phải chặn và ép về NHOM (Fail-closed)
        PhanQuyenDuLieuService pqService = new PhanQuyenDuLieuService();
        PhamViDuLieu scopeThucTe = pqService.xacDinhPhamViHieuLuc(leader, PhamViDuLieu.TOAN_BO);
        assertEquals(PhamViDuLieu.NHOM, scopeThucTe, "Team Lead không thể chọn xem phạm vi TOAN_BO");
    }

    @Test
    @DisplayName("Test 12: DANH_MUC READ - Role có READ được GET, mutation bị chặn nếu không có WRITE/FULL")
    void test12_DanhMucReadGetDuocMutationBiChan() throws Exception {
        vn.nhom10.crm.service.DanhMucBanHangService mockService = mock(vn.nhom10.crm.service.DanhMucBanHangService.class);
        when(mockService.timKiem(any(), any())).thenReturn(new ArrayList<>());
        when(mockService.tinhThongKe(any())).thenReturn(new long[]{0, 0, 0});
        DanhMucBanHangServlet servlet = new DanhMucBanHangServlet(mockService);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        // Sales Rep có quyền READ trên DANH_MUC (không có WRITE)
        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(sales);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);

        // 1. GET: Được phép truy cập (HTTP 200 / forward)
        invokeDoGet(servlet, req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(req).getRequestDispatcher("/WEB-INF/views/danh-muc/quan-ly-danh-muc.jsp");

        // 2. POST (thêm/sửa danh mục): Bị chặn 403 Forbidden
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("action")).thenReturn("them");
        when(req.getParameter("loaiDanhMuc")).thenReturn("NGANH_NGHE");
        invokeDoPost(servlet, req, resp);
        verify(resp).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Test 13: Director audit - Giám đốc xem được nhật ký thay đổi (READ)")
    void test13_DirectorAuditReadDung() throws Exception {
        NhatKyThayDoiServlet servlet = new NhatKyThayDoiServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        NguoiDung director = taoUser(2, "Director", "director@crm.vn", VaiTroEnum.DIRECTOR);
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(director);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);
        when(req.getServletPath()).thenReturn("/nhat-ky-thay-doi");

        invokeDoGet(servlet, req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(req).getRequestDispatcher("/WEB-INF/views/nhat-ky-thay-doi/danh-sach.jsp");
    }

    @Test
    @DisplayName("KhachHang Permission Runtime: SALES_REP + KHACH_HANG READ (GET=200, POST create=403, POST update=403)")
    void testKhachHangRuntimePermission_SalesRep_ReadLevel() throws Exception {
        // Cấu hình CSDL: SALES_REP / KHACH_HANG = READ / CA_NHAN
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("UPDATE vai_tro_module SET muc_quyen = 'READ', pham_vi_du_lieu = 'CA_NHAN' WHERE vai_tro_id = 4 AND module_id = 2");
        }
        permissionService.xoaCache();

        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);
        assertTrue(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.READ));
        assertFalse(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.WRITE));

        PhanQuyenDuLieuService pqService = mock(PhanQuyenDuLieuService.class);
        vn.nhom10.crm.service.KhachHangService khService = mock(vn.nhom10.crm.service.KhachHangService.class);
        KhachHangServlet servlet = new KhachHangServlet(pqService, khService, permissionService);

        // 1. GET /khach-hang với READ: Thành công HTTP 200, UI ẩn nút Thêm / Sửa / Xóa
        HttpServletRequest reqGet = mock(HttpServletRequest.class);
        HttpServletResponse respGet = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        when(reqGet.getSession(false)).thenReturn(session);
        when(session.getAttribute("nguoiDung")).thenReturn(sales);
        when(reqGet.getRequestDispatcher(anyString())).thenReturn(rd);
        when(reqGet.getServletPath()).thenReturn("/khach-hang");
        when(pqService.layDanhSachDuLieu(any(vn.nhom10.crm.dto.NguoiDungDTO.class), any(), any(), any())).thenReturn(new ArrayList<>());
        when(khService.timKiemVaLoc(any(vn.nhom10.crm.dto.NguoiDungDTO.class), any())).thenReturn(new ArrayList<>());

        invokeDoGet(servlet, reqGet, respGet);
        verify(respGet, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(reqGet).setAttribute("coQuyenThemKhach", false);
        verify(reqGet).setAttribute("coQuyenSuaKhach", false);
        verify(reqGet).setAttribute("coQuyenXoaKhach", false);
        verify(reqGet).getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp");

        // 2. POST create khách hàng khi chỉ có READ: Trả về HTTP 403 Forbidden
        HttpServletRequest reqCreate = mock(HttpServletRequest.class);
        HttpServletResponse respCreate = mock(HttpServletResponse.class);
        when(reqCreate.getSession(false)).thenReturn(session);
        when(reqCreate.getParameter("action")).thenReturn("them");
        when(reqCreate.getParameter("tenCongTy")).thenReturn("Công ty Mới");
        when(reqCreate.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoPost(servlet, reqCreate, respCreate);
        verify(respCreate).setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 3. POST update khách hàng khi chỉ có READ: Trả về HTTP 403 Forbidden
        HttpServletRequest reqUpdate = mock(HttpServletRequest.class);
        HttpServletResponse respUpdate = mock(HttpServletResponse.class);
        when(reqUpdate.getSession(false)).thenReturn(session);
        when(reqUpdate.getParameter("action")).thenReturn("sua");
        when(reqUpdate.getParameter("id")).thenReturn("123");
        when(reqUpdate.getParameter("tenCongTy")).thenReturn("Công ty Sửa");
        when(reqUpdate.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoPost(servlet, reqUpdate, respUpdate);
        verify(respUpdate).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Runtime Permission Khach Hang: WRITE level được GET, create, update nhưng delete bị 403")
    void testKhachHangRuntimePermission_WriteLevel() throws Exception {
        // Cấu hình CSDL: SALES_REP / KHACH_HANG = WRITE / CA_NHAN
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("UPDATE vai_tro_module SET muc_quyen = 'WRITE', pham_vi_du_lieu = 'CA_NHAN' WHERE vai_tro_id = 4 AND module_id = 2");
        }
        permissionService.xoaCache();

        NguoiDung sales = taoUser(4, "Sales Rep", "sales@crm.vn", VaiTroEnum.SALES_REP);
        assertTrue(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.WRITE));
        assertFalse(permissionService.coQuyen(sales, "KHACH_HANG", MucQuyen.FULL));

        PhanQuyenDuLieuService pqService = mock(PhanQuyenDuLieuService.class);
        vn.nhom10.crm.service.KhachHangService khService = mock(vn.nhom10.crm.service.KhachHangService.class);
        KhachHangServlet servlet = new KhachHangServlet(pqService, khService, permissionService);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("nguoiDung")).thenReturn(sales);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        // 1. GET danh sách: coQuyenThemKhach=true, coQuyenSuaKhach=true, coQuyenXoaKhach=false
        HttpServletRequest reqGet = mock(HttpServletRequest.class);
        HttpServletResponse respGet = mock(HttpServletResponse.class);
        when(reqGet.getSession(false)).thenReturn(session);
        when(reqGet.getRequestDispatcher(anyString())).thenReturn(rd);
        when(reqGet.getServletPath()).thenReturn("/khach-hang");
        when(pqService.layDanhSachDuLieu(any(vn.nhom10.crm.dto.NguoiDungDTO.class), any(), any(), any())).thenReturn(new ArrayList<>());
        when(khService.timKiemVaLoc(any(vn.nhom10.crm.dto.NguoiDungDTO.class), any())).thenReturn(new ArrayList<>());

        invokeDoGet(servlet, reqGet, respGet);
        verify(respGet, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(reqGet).setAttribute("coQuyenThemKhach", true);
        verify(reqGet).setAttribute("coQuyenSuaKhach", true);
        verify(reqGet).setAttribute("coQuyenXoaKhach", false);

        // 2. POST create: được phép (không trả về 403)
        HttpServletRequest reqCreate = mock(HttpServletRequest.class);
        HttpServletResponse respCreate = mock(HttpServletResponse.class);
        when(reqCreate.getSession(false)).thenReturn(session);
        when(reqCreate.getParameter("action")).thenReturn("them");
        when(reqCreate.getParameter("tenCongTy")).thenReturn("Công ty Test WRITE");
        when(reqCreate.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoPost(servlet, reqCreate, respCreate);
        verify(respCreate, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 3. POST update: được phép (không trả về 403)
        HttpServletRequest reqUpdate = mock(HttpServletRequest.class);
        HttpServletResponse respUpdate = mock(HttpServletResponse.class);
        when(reqUpdate.getSession(false)).thenReturn(session);
        when(reqUpdate.getParameter("action")).thenReturn("sua");
        when(reqUpdate.getParameter("id")).thenReturn("555");
        when(reqUpdate.getParameter("tenCongTy")).thenReturn("Công ty Đã Sửa");
        when(reqUpdate.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoPost(servlet, reqUpdate, respUpdate);
        verify(respUpdate, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);

        // 4. POST delete: WRITE không có FULL nên BỊ CHẶN 403 Forbidden!
        HttpServletRequest reqDelete = mock(HttpServletRequest.class);
        HttpServletResponse respDelete = mock(HttpServletResponse.class);
        when(reqDelete.getSession(false)).thenReturn(session);
        when(reqDelete.getParameter("action")).thenReturn("xoa");
        when(reqDelete.getParameter("id")).thenReturn("555");
        when(reqDelete.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoPost(servlet, reqDelete, respDelete);
        verify(respDelete).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("Runtime Permission Khach Hang: FULL level được delete thành công")
    void testKhachHangRuntimePermission_FullLevel_DeleteThanhCong() throws Exception {
        // Cấu hình CSDL: TEAM_LEAD / KHACH_HANG = FULL / NHOM (đúng trần Sheet 2)
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.execute("UPDATE vai_tro_module SET muc_quyen = 'FULL', pham_vi_du_lieu = 'NHOM' WHERE vai_tro_id = 3 AND module_id = 2");
        }
        permissionService.xoaCache();

        NguoiDung leader = taoUser(3, "Team Lead", "lead@crm.vn", VaiTroEnum.TEAM_LEAD);
        assertTrue(permissionService.coQuyen(leader, "KHACH_HANG", MucQuyen.FULL));

        PhanQuyenDuLieuService pqService = mock(PhanQuyenDuLieuService.class);
        vn.nhom10.crm.service.KhachHangService khService = mock(vn.nhom10.crm.service.KhachHangService.class);
        when(khService.xoaKhachHang(eq(999L), any(NguoiDung.class))).thenReturn(true);
        KhachHangServlet servlet = new KhachHangServlet(pqService, khService, permissionService);

        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("nguoiDung")).thenReturn(leader);
        RequestDispatcher rd = mock(RequestDispatcher.class);

        // 1. GET danh sách: coQuyenXoaKhach=true
        HttpServletRequest reqGet = mock(HttpServletRequest.class);
        HttpServletResponse respGet = mock(HttpServletResponse.class);
        when(reqGet.getSession(false)).thenReturn(session);
        when(reqGet.getRequestDispatcher(anyString())).thenReturn(rd);
        when(reqGet.getServletPath()).thenReturn("/khach-hang");
        when(pqService.layDanhSachDuLieu(any(vn.nhom10.crm.dto.NguoiDungDTO.class), any(), any(), any())).thenReturn(new ArrayList<>());
        when(khService.timKiemVaLoc(any(vn.nhom10.crm.dto.NguoiDungDTO.class), any())).thenReturn(new ArrayList<>());

        invokeDoGet(servlet, reqGet, respGet);
        verify(reqGet).setAttribute("coQuyenXoaKhach", true);

        // 2. POST delete với FULL: Thành công, không bị 403
        HttpServletRequest reqDelete = mock(HttpServletRequest.class);
        HttpServletResponse respDelete = mock(HttpServletResponse.class);
        when(reqDelete.getSession(false)).thenReturn(session);
        when(reqDelete.getParameter("action")).thenReturn("xoa");
        when(reqDelete.getParameter("id")).thenReturn("999");
        when(reqDelete.getRequestDispatcher(anyString())).thenReturn(rd);

        invokeDoPost(servlet, reqDelete, respDelete);
        verify(respDelete, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(khService).xoaKhachHang(eq(999L), eq(leader));
    }

    private void invokeDoGet(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method m = findMethod(servlet.getClass(), "doGet", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(servlet, req, resp);
    }

    private void invokeDoPost(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Method m = findMethod(servlet.getClass(), "doPost", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(servlet, req, resp);
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }
}
