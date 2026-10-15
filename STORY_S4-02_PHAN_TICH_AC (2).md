# STORY S4-02 — PHÂN TÍCH ACCEPTANCE CRITERIA

## 1. Thông tin Story và nguồn

- **Story ID:** S4-02
- **[BACKLOG GỐC] Sprint:** 4
- **[BACKLOG GỐC] Epic:** EP-04 — Lead & Phân bổ
- **[BACKLOG GỐC] Vai trò:** Nhân viên Marketing
- **[BACKLOG GỐC] User Story (nguyên văn):** “Là Nhân viên Marketing, tôi muốn tạo lead thủ công và nhập lead hàng loạt từ Excel, để đưa danh sách thu được từ hội thảo vào hệ thống ngay hôm sau.”
- **[BACKLOG GỐC] Acceptance Criteria (nguyên văn):**
  1. “Nhập tay một lead từ sự kiện hoặc danh thiếp”
  2. “Nhập hàng loạt có tệp mẫu, xem trước và báo lỗi theo từng dòng”
  3. “Mọi lead nhập vào đều bắt buộc có nguồn”
- **[BACKLOG GỐC] Story Points:** 5
- **[BACKLOG GỐC] Ưu tiên:** Must
- **[BACKLOG GỐC] Trạng thái:** Chưa bắt đầu
- **[BACKLOG GỐC] Epic description:** “Thu thập lead từ web, theo dõi theo chiến dịch, chấm điểm, phát hiện trùng, quy tắc phân bổ tự động, SLA phản hồi, chuyển đổi.”
- **Nguồn:** workbook `HỆ THỐNG QUẢN LÝ KHÁCH HÀNG.xlsx`, sheet `4. Product Backlog`, dòng chứa S4-02; sheet `3. Epics`, dòng EP-04; sheet `2. User Roles`, ma trận module.
- **[CHƯA XÁC MINH]** Repo chưa được cung cấp. Không có bằng chứng về URL, Servlet, DTO, tên bảng/cột, cơ chế import hoặc test hiện hữu. Tất cả đề xuất kỹ thuật trong tài liệu phải đối chiếu repo trước khi triển khai.

## 2. Vai trò và phạm vi quyền

**[BACKLOG GỐC]** Sheet `2. User Roles` định nghĩa 7 vai trò: Sales Rep, Team Lead, Director, Marketing, Cust. Success, Accountant, Admin. Quy ước: `F` = toàn quyền; `W` = ghi trong phạm vi được giao; `R` = chỉ xem; `–` = không truy cập. Chú thích `*`: chỉ trên dữ liệu người dùng sở hữu hoặc nhóm mình quản lý đối với Team Lead; giá vốn và biên lợi nhuận chỉ Director xem; Admin có toàn quyền mọi module. Lọc quyền dữ liệu phải được thực hiện ở truy vấn phía server, không chỉ ẩn giao diện.

**[BACKLOG GỐC]** Ở module `Lead & phân bổ`: Marketing = `F`; Sales Rep = `W*`; Team Lead = `F`; Director = `F`; Cust. Success và Accountant = `–`. Admin có toàn quyền theo chú thích chung của workbook (dù không có cột Admin trong ma trận module).

**[GIẢI THÍCH]** Story được viết cho Marketing. Điều đó không tự chứng minh rằng các vai trò khác không thể dùng chức năng tạo lead: ma trận module cho biết quyền ở cấp module, còn khả năng truy cập từng màn hình/thao tác cần được xác nhận với leader và kiểm tra implementation. `F` cũng không có nghĩa là bỏ qua validation hoặc kiểm soát dữ liệu.

## 3. Phân tích từng AC

### AC1 — Nhập tay một lead từ sự kiện hoặc danh thiếp

