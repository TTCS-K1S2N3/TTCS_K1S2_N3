# S2-05: Đặc Tả Backend - Danh Mục Sản Phẩm & Bảng Giá Niêm Yết

## 1. Mục tiêu Nghiệp vụ
- Cho phép phòng kinh doanh định nghĩa danh mục sản phẩm/dịch vụ của doanh nghiệp.
- Quản lý bảng giá niêm yết, đơn vị tính, thuế VAT, trạng thái kinh doanh phục vụ tính toán báo giá và cơ hội.

## 2. Mô hình Xử lý
- `SanPhamDAO`: Thực thi truy vấn danh mục sản phẩm, tìm kiếm theo mã sản phẩm (SKU) và nhóm hàng.
- `SanPhamService`: Xác thực ràng buộc giá bán không âm, kiểm tra điều kiện không cho xóa sản phẩm khi đã có báo giá/hợp đồng phát sinh.
