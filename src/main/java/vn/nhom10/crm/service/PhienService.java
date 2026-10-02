package vn.nhom10.crm.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.dao.PhienDangNhapDAO;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.model.PhienDangNhap;

import java.sql.Timestamp;

/**
 * Service quản lý vòng đời phiên đăng nhập (Session Lifecycle)
 * Phục vụ Story S1-02:
 * - Gia hạn phiên tự động khi còn hoạt động (AC1)
 * - Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server và database (AC2)
 * - Kiểm tra phiên hết hạn và điều hướng an toàn (AC3)
 */
public class PhienService {

    public static final String SESSION_USER_KEY = "nguoiDung";
    public static final String SESSION_TOKEN_KEY = "maPhienDangNhap";
    public static final int THOI_GIAN_HET_HAN_MAC_DINH_PHUT = 30;

    private final PhienDangNhapDAO phienDangNhapDAO;

    public PhienService() {
        this.phienDangNhapDAO = new PhienDangNhapDAO();
    }

    public PhienService(PhienDangNhapDAO phienDangNhapDAO) {
        this.phienDangNhapDAO = phienDangNhapDAO;
    }

    /**
     * Khởi tạo phiên đăng nhập mới sau khi xác thực thành công.
     */
    public PhienDangNhap taoPhienMoi(NguoiDung nguoiDung, HttpSession httpSession, String diaChiIp, String thongTinTrinhDuyet) {
        if (nguoiDung == null || httpSession == null) {
            throw new IllegalArgumentException("Người dùng và HTTP Session không được null");
        }

        String maPhien = httpSession.getId();
        if (maPhien == null || maPhien.isBlank()) {
            maPhien = java.util.UUID.randomUUID().toString();
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());

        PhienDangNhap phien = new PhienDangNhap();
        phien.setNguoiDungId(nguoiDung.getId());
        phien.setMaPhien(maPhien);
        phien.setDiaChiIp(diaChiIp);
        phien.setThongTinThietBi(thongTinTrinhDuyet);
        phien.setThoiGianTao(now);
        phien.setThoiGianHoatDongCuoi(now);
        phien.setTrangThai(PhienDangNhap.TRANG_THAI_HOAT_DONG);

        phienDangNhapDAO.luuPhien(phien);

        try {
            httpSession.setMaxInactiveInterval(THOI_GIAN_HET_HAN_MAC_DINH_PHUT * 60);
        } catch (Exception ignored) {}

        httpSession.setAttribute(SESSION_USER_KEY, nguoiDung);
        httpSession.setAttribute(SESSION_TOKEN_KEY, maPhien);

        return phien;
    }

    /**
     * Gia hạn phiên tự động khi có hoạt động (AC1)
     * Cập nhật thời gian hoạt động cuối ở server và DB
     */
    public boolean giaHanPhien(String maPhien, HttpSession httpSession) {
        if (maPhien == null || maPhien.trim().isEmpty()) {
            return false;
        }

        PhienDangNhap phien = phienDangNhapDAO.timTheoMaPhien(maPhien);
        if (phien == null || !phien.isDangHoatDong()) {
            return false;
        }

        if (phien.isHetHan(THOI_GIAN_HET_HAN_MAC_DINH_PHUT)) {
            phienDangNhapDAO.voHieuHoaPhien(maPhien, PhienDangNhap.TRANG_THAI_HET_HAN);
            return false;
        }

        boolean thanhCong = phienDangNhapDAO.capNhatHoatDongCuoi(maPhien);

        if (thanhCong && httpSession != null) {
            try {
                httpSession.setMaxInactiveInterval(THOI_GIAN_HET_HAN_MAC_DINH_PHUT * 60);
            } catch (IllegalStateException e) {
                // Session có thể đã bị invalidate
                return false;
            }
        }

        return thanhCong;
    }

    /**
     * Kiểm tra trạng thái phiên hợp lệ hay đã hết hạn (AC3)
     */
    public boolean kiemTraPhienHopLe(String maPhien) {
        if (maPhien == null || maPhien.trim().isEmpty()) {
            return false;
        }

        PhienDangNhap phien = phienDangNhapDAO.timTheoMaPhien(maPhien);
        if (phien == null || !phien.isDangHoatDong()) {
            return false;
        }

        if (phien.isHetHan(THOI_GIAN_HET_HAN_MAC_DINH_PHUT)) {
            phienDangNhapDAO.voHieuHoaPhien(maPhien, PhienDangNhap.TRANG_THAI_HET_HAN);
            return false;
        }

        return true;
    }

    /**
     * Đăng xuất làm mất hiệu lực phiên ngay lập tức phía server (AC2)
     * - Hủy session phía web container (session.invalidate())
     * - Cập nhật trạng thái DA_DANG_XUAT trong DB
     * - Xóa cookie JSESSIONID trên trình duyệt
     */
    public void dangXuat(HttpServletRequest request, HttpServletResponse response) {
        if (request == null) {
            return;
        }

        HttpSession session = request.getSession(false);
        String maPhien = null;

        if (session != null) {
            try {
                maPhien = (String) session.getAttribute(SESSION_TOKEN_KEY);
                if (maPhien == null) {
                    maPhien = session.getId();
                }

                session.removeAttribute(SESSION_USER_KEY);
                session.removeAttribute(SESSION_TOKEN_KEY);
                session.invalidate();
            } catch (IllegalStateException e) {
                // Session có thể đã bị invalidate trước đó
            }
        }

        if (maPhien != null && !maPhien.trim().isEmpty()) {
            phienDangNhapDAO.voHieuHoaPhien(maPhien, PhienDangNhap.TRANG_THAI_DA_DANG_XUAT);
        }

        if (response != null) {
            xoaCookieSession(request, response);
        }
    }

    /**
     * Xóa cookie JSESSIONID phía client
     */
    public void xoaCookieSession(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                    cookie.setMaxAge(0);
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * Lấy thông tin phiên hiện tại theo mã phiên
     */
    public PhienDangNhap layPhienTheoMa(String maPhien) {
        return phienDangNhapDAO.timTheoMaPhien(maPhien);
    }
}
