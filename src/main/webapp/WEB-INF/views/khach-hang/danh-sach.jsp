<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Danh mục khách hàng - Hệ thống CRM Bán Hàng">
    <title>Danh Mục Khách Hàng - CRM Bán Hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/khach-hang/khach-hang.css">
</head>
<body>
    <!-- Top Navigation Bar -->
    <header class="navbar">
        <a href="${pageContext.request.contextPath}/khach-hang" class="nav-brand">
            <div class="nav-brand-logo">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
            </div>
            <span>CRM Doanh Nghiệp</span>
        </a>

        <div class="user-nav">
            <div class="user-badge">
                <div class="avatar" title="<c:out value="${nguoiDungHienTai.hoTen}"/>">
                    <c:out value="${nguoiDungHienTai.tenVietTat}"/>
                </div>
                <div class="user-info">
                    <div class="user-name"><c:out value="${nguoiDungHienTai.hoTen}"/></div>
                    <div class="user-role-label"><c:out value="${nguoiDungHienTai.email}"/></div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/dang-xuat" class="btn-logout" title="Đăng xuất khỏi hệ thống">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>Đăng Xuất</span>
            </a>
        </div>
    </header>

    <!-- Main Content Area -->
    <main class="main-container">
        <!-- Welcome Hero Section -->
        <section class="welcome-hero">
            <div class="welcome-text">
                <h2>Xin chào, <c:out value="${nguoiDungHienTai.hoTen}"/>! 👋</h2>
                <p>Bạn đã đăng nhập thành công vào Hệ Thống Quản Lý Khách Hàng CRM với vai trò được phân quyền bảo mật cấp hệ thống.</p>
                <div class="role-tags">
                    <c:forEach items="${nguoiDungHienTai.danhSachVaiTro}" var="vt">
                        <span class="role-tag">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                                <circle cx="12" cy="12" r="10"></circle>
                                <polyline points="12 6 12 12 16 14"></polyline>
                            </svg>
                            <c:out value="${vt.tenVaiTro}"/>
                        </span>
                    </c:forEach>
                </div>
            </div>
            <div class="session-security-pill">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                </svg>
                <span>Phiên đăng nhập an toàn</span>
            </div>
        </section>

        <!-- KPI Summary Cards -->
        <section class="kpi-grid">
            <div class="kpi-card">
                <div>
                    <div class="kpi-info-title">Tổng Khách Hàng</div>
                    <div class="kpi-info-val">24</div>
                </div>
                <div class="kpi-icon blue">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </svg>
                </div>
            </div>

            <div class="kpi-card">
                <div>
                    <div class="kpi-info-title">Đang Chăm Sóc</div>
                    <div class="kpi-info-val">16</div>
                </div>
                <div class="kpi-icon green">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                        <polyline points="22 4 12 14.01 9 11.01"></polyline>
                    </svg>
                </div>
            </div>

            <div class="kpi-card">
                <div>
                    <div class="kpi-info-title">Khách Tiềm Năng</div>
                    <div class="kpi-info-val">8</div>
                </div>
                <div class="kpi-icon amber">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                    </svg>
                </div>
            </div>
        </section>

        <!-- Customer Data Table Section -->
        <section class="content-card">
            <div class="content-header">
                <div class="content-title-area">
                    <h3>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                            <line x1="3" y1="9" x2="21" y2="9"></line>
                            <line x1="9" y1="21" x2="9" y2="9"></line>
                        </svg>
                        Danh Mục Khách Hàng Được Phân Quyền
                    </h3>
                    <p>Dữ liệu khách hàng hiển thị dựa trên phạm vi phân quyền và vai trò công tác của bạn</p>
                </div>
            </div>

            <div class="table-responsive">
                <table class="customer-table">
                    <thead>
                        <tr>
                            <th>Mã Khách Hàng</th>
                            <th>Tên Doanh Nghiệp</th>
                            <th>Lĩnh Vực</th>
                            <th>Người Phụ Trách</th>
                            <th>Trạng Thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><span class="customer-code">KH-001</span></td>
                            <td><strong>Tập đoàn Công nghệ Alpha Tech</strong></td>
                            <td>Phần mềm & Viễn thông</td>
                            <td><c:out value="${nguoiDungHienTai.hoTen}"/></td>
                            <td>
                                <span class="badge-status active">
                                    <span class="badge-status-dot"></span>
                                    Đang chăm sóc
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td><span class="customer-code">KH-002</span></td>
                            <td><strong>Công ty Cổ phần Giải pháp Số Beta</strong></td>
                            <td>Thương mại điện tử & Bán lẻ</td>
                            <td><c:out value="${nguoiDungHienTai.hoTen}"/></td>
                            <td>
                                <span class="badge-status potential">
                                    <span class="badge-status-dot"></span>
                                    Tiềm năng
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td><span class="customer-code">KH-003</span></td>
                            <td><strong>Công ty TNHH Đầu tư & Phát triển Gamma</strong></td>
                            <td>Sản xuất & Vận tải Logistics</td>
                            <td><c:out value="${nguoiDungHienTai.hoTen}"/></td>
                            <td>
                                <span class="badge-status active">
                                    <span class="badge-status-dot"></span>
                                    Đang chăm sóc
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
