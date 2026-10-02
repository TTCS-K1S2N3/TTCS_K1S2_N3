package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dto.KetQuaKhoaVaBanGiaoDTO;
import vn.nhom10.crm.dto.ThongTinBanGiaoDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhatKyBanGiao;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.KhoaTaiKhoanService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử KhoaTaiKhoanServlet (Controller S1-10)")
class KhoaTaiKhoanServletTest {

    private KhoaTaiKhoanServlet servlet;
    private FakeKhoaTaiKhoanService fakeService;

    private NguoiDung adminUser;
    private NguoiDung salesUser;

    @BeforeEach
    void setUp() {
        servlet = new KhoaTaiKhoanServlet();
        fakeService = new FakeKhoaTaiKhoanService();
        servlet.setKhoaTaiKhoanService(fakeService);

        adminUser = new NguoiDung(1, "Nguyễn Quản Trị", "admin@crm.vn");
        adminUser.themVaiTro(VaiTroEnum.ADMIN);
        adminUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);

        salesUser = new NguoiDung(4, "Thào A Khua", "sales@crm.vn");
        salesUser.themVaiTro(VaiTroEnum.SALES_REP);
        salesUser.setTrangThai(NguoiDung.TRANG_THAI_HOAT_DONG);
    }

    @Test
    @DisplayName("Quyền truy cập: Từ chối người dùng chưa đăng nhập (Chuyển hướng đến /dang-nhap)")
    void testDoGet_TuChoiChuaDangNhap() throws ServletException, IOException {
        Map<String, Object> sessionAttrs = new HashMap<>(); // Chưa đăng nhập

        AtomicInteger statusHolder = new AtomicInteger(200);
        String[] redirectHolder = new String[1];
        HttpServletRequest request = taoFakeRequest("GET", new HashMap<>(), sessionAttrs, statusHolder, redirectHolder);
        HttpServletResponse response = taoFakeResponse(statusHolder, redirectHolder);

        servlet.doGet(request, response);

        assertTrue(redirectHolder[0] != null && redirectHolder[0].contains("/dang-nhap"));
    }

    @Test
    @DisplayName("Quyền truy cập: Từ chối người dùng không phải ADMIN (Trả về 403 Forbidden)")
    void testDoGet_TuChoiNguoiDungKhongPhaiAdmin() throws ServletException, IOException {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("nguoiDung", salesUser); // Non-admin

        AtomicInteger statusHolder = new AtomicInteger(200);
        String[] redirectHolder = new String[1];
        HttpServletRequest request = taoFakeRequest("GET", new HashMap<>(), sessionAttrs, statusHolder, redirectHolder);
        HttpServletResponse response = taoFakeResponse(statusHolder, redirectHolder);

        servlet.doGet(request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, statusHolder.get());
    }

    @Test
    @DisplayName("Quyền truy cập: Cho phép ADMIN xem thông tin bàn giao")
    void testDoGet_ChoPhepAdmin_NapThongTin() throws ServletException, IOException {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("nguoiDung", adminUser);

        Map<String, String> params = new HashMap<>();
        params.put("id", "9");

        AtomicInteger statusHolder = new AtomicInteger(200);
        String[] redirectHolder = new String[1];
        HttpServletRequest request = taoFakeRequest("GET", params, sessionAttrs, statusHolder, redirectHolder);
        HttpServletResponse response = taoFakeResponse(statusHolder, redirectHolder);

        servlet.doGet(request, response);

        assertEquals(200, statusHolder.get());
        assertNotNull(request.getAttribute("thongTinBanGiao"));
    }

    @Test
    @DisplayName("POST: ADMIN thực hiện bàn giao thành công qua form")
    void testDoPost_AdminThucHienBanGiaoThanhCong() throws ServletException, IOException {
        Map<String, Object> sessionAttrs = new HashMap<>();
        sessionAttrs.put("nguoiDung", adminUser);

        Map<String, String> params = new HashMap<>();
        params.put("nguoiBiKhoaId", "9");
        params.put("nguoiTiepNhanId", "4");
        params.put("lyDo", "Nghỉ việc");

        AtomicInteger statusHolder = new AtomicInteger(200);
        String[] redirectHolder = new String[1];
        HttpServletRequest request = taoFakeRequest("POST", params, sessionAttrs, statusHolder, redirectHolder);
        HttpServletResponse response = taoFakeResponse(statusHolder, redirectHolder);

        servlet.doPost(request, response);

        KetQuaKhoaVaBanGiaoDTO ketQua = (KetQuaKhoaVaBanGiaoDTO) request.getAttribute("ketQua");
        assertNotNull(ketQua);
        assertTrue(ketQua.isThanhCong());
        assertEquals(5, ketQua.getSoKhachHangChuyen());
        assertEquals(3, ketQua.getSoCoHoiChuyen());
    }

    // Helper tạo fake Servlet Request & Response qua Proxy
    private HttpServletRequest taoFakeRequest(String method, Map<String, String> params,
                                              Map<String, Object> sessionAttrs, AtomicInteger statusHolder,
                                              String[] redirectHolder) {
        Map<String, Object> reqAttrs = new HashMap<>();

        HttpSession fakeSession = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, m, args) -> {
                    if ("getAttribute".equals(m.getName())) {
                        return sessionAttrs.get(args[0]);
                    }
                    if ("setAttribute".equals(m.getName())) {
                        sessionAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getId".equals(m.getName())) {
                        return "fake-session-test";
                    }
                    return null;
                }
        );

        RequestDispatcher fakeDispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, m, args) -> null
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) return method;
                    if ("getParameter".equals(name)) return params.get(args[0]);
                    if ("getSession".equals(name)) {
                        boolean create = args.length == 0 || Boolean.TRUE.equals(args[0]);
                        return (sessionAttrs.isEmpty() && !create) ? null : fakeSession;
                    }
                    if ("getAttribute".equals(name)) return reqAttrs.get(args[0]);
                    if ("setAttribute".equals(name)) {
                        reqAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getContextPath".equals(name)) return "";
                    if ("getRequestDispatcher".equals(name)) return fakeDispatcher;
                    if ("getHeader".equals(name)) return null;
                    if ("setCharacterEncoding".equals(name)) return null;
                    return null;
                }
        );
    }

    private HttpServletResponse taoFakeResponse(AtomicInteger statusHolder, String[] redirectHolder) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("setStatus".equals(name)) {
                        statusHolder.set((Integer) args[0]);
                        return null;
                    }
                    if ("sendError".equals(name)) {
                        statusHolder.set((Integer) args[0]);
                        return null;
                    }
                    if ("sendRedirect".equals(name)) {
                        redirectHolder[0] = (String) args[0];
                        return null;
                    }
                    if ("getWriter".equals(name)) return pw;
                    if ("setContentType".equals(name)) return null;
                    if ("setCharacterEncoding".equals(name)) return null;
                    return null;
                }
        );
    }

    static class FakeKhoaTaiKhoanService extends KhoaTaiKhoanService {
        @Override
        public ThongTinBanGiaoDTO layThongTinBanGiao(int nguoiBiKhoaId) {
            NguoiDung u = new NguoiDung(nguoiBiKhoaId, "Nhân Viên Sắp Nghỉ", "nhanvien_nghi@crm.vn");
            return new ThongTinBanGiaoDTO(u, 5, 3, new ArrayList<>());
        }

        @Override
        public KetQuaKhoaVaBanGiaoDTO khoaVaBanGiao(int nguoiBiKhoaId, Integer nguoiTiepNhanId, int nguoiThucHienId, String lyDo) {
            if (nguoiTiepNhanId == null || nguoiTiepNhanId <= 0) {
                return new KetQuaKhoaVaBanGiaoDTO(false, "Bắt buộc chọn người tiếp nhận.");
            }
            return new KetQuaKhoaVaBanGiaoDTO(true, "Khoá tài khoản và bàn giao thành công!", 5, 3, 101);
        }

        @Override
        public List<NhatKyBanGiao> layLichSuBanGiao(int limit) {
            return new ArrayList<>();
        }
    }
}
