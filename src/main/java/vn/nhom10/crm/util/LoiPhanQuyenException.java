package vn.nhom10.crm.util;

/**
 * Ngoại lệ ném ra khi người dùng không có quyền truy cập chức năng
 * hoặc truy cập bản ghi nằm ngoài phạm vi dữ liệu (Data Scope).
 * Tương ứng với mã lỗi HTTP 403 Forbidden.
 */
public class LoiPhanQuyenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LoiPhanQuyenException(String message) {
        super(message);
    }

    public LoiPhanQuyenException(String message, Throwable cause) {
        super(message, cause);
    }
}
