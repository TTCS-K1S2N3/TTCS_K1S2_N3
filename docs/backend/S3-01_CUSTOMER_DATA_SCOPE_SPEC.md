# S3-01: Đặc Tả Backend - Hồ Sơ Khách Hàng Doanh Nghiệp & Phân Quyền Data Scope

## 1. Nghiệp vụ Khách Hàng Doanh Nghiệp (B2B CRM)
- Quản lý thông tin tổ chức: Mã số thuế, tên pháp nhân, địa chỉ trụ sở, lĩnh vực hoạt động, quy mô nhân sự.
- Tích hợp lịch sử tương tác 360 độ (cuộc gọi, email, báo giá, hợp đồng).

## 2. Cơ chế Phân quyền Phạm vi Dữ liệu (Data Scope)
- 3 cấp độ truy cập nghiêm ngặt:
  1. `Cá nhân (Mine)`: Chỉ xem và thao tác trên khách hàng do mình trực tiếp phụ trách.
  2. `Nhóm (Team)`: Trưởng nhóm kinh doanh xem toàn bộ khách hàng của các thành viên trong nhóm.
  3. `Toàn công ty (All)`: Ban giám đốc và Admin xem và tổng hợp báo cáo toàn hệ thống.
- Kiểm soát bảo mật trực tiếp tại tầng SQL DAO (Query Filtering) để chống bypass qua API.
