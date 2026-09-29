package vn.nhom10.crm.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model đại diện cho bảng nhom_kinh_doanh trong cơ sở dữ liệu.
 */
public class NhomKinhDoanh implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String maNhom;
    private String tenNhom;
    private String moTa;
    private Integer nhomChaId;

    public NhomKinhDoanh() {
    }

    public NhomKinhDoanh(int id, String maNhom, String tenNhom, String moTa, Integer nhomChaId) {
        this.id = id;
        this.maNhom = maNhom;
        this.tenNhom = tenNhom;
        this.moTa = moTa;
        this.nhomChaId = nhomChaId;
    }

    public NhomKinhDoanh(int id, String tenNhom) {
        this.id = id;
        this.tenNhom = tenNhom;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhomKinhDoanh that = (NhomKinhDoanh) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return tenNhom;
    }
}
