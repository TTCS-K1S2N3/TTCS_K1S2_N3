package vn.nhom10.crm.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Model đại diện cho bảng pipeline_ban_hang theo schema dự án.
 */
public class PipelineBanHang implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String maPipeline;
    private String tenPipeline;
    private String moTa;
    private boolean macDinh = true;
    private boolean hoatDong = true;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public PipelineBanHang() {
    }

    public PipelineBanHang(long id, String maPipeline, String tenPipeline, String moTa) {
        this.id = id;
        this.maPipeline = maPipeline;
        this.tenPipeline = tenPipeline;
        this.moTa = moTa;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMaPipeline() {
        return maPipeline;
    }

    public void setMaPipeline(String maPipeline) {
        this.maPipeline = maPipeline;
    }

    public String getTenPipeline() {
        return tenPipeline;
    }

    public void setTenPipeline(String tenPipeline) {
        this.tenPipeline = tenPipeline;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isMacDinh() {
        return macDinh;
    }

    public void setMacDinh(boolean macDinh) {
        this.macDinh = macDinh;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineBanHang that = (PipelineBanHang) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
