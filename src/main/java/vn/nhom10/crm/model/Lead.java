package vn.nhom10.crm.model;

import java.time.LocalDateTime;

/**
 * Model ánh xạ bảng 'lead' phục vụ quản lý Khách hàng tiềm năng (Epic EP-04).
 * Story S4-01: Thu thập lead từ biểu mẫu nhúng, tạo ở trạng thái MỚI và gắn đúng nguồn của biểu mẫu.
 */
public class Lead {

    private Long id;
    private String maLead;
    private String hoTen;
    private String email;
    private String emailChuanHoa;
    private String soDienThoai;
    private String soDienThoaiChuanHoa;
    private String tenCongTy;
    private String tenCongTyChuanHoa;
    private String nhuCauQuanTam;
    private String mucDoQuanTam;
    private Long nganhNgheId;
    private Long quyMoId;
    private Long khuVucId;
    private Long nguonLeadId;
    private String tenNguonLead;
    private Long chienDichId;
    private String tenChienDich;
    private Long bieuMauId;
    private String tenBieuMau;
    private int diem = 0;
    private String phanLoai;
    private TrangThaiLeadEnum trangThai = TrangThaiLeadEnum.MOI;
    private Long nguoiPhuTrachId;
    private String tenNguoiPhuTrach;
    private Long nhomPhanBoId;
    private LocalDateTime duocPhanBoLuc;
    private LocalDateTime hanPhanHoiSla;
    private LocalDateTime duocNhanLuc;
    private LocalDateTime lienHeDauLuc;
    private String lyDoTuChoi;
    private boolean quaSla = false;
    private Long khachHangChuyenDoiId;
    private Long nguoiLienHeChuyenDoiId;
    private Long coHoiChuyenDoiId;
    private LocalDateTime chuyenDoiLuc;
    private Long gopVaoLeadId;
    private String ghiChu;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Lead() {
    }

