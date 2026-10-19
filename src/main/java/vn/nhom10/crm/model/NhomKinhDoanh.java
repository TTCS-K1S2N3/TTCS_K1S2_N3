package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho nhóm kinh doanh trong hệ thống CRM (Story S2-06).
 * Nhóm kinh doanh có cấu trúc cây phân cấp (nhom_cha_id),
 * mỗi nhóm có đúng một trưởng nhóm (truong_nhom_id) và thuộc một khu vực địa lý (khu_vuc_id).
 */
public class NhomKinhDoanh implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String maNhom;
    private String tenNhom;
    private String moTa;
    private Long nhomChaId;
    private Long khuVucId;
    private Long truongNhomId;
    private boolean hoatDong = true;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Các trường quan hệ và hiển thị cây tổ chức
    private String tenNhomCha;
    private String tenKhuVuc;
    private String maKhuVuc;
    private String tenTruongNhom;
    private String emailTruongNhom;
    private int soLuongThanhVien = 0;
    private int capDo = 1;

    private KhuVucDiaLy khuVuc;
    private NguoiDung truongNhom;
    private List<NhomKinhDoanh> dsNhomCon = new ArrayList<>();
    private List<NguoiDung> dsThanhVien = new ArrayList<>();

    public NhomKinhDoanh() {
    }

    public NhomKinhDoanh(int id, String tenNhom) {
        this.id = id;
        this.tenNhom = tenNhom;
    }

    public NhomKinhDoanh(int id, String maNhom, String tenNhom, String moTa, Integer nhomChaId) {
        this.id = id;
        this.maNhom = maNhom;
        this.tenNhom = tenNhom;
        this.moTa = moTa;
        this.nhomChaId = nhomChaId != null ? nhomChaId.longValue() : null;
    }

    public NhomKinhDoanh(long id, String maNhom, String tenNhom, String moTa, Long nhomChaId, Long khuVucId, Long truongNhomId, boolean hoatDong) {
        this.id = id;
        this.maNhom = maNhom;
        this.tenNhom = tenNhom;
        this.moTa = moTa;
        this.nhomChaId = nhomChaId;
        this.khuVucId = khuVucId;
        this.truongNhomId = truongNhomId;
        this.hoatDong = hoatDong;
    }

    public int getId() {
        return (int) id;
    }

    public long getIdLong() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaNhom() {
        return maNhom;
    }

    public void setMaNhom(String maNhom) {
        this.maNhom = maNhom;
    }

    public String getTenNhom() {
        return tenNhom;
    }

    public void setTenNhom(String tenNhom) {
        this.tenNhom = tenNhom;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getNhomChaId() {
        return nhomChaId != null ? nhomChaId.intValue() : null;
    }

    public Long getNhomChaIdLong() {
        return nhomChaId;
    }

    public void setNhomChaId(Integer nhomChaId) {
        this.nhomChaId = nhomChaId != null ? nhomChaId.longValue() : null;
    }

    public void setNhomChaId(Long nhomChaId) {
        this.nhomChaId = nhomChaId;
    }

    public Long getKhuVucId() {
        return khuVucId;
    }

    public void setKhuVucId(Long khuVucId) {
        this.khuVucId = khuVucId;
    }

    public Long getTruongNhomId() {
        return truongNhomId;
    }

    public void setTruongNhomId(Long truongNhomId) {
        this.truongNhomId = truongNhomId;
    }

    public boolean isHoatDong() {
        return hoatDong;
    }

    public void setHoatDong(boolean hoatDong) {
        this.hoatDong = hoatDong;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTenNhomCha() {
        return tenNhomCha;
    }

    public void setTenNhomCha(String tenNhomCha) {
        this.tenNhomCha = tenNhomCha;
    }

    public String getTenKhuVuc() {
        return tenKhuVuc;
    }

    public void setTenKhuVuc(String tenKhuVuc) {
        this.tenKhuVuc = tenKhuVuc;
    }

    public String getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(String maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public String getTenTruongNhom() {
        return tenTruongNhom;
    }

    public void setTenTruongNhom(String tenTruongNhom) {
        this.tenTruongNhom = tenTruongNhom;
    }

    public String getEmailTruongNhom() {
        return emailTruongNhom;
    }

    public void setEmailTruongNhom(String emailTruongNhom) {
        this.emailTruongNhom = emailTruongNhom;
    }

    public int getSoLuongThanhVien() {
        return soLuongThanhVien;
    }

    public void setSoLuongThanhVien(int soLuongThanhVien) {
        this.soLuongThanhVien = soLuongThanhVien;
    }

    public int getCapDo() {
        return capDo;
    }

    public void setCapDo(int capDo) {
        this.capDo = capDo;
    }

    public KhuVucDiaLy getKhuVuc() {
        return khuVuc;
    }

    public void setKhuVuc(KhuVucDiaLy khuVuc) {
        this.khuVuc = khuVuc;
    }

    public NguoiDung getTruongNhom() {
        return truongNhom;
    }

    public void setTruongNhom(NguoiDung truongNhom) {
        this.truongNhom = truongNhom;
    }

    public List<NhomKinhDoanh> getDsNhomCon() {
        return dsNhomCon;
    }

    public void setDsNhomCon(List<NhomKinhDoanh> dsNhomCon) {
        this.dsNhomCon = dsNhomCon;
    }

    public void themNhomCon(NhomKinhDoanh con) {
        if (this.dsNhomCon == null) {
            this.dsNhomCon = new ArrayList<>();
        }
        this.dsNhomCon.add(con);
    }

    public List<NguoiDung> getDsThanhVien() {
        return dsThanhVien;
    }

    public void setDsThanhVien(List<NguoiDung> dsThanhVien) {
        this.dsThanhVien = dsThanhVien;
    }
}
