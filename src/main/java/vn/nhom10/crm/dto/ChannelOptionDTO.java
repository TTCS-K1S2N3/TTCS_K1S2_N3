package vn.nhom10.crm.dto;

import java.io.Serializable;

/**
 * DTO tùy chọn Kênh chiến dịch (Story S4-03, AC1).
 * Khớp đúng cấu trúc ChannelOption trong Contract FE-BE (mục 3.1).
 */
public class ChannelOptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String label;
    private boolean enabled = true;

    public ChannelOptionDTO() {
    }

    public ChannelOptionDTO(String code, String label, boolean enabled) {
        this.code = code;
        this.label = label;
        this.enabled = enabled;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
