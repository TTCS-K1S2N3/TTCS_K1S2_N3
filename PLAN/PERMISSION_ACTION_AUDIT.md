# AUDIT MA TRẬN PHÂN QUYỀN ROUTE & ACTION ĐÃ TRIỂN KHAI (3 MODULE)

Tài liệu kiểm kê toàn diện tất cả các Servlet mapping, route, action parameter, phương thức HTTP, cấp quyền yêu cầu, Data Scope và Business Rule cho ba module đã triển khai:
1. `KHACH_HANG`
2. `DANH_MUC`
3. `NGUOI_DUNG_NHAT_KY`

---

## I. MODULE KHACH_HANG (Khách Hàng & Liên Hệ)

| Story | Module | Resource | Route thực tế | Method | action param | UI trigger | Cấp quyền | Data Scope | Business Rule | Gate backend cũ/mới | UI cũ/mới | Test ID |
| :--- | :--- | :--- | :--- | :---: | :--- | :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| S1-05, S3-01 | KHACH_HANG | Danh sách | `/khach-hang` | GET | `list` / rỗng | Menu "Khách hàng" | READ | CA_NHAN / NHOM / TOAN_BO | Lọc theo quyền sở hữu tài khoản | Đã có `PermissionService.coQuyen(READ)` | Đã ẩn nút Thêm/Sửa khi READ | TC-KH-01 |
| S3-03 | KHACH_HANG | Hồ sơ 360 | `/khach-hang/360`, `/khach-hang/chi-tiet` | GET | `360` / `chi-tiet` | Bấm tên khách hàng | READ | Record Scope | Phải thuộc phạm vi phụ trách | Đã kiểm tra Scope bản ghi | Mới vá: Ẩn controls ghi/sửa khi READ | TC-KH-02 |
| S3-03 | KHACH_HANG | API Hoạt động | `/khach-hang` | GET | `api-hoat-dong` | Tab Hoạt động 360 | READ | Record Scope | Trả về JSON hoạt động | Đã kiểm tra Scope bản ghi | Không ảnh hưởng | TC-KH-03 |
| S3-01 | KHACH_HANG | Thêm khách | `/khach-hang` | POST | `them` / `create` | Nút "Thêm khách hàng" | WRITE | Owner Scope | Phải gán NVKD trong phạm vi | Đã kiểm tra `WRITE` | Đã ẩn form khi READ | TC-KH-04 |
| S3-01 | KHACH_HANG | Sửa khách | `/khach-hang` | POST | `sua` | Modal "Sửa khách hàng" | WRITE | Record Scope | Thuộc phạm vi được sửa | Đã kiểm tra `WRITE` | Mới vá: Ẩn nút/modal sửa trong 360 khi READ | TC-KH-05 |
| Spike | KHACH_HANG | Xóa khách | `/khach-hang` | POST | `xoa` / `delete` | Nút "Xóa" trên bảng | FULL | Record Scope | Yêu cầu quyền FULL | Đã kiểm tra `FULL` | Đã ẩn nút Xóa khi thiếu FULL | TC-KH-06 |
| S3-04 | KHACH_HANG | Gộp trùng | `/khach-hang` | POST | `gop` | Nút "Gộp khách hàng" | WRITE | Record Scope | Chỉ Trưởng nhóm trở lên (`laTruongNhomTroLen`) | Đã kiểm tra `WRITE` + Trưởng nhóm | Đã có cảnh báo UI | TC-KH-07 |
| S3-05 | KHACH_HANG | Gắn công ty con | `/khach-hang` | POST | `gan-cong-ty-con` | Modal "Gắn công ty con" | WRITE | Record Scope | Cả mẹ và con thuộc scope | Đã kiểm tra `WRITE` | Mới vá: Ẩn nút gán con khi READ | TC-KH-08 |
| S3-05 | KHACH_HANG | Gỡ công ty con | `/khach-hang` | POST | `go-cong-ty-con` | Nút "Gỡ công ty con" | WRITE | Record Scope | Thuộc scope quản lý | Đã kiểm tra `WRITE` | Mới vá: Ẩn nút gỡ con khi READ | TC-KH-09 |
| S3-05 | KHACH_HANG | Đổi công ty mẹ | `/khach-hang` | POST | `cap-nhat-cong-ty-me` | Modal "Chọn công ty mẹ" | WRITE | Record Scope | Thuộc scope quản lý | Đã kiểm tra `WRITE` | Mới vá: Ẩn nút đổi mẹ khi READ | TC-KH-10 |
| S3-03 | KHACH_HANG | Ghi hoạt động | `/khach-hang` | POST | `them-hoat-dong` | Form timeline 360 | WRITE | Record Scope | Chỉ ghi vào khách thuộc quyền | Mới vá: Thêm gate `WRITE` trên KHACH_HANG | Mới vá: Ẩn timeline form khi READ | TC-KH-11 |
| S3-09 | KHACH_HANG | Đánh dấu CS | `/khach-hang` | POST | `danhDauLienHe` | Nút "Đánh dấu liên hệ" | WRITE | Record Scope | Chỉ khách thuộc phụ trách | Mới vá: Thêm gate `WRITE` trên KHACH_HANG | Mới vá: Ẩn nút đánh dấu khi READ | TC-KH-12 |
| S3-07 | KHACH_HANG | Lưu bộ lọc | `/khach-hang` | POST | `luu-bo-loc` | Nút "Lưu bộ lọc" | READ | CA_NHAN | Lưu cấu hình lọc cho user | Đã kiểm tra User Session | Giữ nguyên | TC-KH-13 |
| S3-07 | KHACH_HANG | Xóa bộ lọc | `/khach-hang` | POST | `xoa-bo-loc` | Nút xóa bộ lọc đã lưu | READ | CA_NHAN | Thuộc quyền sở hữu bộ lọc | Đã kiểm tra User Session | Giữ nguyên | TC-KH-14 |
| S3-01 | KHACH_HANG | Xuất Excel | `/khach-hang` | GET | `xuat-excel` | Nút "Xuất Excel" | READ | Current Scope | Chỉ xuất dữ liệu trong scope | Đã kiểm tra scope lọc | Giữ nguyên | TC-KH-15 |
| S3-06 | KHACH_HANG | Tải mẫu Excel | `/khach-hang/tai-tep-mau` | GET | `tai-mau` | Nút "Tải tệp mẫu" | READ | N/A | Tải file template Excel | Mới vá: Chặn khi module NONE | Giữ nguyên | TC-KH-16 |
| S3-06 | KHACH_HANG | Nhập Excel | `/khach-hang/import-excel` | GET+POST | `xem-truoc`, `nhap-du-lieu` | Nút "Nhập Excel" | WRITE | Owner Scope | Phải có quyền ghi để nạp dữ liệu | Mới vá: Gate `WRITE` ở cả GET và POST | Mới vá: Ẩn nút Nhập Excel khi READ | TC-KH-17 |
| S3-02 | KHACH_HANG | Danh sách NLH | `/nguoi-lien-he` | GET | `list`, `detail`, `lich-su` | Tab Người liên hệ 360 | READ | Record Scope | Khách hàng thuộc scope | Mới vá: Gate `READ` trên KHACH_HANG | Giữ nguyên | TC-KH-18 |
| S3-02 | KHACH_HANG | Thêm/Sửa NLH | `/nguoi-lien-he` | POST | `create`, `update`, `set-main` | Modal người liên hệ | WRITE | Record Scope | Khách hàng thuộc scope | Mới vá: Gate `WRITE` trên KHACH_HANG | Mới vá: Ẩn nút thêm/sửa NLH khi READ | TC-KH-19 |
| S3-02 | KHACH_HANG | Chuyển công ty | `/nguoi-lien-he` | POST | `transfer-company` | Modal chuyển công ty | WRITE | Record Scope | Cả 2 khách hàng thuộc scope | Mới vá: Gate `WRITE` trên KHACH_HANG | Mới vá: Ẩn nút chuyển công ty khi READ | TC-KH-20 |
| S3-02 | KHACH_HANG | Xóa NLH | `/nguoi-lien-he` | POST | `delete` | Nút xóa người liên hệ | FULL | Record Scope | Yêu cầu quyền FULL | Mới vá: Gate `FULL` trên KHACH_HANG | Mới vá: Ẩn nút xóa NLH khi thiếu FULL | TC-KH-21 |
| S3-08 | KHACH_HANG | Yêu cầu hỗ trợ | `/yeu-cau-ho-tro` | GET | `list`, `api-rui-ro` | Menu/Tab hỗ trợ | READ | CA_NHAN/TOAN_BO | Xem danh sách ticket | Mới vá: Gate `READ` trên KHACH_HANG | Giữ nguyên | TC-KH-22 |
| S3-08 | KHACH_HANG | Tạo/Sửa ticket | `/yeu-cau-ho-tro` | POST | `tao`, `cap-nhat`, `trang-thai` | Modal ghi nhận yêu cầu | WRITE | Record Scope | Khách hàng thuộc scope | Mới vá: Gate `WRITE` trên KHACH_HANG | Mới vá: Ẩn nút tạo ticket khi READ | TC-KH-23 |
| S3-08 | KHACH_HANG | Xóa ticket | `/yeu-cau-ho-tro` | POST | `xoa` | Nút xóa ticket | FULL | Record Scope | Yêu cầu quyền FULL | Mới vá: Gate `FULL` trên KHACH_HANG | Mới vá: Ẩn nút xóa ticket | TC-KH-24 |

