package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO tổng hợp toàn bộ cấu hình quy tắc chấm điểm và ngưỡng phân loại Lead (Story S4-05).
 */
public class CauHinhChamDiemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int phienBan;
    private List<TieuChiChamDiemDTO> danhSachTieuChi;
    private NguongPhanLoaiLeadDTO nguongPhanLoai;
    private LocalDateTime ngayCapNhat;
    private String nguoiCapNhat;
    private boolean duocPhepSua;

    public CauHinhChamDiemDTO() {
        this.phienBan = 1;
        this.danhSachTieuChi = new ArrayList<>();
        this.nguongPhanLoai = new NguongPhanLoaiLeadDTO(70, 40);
        this.ngayCapNhat = LocalDateTime.now();
        this.duocPhepSua = true;
    }

    public static CauHinhChamDiemDTO taoCauHinhMacDinh() {
        CauHinhChamDiemDTO config = new CauHinhChamDiemDTO();
        config.setPhienBan(1);
        config.setNguoiCapNhat("Giám đốc kinh doanh");
        config.setNguongPhanLoai(new NguongPhanLoaiLeadDTO(70, 40));

        // 1. Tiêu chí: Ngành nghề phù hợp (Tối đa 25 điểm)
        TieuChiChamDiemDTO tcNganh = new TieuChiChamDiemDTO(
                "INDUSTRY_FIT", "QC_NGANH_NGHE", "Ngành nghề phù hợp",
                "Đánh giá độ khớp giữa ngành nghề của khách hàng với phân khúc giải pháp CRM",
                "domain", true, 25, 1
        );
        tcNganh.themTuyChon("IT_PHAN_MEM", "Công nghệ thông tin / Phần mềm", 25);
        tcNganh.themTuyChon("TAI_CHINH_NGAN_HANG", "Tài chính / Ngân hàng / Bảo hiểm", 25);
        tcNganh.themTuyChon("SAN_XUAT_PHAN_PHOI", "Sản xuất & Bán buôn / Phân phối", 20);
        tcNganh.themTuyChon("BAT_DONG_SAN", "Bất động sản & Xây dựng", 15);
        tcNganh.themTuyChon("BAN_LE_FMCG", "Bán lẻ chuỗi / FMCG", 15);
        tcNganh.themTuyChon("DICH_VU_KHAC", "Dịch vụ thương mại khác", 10);
        config.getDanhSachTieuChi().add(tcNganh);

        // 2. Tiêu chí: Quy mô doanh nghiệp (Tối đa 20 điểm)
        TieuChiChamDiemDTO tcQuyMo = new TieuChiChamDiemDTO(
                "COMPANY_SIZE", "QC_QUY_MO", "Quy mô doanh nghiệp",
                "Ưu tiên doanh nghiệp có quy mô nhân sự lớn và năng lực chi trả cao",
                "groups", true, 20, 2
        );
        tcQuyMo.themTuyChon("TREN_500", "Doanh nghiệp lớn (> 500 nhân sự)", 20);
        tcQuyMo.themTuyChon("100_500", "Doanh nghiệp vừa lớn (100 - 500 nhân sự)", 15);
        tcQuyMo.themTuyChon("20_100", "Doanh nghiệp vừa (20 - 100 nhân sự)", 10);
        tcQuyMo.themTuyChon("DUOI_20", "Doanh nghiệp nhỏ / Startup (< 20 nhân sự)", 5);
        config.getDanhSachTieuChi().add(tcQuyMo);

        // 3. Tiêu chí: Nguồn thu thập lead (Tối đa 15 điểm)
        TieuChiChamDiemDTO tcNguon = new TieuChiChamDiemDTO(
                "SOURCE", "QC_NGUON_LEAD", "Nguồn thu thập lead",
                "Phản ánh mức độ tin cậy và sự chủ động tìm kiếm giải pháp của khách hàng",
                "share_location", true, 15, 3
        );
        tcNguon.themTuyChon("GIOI_THIEU", "Khách hàng cũ hoặc đối tác giới thiệu", 15);
        tcNguon.themTuyChon("WEB_FORM", "Đăng ký biểu mẫu từ website chính thức", 15);
        tcNguon.themTuyChon("HOI_THAO", "Gặp gỡ trực tiếp tại sự kiện / Hội thảo", 12);
        tcNguon.themTuyChon("QUANG_CAO_GOOGLE", "Quảng cáo tìm kiếm Google Ads", 10);
        tcNguon.themTuyChon("MANG_XA_HOI", "Chiến dịch Mạng xã hội / Facebook Ads", 8);
        tcNguon.themTuyChon("OUTBOUND_COLD", "Danh sách tiếp cận Outbound / Gọi nguội", 5);
        config.getDanhSachTieuChi().add(tcNguon);

        // 4. Tiêu chí: Mức độ quan tâm (Tối đa 40 điểm)
        TieuChiChamDiemDTO tcQuanTam = new TieuChiChamDiemDTO(
                "INTEREST_LEVEL", "QC_MUC_DO_QUAN_TAM", "Mức độ quan tâm",
                "Nhu cầu mua và mức độ cấp thiết trong kế hoạch chuyển đổi số của khách",
                "local_fire_department", true, 40, 4
        );
        tcQuanTam.themTuyChon("CAN_GAP", "Rất cấp thiết — Cần triển khai ngay trong tháng", 40);
        tcQuanTam.themTuyChon("QUAN_TAM_CAO", "Quan tâm cao — Đang xin ngân sách quý này", 30);
        tcQuanTam.themTuyChon("TIM_HIEU", "Đang tìm hiểu & so sánh các giải pháp CRM", 15);
        tcQuanTam.themTuyChon("CHUA_RO", "Chưa có kế hoạch cụ thể / Tham khảo tài liệu", 5);
        config.getDanhSachTieuChi().add(tcQuanTam);

        return config;
    }

    public int getTongDiemToiDa() {
        int tong = 0;
        if (danhSachTieuChi != null) {
            for (TieuChiChamDiemDTO tc : danhSachTieuChi) {
                if (tc.isKichHoat()) {
                    tong += tc.getSoDiem();
                }
            }
        }
        return tong;
    }

    public int getPhienBan() {
        return phienBan;
    }

    public void setPhienBan(int phienBan) {
        this.phienBan = phienBan;
    }

    public List<TieuChiChamDiemDTO> getDanhSachTieuChi() {
        return danhSachTieuChi;
    }

    public void setDanhSachTieuChi(List<TieuChiChamDiemDTO> danhSachTieuChi) {
        this.danhSachTieuChi = danhSachTieuChi;
    }

    public NguongPhanLoaiLeadDTO getNguongPhanLoai() {
        return nguongPhanLoai;
    }

    public void setNguongPhanLoai(NguongPhanLoaiLeadDTO nguongPhanLoai) {
        this.nguongPhanLoai = nguongPhanLoai;
    }

    public LocalDateTime getNgayCapNhat() {
        return ngayCapNhat;
    }

    public void setNgayCapNhat(LocalDateTime ngayCapNhat) {
        this.ngayCapNhat = ngayCapNhat;
    }

    public String getNguoiCapNhat() {
        return nguoiCapNhat;
    }

    public void setNguoiCapNhat(String nguoiCapNhat) {
        this.nguoiCapNhat = nguoiCapNhat;
    }

    public boolean isDuocPhepSua() {
        return duocPhepSua;
    }

    public void setDuocPhepSua(boolean duocPhepSua) {
        this.duocPhepSua = duocPhepSua;
    }
}
