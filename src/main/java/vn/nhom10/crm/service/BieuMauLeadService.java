package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.BieuMauLeadDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dto.GuiFormLeadDTO;
import vn.nhom10.crm.dto.KetQuaGuiFormDTO;
import vn.nhom10.crm.dto.MaNhungDTO;
import vn.nhom10.crm.model.BieuMauLead;
import vn.nhom10.crm.model.Lead;
import vn.nhom10.crm.model.NhatKyGuiFormLead;
import vn.nhom10.crm.model.TrangThaiLeadEnum;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service xử lý toàn bộ nghiệp vụ của Story S4-01:
 * - Sinh mã nhúng cho một biểu mẫu, dán được vào website bất kỳ (AC1).
 * - Nhận dữ liệu biểu mẫu gồm họ tên, email, SĐT, công ty, nhu cầu quan tâm (AC2).
 * - Chống spam (Honeypot) và giới hạn tần suất theo địa chỉ IP (AC3).
 * - Gửi thành công tạo lead ở trạng thái Mới và gắn đúng nguồn của biểu mẫu trong Transaction (AC4).
 */
public class BieuMauLeadService {

    private static final Logger LOGGER = Logger.getLogger(BieuMauLeadService.class.getName());

    private static final Pattern PATTERN_EMAIL =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PATTERN_SO_DIEN_THOAI =
            Pattern.compile("^[0-9+() -]{8,20}$");

    private final BieuMauLeadDAO bieuMauLeadDAO;
    private final LeadDAO leadDAO;

    public BieuMauLeadService() {
        this(new BieuMauLeadDAO(), new LeadDAO());
    }

    public BieuMauLeadService(BieuMauLeadDAO bieuMauLeadDAO, LeadDAO leadDAO) {
        this.bieuMauLeadDAO = bieuMauLeadDAO;
        this.leadDAO = leadDAO;
    }

    /**
     * Tạo biểu mẫu lead mới cho nhân viên Marketing (AC1).
     * Tự động sinh token công khai duy nhất phục vụ nhúng web.
     */
    public BieuMauLead taoBieuMau(BieuMauLead bm, Long nguoiTaoId) {
        if (bm == null) {
            throw new IllegalArgumentException("Thông tin biểu mẫu không được để trống.");
        }
        if (bm.getMaBieuMau() == null || bm.getMaBieuMau().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã biểu mẫu không được để trống.");
        }
        String maChuan = bm.getMaBieuMau().trim().toUpperCase();
        if (bieuMauLeadDAO.kiemTraTonTaiMa(maChuan, null)) {
            throw new IllegalArgumentException("Mã biểu mẫu '" + maChuan + "' đã tồn tại trên hệ thống.");
        }
        bm.setMaBieuMau(maChuan);

        if (bm.getTenBieuMau() == null || bm.getTenBieuMau().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên biểu mẫu không được để trống.");
        }
        bm.setTenBieuMau(bm.getTenBieuMau().trim());

        if (bm.getNguonLeadId() == null || bm.getNguonLeadId() <= 0) {
            throw new IllegalArgumentException("Biểu mẫu bắt buộc phải gắn với một Nguồn Lead hợp lệ.");
        }

        // Tự động sinh token công khai ngẫu nhiên an toàn nếu chưa có
        if (bm.getTokenCongKhai() == null || bm.getTokenCongKhai().trim().isEmpty()) {
            bm.setTokenCongKhai(UUID.randomUUID().toString().replace("-", ""));
        } else {
            bm.setTokenCongKhai(bm.getTokenCongKhai().trim());
        }

        if (bm.getGioiHanGuiMoiGio() == null || bm.getGioiHanGuiMoiGio() <= 0) {
            bm.setGioiHanGuiMoiGio(10); // Mặc định 10 lượt gửi / IP / giờ
        }

        bm.setCreatedBy(nguoiTaoId);
        bm.setCreatedAt(LocalDateTime.now());

        Long id = bieuMauLeadDAO.them(bm);
        bm.setId(id);
        return bm;
    }

