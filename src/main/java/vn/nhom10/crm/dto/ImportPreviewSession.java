package vn.nhom10.crm.dto;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Đối tượng lưu trữ dữ liệu xem trước tạm thời phía server (Story S2-01).
 * Cho phép người dùng bấm "Thực hiện nhập" trực tiếp sau Preview mà không phải upload lại tệp Excel.
 * Đảm bảo:
 * - Gắn với user/session
 * - Token dùng một lần duy nhất (chống double-submit / replay)
 * - Tự động hết hạn (TTL 30 phút)
 */
public class ImportPreviewSession implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final long TTL_MS = 30 * 60 * 1000L; // 30 phút

    private final String token;
    private final Long userId;
    private final String fileName;
    private final byte[] fileBytes;
    private final int soDongHopLe;
    private final long createdAt;
    private final AtomicBoolean used = new AtomicBoolean(false);

    public ImportPreviewSession(String token, Long userId, String fileName, byte[] fileBytes, int soDongHopLe) {
        this.token = token;
        this.userId = userId;
        this.fileName = fileName;
        this.fileBytes = fileBytes != null ? fileBytes : new byte[0];
        this.soDongHopLe = soDongHopLe;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Kiểm tra xem phiên preview đã hết hạn hay chưa.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > TTL_MS;
    }

    /**
     * Đánh dấu token đã được sử dụng một cách thread-safe.
     * @return true nếu đánh dấu thành công (chưa từng dùng), false nếu đã dùng rồi (trùng submit).
     */
    public boolean markUsed() {
        return used.compareAndSet(false, true);
    }

    public boolean isUsed() {
        return used.get();
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public int getSoDongHopLe() {
        return soDongHopLe;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
