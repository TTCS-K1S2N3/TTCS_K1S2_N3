package vn.nhom10.crm.service;

import vn.nhom10.crm.dto.KetQuaPhanBoDTO;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker chạy ngầm định kỳ quét và phân bổ tự động các Lead mới (Story S4-06, AC4).
 * Cam kết: Hoàn tất phân bổ trong vòng 5 phút kể từ khi lead được tạo.
 * Mặc định chu kỳ quét: 30 giây / lần.
 */
public class LeadAssignmentBackgroundWorker {

    private static final Logger LOGGER = Logger.getLogger(LeadAssignmentBackgroundWorker.class.getName());
    private static LeadAssignmentBackgroundWorker instance;

    private final QuyTacPhanBoService quyTacPhanBoService;
    private final LeadTiepNhanService leadTiepNhanService;
    private ScheduledExecutorService scheduler;
    private boolean dangChay = false;

    public LeadAssignmentBackgroundWorker() {
        this(new QuyTacPhanBoService(), new LeadTiepNhanService());
    }

    public LeadAssignmentBackgroundWorker(QuyTacPhanBoService quyTacPhanBoService) {
        this(quyTacPhanBoService, new LeadTiepNhanService());
    }

    public LeadAssignmentBackgroundWorker(QuyTacPhanBoService quyTacPhanBoService, LeadTiepNhanService leadTiepNhanService) {
        this.quyTacPhanBoService = quyTacPhanBoService;
        this.leadTiepNhanService = leadTiepNhanService != null ? leadTiepNhanService : new LeadTiepNhanService();
    }

    public static synchronized LeadAssignmentBackgroundWorker getInstance() {
        if (instance == null) {
            instance = new LeadAssignmentBackgroundWorker();
        }
        return instance;
    }

    /**
     * Khởi động tiến trình quét ngầm định kỳ.
     */
    public synchronized void khoiDong(int chuKyGiay) {
        if (dangChay) {
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LeadAssignmentWorker-Thread");
            t.setDaemon(true);
            return t;
        });

        int period = (chuKyGiay > 0) ? chuKyGiay : 30; // Mặc định 30 giây
        scheduler.scheduleAtFixedRate(this::thucThiQuetPhanBo, 5, period, TimeUnit.SECONDS);
        dangChay = true;
        LOGGER.info("LeadAssignmentBackgroundWorker đã khởi động (chu kỳ " + period + "s).");
    }

    /**
     * Dừng tiến trình ngầm an toàn khi ứng dụng shutdown.
     */
    public synchronized void dungLai() {
        if (!dangChay || scheduler == null) {
            return;
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        dangChay = false;
        LOGGER.info("LeadAssignmentBackgroundWorker đã dừng.");
    }

    /**
     * Kích hoạt quét và phân bổ ngay lập tức (phục vụ nút test hoặc demo của Giám đốc/Admin).
     */
    public KetQuaPhanBoDTO chayNgay(int batchSize) {
        return quyTacPhanBoService.chayTienTrinhPhanBoBatch(batchSize);
    }

    public boolean isDangChay() {
        return dangChay;
    }

    private void thucThiQuetPhanBo() {
        try {
            KetQuaPhanBoDTO kq = quyTacPhanBoService.chayTienTrinhPhanBoBatch(50);
            if (kq.getSoLeadDaQuet() > 0) {
                LOGGER.info(String.format("Worker phân bổ: Đã quét %d lead, thành công %d, hàng chờ %d (thời gian: %d ms)",
                        kq.getSoLeadDaQuet(), kq.getSoLeadPhanBoThanhCong(), kq.getSoLeadVaoHangCho(), kq.getThoiGianXuLyMs()));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi trong chu kỳ chạy ngầm của LeadAssignmentBackgroundWorker: " + e.getMessage(), e);
        }

        // Tự động kiểm tra và cập nhật quá hạn SLA định kỳ (Story S4-07 AC3, S4-09 AC2, BUG-07)
        try {
            int soQuaSla = leadTiepNhanService.kiemTraVaCapNhatQuaSla(null);
            if (soQuaSla > 0) {
                LOGGER.info("Worker SLA: Đã phát hiện và gắn cờ quá SLA cho " + soQuaSla + " lead.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi trong chu kỳ quét SLA của LeadAssignmentBackgroundWorker: " + e.getMessage(), e);
        }
    }
}
