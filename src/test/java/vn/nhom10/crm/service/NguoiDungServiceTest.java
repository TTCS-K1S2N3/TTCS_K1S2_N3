package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.NguoiDungDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.VaiTroDAO;
import vn.nhom10.crm.dto.KetQuaNguoiDungDTO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.NhomKinhDoanh;
import vn.nhom10.crm.model.VaiTro;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử nghiệp vụ NguoiDungService (Story S1-08)")
class NguoiDungServiceTest {

    @Mock
    private NguoiDungDAO nguoiDungDAO;

    @Mock
    private VaiTroDAO vaiTroDAO;

    @Mock
    private NhomKinhDoanhDAO nhomKinhDoanhDAO;

    @Mock
    private EmailService emailService;

    private NguoiDungService nguoiDungService;

    @BeforeEach
    void setUp() {
        nguoiDungService = new NguoiDungService(nguoiDungDAO, vaiTroDAO, nhomKinhDoanhDAO, emailService);
    }

    @Test
    @DisplayName("AC: Tạo tài khoản thành công, gửi email kích hoạt kèm mật khẩu tạm và trạng thái CHO_KICH_HOAT")
    void testTaoTaiKhoanThanhCong() throws Exception {
        NguoiDung nd = new NguoiDung();
        nd.setHoTen("Trần Thị Hoa");
        nd.setEmail("hoatt@crm.vn");
        nd.setNhomKinhDoanhId(1);
        List<Integer> dsVaiTroIds = Collections.singletonList(4); // SALES_REP

        when(nguoiDungDAO.kiemTraEmailTonTai(eq("hoatt@crm.vn"), isNull())).thenReturn(false);
        when(nguoiDungDAO.themNguoiDung(any(NguoiDung.class), eq(dsVaiTroIds))).thenReturn(10);
        when(emailService.guiEmailKichHoatTaiKhoan(eq("hoatt@crm.vn"), eq("Trần Thị Hoa"), anyString(), anyString())).thenReturn(true);

        KetQuaNguoiDungDTO ketQua = nguoiDungService.taoTaiKhoan(nd, dsVaiTroIds, "http://localhost:8080/crm");

        assertTrue(ketQua.isThanhCong());
        assertNotNull(ketQua.getMatKhauTam(), "Mật khẩu tạm phải được sinh ngẫu nhiên");
        assertTrue(ketQua.getMatKhauTam().length() >= 8);
        assertEquals(NguoiDung.TRANG_THAI_CHO_KICH_HOAT, nd.getTrangThai());
        assertNotNull(nd.getMatKhau(), "Mật khẩu phải được băm BCrypt");

        verify(emailService, times(1)).guiEmailKichHoatTaiKhoan(
                eq("hoatt@crm.vn"),
                eq("Trần Thị Hoa"),
                eq(ketQua.getMatKhauTam()),
                contains("/dang-nhap")
        );
    }

