package vn.nhom10.crm.service;

import vn.nhom10.crm.dao.DanhMucBanHangDAO;
import vn.nhom10.crm.dao.KhuVucDiaLyDAO;
import vn.nhom10.crm.dao.LeadDAO;
import vn.nhom10.crm.dao.LichSuPhanBoLeadDAO;
import vn.nhom10.crm.dao.NhomKinhDoanhDAO;
import vn.nhom10.crm.dao.QuyTacPhanBoLeadDAO;
import vn.nhom10.crm.dto.KetQuaPhanBoDTO;
import vn.nhom10.crm.dto.LeadHangChoDTO;
import vn.nhom10.crm.dto.PhanBoLeadRequestDTO;
import vn.nhom10.crm.dto.QuyTacPhanBoDTO;
import vn.nhom10.crm.model.*;
import vn.nhom10.crm.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service xử lý toàn bộ logic nghiệp vụ phân bổ lead tự động (Story S4-06).
 * - AC1: Phân bổ theo khu vực, theo ngành nghề, hoặc xoay vòng đều trong nhóm (Round-Robin).
 * - AC2: Nhiều quy tắc xếp theo thứ tự ưu tiên, quy tắc đầu tiên khớp sẽ thắng (First-Match-Wins).
 * - AC3: Lead không khớp quy tắc nào rơi vào hàng chờ để trưởng nhóm phân tay (Enforce Data Scope).
 * - AC4: Phân bổ chạy nền hoàn tất trong vòng 5 phút kể từ khi lead vào.
 */
public class QuyTacPhanBoService {

    private static final Logger LOGGER = Logger.getLogger(QuyTacPhanBoService.class.getName());

    private final QuyTacPhanBoLeadDAO quyTacDAO;
    private final LichSuPhanBoLeadDAO lichSuDAO;
    private final LeadDAO leadDAO;
    private final NhomKinhDoanhDAO nhomKinhDoanhDAO;
    private final KhuVucDiaLyDAO khuVucDAO;
    private final DanhMucBanHangDAO danhMucDAO;

    public QuyTacPhanBoService() {
        this(new QuyTacPhanBoLeadDAO(), new LichSuPhanBoLeadDAO(), new LeadDAO(),
                new NhomKinhDoanhDAO(), new KhuVucDiaLyDAO(), new DanhMucBanHangDAO());
    }

    public QuyTacPhanBoService(QuyTacPhanBoLeadDAO quyTacDAO, LichSuPhanBoLeadDAO lichSuDAO, LeadDAO leadDAO,
                               NhomKinhDoanhDAO nhomKinhDoanhDAO, KhuVucDiaLyDAO khuVucDAO, DanhMucBanHangDAO danhMucDAO) {
        this.quyTacDAO = quyTacDAO;
        this.lichSuDAO = lichSuDAO;
        this.leadDAO = leadDAO;
        this.nhomKinhDoanhDAO = nhomKinhDoanhDAO;
        this.khuVucDAO = khuVucDAO;
        this.danhMucDAO = danhMucDAO;
    }

