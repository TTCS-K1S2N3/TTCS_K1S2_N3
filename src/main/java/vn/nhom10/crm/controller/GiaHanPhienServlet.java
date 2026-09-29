package vn.nhom10.crm.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.nhom10.crm.model.NguoiDung;
import vn.nhom10.crm.service.PhienService;

import java.io.IOException;

/**
 * API gia hạn phiên đăng nhập tự động khi người dùng còn hoạt động (AC1).
 * Giúp người dùng không bị mất dữ liệu đang soạn thảo (ví dụ: ghi chú cuộc gặp)
 * khi làm việc liên tục trên cùng một trang.
 */
@WebServlet(name = "GiaHanPhienServlet", urlPatterns = {"/api/phien/gia-han", "/phien/keep-alive"})
public class GiaHanPhienServlet extends HttpServlet {

    private PhienService phienService;

    @Override
    public void init() {
        if (this.phienService == null) {
            this.phienService = new PhienService();
        }
    }

    public void setPhienService(PhienService phienService) {
        this.phienService = phienService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        xuLyGiaHan(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        xuLyGiaHan(request, response);
    }

    private void xuLyGiaHan(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(PhienService.SESSION_USER_KEY) == null) {
            traVeLoiHetHan(response);
            return;
        }

        String maPhien = (String) session.getAttribute(PhienService.SESSION_TOKEN_KEY);
        if (maPhien == null) {
            maPhien = session.getId();
        }

        boolean thanhCong = phienService.giaHanPhien(maPhien, session);
        if (!thanhCong) {
            phienService.dangXuat(request, response);
            traVeLoiHetHan(response);
            return;
        }

        NguoiDung nguoiDung = (NguoiDung) session.getAttribute(PhienService.SESSION_USER_KEY);
        long soGiayConLai = (long) PhienService.THOI_GIAN_HET_HAN_MAC_DINH_PHUT * 60;

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(String.format(
                "{\"thanhCong\":true,\"thoiGianConLai\":%d,\"email\":\"%s\",\"thongBao\":\"Phiên đã được tự động gia hạn thành công.\"}",
                soGiayConLai,
                nguoiDung != null ? nguoiDung.getEmail() : ""
        ));
    }

    private void traVeLoiHetHan(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"thanhCong\":false,\"maLoi\":\"SESSION_EXPIRED\",\"thongBao\":\"Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.\"}");
    }
}
