package vn.nhom10.crm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.nhom10.crm.dao.GopLeadDAO;
import vn.nhom10.crm.dto.DuplicatePermissionsDTO;
import vn.nhom10.crm.dto.KetQuaGopLeadDTO;
import vn.nhom10.crm.dto.NguoiDungDTO;
import vn.nhom10.crm.dto.SoSanhLeadDTO;
import vn.nhom10.crm.dto.UngVienTrungLeadDTO;
import vn.nhom10.crm.model.HanhDongThayDoi;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.LoaiDoiTuongNhayCam;
import vn.nhom10.crm.model.PhamViDuLieu;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.model.VaiTroEnum;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Kiểm thử GopLeadService - Story S4-04 (AC1, AC2, AC3)")
class GopLeadServiceTest {

    @Mock
    private GopLeadDAO gopLeadDAO;

    @Mock
    private NhatKyThayDoiService nhatKyThayDoiService;

    private GopLeadService service;

    @BeforeEach
    void setUp() {
        service = new GopLeadService(gopLeadDAO, nhatKyThayDoiService);
    }

    @Test
    @DisplayName("Q2: Phân quyền RBAC cho các vai trò trong hệ thống")
    void testPhanQuyenRbac() {
        NguoiDungDTO admin = new NguoiDungDTO(1L, "Admin", "admin@crm.vn", VaiTroEnum.ADMIN, 1L, "HN", PhamViDuLieu.TOAN_BO);
        NguoiDungDTO marketing = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);
        NguoiDungDTO teamLead = new NguoiDungDTO(3L, "TL", "tl@crm.vn", VaiTroEnum.TEAM_LEAD, 1L, "HN", PhamViDuLieu.NHOM);
        NguoiDungDTO sales = new NguoiDungDTO(4L, "Sales", "sales@crm.vn", VaiTroEnum.SALES_REP, 1L, "HN", PhamViDuLieu.CA_NHAN);
        NguoiDungDTO cskh = new NguoiDungDTO(5L, "CS", "cs@crm.vn", VaiTroEnum.CUST_SUCCESS, 1L, "HN", PhamViDuLieu.CA_NHAN);

        assertTrue(service.coQuyenXem(admin));
        assertTrue(service.coQuyenXem(marketing));
        assertTrue(service.coQuyenXem(teamLead));
        assertTrue(service.coQuyenXem(sales));
        assertFalse(service.coQuyenXem(cskh));