- **[BACKLOG GỐC]** Yêu cầu nguyên văn là “Nhập tay một lead từ sự kiện hoặc danh thiếp”.
- **[GIẢI THÍCH]** Người dùng Marketing mở màn hình tạo lead, điền thông tin lấy từ nguồn ngoại tuyến, gửi form và nhận kết quả rõ ràng. Tối thiểu lead phải có nguồn do AC3 yêu cầu. Workbook không liệt kê các trường bắt buộc khác, nên không tự coi tên, email, số điện thoại, công ty hay chiến dịch là required.
- **[ĐỀ XUẤT] Luồng FE:** trang danh sách lead → nút “Tạo lead” → form → validation UX cơ bản → POST form → khi thành công hiển thị thông báo và trang chi tiết/danh sách; khi lỗi giữ lại dữ liệu đã nhập và chỉ rõ trường/lỗi. Nguồn phải hiển thị rõ, không mặc định âm thầm.
- **[ĐỀ XUẤT] Luồng BE:** kiểm tra phiên đăng nhập; kiểm tra role và phạm vi; chuẩn hóa và validate dữ liệu ở server; xác nhận nguồn hợp lệ; kiểm tra quy tắc trùng nếu nghiệp vụ đã chốt; ghi lead bằng DAO/Service; commit rồi mới báo thành công. Không dùng validation FE làm ranh giới bảo mật.
- **[CHƯA XÁC MINH]** Danh sách trường, quy tắc trùng, nguồn là danh mục hay văn bản tự do, trạng thái mặc định, ownership/creator, audit, redirect sau lưu và trang đích chưa được xác định trong AC.
- **Lỗi/biên:** form trống; nguồn thiếu; giá trị quá dài; định dạng email/điện thoại không hợp lệ nếu các trường đó được chốt; nguồn không còn tồn tại; gửi hai lần; mất phiên; user không có quyền; lỗi DB. Chống tạo trùng khi double-click nên dùng cơ chế chống submit lặp phù hợp, nhưng không tự thêm quy tắc chống trùng nghiệp vụ chưa được duyệt.
- **Test gợi ý:** tạo lead hợp lệ với nguồn đã chọn; bỏ trống nguồn; nhập chuỗi biên; giả lập lỗi lưu; gửi lặp; thử bằng Marketing và bằng vai trò không có quyền module. Kiểm tra DB thật sau khi triển khai, không chỉ dựa vào toast UI.
- **IN SCOPE:** tạo thủ công một lead và phản hồi kết quả.
- **OUT OF SCOPE trừ khi được xác nhận:** chuyển đổi lead thành khách hàng/cơ hội, tự phân bổ, chấm điểm lead, chiến dịch marketing đầy đủ, gửi email, đồng bộ ngoài hệ thống.

### AC2 — Nhập hàng loạt có tệp mẫu, xem trước và báo lỗi theo từng dòng

- **[BACKLOG GỐC]** Yêu cầu nguyên văn: “Nhập hàng loạt có tệp mẫu, xem trước và báo lỗi theo từng dòng”.
- **[GIẢI THÍCH]** Người dùng lấy tệp mẫu, điền nhiều dòng, tải lên, xem các dòng được đọc và lỗi trước khi quyết định nhập. Lỗi phải gắn với số dòng và nguyên nhân đủ để sửa. Không được báo toàn bộ tệp thành công nếu có dòng lỗi mà thực tế chưa được lưu.
- **[ĐỀ XUẤT] Luồng FE:** liên kết tải mẫu → chọn tệp → hiển thị tên/loại/kích thước → tải lên → bảng preview có số dòng, dữ liệu được đọc và trạng thái validation → tổng kết số hợp lệ/lỗi → người dùng xác nhận nhập. Trạng thái loading, empty, upload error, validation error và success cần rõ ràng; bảng cuộn/phân trang nếu tệp lớn; không hiển thị dữ liệu nhạy cảm quá mức.
- **[ĐỀ XUẤT] Luồng BE:** kiểm tra đăng nhập/quyền trước xử lý; giới hạn loại tệp/kích thước/số dòng (cần chốt); đọc tệp an toàn; map header; validate từng dòng; trả preview không ghi dữ liệu; khi người dùng xác nhận, revalidate ở server rồi ghi các dòng hợp lệ theo chính sách import được duyệt. Không tin kết quả preview do browser gửi ngược lên. Làm rõ transaction: tất cả hoặc không dòng nào, hay cho phép lưu dòng hợp lệ và báo dòng thất bại.
- **[CHƯA XÁC MINH]** Định dạng Excel hỗ trợ `.xlsx` hay cả `.csv`; tên/header cột; kích thước và số dòng tối đa; chính sách dòng lỗi; cách xử lý trùng; import có bước xác nhận hay preview tự động ghi; tính nguyên tử; cách tải template và báo lỗi chưa có trong workbook.
- **Lỗi/biên:** tệp không chọn; tệp rỗng; định dạng sai; tệp hỏng; thiếu/thừa header; ô bắt buộc rỗng; dòng có nguồn không hợp lệ; dữ liệu vượt giới hạn; dòng trùng trong cùng tệp hoặc trùng DB; một dòng lỗi giữa nhiều dòng tốt; user rời trang; submit xác nhận hai lần; lỗi DB giữa chừng; nội dung tệp độc hại. Không thực thi macro; không tin tên file; cần giới hạn tài nguyên.
- **Test gợi ý:** template đúng; 0 dòng; 1 dòng hợp lệ; nhiều dòng hợp lệ; tệp có 1 dòng lỗi xen giữa; header sai; file không hỗ trợ; quá giới hạn; hai dòng trùng; lỗi persistence giữa chừng theo chính sách transaction. Đối chiếu từng số dòng, thông báo lỗi, số bản ghi thực sự được lưu và khả năng thử lại.
- **IN SCOPE:** template, upload/import hàng loạt, preview, lỗi theo dòng.
- **OUT OF SCOPE trừ khi được xác nhận:** import nền/asynchronous, tiếp tục sau khi ngắt kết nối, cập nhật lead có sẵn, undo import, tự động phân bổ/chấm điểm, lịch sử import/audit riêng.