    /**
     * Lấy danh sách quy tắc phân bổ kèm mô tả điều kiện chi tiết hiển thị cho UI.
     */
    public List<QuyTacPhanBoDTO> layDanhSachQuyTac(boolean chiLayHoatDong) {
        List<QuyTacPhanBoLead> ds = quyTacDAO.layDanhSach(chiLayHoatDong);
        List<QuyTacPhanBoDTO> dtos = new ArrayList<>();

        for (QuyTacPhanBoLead qt : ds) {
            QuyTacPhanBoDTO dto = chuyenSangDTO(qt);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Tạo mới quy tắc phân bổ (AC1).
     */
    public QuyTacPhanBoLead taoQuyTac(PhanBoLeadRequestDTO req, Long userId) {
        validateRequest(req);

        QuyTacPhanBoLead qt = new QuyTacPhanBoLead();
        qt.setMaQuyTac("QT_" + System.currentTimeMillis());
        qt.setTenQuyTac(req.getTenQuyTac().trim());

        KieuPhanBoEnum kieu = KieuPhanBoEnum.tuChuoi(req.getKieuPhanBo());
        qt.setKieuPhanBo(kieu);

        String json = buildDieuKienJson(kieu, req.getKhuVucIds(), req.getNganhNgheIds());
        qt.setDieuKienJson(json);

        qt.setNhomDichId(req.getNhomDichId());
        qt.setNguoiDungDichId(req.getNguoiDungDichId());
        qt.setHoatDong(req.getHoatDong() != null ? req.getHoatDong() : true);
        qt.setCreatedBy(userId);

        int maxOrder = quyTacDAO.layThuTuUuTienLonNhat();
        qt.setThuTuUuTien(maxOrder + 1);

        Long id = quyTacDAO.them(qt);
        qt.setId(id);
        return qt;
    }

    /**
     * Cập nhật quy tắc phân bổ (AC1).
     */
    public boolean suaQuyTac(Long id, PhanBoLeadRequestDTO req, Long userId) {
        if (id == null) throw new IllegalArgumentException("ID quy tắc không được để trống.");
        validateRequest(req);

        QuyTacPhanBoLead existing = quyTacDAO.timTheoId(id);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy quy tắc phân bổ với ID=" + id);
        }

        existing.setTenQuyTac(req.getTenQuyTac().trim());
        KieuPhanBoEnum kieu = KieuPhanBoEnum.tuChuoi(req.getKieuPhanBo());
        existing.setKieuPhanBo(kieu);

        String json = buildDieuKienJson(kieu, req.getKhuVucIds(), req.getNganhNgheIds());
        existing.setDieuKienJson(json);

        existing.setNhomDichId(req.getNhomDichId());
        existing.setNguoiDungDichId(req.getNguoiDungDichId());
        if (req.getHoatDong() != null) {
            existing.setHoatDong(req.getHoatDong());
        }

        return quyTacDAO.capNhat(existing);
    }

    /**
     * Đổi thứ tự ưu tiên (AC2).
     */
    public boolean doiThuTuUuTien(Long id, String huongDiChuyen) {
        if (id == null || huongDiChuyen == null) return false;
        return quyTacDAO.doiThuTuUuTien(id, huongDiChuyen);
    }

    /**
     * Đổi trạng thái hoạt động (Bật/Tắt).
     */
    public boolean doiTrangThai(Long id, boolean hoatDong) {
        if (id == null) return false;
        return quyTacDAO.capNhatTrangThai(id, hoatDong);
    }

    /**
     * Xóa quy tắc phân bổ.
     */
    public boolean xoaQuyTac(Long id) {
        if (id == null) return false;
        return quyTacDAO.xoa(id);
    }

    /**
     * Phân bổ tự động cho MỘT Lead cụ thể (AC1, AC2, AC3).
     * Trả về true nếu phân bổ thành công cho Sales Rep; false nếu rơi vào hàng chờ.
     */
    public boolean phanBoChoMotLead(Connection conn, Lead lead) throws SQLException {
        if (lead == null || lead.getId() == null) return false;

        List<QuyTacPhanBoLead> activeRules = quyTacDAO.layDanhSach(conn, true);

        for (QuyTacPhanBoLead rule : activeRules) {
            if (kiemTraLeadKhopQuyTac(lead, rule)) {
                // Khớp quy tắc đầu tiên -> Áp dụng ngay và dừng duyệt (AC2: First-Match-Wins)
                Long targetUserId = rule.getNguoiDungDichId();
                Long targetTeamId = rule.getNhomDichId();

                if (targetUserId == null && targetTeamId != null) {
                    // Cơ chế xoay vòng Round-Robin nội bộ nhóm (AC1)
                    targetUserId = xacDinhNguoiDungXoayVong(conn, rule);
                }

                if (targetUserId != null) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime sla = now.plusHours(24); // Mặc định 24h phản hồi theo chuẩn SLA

                    leadDAO.capNhatPhanBo(conn, lead.getId(), targetUserId, targetTeamId,
                            TrangThaiLeadEnum.DA_PHAN_BO, now, sla);

                    LichSuPhanBoLead ls = new LichSuPhanBoLead(
                            lead.getId(),
                            rule.getId(),
                            targetUserId,
                            targetTeamId,
                            HanhDongPhanBoEnum.TU_DONG_THEO_QUY_TAC,
                            "Khớp quy tắc ưu tiên #" + rule.getThuTuUuTien() + " (" + rule.getTenQuyTac() + ")",
                            null
                    );
                    lichSuDAO.them(conn, ls);
                    return true;
                }
            }
        }

        // Không khớp bất kỳ quy tắc nào -> Rơi vào hàng chờ phân bổ (AC3)
        leadDAO.chuyenVaoHangCho(conn, lead.getId());
        LichSuPhanBoLead lsHangCho = new LichSuPhanBoLead(
                lead.getId(),
                null,
                null,
                null,
                HanhDongPhanBoEnum.VAO_HANG_CHO,
                "Không khớp bất kỳ quy tắc phân bổ tự động nào",
                null
        );
        lichSuDAO.them(conn, lsHangCho);
        return false;
    }