    public Lead(String maLead, String hoTen, String email, String soDienThoai, String tenCongTy, String nhuCauQuanTam, Long nguonLeadId) {
        this.maLead = maLead;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.tenCongTy = tenCongTy;
        this.nhuCauQuanTam = nhuCauQuanTam;
        this.nguonLeadId = nguonLeadId;
        this.trangThai = TrangThaiLeadEnum.MOI;
        this.diem = 0;
        this.quaSla = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaLead() {
        return maLead;
    }

    public void setMaLead(String maLead) {
        this.maLead = maLead;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailChuanHoa() {
        return emailChuanHoa;
    }

    public void setEmailChuanHoa(String emailChuanHoa) {
        this.emailChuanHoa = emailChuanHoa;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getSoDienThoaiChuanHoa() {
        return soDienThoaiChuanHoa;
    }

    public void setSoDienThoaiChuanHoa(String soDienThoaiChuanHoa) {
        this.soDienThoaiChuanHoa = soDienThoaiChuanHoa;
    }

    public String getTenCongTy() {
        return tenCongTy;
    }

    public void setTenCongTy(String tenCongTy) {
        this.tenCongTy = tenCongTy;
    }

    public String getTenCongTyChuanHoa() {
        return tenCongTyChuanHoa;
    }

    public void setTenCongTyChuanHoa(String tenCongTyChuanHoa) {
        this.tenCongTyChuanHoa = tenCongTyChuanHoa;
    }

    public String getNhuCauQuanTam() {
        return nhuCauQuanTam;
    }

    public void setNhuCauQuanTam(String nhuCauQuanTam) {
        this.nhuCauQuanTam = nhuCauQuanTam;
    }

    public String getMucDoQuanTam() {
        return mucDoQuanTam;
    }

    public void setMucDoQuanTam(String mucDoQuanTam) {
        this.mucDoQuanTam = mucDoQuanTam;
    }

    public Long getNganhNgheId() {
        return nganhNgheId;
    }

    public void setNganhNgheId(Long nganhNgheId) {
        this.nganhNgheId = nganhNgheId;
    }

    public Long getQuyMoId() {
        return quyMoId;
    }

    public void setQuyMoId(Long quyMoId) {
        this.quyMoId = quyMoId;
    }

    public Long getKhuVucId() {
        return khuVucId;
    }

    public void setKhuVucId(Long khuVucId) {
        this.khuVucId = khuVucId;
    }

    public Long getNguonLeadId() {
        return nguonLeadId;
    }

    public void setNguonLeadId(Long nguonLeadId) {
        this.nguonLeadId = nguonLeadId;
    }

    public String getTenNguonLead() {
        return tenNguonLead;
    }

    public void setTenNguonLead(String tenNguonLead) {
        this.tenNguonLead = tenNguonLead;
    }

    public Long getChienDichId() {
        return chienDichId;
    }

    public void setChienDichId(Long chienDichId) {
        this.chienDichId = chienDichId;
    }

    public String getTenChienDich() {
        return tenChienDich;
    }

    public void setTenChienDich(String tenChienDich) {
        this.tenChienDich = tenChienDich;
    }

    public Long getBieuMauId() {
        return bieuMauId;
    }

    public void setBieuMauId(Long bieuMauId) {
        this.bieuMauId = bieuMauId;
    }

    public String getTenBieuMau() {
        return tenBieuMau;
    }

    public void setTenBieuMau(String tenBieuMau) {
        this.tenBieuMau = tenBieuMau;
    }

    public int getDiem() {
        return diem;
    }

    public void setDiem(int diem) {
        this.diem = diem;
    }

    public String getPhanLoai() {
        return phanLoai;
    }

    public void setPhanLoai(String phanLoai) {
        this.phanLoai = phanLoai;
    }

    public TrangThaiLeadEnum getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiLeadEnum trangThai) {
        this.trangThai = trangThai;
    }

    public Long getNguoiPhuTrachId() {
        return nguoiPhuTrachId;
    }

    public void setNguoiPhuTrachId(Long nguoiPhuTrachId) {
        this.nguoiPhuTrachId = nguoiPhuTrachId;
    }

    public String getTenNguoiPhuTrach() {
        return tenNguoiPhuTrach;
    }

    public void setTenNguoiPhuTrach(String tenNguoiPhuTrach) {
        this.tenNguoiPhuTrach = tenNguoiPhuTrach;
    }

    public Long getNhomPhanBoId() {
        return nhomPhanBoId;
    }

    public void setNhomPhanBoId(Long nhomPhanBoId) {
        this.nhomPhanBoId = nhomPhanBoId;
    }

    public LocalDateTime getDuocPhanBoLuc() {
        return duocPhanBoLuc;
    }

    public void setDuocPhanBoLuc(LocalDateTime duocPhanBoLuc) {
        this.duocPhanBoLuc = duocPhanBoLuc;
    }

    public LocalDateTime getHanPhanHoiSla() {
        return hanPhanHoiSla;
    }

    public void setHanPhanHoiSla(LocalDateTime hanPhanHoiSla) {
        this.hanPhanHoiSla = hanPhanHoiSla;
    }

    public LocalDateTime getDuocNhanLuc() {
        return duocNhanLuc;
    }

    public void setDuocNhanLuc(LocalDateTime duocNhanLuc) {
        this.duocNhanLuc = duocNhanLuc;
    }

    public LocalDateTime getLienHeDauLuc() {
        return lienHeDauLuc;
    }

    public void setLienHeDauLuc(LocalDateTime lienHeDauLuc) {
        this.lienHeDauLuc = lienHeDauLuc;
    }

    public String getLyDoTuChoi() {
        return lyDoTuChoi;
    }

    public void setLyDoTuChoi(String lyDoTuChoi) {
        this.lyDoTuChoi = lyDoTuChoi;
    }

    public boolean isQuaSla() {
        return quaSla;
    }

    public void setQuaSla(boolean quaSla) {
        this.quaSla = quaSla;
    }

    public Long getKhachHangChuyenDoiId() {
        return khachHangChuyenDoiId;
    }

    public void setKhachHangChuyenDoiId(Long khachHangChuyenDoiId) {
        this.khachHangChuyenDoiId = khachHangChuyenDoiId;
    }

    public Long getNguoiLienHeChuyenDoiId() {
        return nguoiLienHeChuyenDoiId;
    }

    public void setNguoiLienHeChuyenDoiId(Long nguoiLienHeChuyenDoiId) {
        this.nguoiLienHeChuyenDoiId = nguoiLienHeChuyenDoiId;
    }

    public Long getCoHoiChuyenDoiId() {
        return coHoiChuyenDoiId;
    }

    public void setCoHoiChuyenDoiId(Long coHoiChuyenDoiId) {
        this.coHoiChuyenDoiId = coHoiChuyenDoiId;
    }

    public LocalDateTime getChuyenDoiLuc() {
        return chuyenDoiLuc;
    }

    public void setChuyenDoiLuc(LocalDateTime chuyenDoiLuc) {
        this.chuyenDoiLuc = chuyenDoiLuc;
    }

    public Long getGopVaoLeadId() {
        return gopVaoLeadId;
    }

    public void setGopVaoLeadId(Long gopVaoLeadId) {
        this.gopVaoLeadId = gopVaoLeadId;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