### AC3 — Mọi lead nhập vào đều bắt buộc có nguồn

- **[BACKLOG GỐC]** Yêu cầu nguyên văn: “Mọi lead nhập vào đều bắt buộc có nguồn”.
- **[GIẢI THÍCH]** Quy tắc áp dụng cả tạo thủ công và import hàng loạt. Không được lưu lead không có nguồn; nguồn không hợp lệ cũng không được chấp nhận. Với import, dòng thiếu nguồn phải được đánh dấu lỗi riêng ở preview.
- **[ĐỀ XUẤT] Luồng FE:** đánh dấu trường nguồn bắt buộc; không gửi nếu thiếu để giúp người dùng; với Excel, chỉ rõ dòng và cột nguồn bị thiếu/sai. FE hỗ trợ trải nghiệm nhưng BE vẫn là nguồn quyết định cuối cùng.
- **[ĐỀ XUẤT] Luồng BE/DB:** kiểm tra nguồn sau khi chuẩn hóa; xác nhận giá trị thuộc tập nguồn hợp lệ nếu nguồn là danh mục; ràng buộc dữ liệu ở tầng lưu trữ phù hợp sau khi xác minh schema hiện có. Cần quyết định có cho phép nguồn “Khác” và cách nhập chi tiết.
- **[CHƯA XÁC MINH]** Nguồn là ID danh mục, mã enum hay chuỗi; danh mục nguồn do ai quản lý; có liên kết campaign không; giá trị chuẩn; quy tắc với dữ liệu lịch sử/import.
- **Lỗi/biên:** null, chuỗi rỗng, chỉ có khoảng trắng, ID nguồn không tồn tại, nguồn bị vô hiệu hóa, tên nguồn khác chữ hoa/thường, cột nguồn không có trong tệp.
- **Test gợi ý:** thử thiếu nguồn ở form và từng kiểu import; gửi request giả bỏ qua FE; nguồn hợp lệ; nguồn không tồn tại; nguồn chỉ có khoảng trắng. Xác nhận server từ chối và không có lead sai được lưu.
- **IN SCOPE:** bắt buộc nguồn cho tất cả lead được tạo qua hai luồng của Story.
- **OUT OF SCOPE trừ khi được xác nhận:** bắt buộc bổ sung nguồn cho mọi lead cũ trong hệ thống; quy trình quản trị danh mục nguồn.

## 4. Bảng mapping AC → quy tắc → FE → BE → dữ liệu → test