---

## II. MODULE DANH_MUC (Danh Mục & Cấu Hình)

| Story | Module | Resource | Route thực tế | Method | action param | UI trigger | Cấp quyền | Data Scope | Business Rule | Gate backend cũ/mới | UI cũ/mới | Test ID |
| :--- | :--- | :--- | :--- | :---: | :--- | :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| S2-07 | DANH_MUC | Danh mục bán hàng | `/danh-muc-ban-hang`, `/danh-muc` | GET | `list` / rỗng | Menu "Danh mục" | READ | N/A | Dùng chung toàn cty | Đã có `PermissionService.coQuyen(READ)` | Cho phép xem bảng danh mục | TC-DM-01 |
| S2-07 | DANH_MUC | Thêm/Sửa danh mục | `/danh-muc-ban-hang` | POST | `them`, `sua` | Form thêm/sửa danh mục | WRITE | N/A | Chỉ Director / Admin sửa | Đã có `PermissionService.coQuyen(WRITE)` | Ẩn form sửa đối với vai trò READ | TC-DM-02 |
| S2-03 | DANH_MUC | Danh sách sản phẩm | `/san-pham` | GET | `list` / rỗng | Menu Sản phẩm / Bảng giá | READ | N/A | Giá vốn chỉ Director xem | Mới vá: NavigationFilter & Servlet check READ | Đã ẩn cột giá vốn khi không phải Director | TC-DM-03 |
| S2-03 | DANH_MUC | Thêm/Sửa sản phẩm | `/san-pham/tao`, `/san-pham/sua` | GET+POST | form submit | Nút "Thêm sản phẩm" | WRITE | N/A | Chỉ Director / Admin quản lý | Đã có `coQuyenQuanLy()` | Đã ẩn nút thêm/sửa khi không có quyền | TC-DM-04 |
| S2-03 | DANH_MUC | Xóa sản phẩm | `/san-pham/xoa` | POST | id | Nút "Xóa sản phẩm" | FULL | N/A | Chỉ Director / Admin xóa | Đã có `coQuyenQuanLy()` | Đã ẩn nút xóa | TC-DM-05 |
| S2-04 | DANH_MUC | Trường tùy chỉnh | `/truong-tuy-chinh`, `/truong-tuy-chinh/*` | GET+POST | `tao`, `sua`, `trang-thai` | Cấu hình trường động | WRITE/FULL | N/A | Chỉ Admin quản trị schema | Đã có `kiemTraQuyenQuanTri()` (Admin only) | Chỉ hiển thị cho Admin | TC-DM-06 |
| S2-05 | DANH_MUC | Pipeline cơ hội | `/giai-doan-pipeline`, `/pipeline/*` | GET+POST | `tao`, `sua`, `xoa`, `thu-tu` | Cấu hình pipeline bán hàng | READ / WRITE | N/A | Chỉ Director / Admin cấu hình | Đã có `coQuyenCauHinh()` | Ẩn nút cấu hình khi READ | TC-DM-07 |
| S2-06 | DANH_MUC | Lý do thắng-thua | `/ly-do-thang-thua`, `/ly-do-thang-thua/*` | GET+POST | `tao`, `sua`, `xoa` | Cấu hình lý do chốt deal | READ / WRITE | N/A | Chỉ Director / Admin quản lý | Đã có `kiemTraQuyenQuanLy()` | Ẩn nút quản lý khi READ | TC-DM-08 |
| S2-02 | DANH_MUC | Cơ cấu tổ chức | `/co-cau-to-chuc`, `/co-cau-to-chuc/*` | GET+POST | `them-nhom`, `sua-nhom` | Cây sơ đồ kinh doanh | READ / WRITE | N/A | Chỉ Director / Admin quản trị | Đã có role check Director/Admin | Ẩn nút sửa khi READ | TC-DM-09 |