        assertTrue(service.coQuyenThaoTac(admin));
        assertTrue(service.coQuyenThaoTac(marketing));
        assertTrue(service.coQuyenThaoTac(teamLead));
        assertFalse(service.coQuyenThaoTac(sales));
        assertFalse(service.coQuyenThaoTac(cskh));
    }

    @Test
    @DisplayName("Q2: Kiểm tra Data Scope cho Trưởng nhóm khi gộp Lead")
    void testKiemTraDataScopeTeamLead() {
        NguoiDungDTO teamLead = new NguoiDungDTO(10L, "Trưởng nhóm 1", "tl1@crm.vn", VaiTroEnum.TEAM_LEAD, 1L, "Nhóm 1", PhamViDuLieu.NHOM);

        Lead leadThuocNhom = new Lead();
        leadThuocNhom.setId(101L);
        leadThuocNhom.setNhomPhanBoId(1L);

        Lead leadNgoaiNhom = new Lead();
        leadNgoaiNhom.setId(102L);
        leadNgoaiNhom.setNhomPhanBoId(2L);

        Lead leadNgoaiNhom2 = new Lead();
        leadNgoaiNhom2.setId(103L);
        leadNgoaiNhom2.setNhomPhanBoId(2L);

        // Có 1 lead thuộc nhóm -> cho phép gộp
        assertTrue(service.kiemTraQuyenGop(teamLead, leadThuocNhom, leadNgoaiNhom));

        // Cả 2 lead ngoài nhóm -> từ chối
        assertFalse(service.kiemTraQuyenGop(teamLead, leadNgoaiNhom, leadNgoaiNhom2));
    }

    @Test
    @DisplayName("AC1: Quét ứng viên trùng trả về danh sách đầy đủ")
    void testQuetUngVienTrung() throws SQLException {
        NguoiDungDTO mkt = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);
        Lead lead = new Lead();
        lead.setId(101L);

        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(lead);
        UngVienTrungLeadDTO uvLead = new UngVienTrungLeadDTO("LEAD", 102L, "LEAD-002", "Lead 2");
        UngVienTrungLeadDTO uvCustomer = new UngVienTrungLeadDTO("CUSTOMER", 801L, "KH-801", "Cty Alpha");

        when(gopLeadDAO.timUngVienLeadTrung(eq(lead), eq(mkt), any())).thenReturn(List.of(uvLead));
        when(gopLeadDAO.timUngVienCustomerTrung(eq(lead), eq(mkt), any())).thenReturn(List.of(uvCustomer));

        List<UngVienTrungLeadDTO> ketQua = service.quetUngVienTrung(101L, mkt);
        assertEquals(2, ketQua.size());
    }

    @Test
    @DisplayName("AC2: Gắn Lead vào Khách hàng thành công và ghi Audit Log")
    void testGanKhachHangThanhCong() throws SQLException {
        NguoiDungDTO mkt = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);
        Lead lead = new Lead();
        lead.setId(101L);
        lead.setMaLead("LEAD-101");
        lead.setHoTen("Nguyen Van A");

        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(lead);
        when(gopLeadDAO.ganKhachHang(101L, 801L)).thenReturn(true);

        boolean ok = service.ganKhachHang(101L, 801L, mkt, "127.0.0.1", "JUnit");
        assertTrue(ok);

        verify(gopLeadDAO).ganKhachHang(101L, 801L);
        verify(nhatKyThayDoiService).ghiNhatKyThayDoi(
                eq(2L), eq("MKT"), eq("mkt@crm.vn"),
                eq(LoaiDoiTuongNhayCam.QUYEN_SO_HUU),
                eq("LEAD-101"), eq("Nguyen Van A"),
                eq("khach_hang_chuyen_doi_id"),
                any(), eq("801"),
                eq(HanhDongThayDoi.CAP_NHAT),
                anyString(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("AC3: Chuẩn bị dữ liệu so sánh preview trước khi gộp")
    void testLayDuLieuSoSanh() throws SQLException {
        NguoiDungDTO mkt = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);

        Lead src = new Lead();
        src.setId(102L);
        src.setEmail("dup@test.com");
        src.setNhomPhanBoId(1L);

        Lead dst = new Lead();
        dst.setId(101L);
        dst.setEmail("dup@test.com");
        dst.setNhomPhanBoId(1L);

        when(gopLeadDAO.layChiTietLead(102L)).thenReturn(src);
        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(dst);

        GopLeadDAO.ThongKeLichSuLead tkSrc = new GopLeadDAO.ThongKeLichSuLead();
        tkSrc.soHoatDong = 3;
        tkSrc.soCongViec = 1;

        GopLeadDAO.ThongKeLichSuLead tkDst = new GopLeadDAO.ThongKeLichSuLead();
        tkDst.soHoatDong = 2;

        when(gopLeadDAO.layThongKeLichSu(102L)).thenReturn(tkSrc);
        when(gopLeadDAO.layThongKeLichSu(101L)).thenReturn(tkDst);

        SoSanhLeadDTO soSanh = service.layDuLieuSoSanh(102L, 101L, mkt);
        assertNotNull(soSanh);
        assertEquals(3, soSanh.getSoHoatDongNguon());
        assertEquals(2, soSanh.getSoHoatDongDich());
        assertEquals(6, soSanh.getTongLichSuSauGop());
        assertTrue(soSanh.isCanMerge());
        assertTrue(soSanh.getMatchReasons().contains("EMAIL"));
    }

    @Test
    @DisplayName("AC3: Gộp Lead thất bại khi trùng ID nguồn và đích")
    void testGopLeadTrungIdThatBai() {
        NguoiDungDTO mkt = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);
        KetQuaGopLeadDTO kq = service.thucHienGopLead(101L, 101L, "Ly do", mkt, "127.0.0.1", "JUnit");
        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongDiep().contains("Không thể gộp một Lead vào chính nó"));
    }

    @Test
    @DisplayName("AC3: Gộp Lead thất bại khi Lead nguồn đã bị gộp trước đó")
    void testGopLeadNguonDaBiGopThatBai() throws SQLException {
        NguoiDungDTO mkt = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);

        Lead src = new Lead();
        src.setId(102L);
        src.setGopVaoLeadId(99L);
        src.setTrangThai(TrangThaiLeadEnum.DA_GOP);

        Lead dst = new Lead();
        dst.setId(101L);

        when(gopLeadDAO.layChiTietLead(102L)).thenReturn(src);
        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(dst);

        KetQuaGopLeadDTO kq = service.thucHienGopLead(102L, 101L, "Ly do", mkt, "127.0.0.1", "JUnit");
        assertFalse(kq.isThanhCong());
        assertTrue(kq.getThongDiep().contains("đã được gộp trước đó"));
    }

    @Test
    @DisplayName("AC3: Gộp Lead thành công và gọi Audit Log")
    void testThucHienGopLeadThanhCong() throws SQLException {
        NguoiDungDTO mkt = new NguoiDungDTO(2L, "MKT", "mkt@crm.vn", VaiTroEnum.MARKETING, 1L, "HN", PhamViDuLieu.TOAN_BO);

        Lead src = new Lead();
        src.setId(102L);
        src.setMaLead("LEAD-102");
        src.setHoTen("Lead B");

        Lead dst = new Lead();
        dst.setId(101L);
        dst.setMaLead("LEAD-101");
        dst.setHoTen("Lead A");

        when(gopLeadDAO.layChiTietLead(102L)).thenReturn(src);
        when(gopLeadDAO.layChiTietLead(101L)).thenReturn(dst);

        KetQuaGopLeadDTO mockKq = KetQuaGopLeadDTO.thanhCong(101L, 102L, 2, 1, 0, "Thành công");
        when(gopLeadDAO.thucHienGopLead(eq(102L), eq(101L), eq(2L), anyString(), anyString())).thenReturn(mockKq);

        KetQuaGopLeadDTO kq = service.thucHienGopLead(102L, 101L, "Trùng email", mkt, "127.0.0.1", "JUnit");
        assertTrue(kq.isThanhCong());

        verify(nhatKyThayDoiService).ghiNhatKyThayDoi(
                eq(2L), eq("MKT"), eq("mkt@crm.vn"),
                eq(LoaiDoiTuongNhayCam.QUYEN_SO_HUU),
                eq("LEAD-102"), eq("Lead B"),
                eq("trang_thai / gop_vao_lead_id"),
                eq("Lead B"), eq("Lead A"),
                eq(HanhDongThayDoi.CAP_NHAT),
                anyString(), anyString(), anyString()
        );
    }
}