| AC | Diễn giải chính xác | Luồng FE | Luồng BE/DB dự kiến | Quyền/phạm vi | Test / bằng chứng đạt | Câu hỏi chưa chốt |
|---|---|---|---|---|---|---|
| AC1 | Nhập thủ công một lead từ sự kiện/danh thiếp | Form, validation hỗ trợ, giữ dữ liệu khi lỗi, thông báo thành công | Auth/RBAC/scope, server validation, xác thực nguồn, persistence | Marketing có `F` module Lead & phân bổ; quyền thao tác cụ thể phải đối chiếu | Tạo lead hợp lệ; lỗi nguồn; lỗi DB; gửi lặp; xác minh bản ghi thật | Trường bắt buộc; trùng; trạng thái mặc định; nguồn |
| AC2 | Template, upload hàng loạt, preview, lỗi theo dòng | Tải mẫu, chọn file, preview, lỗi theo dòng, xác nhận | Giới hạn tệp, parse an toàn, validate mỗi dòng, revalidate và ghi theo chính sách | Kiểm tra quyền trước upload và trước commit; không tin FE | File nhiều dòng có lỗi; đối chiếu từng dòng và DB; file sai/ quá giới hạn | Định dạng, header, giới hạn, partial/all-or-nothing, duplicate |
| AC3 | Lead nhập vào phải có nguồn | Required field và lỗi gắn dòng | Server-side required + kiểm tra nguồn hợp lệ; DB constraint nếu phù hợp | Áp dụng cho mọi luồng tạo/import, bất kể FE | Gửi request bỏ nguồn; import thiếu nguồn; xác minh không lưu | Mô hình nguồn; danh mục; “Khác”; nguồn inactive |

## 5. Luồng tổng thể đề xuất

1. Marketing mở trang lead và chọn tạo thủ công hoặc import Excel.
2. Server xác thực phiên, vai trò và quyền module.
3. Tạo thủ công: FE gửi form; server validate mọi trường và nguồn; Service lưu; server trả redirect/message hoặc response theo kiến trúc hiện có.
4. Import: tải template; upload file; server phân tích và trả preview + lỗi theo dòng, chưa ghi dữ liệu ở bước preview.
5. Người dùng xác nhận import; server kiểm tra lại quyền/dữ liệu và ghi theo chính sách transaction đã duyệt.
6. FE báo kết quả đúng với số bản ghi thực sự lưu; không suy diễn thành công từ mock hoặc số dòng hợp lệ ở preview.

