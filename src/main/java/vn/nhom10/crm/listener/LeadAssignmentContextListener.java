package vn.nhom10.crm.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import vn.nhom10.crm.service.LeadAssignmentBackgroundWorker;

/**
 * Listener vòng đời ứng dụng web để tự động khởi động và dừng tiến trình phân bổ lead ngầm (Story S4-06, AC4).
 */
@WebListener
public class LeadAssignmentContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Khởi động tiến trình ngầm với chu kỳ quét mặc định 30 giây
        LeadAssignmentBackgroundWorker.getInstance().khoiDong(30);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Dừng an toàn tiến trình ngầm
        LeadAssignmentBackgroundWorker.getInstance().dungLai();
    }
}