    /**
     * Cập nhật thông tin cấu hình biểu mẫu lead.
     */
    public boolean capNhatBieuMau(BieuMauLead bm) {
        if (bm == null || bm.getId() == null) {
            throw new IllegalArgumentException("Biểu mẫu cần cập nhật không hợp lệ.");
        }
        if (bm.getTenBieuMau() == null || bm.getTenBieuMau().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên biểu mẫu không được để trống.");
        }
        if (bm.getNguonLeadId() == null || bm.getNguonLeadId() <= 0) {
            throw new IllegalArgumentException("Biểu mẫu bắt buộc phải gắn với một Nguồn Lead.");
        }
        if (bm.getGioiHanGuiMoiGio() == null || bm.getGioiHanGuiMoiGio() <= 0) {
            bm.setGioiHanGuiMoiGio(10);
        }
        return bieuMauLeadDAO.capNhat(bm);
    }

    /**
     * Lấy chi tiết biểu mẫu theo ID.
     */
    public BieuMauLead layBieuMauTheoId(Long id) {
        return bieuMauLeadDAO.timTheoId(id);
    }

    /**
     * Lấy biểu mẫu theo Token công khai.
     */
    public BieuMauLead layBieuMauTheoToken(String tokenCongKhai) {
        if (tokenCongKhai == null || tokenCongKhai.trim().isEmpty()) return null;
        return bieuMauLeadDAO.timTheoToken(tokenCongKhai.trim());
    }

    /**
     * Lấy danh sách biểu mẫu.
     */
    public List<BieuMauLead> layDanhSachBieuMau(Boolean chiLayHoatDong) {
        return bieuMauLeadDAO.layDanhSach(chiLayHoatDong);
    }

    /**
     * Sinh mã nhúng (iframe và script) để Marketing dán vào website bất kỳ (AC1).
     */
    public MaNhungDTO sinhMaNhung(Long id, String baseUrl) {
        BieuMauLead bm = bieuMauLeadDAO.timTheoId(id);
        if (bm == null) {
            throw new IllegalArgumentException("Không tìm thấy biểu mẫu với ID: " + id);
        }
        return sinhMaNhung(bm, baseUrl);
    }

    /**
     * Sinh mã nhúng từ đối tượng BieuMauLead.
     */
    public MaNhungDTO sinhMaNhung(BieuMauLead bm, String baseUrl) {
        if (bm == null) return null;
        String cleanBase = (baseUrl != null) ? baseUrl.replaceAll("/+$", "") : "";
        String maIframe = bm.sinhMaNhungIframe(cleanBase);
        String maScript = bm.sinhMaNhungScript(cleanBase);
        String duongDanTrucTiep = bm.sinhDuongDanNhung(cleanBase);

        return new MaNhungDTO(
                bm.getMaBieuMau(),
                bm.getTenBieuMau(),
                bm.getTokenCongKhai(),
                maIframe,
                maScript,
                duongDanTrucTiep
        );
    }

