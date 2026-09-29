package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Model đại diện cho nhóm kinh doanh.
 */
public class NhomKinhDoanh implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String maNhom;
    private String tenNhom;
    private String moTa;
    private Integer nhomChaId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public NhomKinhDoanh() {
    }

    public NhomKinhDoanh(int id, String maNhom, String tenNhom, String moTa, Integer nhomChaId) {
        this.id = id;
        this.maNhom = maNhom;
        this.tenNhom = tenNhom;
        this.moTa = moTa;
        this.nhomChaId = nhomChaId;
    }

    public int getId() {
        return id;
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
        return nhomChaId;
    }

    public void setNhomChaId(Integer nhomChaId) {
        this.nhomChaId = nhomChaId;
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
}
