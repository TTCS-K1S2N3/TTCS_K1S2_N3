package vn.nhom10.crm.util;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên, trang hoặc bản ghi dữ liệu yêu cầu.
 * Tương ứng với mã lỗi HTTP 404 Not Found.
 */
public class LoiKhongTimThayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public LoiKhongTimThayException(String message) {
        super(message);
    }

    public LoiKhongTimThayException(String message, Throwable cause) {
        super(message, cause);
    }
}