    /**
     * Chạy tiến trình phân bổ hàng loạt cho các lead chưa phân bổ (AC4).
     */
    public KetQuaPhanBoDTO chayTienTrinhPhanBoBatch(int batchSize) {
        long startTime = System.currentTimeMillis();
        List<Lead> pendingLeads = leadDAO.layDanhSachChuaPhanBo(batchSize > 0 ? batchSize : 50);

        int thanhCong = 0;
        int vaoHangCho = 0;

        for (Lead lead : pendingLeads) {
            try (Connection conn = DatabaseConnection.layKetNoi()) {
                conn.setAutoCommit(false);
                try {
                    boolean assigned = phanBoChoMotLead(conn, lead);
                    if (assigned) {
                        thanhCong++;
                    } else {
                        vaoHangCho++;
                    }
                    conn.commit();
                } catch (Exception ex) {
                    conn.rollback();
                    LOGGER.log(Level.SEVERE, "Lỗi phân bổ lead ID=" + lead.getId(), ex);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Lỗi kết nối khi phân bổ batch lead: " + e.getMessage(), e);
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return new KetQuaPhanBoDTO(pendingLeads.size(), thanhCong, vaoHangCho, elapsed);
    }

    /**
     * Trưởng nhóm hoặc Giám đốc phân tay một lead từ hàng chờ (AC3).
     */
    public boolean phanTayTuHangCho(Long leadId, Long nhanVienId, NguoiDung nguoiThaoTac, String ghiChu) {
        if (leadId == null || nhanVienId == null || nguoiThaoTac == null) {
            throw new IllegalArgumentException("Dữ liệu phân bổ không hợp lệ.");
        }

        // Kiểm tra phân quyền và Data Scope
        boolean laAdmin = nguoiThaoTac.coVaiTro(VaiTroEnum.ADMIN);
        boolean laDirector = nguoiThaoTac.coVaiTro(VaiTroEnum.DIRECTOR);
        boolean laTeamLead = nguoiThaoTac.coVaiTro(VaiTroEnum.TEAM_LEAD);

        if (!laAdmin && !laDirector && !laTeamLead) {
            throw new SecurityException("Bạn không có quyền phân bổ lead.");
        }

        if (laTeamLead && !laAdmin && !laDirector) {
            // Team Lead chỉ được phân bổ cho nhân viên trong nhóm do mình quản lý
            Integer nhomIdCuaLead = nguoiThaoTac.getNhomKinhDoanhId();
            if (nhomIdCuaLead == null) {
                throw new SecurityException("Trưởng nhóm chưa được gán vào nhóm kinh doanh nào.");
            }

            List<NguoiDung> thanhVienNhom = nhomKinhDoanhDAO.layDsThanhVien(nhomIdCuaLead.longValue());
            boolean nhanVienThuocNhom = thanhVienNhom.stream().anyMatch(nd -> nhanVienId != null && nd.getId() == nhanVienId);
            if (!nhanVienThuocNhom) {
                throw new SecurityException("Bạn chỉ có quyền phân bổ cho nhân viên thuộc nhóm của mình.");
            }
        }

        try (Connection conn = DatabaseConnection.layKetNoi()) {
            conn.setAutoCommit(false);
            try {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime sla = now.plusHours(24);

                Long nhomId = nguoiThaoTac.getNhomKinhDoanhId() != null ? Long.valueOf(nguoiThaoTac.getNhomKinhDoanhId()) : null;
                leadDAO.capNhatPhanBo(conn, leadId, nhanVienId, nhomId,
                        TrangThaiLeadEnum.DA_PHAN_BO, now, sla);

                String lyDo = (ghiChu != null && !ghiChu.isBlank()) ? ghiChu.trim() : "Trưởng nhóm phân bổ thủ công";
                LichSuPhanBoLead ls = new LichSuPhanBoLead(
                        leadId,
                        null,
                        nhanVienId,
                        nhomId,
                        HanhDongPhanBoEnum.TRUONG_NHOM_PHAN_TAY,
                        lyDo,
                        nguoiThaoTac.getId()
                );
                lichSuDAO.them(conn, ls);

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Lỗi phân bổ tay lead ID=" + leadId, ex);
                throw new RuntimeException("Lỗi phân bổ lead: " + ex.getMessage(), ex);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Lỗi kết nối khi phân bổ tay: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy danh sách lead trong hàng chờ phân bổ (AC3).
     */
    public List<LeadHangChoDTO> layDanhSachHangCho(int limit, int offset) {
        List<Lead> leads = leadDAO.layDanhSachHangCho(limit, offset);
        List<LeadHangChoDTO> dtos = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Lead l : leads) {
            LeadHangChoDTO dto = new LeadHangChoDTO();
            dto.setId(l.getId());
            dto.setMaLead(l.getMaLead());
            dto.setHoTen(l.getHoTen());
            dto.setEmail(l.getEmail());
            dto.setSoDienThoai(l.getSoDienThoai());
            dto.setTenCongTy(l.getTenCongTy());
            dto.setNhuCauQuanTam(l.getNhuCauQuanTam());
            dto.setKhuVucId(l.getKhuVucId());
            dto.setNganhNgheId(l.getNganhNgheId());
            dto.setNguonLeadId(l.getNguonLeadId());
            dto.setTenNguonLead(l.getTenNguonLead());
            dto.setDiem(l.getDiem());
            dto.setPhanLoai(l.getPhanLoai());
            dto.setCreatedAt(l.getCreatedAt());

            if (l.getCreatedAt() != null) {
                Duration d = Duration.between(l.getCreatedAt(), now);
                long phut = d.toMinutes();
                if (phut < 60) {
                    dto.setThoiGianChoFormatted(phut + " phút trước");
                } else {
                    long gio = d.toHours();
                    dto.setThoiGianChoFormatted(gio + " giờ trước");
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

    public int demLeadHangCho() {
        return leadDAO.demLeadHangCho();
    }

    // ==========================================
    // THUẬT TOÁN SO KHỚP & XOAY VÒNG ROUND-ROBIN
    // ==========================================

    private boolean kiemTraLeadKhopQuyTac(Lead lead, QuyTacPhanBoLead rule) {
        if (rule.getKieuPhanBo() == null) return false;

        switch (rule.getKieuPhanBo()) {
            case THEO_KHU_VUC:
                if (lead.getKhuVucId() == null) return false;
                List<Long> khuVucIds = trichXuatDanhSachIdTuJson(rule.getDieuKienJson(), "khu_vuc_ids");
                return khuVucIds.contains(lead.getKhuVucId());

            case THEO_NGANH_NGHE:
                if (lead.getNganhNgheId() == null) return false;
                List<Long> nganhNgheIds = trichXuatDanhSachIdTuJson(rule.getDieuKienJson(), "nganh_nghe_ids");
                return nganhNgheIds.contains(lead.getNganhNgheId());

            case XOAY_VONG:
                // Quy tắc xoay vòng: nếu có điều kiện bổ sung thì kiểm tra, nếu rỗng thì áp dụng
                return true;

            default:
                return false;
        }
    }

    private Long xacDinhNguoiDungXoayVong(Connection conn, QuyTacPhanBoLead rule) throws SQLException {
        if (rule.getNhomDichId() == null) return null;

        List<NguoiDung> thanhVienNhom = nhomKinhDoanhDAO.layDsThanhVien(rule.getNhomDichId());
        if (thanhVienNhom == null || thanhVienNhom.isEmpty()) return null;

        // Chỉ chọn nhân viên đang hoạt động
        List<NguoiDung> salesReps = new ArrayList<>();
        for (NguoiDung nd : thanhVienNhom) {
            if (NguoiDung.TRANG_THAI_HOAT_DONG.equals(nd.getTrangThai())) {
                salesReps.add(nd);
            }
        }
        if (salesReps.isEmpty()) return null;

        // Sắp xếp tăng dần theo ID
        salesReps.sort(Comparator.comparing(NguoiDung::getId));

        Long lastAssignedId = rule.getNguoiDungLanCuoiId();
        NguoiDung selectedUser = salesReps.get(0); // Mặc định người đầu tiên

        if (lastAssignedId != null) {
            int lastIndex = -1;
            for (int i = 0; i < salesReps.size(); i++) {
                if (salesReps.get(i).getId() == lastAssignedId.longValue()) {
                    lastIndex = i;
                    break;
                }
            }
            if (lastIndex != -1) {
                int nextIndex = (lastIndex + 1) % salesReps.size();
                selectedUser = salesReps.get(nextIndex);
            }
        }

        // Cập nhật con trỏ người dùng lần cuối
        quyTacDAO.capNhatNguoiDungLanCuoi(conn, rule.getId(), selectedUser.getId());
        rule.setNguoiDungLanCuoiId(selectedUser.getId());

        return selectedUser.getId();
    }

    private void validateRequest(PhanBoLeadRequestDTO req) {
        if (req == null) throw new IllegalArgumentException("Yêu cầu không được rỗng.");
        if (req.getTenQuyTac() == null || req.getTenQuyTac().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên quy tắc không được để trống.");
        }
        if (req.getTenQuyTac().trim().length() > 200) {
            throw new IllegalArgumentException("Tên quy tắc tối đa 200 ký tự.");
        }
        KieuPhanBoEnum kieu = KieuPhanBoEnum.tuChuoi(req.getKieuPhanBo());
        if (kieu == null) {
            throw new IllegalArgumentException("Kiểu phân bổ không hợp lệ. Chỉ chấp nhận THEO_KHU_VUC, THEO_NGANH_NGHE, XOAY_VONG.");
        }

        if (kieu == KieuPhanBoEnum.THEO_KHU_VUC) {
            if (req.getKhuVucIds() == null || req.getKhuVucIds().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một khu vực áp dụng.");
            }
        } else if (kieu == KieuPhanBoEnum.THEO_NGANH_NGHE) {
            if (req.getNganhNgheIds() == null || req.getNganhNgheIds().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn ít nhất một ngành nghề áp dụng.");
            }
        } else if (kieu == KieuPhanBoEnum.XOAY_VONG) {
            if (req.getNhomDichId() == null) {
                throw new IllegalArgumentException("Vui lòng chọn nhóm kinh doanh để xoay vòng phân bổ.");
            }
        }
    }

    private String buildDieuKienJson(KieuPhanBoEnum kieu, List<Long> khuVucIds, List<Long> nganhNgheIds) {
        StringBuilder sb = new StringBuilder("{");
        if (kieu == KieuPhanBoEnum.THEO_KHU_VUC && khuVucIds != null && !khuVucIds.isEmpty()) {
            sb.append("\"khu_vuc_ids\":[");
            for (int i = 0; i < khuVucIds.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(khuVucIds.get(i));
            }
            sb.append("]");
        } else if (kieu == KieuPhanBoEnum.THEO_NGANH_NGHE && nganhNgheIds != null && !nganhNgheIds.isEmpty()) {
            sb.append("\"nganh_nghe_ids\":[");
            for (int i = 0; i < nganhNgheIds.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(nganhNgheIds.get(i));
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    public List<Long> trichXuatDanhSachIdTuJson(String json, String arrayKey) {
        List<Long> result = new ArrayList<>();
        if (json == null || json.isBlank() || arrayKey == null) return result;

        Pattern pattern = Pattern.compile("\"" + Pattern.quote(arrayKey) + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String elements = matcher.group(1).trim();
            if (!elements.isEmpty()) {
                String[] parts = elements.split(",");
                for (String p : parts) {
                    try {
                        result.add(Long.parseLong(p.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return result;
    }

    private QuyTacPhanBoDTO chuyenSangDTO(QuyTacPhanBoLead qt) {
        QuyTacPhanBoDTO dto = new QuyTacPhanBoDTO();
        dto.setId(qt.getId());
        dto.setMaQuyTac(qt.getMaQuyTac());
        dto.setTenQuyTac(qt.getTenQuyTac());
        dto.setThuTuUuTien(qt.getThuTuUuTien());
        dto.setKieuPhanBo(qt.getKieuPhanBo());
        dto.setKieuPhanBoTenHienThi(qt.getKieuPhanBo() != null ? qt.getKieuPhanBo().getTenHienThi() : "");
        dto.setDieuKienJson(qt.getDieuKienJson());
        dto.setNhomDichId(qt.getNhomDichId());
        dto.setTenNhomDich(qt.getTenNhomDich());
        dto.setNguoiDungDichId(qt.getNguoiDungDichId());
        dto.setTenNguoiDungDich(qt.getTenNguoiDungDich());
        dto.setNguoiDungLanCuoiId(qt.getNguoiDungLanCuoiId());
        dto.setTenNguoiDungLanCuoi(qt.getTenNguoiDungLanCuoi());
        dto.setHoatDong(qt.isHoatDong());
        dto.setUpdatedAt(qt.getUpdatedAt());

        // Mô tả điều kiện
        if (qt.getKieuPhanBo() == KieuPhanBoEnum.THEO_KHU_VUC) {
            List<Long> ids = trichXuatDanhSachIdTuJson(qt.getDieuKienJson(), "khu_vuc_ids");
            dto.setDieuKienMoTa("Áp dụng " + ids.size() + " khu vực địa lý");
        } else if (qt.getKieuPhanBo() == KieuPhanBoEnum.THEO_NGANH_NGHE) {
            List<Long> ids = trichXuatDanhSachIdTuJson(qt.getDieuKienJson(), "nganh_nghe_ids");
            dto.setDieuKienMoTa("Áp dụng " + ids.size() + " ngành nghề");
        } else if (qt.getKieuPhanBo() == KieuPhanBoEnum.XOAY_VONG) {
            dto.setDieuKienMoTa("Xoay vòng đều trong nhóm");
        } else {
            dto.setDieuKienMoTa("Mặc định");
        }

        return dto;
    }
}