    @Test
    @DisplayName("AC: Email trùng bị từ chối kèm thông báo cụ thể")
    void testTaoTaiKhoanEmailTrung() {
        NguoiDung nd = new NguoiDung();
        nd.setHoTen("Trần Thị Hoa");
        nd.setEmail("admin@crm.vn"); // Đã có trong hệ thống
        nd.setNhomKinhDoanhId(1);
        List<Integer> dsVaiTroIds = Collections.singletonList(4);

        when(nguoiDungDAO.kiemTraEmailTonTai(eq("admin@crm.vn"), isNull())).thenReturn(true);

        KetQuaNguoiDungDTO ketQua = nguoiDungService.taoTaiKhoan(nd, dsVaiTroIds, "http://localhost:8080/crm");

        assertFalse(ketQua.isThanhCong(), "Tạo tài khoản với email trùng phải thất bại");
        assertEquals("EMAIL_TRUNG", ketQua.getDanhSachLoi().get("email"));
        assertTrue(ketQua.getThongBao().contains("admin@crm.vn"));
        assertTrue(ketQua.getThongBao().contains("đã được sử dụng"));

        // Tuyệt đối không thêm vào DB hay gửi email khi email bị trùng
        try {
            verify(nguoiDungDAO, never()).themNguoiDung(any(), any());
        } catch (Exception ignored) {
        }
        verify(emailService, never()).guiEmailKichHoatTaiKhoan(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Validation dữ liệu tạo tài khoản: thiếu họ tên, sai email, không có vai trò")
    void testTaoTaiKhoanValidationLoi() {
        // Trường hợp 1: Thiếu họ tên
        NguoiDung nd1 = new NguoiDung();
        nd1.setHoTen("   ");
        nd1.setEmail("valid@crm.vn");
        KetQuaNguoiDungDTO kq1 = nguoiDungService.taoTaiKhoan(nd1, Collections.singletonList(4), "");
        assertFalse(kq1.isThanhCong());
        assertTrue(kq1.getDanhSachLoi().containsKey("hoTen"));

        // Trường hợp 2: Định dạng email không hợp lệ
        NguoiDung nd2 = new NguoiDung();
        nd2.setHoTen("Tên Đúng");
        nd2.setEmail("email_sai_dinh_dang");
        KetQuaNguoiDungDTO kq2 = nguoiDungService.taoTaiKhoan(nd2, Collections.singletonList(4), "");
        assertFalse(kq2.isThanhCong());
        assertTrue(kq2.getDanhSachLoi().containsKey("email"));

        // Trường hợp 3: Không chọn vai trò
        NguoiDung nd3 = new NguoiDung();
        nd3.setHoTen("Tên Đúng");
        nd3.setEmail("valid@crm.vn");
        KetQuaNguoiDungDTO kq3 = nguoiDungService.taoTaiKhoan(nd3, Collections.emptyList(), "");
        assertFalse(kq3.isThanhCong());
        assertTrue(kq3.getDanhSachLoi().containsKey("vaiTro"));
    }

    @Test
    @DisplayName("Cập nhật tài khoản: Bị từ chối khi sửa thành email của người khác")
    void testCapNhatTaiKhoanEmailTrung() {
        NguoiDung nd = new NguoiDung();
        nd.setId(5);
        nd.setHoTen("Người dùng 5");
        nd.setEmail("admin@crm.vn"); // Đổi sang email của id 1
        List<Integer> dsVaiTroIds = Collections.singletonList(4);

        when(nguoiDungDAO.kiemTraEmailTonTai(eq("admin@crm.vn"), eq(5))).thenReturn(true);

        KetQuaNguoiDungDTO ketQua = nguoiDungService.capNhatTaiKhoan(nd, dsVaiTroIds);

        assertFalse(ketQua.isThanhCong());
        assertEquals("EMAIL_TRUNG", ketQua.getDanhSachLoi().get("email"));
        try {
            verify(nguoiDungDAO, never()).capNhatNguoiDung(any(), any());
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("AC: Danh sách phân trang mặc định 20 dòng và tìm kiếm theo tên, email, nhóm, vai trò, trạng thái")
    void testLayDanhSachNguoiDungPhanTrang() {
        when(nguoiDungDAO.timKiemVaPhanTrang("Hoa", 2, 4, "HOAT_DONG", 20, 0))
                .thenReturn(Arrays.asList(new NguoiDung(1, "Hoa", "hoa@crm.vn")));

        List<NguoiDung> ds = nguoiDungService.layDanhSachNguoiDung("Hoa", 2, 4, "HOAT_DONG", 1, 20);
        assertNotNull(ds);
        assertEquals(1, ds.size());

        // Kiểm tra phân trang trang 2 với số dòng mặc định 20
        nguoiDungService.layDanhSachNguoiDung(null, null, null, null, 2, 0); // 0 -> tự động dùng mặc định 20
        verify(nguoiDungDAO).timKiemVaPhanTrang(null, null, null, null, 20, 20);
    }

    @Test
    @DisplayName("S1-09 Regression: SALES_REP + TEAM_LEAD + nhom=null -> thất bại, dữ liệu cũ giữ nguyên hoàn toàn")
    void testCapNhatTaiKhoan_TeamLead_KhongCoNhom_ThatBai() throws Exception {
        NguoiDung nd = new NguoiDung();
        nd.setId(10);
        nd.setHoTen("Nguyễn Văn Test");
        nd.setEmail("kienteu123@gmail.com");
        nd.setNhomKinhDoanhId(null); // Chọn -- Chưa gán nhóm --

        List<Integer> dsVaiTroIds = Arrays.asList(4, 3); // SALES_REP + TEAM_LEAD

        when(vaiTroDAO.timTheoId(4)).thenReturn(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep"));
        when(vaiTroDAO.timTheoId(3)).thenReturn(new VaiTro(3, "TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead"));

        KetQuaNguoiDungDTO ketQua = nguoiDungService.capNhatTaiKhoan(nd, dsVaiTroIds);

        assertFalse(ketQua.isThanhCong(), "Gán TEAM_LEAD mà không có nhóm phải thất bại");
        assertTrue(ketQua.getDanhSachLoi().containsKey("nhomId"));
        assertTrue(ketQua.getDanhSachLoi().get("nhomId").contains("Trưởng nhóm kinh doanh bắt buộc phải được gán vào một nhóm kinh doanh cụ thể"));

        // Tuyệt đối không gọi DAO cập nhật DB
        verify(nguoiDungDAO, never()).capNhatNguoiDung(any(), any());
    }

    @Test
    @DisplayName("S1-09 Regression: SALES_REP + TEAM_LEAD + nhom hợp lệ -> lưu cả 2 role và nhóm")
    void testCapNhatTaiKhoan_TeamLead_CoNhomHopLe_ThanhCong() throws Exception {
        NguoiDung nd = new NguoiDung();
        nd.setId(10);
        nd.setHoTen("Nguyễn Văn Test");
        nd.setEmail("kienteu123@gmail.com");
        nd.setNhomKinhDoanhId(2);

        List<Integer> dsVaiTroIds = Arrays.asList(4, 3);

        when(vaiTroDAO.timTheoId(4)).thenReturn(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep"));
        when(vaiTroDAO.timTheoId(3)).thenReturn(new VaiTro(3, "TEAM_LEAD", "Trưởng nhóm kinh doanh", "Team Lead"));
        when(nhomKinhDoanhDAO.timTheoId(2)).thenReturn(new NhomKinhDoanh(2, "KD_BAC", "Nhóm Miền Bắc", "...", 1));
        when(nguoiDungDAO.kiemTraEmailTonTai(eq("kienteu123@gmail.com"), eq(10))).thenReturn(false);
        when(nguoiDungDAO.capNhatNguoiDung(any(), any())).thenReturn(true);

        KetQuaNguoiDungDTO ketQua = nguoiDungService.capNhatTaiKhoan(nd, dsVaiTroIds);

        assertTrue(ketQua.isThanhCong(), "Gán TEAM_LEAD với nhóm hợp lệ phải thành công");
        verify(nguoiDungDAO).capNhatNguoiDung(eq(nd), eq(dsVaiTroIds));
    }

    @Test
    @DisplayName("S1-09 Regression: Admin tự bỏ ADMIN -> bị từ chối và dữ liệu giữ nguyên")
    void testCapNhatTaiKhoan_AdminTuBoAdmin_BiTuChoi() throws Exception {
        NguoiDung admin = new NguoiDung();
        admin.setId(1);
        admin.setHoTen("Admin Hệ Thống");
        admin.setEmail("admin@crm.vn");
        admin.themVaiTro(new VaiTro(1, "ADMIN", "Quản trị hệ thống", "Admin"));

        when(nguoiDungDAO.timTheoId(1L)).thenReturn(admin);
        when(vaiTroDAO.timTheoId(4)).thenReturn(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep"));

        // Admin 1 tự sửa chính mình (nguoiThucHienId = 1), bỏ ADMIN chỉ chọn SALES_REP (4)
        KetQuaNguoiDungDTO ketQua = nguoiDungService.capNhatTaiKhoan(admin, Collections.singletonList(4), 1);

        assertFalse(ketQua.isThanhCong(), "Admin tự bỏ ADMIN của chính mình phải bị từ chối");
        assertTrue(ketQua.getDanhSachLoi().containsKey("vaiTro"));
        assertTrue(ketQua.getDanhSachLoi().get("vaiTro").contains("Không thể tự thu hồi vai trò quản trị"));

        verify(nguoiDungDAO, never()).capNhatNguoiDung(any(), any());
    }

    @Test
    @DisplayName("S1-09 Regression: Admin sửa user khác -> hoạt động bình thường")
    void testCapNhatTaiKhoan_AdminSuaUserKhac_ThanhCong() throws Exception {
        NguoiDung other = new NguoiDung();
        other.setId(8);
        other.setHoTen("Nhân Viên Khác");
        other.setEmail("other@crm.vn");
        other.setNhomKinhDoanhId(2);

        when(vaiTroDAO.timTheoId(4)).thenReturn(new VaiTro(4, "SALES_REP", "Nhân viên kinh doanh", "Sales Rep"));
        when(nguoiDungDAO.kiemTraEmailTonTai(eq("other@crm.vn"), eq(8))).thenReturn(false);
        when(nguoiDungDAO.capNhatNguoiDung(any(), any())).thenReturn(true);

        // Admin 1 sửa User 8
        KetQuaNguoiDungDTO ketQua = nguoiDungService.capNhatTaiKhoan(other, Collections.singletonList(4), 1);

        assertTrue(ketQua.isThanhCong(), "Admin sửa user khác phải thành công");
        verify(nguoiDungDAO).capNhatNguoiDung(eq(other), eq(Collections.singletonList(4)));
    }
}
