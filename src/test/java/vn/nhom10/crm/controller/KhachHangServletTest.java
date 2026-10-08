package vn.nhom10.crm.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vn.nhom10.crm.dao.KhachHangDAO;
import vn.nhom10.crm.dto.BanGhiNghiepVuDTO;
import vn.nhom10.crm.model.KhachHang;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.VaiTro;
import vn.nhom10.crm.model.VaiTroEnum;
import vn.nhom10.crm.service.DanhMucBanHangService;
import vn.nhom10.crm.service.KhachHangService;
import vn.nhom10.crm.service.PhanQuyenDuLieuService;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Kiểm thử KhachHangServlet - Quản lý hồ sơ khách hàng doanh nghiệp & Data Scope (Story S3-01 & S1-05)")
class KhachHangServletTest {

    private KhachHangServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;
    private RequestDispatcher dispatcher;

    private PhanQuyenDuLieuService phanQuyenService;
    private KhachHangService khachHangService;
    private KhachHangDAO khachHangDAO;

    private NguoiDung userA;
    private NguoiDung userLeadBac;

    @BeforeEach
    void setUp() {
        phanQuyenService = new PhanQuyenDuLieuService(null);
        khachHangDAO = mock(KhachHangDAO.class);
        khachHangService = spy(new KhachHangService(khachHangDAO));
        servlet = new KhachHangServlet(phanQuyenService, khachHangService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
        dispatcher = mock(RequestDispatcher.class);

        when(request.getContextPath()).thenReturn("/crm");
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // User A: Nhân viên kinh doanh (Sales Rep - Nhóm Miền Bắc, ID = 101)
        userA = new NguoiDung();
        userA.setId(101L);
        userA.setHoTen("Nguyễn Văn A (Sales)");
        userA.setEmail("sales.a@crm.vn");
        userA.setNhomKinhDoanhId(1);
        userA.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        userA.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.SALES_REP, PhamViDuLieu.CA_NHAN)));

        // User Lead: Trưởng nhóm kinh doanh (Team Lead - Nhóm Miền Bắc, ID = 100, scope NHOM)
        userLeadBac = new NguoiDung();
        userLeadBac.setId(100L);
        userLeadBac.setHoTen("Lê Thị Trưởng Nhóm");
        userLeadBac.setEmail("lead.bac@crm.vn");
        userLeadBac.setNhomKinhDoanhId(1);
        userLeadBac.setTenNhomKinhDoanh("Nhóm Miền Bắc");
        userLeadBac.setDanhSachVaiTro(Collections.singleton(new VaiTro(VaiTroEnum.TEAM_LEAD, PhamViDuLieu.NHOM)));
    }

    @Test
    @DisplayName("Bảo mật: Chưa đăng nhập truy cập /khach-hang bị chuyển hướng về /dang-nhap")
    void testKhachHang_ChuaDangNhap_RedirectDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("AC1 & AC2: User A (CA_NHAN) truy cập /khach-hang chỉ thấy khách của mình, không thấy khách của B")
    void testKhachHang_NhanVienA_XemDanhSach_ChiThayKhachCuaMinh() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("danhSachKhachHang"), argThat(list -> {
            List<?> ds = (List<?>) list;
            assertFalse(ds.isEmpty());
            for (Object obj : ds) {
                BanGhiNghiepVuDTO bg = (BanGhiNghiepVuDTO) obj;
                assertEquals(101L, bg.getNguoiPhuTrachId(), "Mọi khách hàng trả về phải do A phụ trách");
                assertNotEquals(102L, bg.getNguoiPhuTrachId(), "Khách hàng của B tuyệt đối không được xuất hiện");
            }
            return true;
        }));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC3 & AC4: User A cố tình gõ URL /khach-hang?id=5 (khách của B) bị từ chối với HTTP 403")
    void testKhachHang_NhanVienA_XemChiTiet_KhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("5"); // ID 5 là khách Viettel của Sales B (102)

        servlet.doGet(request, response);

        // Bắt buộc chặn với HTTP 403 Forbidden
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối truy cập"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC3: User A truy cập URL /khach-hang?id=1 (khách của chính mình) trả về HTTP 200")
    void testKhachHang_NhanVienA_XemChiTiet_KhachCuaMinh_TraVe200() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("1"); // ID 1 là FPT của chính A (101)

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("banGhiChiTiet"), any(BanGhiNghiepVuDTO.class));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Security & AC3: User A cố tình gửi POST sửa /khach-hang?id=5 (khách của B) bị chặn HTTP 403")
    void testKhachHang_NhanVienA_SuaKhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("action")).thenReturn("sua");
        when(request.getParameter("tieuDe")).thenReturn("Sửa trộm dữ liệu");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối thao tác sửa"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("AC1: Trưởng nhóm kinh doanh (NHOM) xem khách hàng của B (cùng nhóm) trả về HTTP 200")
    void testKhachHang_TruongNhom_XemKhachTrongNhom_TraVe200() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userLeadBac);
        when(request.getParameter("id")).thenReturn("5"); // Khách hàng của B trong nhóm 1

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("banGhiChiTiet"), any(BanGhiNghiepVuDTO.class));
    }

    @Test
    @DisplayName("AC1: Trưởng nhóm kinh doanh cố tình xem khách hàng của C (nhóm 2 khác) bị chặn HTTP 403")
    void testKhachHang_TruongNhom_XemKhachNhomKhac_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userLeadBac);
        when(request.getParameter("id")).thenReturn("9"); // Khách hàng của C trong nhóm 2 (Miền Nam)

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("không nằm trong phạm vi nhóm quản lý của bạn"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("S1-05: Thêm khách hàng thành công và tự động gán quyền sở hữu cho người dùng hiện tại")
    void testThemKhachHang_ThanhCong_TuDongGanNguoiSoHuu() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Tập đoàn Công nghệ CMC");
        when(request.getParameter("maKhachHang")).thenReturn("KH-CMC-01");
        when(request.getParameter("doanhThuUocTinh")).thenReturn("250,000,000 đ");
        when(request.getParameter("trangThai")).thenReturn("Tiềm năng");
        when(request.getParameter("moTaChiTiet")).thenReturn("Khách hàng doanh nghiệp viễn thông CNTT");

        servlet.doPost(request, response);

        // Xác nhận thông báo thành công và bản ghi mới được tạo gắn với A (101)
        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Tập đoàn Công nghệ CMC"));
        verify(request).setAttribute(eq("khachHangVuaThem"), argThat(arg -> {
            BanGhiNghiepVuDTO bg = (BanGhiNghiepVuDTO) arg;
            assertEquals(101L, bg.getNguoiPhuTrachId(), "Chủ sở hữu phải tự động gán là User A (101)");
            assertEquals("KH-CMC-01", bg.getMaBanGhi());
            assertEquals("Tập đoàn Công nghệ CMC", bg.getTieuDe());
            return true;
        }));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/danh-sach.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("S1-05 Bảo mật: Chặn client-side owner spoofing - luôn ép quyền sở hữu về session user")
    void testThemKhachHang_NganChanGiaMaoNguoiSoHuu() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty Cổ phần MISA");
        when(request.getParameter("maKhachHang")).thenReturn("KH-MISA-99");
        when(request.getParameter("nguoiSoHuuId")).thenReturn("999");
        when(request.getParameter("nguoi_so_huu_id")).thenReturn("102");
        when(request.getParameter("nguoiPhuTrachId")).thenReturn("999");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("khachHangVuaThem"), argThat(arg -> {
            BanGhiNghiepVuDTO bg = (BanGhiNghiepVuDTO) arg;
            assertNotEquals(999L, bg.getNguoiPhuTrachId(), "Tuyệt đối không nhận ID giả mạo từ client");
            assertNotEquals(102L, bg.getNguoiPhuTrachId(), "Tuyệt đối không nhận ID người khác từ client");
            assertEquals(101L, bg.getNguoiPhuTrachId(), "Server bắt buộc lấy chủ sở hữu từ session user A (101)");
            return true;
        }));
    }

    @Test
    @DisplayName("S1-05 Bảo mật: Chưa đăng nhập gửi POST tạo khách hàng bị từ chối chuyển hướng đăng nhập")
    void testThemKhachHang_ChuaDangNhap_RedirectDangNhap() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Doanh Nghiệp Hacker");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/crm/dang-nhap?error=auth_required");
    }

    @Test
    @DisplayName("S1-05 Server-side validation: Tên công ty rỗng bị từ chối với HTTP 400 Bad Request")
    void testThemKhachHang_TenCongTyRong_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("   "); // Trắng

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("không được để trống"));
    }

    @Test
    @DisplayName("S1-05 Server-side validation: Trùng mã khách hàng đã có bị từ chối với HTTP 400 Bad Request")
    void testThemKhachHang_TrungMaKhachHang_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Khách Hàng Trùng Mã");
        when(request.getParameter("maKhachHang")).thenReturn("KH-001");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("đã tồn tại"));
    }

    @Test
    @DisplayName("S3-01 AC1: Thêm khách hàng với đầy đủ tên công ty, mã số thuế, ngành nghề, quy mô, website, địa chỉ")
    void testThemKhachHang_S3_01_DayDuCacTruongAC1() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty Cổ phần Công nghệ Alpha");
        when(request.getParameter("maSoThue")).thenReturn("0108877665");
        when(request.getParameter("nganhNgheId")).thenReturn("1");
        when(request.getParameter("quyMoId")).thenReturn("2");
        when(request.getParameter("website")).thenReturn("https://alpha.com.vn");
        when(request.getParameter("diaChi")).thenReturn("123 Phố Duy Tân, Cầu Giấy, Hà Nội");
        when(request.getParameter("trangThai")).thenReturn("Đang giao dịch");

        KhachHang khTraVe = new KhachHang();
        khTraVe.setId(88L);
        khTraVe.setTenCongTy("Công ty Cổ phần Công nghệ Alpha");
        khTraVe.setMaSoThue("0108877665");
        khTraVe.setNguoiSoHuuId(101L);
        doReturn(khTraVe).when(khachHangService).taoKhachHang(any(NguoiDung.class), any(KhachHang.class));

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Công ty Cổ phần Công nghệ Alpha"));
        verify(request).setAttribute(eq("khachHangMoi"), any(KhachHang.class));
    }

    @Test
    @DisplayName("S3-01 AC2: Trùng mã số thuế bị từ chối với HTTP 400 Bad Request")
    void testThemKhachHang_S3_01_TrungMaSoThue_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty Trùng MST");
        when(request.getParameter("maSoThue")).thenReturn("0101234567");

        doThrow(new IllegalArgumentException("Mã số thuế '0101234567' đã tồn tại trong hệ thống (thuộc khách hàng 'Công ty ABC')."))
                .when(khachHangService).taoKhachHang(any(NguoiDung.class), any(KhachHang.class));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Mã số thuế '0101234567' đã tồn tại trong hệ thống"));
    }

    @Test
    @DisplayName("S3-01 AC3: Trạng thái không hợp lệ bị từ chối với HTTP 400 Bad Request")
    void testThemKhachHang_S3_01_TrangThaiKhongHopLe_TraVe400() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("them");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty Sai Trạng Thái");
        when(request.getParameter("trangThai")).thenReturn("TrangThaiBatHopLe");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Trạng thái khách hàng không hợp lệ"));
    }

    @Test
    @DisplayName("Khách hàng: Xem chi tiết với ID không tồn tại trả về HTTP 404 Not Found")
    void testKhachHang_XemChiTiet_IdKhongTonTai_TraVe404() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("id")).thenReturn("999999");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }

    @Test
    @DisplayName("Khách hàng Export: Sales Rep A xuất XLSX chỉ chứa khách hàng cá nhân (FPT), không lộ B, C")
    void testKhachHang_XuatExcel_Xlsx_SalesRepA() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("xuatExcel")).thenReturn("true");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
            @Override public void write(int b) throws IOException { baos.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        servlet.doGet(request, response);

        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), contains("du-lieu-khach-hang-ca_nhan.xlsx"));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()))) {
            Sheet sheet = wb.getSheet("Du lieu CRM");
            assertNotNull(sheet);
            boolean hasFPT = false;
            boolean hasViettel = false;
            boolean hasVNG = false;
            for (Row row : sheet) {
                for (Cell cell : row) {
                    String str = cell.toString();
                    if (str.contains("FPT")) hasFPT = true;
                    if (str.contains("Viettel")) hasViettel = true;
                    if (str.contains("VNG")) hasVNG = true;
                }
            }
            assertTrue(hasFPT, "File xuất của A phải chứa khách hàng của A (FPT)");
            assertFalse(hasViettel, "File xuất của A tuyệt đối không được chứa khách hàng của B (Viettel)");
            assertFalse(hasVNG, "File xuất của A tuyệt đối không được chứa khách hàng của C (VNG)");
        }
    }

    @Test
    @DisplayName("Khách hàng Export: Team Lead Bắc xuất XLSX chứa dữ liệu nhóm (FPT, Viettel), không chứa VNG")
    void testKhachHang_XuatExcel_Xlsx_TeamLeadBac() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userLeadBac);
        when(request.getParameter("xuatExcel")).thenReturn("true");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
            @Override public void write(int b) throws IOException { baos.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        servlet.doGet(request, response);

        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verify(response).setHeader(eq("Content-Disposition"), contains("du-lieu-khach-hang-nhom.xlsx"));

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()))) {
            Sheet sheet = wb.getSheet("Du lieu CRM");
            assertNotNull(sheet);
            boolean hasFPT = false;
            boolean hasViettel = false;
            boolean hasVNG = false;
            for (Row row : sheet) {
                for (Cell cell : row) {
                    String str = cell.toString();
                    if (str.contains("FPT")) hasFPT = true;
                    if (str.contains("Viettel")) hasViettel = true;
                    if (str.contains("VNG")) hasVNG = true;
                }
            }
            assertTrue(hasFPT, "Trưởng nhóm Bắc phải thấy FPT trong nhóm");
            assertTrue(hasViettel, "Trưởng nhóm Bắc phải thấy Viettel trong nhóm");
            assertFalse(hasVNG, "Trưởng nhóm Bắc không được thấy VNG (nhóm Nam)");
        }
    }

    @Test
    @DisplayName("Khách hàng Export: Định dạng xuất là XLSX, không dùng CSV")
    void testKhachHang_XuatExcel_KhongDungCsv() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("xuatExcel")).thenReturn("true");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener writeListener) {}
            @Override public void write(int b) throws IOException { baos.write(b); }
        };
        when(response.getOutputStream()).thenReturn(sos);

        servlet.doGet(request, response);

        verify(response, never()).setContentType("text/csv; charset=UTF-8");
        verify(response).setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    @DisplayName("S3-09: Đánh dấu đã liên hệ khách hàng chăm sóc định kỳ qua action=danhDauLienHe")
    void testKhachHang_DanhDauLienHe_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("action")).thenReturn("danhDauLienHe");
        when(request.getParameter("khachHangId")).thenReturn("1");
        when(request.getParameter("tenCongTy")).thenReturn("Công ty FPT");
        when(request.getParameter("ghiChu")).thenReturn("Đã gọi điện hỏi thăm dịch vụ");
        when(request.getParameter("kenhLienHe")).thenReturn("CUOC_GOI");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("thongBaoThanhCong"), contains("Đã đánh dấu liên hệ thành công"));
        verify(request).setAttribute(eq("tabHienTai"), eq("cham-soc"));
    }

    @Test
    @DisplayName("S3-09: doGet tiếp nhận tham số tab=cham-soc và soNgay cấu hình chu kỳ")
    void testKhachHang_TabChamSoc_ThanhCong() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getParameter("tab")).thenReturn("cham-soc");
        when(request.getParameter("soNgay")).thenReturn("45");

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("tabHienTai"), eq("cham-soc"));
        verify(request).setAttribute(eq("soNgayCauHinh"), eq("45"));
    }

    @Test
    @DisplayName("S3-03 AC1 & AC2: User A truy cập /khach-hang/chi-tiet?id=1 hiển thị view chi-tiet.jsp (Trang 360)")
    void testKhachHang_NhanVienA_Xem360_KhachCuaMinh_ChuyenHuongChiTietJsp() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("id")).thenReturn("1"); // FPT thuộc về A

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(request).setAttribute(eq("banGhiChiTiet"), any(BanGhiNghiepVuDTO.class));
        verify(request).getRequestDispatcher("/WEB-INF/views/khach-hang/chi-tiet.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("S3-03 Data Scope: User A cố tình truy cập /khach-hang/chi-tiet?id=5 (khách của B) bị chặn 403")
    void testKhachHang_NhanVienA_Xem360_KhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("id")).thenReturn("5"); // Viettel thuộc về B

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(request).setAttribute(eq("thongBaoLoi"), contains("Từ chối truy cập"));
        verify(request).getRequestDispatcher("/WEB-INF/views/phan-quyen/ngoai-pham-vi.jsp");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("S3-03: Truy cập /khach-hang/chi-tiet không truyền id sẽ redirect về /khach-hang")
    void testKhachHang_Xem360_KhongTruyenId_RedirectDanhSach() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("id")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/crm/khach-hang");
    }

    @Test
    @DisplayName("S3-03 AC3: API lấy danh sách hoạt động JSON trả về dữ liệu chuẩn khi có quyền")
    void testKhachHang_ApiHoatDong_TraVeJson() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("action")).thenReturn("api-hoat-dong");
        when(request.getParameter("id")).thenReturn("1");

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doGet(request, response);

        verify(response).setContentType(startsWith("application/json"));
        String json = sw.toString();
        assertTrue(json.startsWith("[") && json.endsWith("]"), "Kết quả trả về phải là mảng JSON");
    }

    @Test
    @DisplayName("S3-03 Data Scope: API hoạt động chặn khách ngoài phạm vi (HTTP 403)")
    void testKhachHang_ApiHoatDong_KhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("action")).thenReturn("api-hoat-dong");
        when(request.getParameter("id")).thenReturn("5"); // Viettel thuộc B

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("S3-03: Gửi POST thêm hoạt động mới qua /khach-hang/chi-tiet thành công")
    void testKhachHang_ThemHoatDong_PostThanhCong() throws Exception {
        vn.nhom10.crm.service.KhachHang360Service mockService = mock(vn.nhom10.crm.service.KhachHang360Service.class);
        vn.nhom10.crm.model.HoatDong mockHd = new vn.nhom10.crm.model.HoatDong();
        mockHd.setId(99L);
        mockHd.setTieuDe("Trao đổi báo giá");
        when(mockService.themHoatDong(anyLong(), any(), any(), any(), any(), any())).thenReturn(mockHd);

        KhachHangServlet servletWithMock = new KhachHangServlet(new PhanQuyenDuLieuService(null), mockService);

        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("action")).thenReturn("them-hoat-dong");
        when(request.getParameter("idKhachHang")).thenReturn("1");
        when(request.getParameter("loai")).thenReturn("CUOC_GOI");
        when(request.getParameter("tieuDe")).thenReturn("Trao đổi báo giá");
        when(request.getParameter("noiDung")).thenReturn("Đã chốt cấu hình");

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servletWithMock.doPost(request, response);

        verify(response).setContentType(startsWith("application/json"));
        String res = sw.toString();
        assertTrue(res.contains("\"success\":true"), "Phải trả về success: true");
    }

    @Test
    @DisplayName("S3-03 Data Scope: Gửi POST thêm hoạt động vào khách ngoài phạm vi bị chặn HTTP 403")
    void testKhachHang_ThemHoatDong_KhachCuaB_TraVe403() throws Exception {
        when(session.getAttribute("nguoiDung")).thenReturn(userA);
        when(request.getServletPath()).thenReturn("/khach-hang/chi-tiet");
        when(request.getParameter("action")).thenReturn("them-hoat-dong");
        when(request.getParameter("idKhachHang")).thenReturn("5"); // Viettel thuộc B
        when(request.getParameter("tieuDe")).thenReturn("Ghi nhận ngoài luồng");

        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