**[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** Ưu tiên cơ chế Servlet/JSP/form submit nếu phù hợp kiến trúc hiện tại. Chỉ dùng fetch/AJAX/JSON nếu repo đã có pattern hoặc trải nghiệm preview cần thiết. Không chuyển framework.

## 6. Câu hỏi bắt buộc chốt với mentor/leader (tối đa 5)

1. **CẦN CHỐT — Các trường nào bắt buộc của lead ngoài `source`?** Phương án: chỉ bắt buộc source theo AC hiện tại, hoặc thêm name/contact/company. Thêm required giúp dữ liệu chất lượng hơn nhưng làm tăng tỷ lệ dòng import lỗi và là mở rộng AC nếu chưa có căn cứ.
2. **CẦN CHỐT — Source được biểu diễn thế nào?** Phương án: ID từ danh mục nguồn có sẵn, mã enum, hay chuỗi tự do. Danh mục giúp chuẩn hóa báo cáo nhưng cần xác minh màn quản trị và schema; chuỗi tự do dễ nhập nhưng dễ phân mảnh.
3. **CẦN CHỐT — Import xử lý khi một số dòng lỗi ra sao?** Phương án A: chỉ cho nhập khi tất cả dòng hợp lệ (atomic); B: nhập dòng hợp lệ và báo dòng lỗi. A dễ đảm bảo nhất quán nhưng buộc sửa toàn bộ; B tiện hơn nhưng cần kết quả partial rõ ràng và tránh ghi lặp khi retry.
4. **CẦN CHỐT — Định dạng/giới hạn file và quy tắc trùng?** Chốt `.xlsx`/`.csv`, dung lượng/số dòng tối đa, trùng trong file và với DB. Đây là điều kiện để contract, validation và test nhất quán.
5. **CẦN CHỐT — Có bước xác nhận sau preview không, và những role nào được dùng màn import?** Xác nhận giảm nguy cơ nhập nhầm; role khác Marketing có quyền module theo ma trận nhưng màn hình/thao tác cụ thể cần được thống nhất.

## 7. Decision Log và giả định

| ID | Nội dung | Trạng thái | Ảnh hưởng |
|---|---|---|---|
| D-01 | Story/AC/role/points/priority/status theo workbook | [BACKLOG GỐC] | Cơ sở nghiệp vụ |
| D-02 | Dùng Servlet/JSP/form nếu phù hợp repo; không ép REST | [ĐỀ XUẤT] | Quyết định kiến trúc sau đối chiếu source |
| D-03 | Preview import chưa ghi dữ liệu; ghi sau xác nhận | [ĐỀ XUẤT — CẦN CHỐT] | UX, endpoint/servlet, transaction |
| D-04 | Chính sách import all-or-nothing hay partial | [CHƯA XÁC NHẬN] | Persistence, response, retry, test |
| D-05 | Trường lead ngoài source, duplicate, giới hạn file | [CHƯA XÁC NHẬN] | Form, template, validation, DB |
| D-06 | Contract tài liệu này | **DRAFT — CHƯA APPROVED** | Cần FE + BE + leader duyệt |

Không có quyết định nào ở trạng thái `[ĐÃ ĐƯỢC NGƯỜI DÙNG CHỐT]` ngoài việc người dùng xác nhận Story ID `S4-02` trong hội thoại.

## 8. Checklist nghiệm thu thực tế

- [ ] Đối chiếu route, Servlet, JSP, Service, DAO, schema và pattern thông báo trong repo.
- [ ] Marketing đăng nhập và truy cập đúng màn hình; role không được phép bị chặn ở server.
- [ ] Tạo thủ công lead hợp lệ; nguồn luôn được lưu đúng.
- [ ] Server từ chối source null/rỗng/không hợp lệ dù request bỏ qua validation FE.
- [ ] Tệp mẫu tải được, header khớp hợp đồng, có hướng dẫn định dạng.
- [ ] Preview không ghi DB trước khi xác nhận (nếu quyết định D-03 được duyệt).
- [ ] Lỗi gắn đúng số dòng/cột; dữ liệu lỗi không bị báo nhầm là thành công.
- [ ] Thử file rỗng, file hỏng, sai header, dòng quá dài, nguồn thiếu, dòng trùng, file quá giới hạn.
- [ ] Kiểm tra double submit, lỗi DB giữa import và retry theo chính sách transaction đã chốt.
- [ ] Xác minh số bản ghi trong DB bằng dữ liệu test giả; kiểm tra quyền và Data Scope phía server.
- [ ] Chạy test tự động với cấu hình test hiện có (lưu ý workbook bối cảnh nói có thể dùng H2 cho một số test; không mặc định test H2 chứng minh MySQL production-like behavior).
- [ ] Đo thời gian xử lý với kích thước file đã thống nhất; không tuyên bố hiệu năng đạt nếu chưa đo.
- [ ] FE + BE + leader review contract; chỉ đổi sang APPROVED sau khi có người duyệt/ngày/decision.

## 9. Các điểm bắt buộc kiểm chứng trong source trước khi code

1. Java/Jakarta Servlet version, Maven structure, servlet mappings, JSP layout, session/auth filter và pattern CSRF nếu có.
2. Cách kiểm tra RBAC và Data Scope hiện hữu; nguồn sự thật của role người dùng trong session/database.
3. Lead entity/model, Service/DAO, schema/migration, tên cột thật, khóa ngoại và transaction pattern; không tạo schema mới trước khi đối chiếu.
4. Danh mục lead source đang tồn tại, cách xác thực và xử lý nguồn không hoạt động.
5. Các màn hình/form upload hiện hữu, thư viện đọc Excel đã có, giới hạn upload Tomcat/app, xử lý file an toàn.
6. Pattern thông báo lỗi/redirect/forward, encoding tiếng Việt, CSRF, logging/audit, validation và test fixture.
7. Quy ước đặt tên branch/API/field và test chạy với H2/MySQL; không tự sửa lịch sử sprint cũ.

**Kết luận trạng thái:** S4-02 được trích từ backlog; tài liệu phân tích là DRAFT. Backlog status “Chưa bắt đầu” không chứng minh mã nguồn chưa tồn tại, và không phải kết quả nghiệm thu.