---

## III. MODULE NGUOI_DUNG_NHAT_KY (Người Dùng & Nhật Ký)

| Story | Module | Resource | Route thực tế | Method | action param | UI trigger | Cấp quyền | Data Scope | Business Rule | Gate backend cũ/mới | UI cũ/mới | Test ID |
| :--- | :--- | :--- | :--- | :---: | :--- | :--- | :---: | :---: | :--- | :--- | :--- | :--- |
| S2-01 | NGUOI_DUNG_NHAT_KY | Danh sách người dùng | `/nguoi-dung` | GET | `list` / rỗng | Menu Người dùng & Nhật ký | READ | TOAN_BO | Director xem read-only; Admin quản lý | Đã có gate `READ` trên NGUOI_DUNG_NHAT_KY | Đã ẩn nút Thêm/Sửa/Khóa với Director | TC-ND-01 |
| S2-01 | NGUOI_DUNG_NHAT_KY | Tạo người dùng | `/nguoi-dung/tao` | GET+POST | form submit | Nút "Tạo tài khoản" | WRITE | TOAN_BO | Chỉ ADMIN được tạo tài khoản | Đã có role check Admin | Đã ẩn với Director | TC-ND-02 |
| S2-01 | NGUOI_DUNG_NHAT_KY | Sửa người dùng | `/nguoi-dung/sua` | GET+POST | form submit | Nút "Sửa" trên bảng | WRITE | TOAN_BO | Chỉ ADMIN được sửa tài khoản | Đã có role check Admin | Đã ẩn với Director | TC-ND-03 |
| S2-01 | NGUOI_DUNG_NHAT_KY | Khóa tài khoản | `/nguoi-dung/khoa-tai-khoan` | POST | `khoa`, `mo-khoa` | Nút "Khóa / Mở khóa" | WRITE | TOAN_BO | Chỉ ADMIN; không tự khóa chính mình | Đã có role check Admin + self check | Đã ẩn với Director | TC-ND-04 |
| S2-01 | NGUOI_DUNG_NHAT_KY | Nhập người dùng Excel | `/nguoi-dung/import`, `/nguoi-dung/tai-tep-mau` | GET+POST | `import`, `tai-mau` | Nút "Nhập Excel" | WRITE | TOAN_BO | Chỉ ADMIN được nhập tài khoản | Đã có role check Admin | Đã ẩn với Director | TC-ND-05 |
| S1-04 | NGUOI_DUNG_NHAT_KY | Gán vai trò/nhóm | `/nguoi-dung/phan-quyen` | GET+POST | `cap-nhat` | Nút "Phân quyền" | WRITE | TOAN_BO | Chỉ ADMIN; không tự tước quyền Admin | Đã có role check Admin | Đã ẩn với Director | TC-ND-06 |
| Spike | NGUOI_DUNG_NHAT_KY | Ma trận phân quyền | `/nguoi-dung/phan-quyen-vai-tro` | GET | `vaiTro` | Tab "Vai trò & Phân quyền" | READ | TOAN_BO | Director xem read-only; Admin cấu hình | Đã có gate Admin/Director | Director bị disable controls | TC-ND-07 |
| Spike | NGUOI_DUNG_NHAT_KY | Lưu ma trận quyền | `/nguoi-dung/phan-quyen-vai-tro` | POST | `maVaiTro`, `mucQuyen_*` | Nút "Lưu cấu hình phân quyền" | FULL | TOAN_BO | Chỉ ADMIN; không sửa Admin row; khóa trần Sheet 2 | Mới vá: Server validation khóa trần cứng Sheet 2 | Mới vá: Disable checkbox vượt trần | TC-ND-08 |
| S1-05 | NGUOI_DUNG_NHAT_KY | Phân quyền dữ liệu | `/phan-quyen-du-lieu`, `/chi-tiet-ban-ghi` | GET | `id`, `loai` | Tra cứu chi tiết bảo mật | READ | TOAN_BO | Admin hoặc Director tra cứu | Đã có gate Admin/Director | Giữ nguyên | TC-ND-09 |
| S1-07 | NGUOI_DUNG_NHAT_KY | Nhật ký thay đổi | `/nhat-ky-thay-doi`, `/nhat-ky-thay-doi/chi-tiet` | GET | `list`, `id` | Tab "Nhật ký kiểm toán" | READ | TOAN_BO | Admin hoặc Director xem audit trail | Đã có gate `READ` trên NGUOI_DUNG_NHAT_KY | Read-only | TC-ND-10 |

---

## IV. TÀI NGUYÊN CÔNG KHAI / CÁ NHÂN (KHÔNG BỊ CHẶN BỞI MODULE PERMISSION)

Các route sau đây thuộc về xác thực và hồ sơ cá nhân của người dùng, được bảo vệ bởi xác thực danh tính phiên (Session Authentication), **KHÔNG** bị phụ thuộc vào 10 module nghiệp vụ:
1. `/ho-so`, `/ho-so/cap-nhat`: Xem và cập nhật thông tin cá nhân.
2. `/doi-mat-khau`: Đổi mật khẩu tài khoản cá nhân.
3. `/avatar`, `/avatar/*`: Tải và hiển thị ảnh đại diện người dùng.
4. `/dang-xuat`, `/logout`, `/auth/logout`: Đăng xuất và hủy phiên làm việc.
5. `/dang-nhap`, `/login`: Trang xác thực đăng nhập.
6. `/quen-mat-khau`, `/dat-lai-mat-khau`: Luồng khôi phục mật khẩu.
7. `/api/phien/gia-han`, `/phien/keep-alive`: Duy trì phiên làm việc đang mở.
8. `/trang-chu`: Bộ định tuyến landing page động sau đăng nhập.