    /**
     * Xử lý gửi biểu mẫu thu thập lead (AC2, AC3, AC4).
     * 1. Kiểm tra tính hợp lệ của token biểu mẫu.
     * 2. Kiểm tra chống spam (Honeypot).
     * 3. Giới hạn tần suất gửi theo địa chỉ IP.
     * 4. Xác thực các trường: họ tên, email, SĐT, công ty, nhu cầu.
     * 5. Tạo Lead ở trạng thái 'MOI' và gắn đúng 'nguon_lead_id' của biểu mẫu trong Transaction.
     */
    public KetQuaGuiFormDTO xuLyGuiForm(GuiFormLeadDTO dto) {
        if (dto == null) {
            return KetQuaGuiFormDTO.thatBai(400, "Dữ liệu gửi lên không được để trống.", "DU_LIEU_RONG");
        }

        // 1. Kiểm tra Token biểu mẫu
        if (dto.getToken() == null || dto.getToken().trim().isEmpty()) {
            return KetQuaGuiFormDTO.thatBai(400, "Mã token biểu mẫu không được để trống.", "TOKEN_RONG");
        }

        BieuMauLead bm = bieuMauLeadDAO.timTheoToken(dto.getToken().trim());
        if (bm == null) {
            return KetQuaGuiFormDTO.thatBai(404, "Biểu mẫu không tồn tại hoặc mã token không chính xác.", "FORM_KHONG_TON_TAI");
        }

        if (!bm.isHoatDong()) {
            return KetQuaGuiFormDTO.thatBai(400, "Biểu mẫu này hiện đang tạm dừng tiếp nhận thông tin.", "FORM_TAM_DUNG");
        }

        String ip = (dto.getDiaChiIp() != null && !dto.getDiaChiIp().trim().isEmpty())
                ? dto.getDiaChiIp().trim()
                : "127.0.0.1";

        // 2. Chống Spam: Kiểm tra trường Honeypot (AC3)
        if (bm.isBatHoneypot() && dto.getHpWebsite() != null && !dto.getHpWebsite().trim().isEmpty()) {
            // Bot tự động điền vào trường ẩn này -> Ghi log từ chối spam
            bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "SPAM_HONEYPOT"));
            LOGGER.warning("Phát hiện spam bot gửi form ID " + bm.getId() + " từ IP " + ip);
            return KetQuaGuiFormDTO.thatBai(400, "Yêu cầu bị từ chối do phát hiện nghi vấn spam.", "SPAM_HONEYPOT");
        }

        // 3. Giới hạn tần suất theo địa chỉ IP (AC3)
        int gioiHanMoiGio = (bm.getGioiHanGuiMoiGio() != null && bm.getGioiHanGuiMoiGio() > 0)
                ? bm.getGioiHanGuiMoiGio()
                : 10;

        int soLanGuiHienTai = bieuMauLeadDAO.demSoLanGuiTrongMotGio(bm.getId(), ip);
        if (soLanGuiHienTai >= gioiHanMoiGio) {
            bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "GIOI_HAN_TAN_SUAT_IP"));
            LOGGER.warning("IP " + ip + " đã vượt giới hạn gửi form ID " + bm.getId() + " (" + soLanGuiHienTai + "/" + gioiHanMoiGio + ")");
            return KetQuaGuiFormDTO.thatBai(429,
                    "Bạn đã gửi biểu mẫu quá số lần cho phép trong 1 giờ (" + gioiHanMoiGio + " lượt/giờ). Vui lòng thử lại sau.",
                    "GIOI_HAN_TAN_SUAT_IP");
        }

        // 4. Kiểm tra hợp lệ dữ liệu các trường biểu mẫu (AC2)
        String hoTen = dto.getHoTen() != null ? dto.getHoTen().trim() : "";
        if (hoTen.isEmpty()) {
            bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "THIEU_HO_TEN"));
            return KetQuaGuiFormDTO.thatBai(400, "Vui lòng nhập họ và tên của bạn.", "THIEU_HO_TEN");
        }
        if (hoTen.length() > 150) {
            bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "HO_TEN_QUA_DAI"));
            return KetQuaGuiFormDTO.thatBai(400, "Họ và tên không được vượt quá 150 ký tự.", "HO_TEN_QUA_DAI");
        }

        String email = dto.getEmail() != null ? dto.getEmail().trim() : "";
        String soDienThoai = dto.getSoDienThoai() != null ? dto.getSoDienThoai().trim() : "";

        // Bắt buộc có ít nhất một kênh liên hệ (Email hoặc Số điện thoại)
        if (email.isEmpty() && soDienThoai.isEmpty()) {
            bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "THIEU_THONG_TIN_LIEN_HE"));
            return KetQuaGuiFormDTO.thatBai(400, "Vui lòng cung cấp ít nhất một phương thức liên lạc: Email hoặc Số điện thoại.", "THIEU_THONG_TIN_LIEN_HE");
        }

        if (!email.isEmpty()) {
            if (!PATTERN_EMAIL.matcher(email).matches()) {
                bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "EMAIL_KHONG_HOP_LE"));
                return KetQuaGuiFormDTO.thatBai(400, "Địa chỉ email không đúng định dạng.", "EMAIL_KHONG_HOP_LE");
            }
            if (email.length() > 255) {
                bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "EMAIL_QUA_DAI"));
                return KetQuaGuiFormDTO.thatBai(400, "Địa chỉ email không được vượt quá 255 ký tự.", "EMAIL_QUA_DAI");
            }
        }

        if (!soDienThoai.isEmpty()) {
            if (!PATTERN_SO_DIEN_THOAI.matcher(soDienThoai).matches()) {
                bieuMauLeadDAO.ghiNhatKy(new NhatKyGuiFormLead(bm.getId(), ip, false, "SDT_KHONG_HOP_LE"));
                return KetQuaGuiFormDTO.thatBai(400, "Số điện thoại không hợp lệ (từ 8 đến 20 chữ số).", "SDT_KHONG_HOP_LE");
            }
        }

        String tenCongTy = dto.getTenCongTy() != null ? dto.getTenCongTy().trim() : null;
        if (tenCongTy != null && tenCongTy.length() > 255) {
            tenCongTy = tenCongTy.substring(0, 255);
        }

        String nhuCauQuanTam = dto.getNhuCauQuanTam() != null ? dto.getNhuCauQuanTam().trim() : null;

        // 5. Chuẩn bị đối tượng Lead & Thực thi lưu trong Transaction (AC4)
        String maLead = sinhMaLeadMoi();

        Lead lead = new Lead();
        lead.setMaLead(maLead);
        lead.setHoTen(hoTen);
        lead.setEmail(!email.isEmpty() ? email : null);
        lead.setEmailChuanHoa(!email.isEmpty() ? email.toLowerCase() : null);
        lead.setSoDienThoai(!soDienThoai.isEmpty() ? soDienThoai : null);
        lead.setSoDienThoaiChuanHoa(!soDienThoai.isEmpty() ? soDienThoai.replaceAll("[^0-9]", "") : null);
        lead.setTenCongTy(tenCongTy);
        lead.setTenCongTyChuanHoa(tenCongTy != null ? tenCongTy.toUpperCase() : null);
        lead.setNhuCauQuanTam(nhuCauQuanTam);

        // GẮN ĐÚNG NGUỒN CỦA BIỂU MẪU (AC4)
        lead.setNguonLeadId(bm.getNguonLeadId());
        lead.setChienDichId(bm.getChienDichId());
        lead.setBieuMauId(bm.getId());

        // TRẠNG THÁI MỚI (AC4)
        lead.setTrangThai(TrangThaiLeadEnum.MOI);
        lead.setDiem(0);
        lead.setQuaSla(false);
        lead.setCreatedAt(LocalDateTime.now());

        // Thực thi Transaction lưu Lead và Nhật ký gửi
        try (Connection conn = DatabaseConnection.layKetNoi()) {
            boolean autoCommitBanDau = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                Long leadId = leadDAO.them(conn, lead);
                lead.setId(leadId);

                NhatKyGuiFormLead nhatKy = new NhatKyGuiFormLead(bm.getId(), ip, true, null);
                bieuMauLeadDAO.ghiNhatKy(conn, nhatKy);

                conn.commit();
                LOGGER.info("Tạo Lead thành công từ biểu mẫu ID " + bm.getId() + " với Mã Lead: " + maLead + " (Nguồn: " + bm.getNguonLeadId() + ")");
                return KetQuaGuiFormDTO.thanhCong(maLead, leadId, "Gửi thông tin thành công! Bộ phận tư vấn sẽ sớm liên hệ với bạn.");
            } catch (Exception e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Lỗi rollback transaction tạo lead từ biểu mẫu: " + bm.getMaBieuMau(), e);
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitBanDau);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối CSDL khi tạo lead từ form", e);
            return KetQuaGuiFormDTO.thatBai(500, "Hệ thống gặp sự cố trong quá trình lưu trữ thông tin. Vui lòng thử lại sau.", "LOI_DATABASE");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi không xác định khi tạo lead từ form", e);
            return KetQuaGuiFormDTO.thatBai(500, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.", "LOI_HE_THONG");
        }
    }

    /**
     * Sinh mã định danh Lead duy nhất: LEAD-yyMMdd-XXXXX
     */
    public synchronized String sinhMaLeadMoi() {
        String ngay = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String ngauNhien = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
        String maLead = "LEAD-" + ngay + "-" + ngauNhien;

        while (leadDAO.kiemTraTonTaiMa(maLead)) {
            ngauNhien = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
            maLead = "LEAD-" + ngay + "-" + ngauNhien;
        }
        return maLead;
    }
}
