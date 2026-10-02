# DATABASE RULES - CRM

File này quy định cách thiết kế và thay đổi database dùng chung của dự án.

Mọi thành viên Backend và AI phải đọc trước khi thay đổi schema hoặc viết DAO.

## 1. Database chính

Dự án sử dụng MySQL.

Không tự ý chuyển sang database khác.

## 2. Quy tắc đặt tên

Table và column dùng snake_case.

Ví dụ:

- nguoi_dung
- vai_tro
- nguoi_dung_vai_tro
- nhom_kinh_doanh
- khach_hang
- nguoi_lien_he
- co_hoi
- bao_gia
- hop_dong

Foreign key nên thể hiện rõ đối tượng tham chiếu.

Ví dụ:

- khach_hang_id
- nguoi_dung_id
- giai_doan_id
- nguoi_phu_trach_id

## 3. Không tạo dữ liệu trùng nghĩa

Trước khi tạo table hoặc column mới phải kiểm tra:

1. Schema hiện tại.
2. SQL/migration hiện tại.
3. Model liên quan.
4. DAO liên quan.
5. Service liên quan.
6. Module khác đang dùng dữ liệu tương tự hay không.

Nếu đã tồn tại:

nguoi_phu_trach_id

thì không tự tạo thêm:

- owner_id
- sales_id
- assigned_user_id

nếu chúng cùng một ý nghĩa.

## 4. Không tự đổi schema dùng chung

Không tự ý:

- Đổi tên table đã dùng.
- Đổi tên column đã dùng.
- Xóa column đang được module khác sử dụng.
- Thay đổi kiểu dữ liệu đã dùng chung.
- Đổi foreign key.
- Đổi enum/status dùng chung.

Nếu Story bắt buộc phải thay đổi schema chung phải ghi rõ ảnh hưởng.

## 5. Migration / SQL

Không sửa ngược migration đã được branch hoặc môi trường khác sử dụng.

Thay đổi schema mới phải được ghi bằng SQL/migration mới.

Không chỉnh database thủ công rồi bỏ qua script.

## 6. Transaction

Nghiệp vụ nhiều bước phải sử dụng transaction.

Ví dụ chuyển Lead:

Lead
→ KhachHang
→ NguoiLienHe
→ CoHoi

Nếu một bước lỗi:

ROLLBACK toàn bộ transaction.

Không để dữ liệu ở trạng thái dở dang.

Các nghiệp vụ quan trọng khác cần xem xét transaction:

- Duyệt báo giá.
- Chuyển báo giá thành hợp đồng.
- Bàn giao dữ liệu khi khóa user.
- Thay đổi ownership nhiều bản ghi.

## 7. Kiểu dữ liệu

Tiền tệ phải sử dụng DECIMAL.

Không sử dụng:

- float
- double

cho tiền.

Ngày giờ toàn hệ thống phải sử dụng một quy ước thống nhất.

Dự án ưu tiên:

Asia/Ho_Chi_Minh

Không để mỗi module xử lý timezone theo một cách khác nhau.

## 8. Foreign Key và Index

Mọi foreign key phải có quan hệ rõ ràng.

Các foreign key quan trọng cần có index phù hợp.

Các trường thường xuyên dùng để lọc hoặc sắp xếp cần xem xét index:

- owner_id
- team_id
- stage_id
- status
- created_at
- expected_close_date

Không tạo index tùy tiện nếu không có lý do truy vấn.

## 9. SQL

DAO sử dụng PreparedStatement.

Không nối trực tiếp input người dùng vào SQL.

Không viết SQL trong:

- JSP
- Servlet
- JavaScript frontend

SQL thuộc tầng DAO hoặc repository tương ứng.

## 10. Quyền truy cập dữ liệu

Query phải tôn trọng Data Scope:

- CA_NHAN
- NHOM
- TOAN_BO

Không chỉ lấy dữ liệu rồi ẩn trên giao diện.

Dữ liệu người dùng không có quyền truy cập phải bị lọc hoặc từ chối phía backend.

## 11. Schema Owner

Schema database là tài nguyên dùng chung của toàn nhóm.

Thay đổi schema chung phải được review trước khi merge vào dev.

Không để nhiều branch tự tạo các biến thể khác nhau của cùng một bảng hoặc field.