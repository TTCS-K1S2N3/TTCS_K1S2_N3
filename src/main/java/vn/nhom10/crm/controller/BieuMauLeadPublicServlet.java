package vn.nhom10.crm.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.nhom10.crm.dto.GuiFormLeadDTO;
import vn.nhom10.crm.dto.KetQuaGuiFormDTO;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.service.BieuMauLeadService;

import java.io.IOException;

/**
 * Servlet công khai phục vụ người dùng bên ngoài website xem và gửi biểu mẫu nhúng (Story S4-01).
 * URL:
 * - /bieu-mau/nhung : Hiển thị giao diện biểu mẫu nhúng iframe hoặc trực tiếp
 * - /bieu-mau/gui   : Xử lý submit form từ website bên ngoài (hỗ trợ cả AJAX JSON và Form POST)
 * - /bieu-mau/embed.js : Cung cấp script nhúng tự động cho website bất kỳ
 */
@WebServlet(name = "BieuMauLeadPublicServlet", urlPatterns = {"/bieu-mau/nhung", "/bieu-mau/gui", "/bieu-mau/embed.js"})
public class BieuMauLeadPublicServlet extends HttpServlet {

    private final BieuMauLeadService bieuMauLeadService;

    public BieuMauLeadPublicServlet() {
        this(new BieuMauLeadService());
    }

    public BieuMauLeadPublicServlet(BieuMauLeadService bieuMauLeadService) {
        this.bieuMauLeadService = bieuMauLeadService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/bieu-mau/embed.js".equals(path)) {
            xuLyTraVeScriptNhung(request, response);
            return;
        }

        // /bieu-mau/nhung: Hiển thị giao diện form
        String token = request.getParameter("token");
        if (token == null || token.trim().isEmpty()) {
            request.setAttribute("thongBaoLoi", "Mã token biểu mẫu không được để trống.");
            request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-nhung.jsp").forward(request, response);
            return;
        }

        BieuMauLead bm = bieuMauLeadService.layBieuMauTheoToken(token.trim());
        if (bm == null) {
            request.setAttribute("thongBaoLoi", "Biểu mẫu không tồn tại hoặc đường dẫn nhúng không chính xác.");
            request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-nhung.jsp").forward(request, response);
            return;
        }

        if (!bm.isHoatDong()) {
            request.setAttribute("thongBaoLoi", "Biểu mẫu này hiện đang tạm dừng tiếp nhận thông tin.");
            request.setAttribute("bieuMau", bm);
            request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-nhung.jsp").forward(request, response);
            return;
        }

        request.setAttribute("bieuMau", bm);
        request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-nhung.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");
        String hoTen = request.getParameter("ho_ten");
        String email = request.getParameter("email");
        String soDienThoai = request.getParameter("so_dien_thoai");
        String tenCongTy = request.getParameter("ten_cong_ty");
        String nhuCauQuanTam = request.getParameter("nhu_cau_quan_tam");
        String hpWebsite = request.getParameter("website_url"); // Honeypot field (ẩn ở form)

        String diaChiIp = layDiaChiIpClient(request);

        GuiFormLeadDTO dto = new GuiFormLeadDTO(token, hoTen, email, soDienThoai, tenCongTy, nhuCauQuanTam);
        dto.setHpWebsite(hpWebsite);
        dto.setDiaChiIp(diaChiIp);

        KetQuaGuiFormDTO ketQua = bieuMauLeadService.xuLyGuiForm(dto);

        boolean laAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"))
                || "true".equalsIgnoreCase(request.getParameter("ajax"));

        if (laAjax) {
            response.setStatus(ketQua.getMaHttp());
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*"); // Cho phép gọi API từ website nhúng bên ngoài

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"thanhCong\":").append(ketQua.isThanhCong()).append(",");
            json.append("\"thongBao\":\"").append(escapeJson(ketQua.getThongBao())).append("\"");
            if (ketQua.getMaLead() != null) {
                json.append(",\"maLead\":\"").append(escapeJson(ketQua.getMaLead())).append("\"");
            }
            if (ketQua.getLyDoTuChoi() != null) {
                json.append(",\"lyDoTuChoi\":\"").append(escapeJson(ketQua.getLyDoTuChoi())).append("\"");
            }
            json.append("}");

            response.getWriter().write(json.toString());
            return;
        }

        // Nếu gửi theo Form submit truyền thống
        if (ketQua.isThanhCong()) {
            request.setAttribute("thongBaoThanhCong", ketQua.getThongBao());
            request.setAttribute("maLead", ketQua.getMaLead());
            request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-ket-qua.jsp").forward(request, response);
        } else {
            request.setAttribute("thongBaoLoi", ketQua.getThongBao());
            request.setAttribute("dto", dto);
            if (token != null) {
                request.setAttribute("bieuMau", bieuMauLeadService.layBieuMauTheoToken(token));
            }
            request.getRequestDispatcher("/WEB-INF/views/lead/bieu-mau-nhung.jsp").forward(request, response);
        }
    }

    private void xuLyTraVeScriptNhung(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/javascript;charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String token = request.getParameter("token");
        String contextPath = request.getContextPath();
        String baseUrl = request.getScheme() + "://" + request.getServerName() +
                ((request.getServerPort() == 80 || request.getServerPort() == 443) ? "" : ":" + request.getServerPort()) +
                contextPath;

        String script = "(function() {\n" +
                "  var token = '" + (token != null ? escapeJson(token) : "") + "';\n" +
                "  if (!token) {\n" +
                "    var currentScript = document.currentScript || (function() {\n" +
                "      var scripts = document.getElementsByTagName('script');\n" +
                "      return scripts[scripts.length - 1];\n" +
                "    })();\n" +
                "    if (currentScript) token = currentScript.getAttribute('data-token');\n" +
                "  }\n" +
                "  if (!token) return console.error('CRM Embed Form: Missing data-token');\n" +
                "  var target = document.getElementById('crm-form-' + token) || document.getElementById('crm-lead-form-' + token);\n" +
                "  if (!target) {\n" +
                "    target = document.createElement('div');\n" +
                "    target.id = 'crm-form-' + token;\n" +
                "    document.body.appendChild(target);\n" +
                "  }\n" +
                "  var iframe = document.createElement('iframe');\n" +
                "  iframe.src = '" + baseUrl + "/bieu-mau/nhung?token=' + encodeURIComponent(token);\n" +
                "  iframe.style.width = '100%';\n" +
                "  iframe.style.height = '520px';\n" +
                "  iframe.style.border = 'none';\n" +
                "  iframe.style.display = 'block';\n" +
                "  iframe.style.maxWidth = '600px';\n" +
                "  iframe.style.margin = '0 auto';\n" +
                "  iframe.setAttribute('loading', 'lazy');\n" +
                "  target.innerHTML = '';\n" +
                "  target.appendChild(iframe);\n" +
                "})();";

        response.getWriter().write(script);
    }

    /**
     * Trích xuất địa chỉ IP client chuẩn xác, hỗ trợ Reverse Proxy / Cloudflare / Load Balancer.
     */
    public String layDiaChiIpClient(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            // Lấy IP đầu tiên trong chuỗi forwarded
            ip = ip.split(",")[0].trim();
        }
        // Xử lý IPv6 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip != null ? ip.trim() : "127.0.0.1";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
