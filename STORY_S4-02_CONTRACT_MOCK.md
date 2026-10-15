# STORY S4-02 — CONTRACT FE–BE \& MOCK CHUNG

## 1\. Trạng thái và căn cứ

* **Story:** S4-02 — tạo lead thủ công và import hàng loạt từ Excel.
* **AC:** AC1 nhập tay lead; AC2 template + import hàng loạt + preview + lỗi từng dòng; AC3 mọi lead nhập vào bắt buộc có nguồn.
* **\[BACKLOG GỐC]** Marketing có `F` ở module `Lead \& phân bổ`; `W\*` Sales Rep; `F` Team Lead/Director; Cust. Success và Accountant `–`; Admin toàn quyền theo chú thích workbook. Phải thực thi auth/RBAC và phạm vi dữ liệu ở server.
* **\[CHƯA XÁC MINH]** Repo chưa cung cấp. Route, Servlet, JSP, request attributes, DTO, table, field, status code dưới đây đều là đề xuất contract, không phải tên đã được phát hiện trong source.
* **Trạng thái quyết định:** **DRAFT — CHƯA APPROVED**.

## 2\. Nguyên tắc hợp đồng

1. Ưu tiên Servlet/JSP và form submit theo pattern repo. Không bắt buộc REST/JSON.
2. Mọi input đều được validate phía server. FE validation chỉ để hỗ trợ UX.
3. `source` là bắt buộc ở cả create thủ công và mọi dòng import. Kiểu biểu diễn nguồn cần mentor/leader chốt trước khi đóng contract.
4. Preview không nên ghi dữ liệu; bước commit phải validate lại ở server. Đây là **đề xuất cần duyệt**, không phải sự thật từ repo.
5. Tất cả dữ liệu mẫu bên dưới là giả hoàn toàn. Mock không chứng minh persistence, quyền hoặc AC đã hoàn thành.
6. Không để thông tin exception/SQL/stack trace nội bộ lọt ra browser.

## 3\. Contract thao tác

### C-01 — Mở form tạo lead (AC1, AC3)

* **\[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** `GET /leads/create` hoặc servlet/JSP mapping tương đương đã có.
* **Auth:** yêu cầu phiên đăng nhập; server kiểm tra quyền module `Lead \& phân bổ`. Ma trận gốc cho Marketing là `F`. Nếu không đăng nhập, dùng flow đăng nhập sẵn có; nếu không có quyền, trả trang 403 hoặc thông báo theo pattern project.
* **Dữ liệu:** danh mục nguồn để render dropdown, nếu repo có danh mục nguồn. Không giả định bảng hoặc endpoint riêng.
* **JSP request attributes đề xuất:** `sources` (list option nguồn hợp lệ), `formData` (dữ liệu cũ nếu forward sau validation), `fieldErrors` (map field → thông báo), `errorMessage`/`successMessage` theo convention hiện hữu. Tên này chỉ là đề xuất.
* **Null/empty:** nếu chưa tải được danh mục nguồn, không được âm thầm cho phép submit nguồn rỗng; hiển thị lỗi có thể thao tác lại.

### C-02 — Lưu lead thủ công (AC1, AC3)

* **\[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** `POST /leads/create` hoặc form action tương đương.
* **Content type:** form-urlencoded hoặc multipart/form-data theo pattern hiện hữu; không cần multipart nếu không có file.
* **Input đề xuất:**

|Field minh họa|Kiểu|Required?|Ghi chú|
|-|-|-|-|
|`source`|string mã/ID nguồn|Có|Kiểu chính xác CHƯA CHỐT; phải thuộc nguồn hợp lệ|
|`name`|string|CHƯA CHỐT|Chỉ thêm nếu model/requrement hiện hữu xác nhận|
|`email`|string|CHƯA CHỐT|Validation chỉ áp dụng nếu field này được xác nhận|
|`phone`|string|CHƯA CHỐT|Không ép format khi chưa thống nhất|
|`company`|string|CHƯA CHỐT|Không tự khẳng định schema có field này|
|`notes`|string|CHƯA CHỐT|Chỉ nếu được scope cho phép|
|CSRF token|string|Theo repo|Dùng cơ chế hiện có nếu được cấu hình|

* **Success:** redirect/forward theo Post-Redirect-Get hoặc pattern hiện hữu; thông báo thành công chỉ sau khi transaction commit. Trang đích/ID trả về cần đối chiếu repo.
* **Validation error:** giữ lại dữ liệu an toàn đã nhập; trả field errors; không ghi bản ghi.
* **Forbidden:** server từ chối kể cả khi người dùng tự gửi POST bằng tay.
* **Duplicate:** chưa có quy tắc nghiệp vụ được chốt; không tự trả 409 hoặc tự gộp lead nếu project không có quy tắc đó.

### C-03 — Tải file mẫu (AC2)

* **\[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** liên kết GET tới template tĩnh hoặc servlet tải file, tùy cấu trúc hiện hữu.
* **Định dạng:** CHƯA CHỐT; cần thống nhất `.xlsx` (và có/không `.csv`).
* **Contract file:** header và ví dụ phải khớp chính xác field đã chốt; có cột source bắt buộc; hướng dẫn định dạng/giới hạn ở sheet README hoặc tài liệu đi kèm nếu phù hợp.
* **Quyền:** kiểm tra quyền trước khi cấp template nếu template chỉ dành cho role có quyền import.

### C-04 — Upload và preview import (AC2, AC3)

* **\[ĐỀ XUẤT — ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO]** `POST /leads/import/preview` dạng multipart hoặc mapping servlet tương đương. Có thể dùng fetch/AJAX nếu repo đang có pattern đó; không bắt buộc JSON.
* **Input:** file; CSRF token nếu dùng; không nhận danh sách bản ghi đã validate từ client làm nguồn đáng tin cậy.
* **Giới hạn:** `MAX\_FILE\_BYTES`, `MAX\_ROWS`, định dạng cho phép là các cấu hình cần chốt; hiện chưa có số liệu gốc, không tự điền giới hạn giả thành chính sách.
* **Preview response DTO đề xuất:** `importToken` (opaque, ngắn hạn, gắn với user/session nếu cần), `totalRows` integer, `validRows` integer, `errorRows` integer, `rows` list gồm `rowNumber` integer, `status` enum `VALID|INVALID`, `fieldErrors` list, `previewValues` chỉ gồm trường cần hiển thị. Tên field/enum đều là đề xuất.
* **Nếu JSP render:** request attributes tương đương `preview`, `totalRows`, `validRows`, `errorRows`, `fileErrors`. Không render HTML do người dùng cung cấp như markup.
* **Lỗi:** file không chọn/không hợp lệ/hỏng → lỗi file; thiếu hoặc sai header → lỗi cấu trúc; từng dòng sai → lỗi gắn `rowNumber` và field; vượt giới hạn → từ chối an toàn, không parse toàn bộ file quá mức.
* **Persistence:** preview không ghi lead theo đề xuất. `importToken` nếu dùng phải có hạn dùng, ràng buộc với user và không thể tái sử dụng tùy tiện.

### C-05 — Xác nhận import và lưu (AC2, AC3)

* **\[ĐỀ XUẤT — CẦN CHỐT]** `POST /leads/import/commit` hoặc form xác nhận tương đương.
* **Input:** token tham chiếu preview (nếu dùng), lựa chọn người dùng và CSRF token. Không tin `validRows` hoặc nội dung preview do client gửi; server tải lại/đọc lại dữ liệu an toàn và validate lần cuối.
* **Transaction decision:** CHƯA CHỐT. Phương án A: all-or-nothing; phương án B: lưu dòng hợp lệ, trả kết quả partial với lỗi từng dòng. Cần leader quyết định trước khi mock thành contract cuối.
* **Response đề xuất:** `importedRows`, `failedRows`, `rowResults\[]`, `status` (`SUCCESS|PARTIAL|FAILED`) chỉ sau khi được duyệt. Không dùng `SUCCESS` khi chưa có persistence thật.
* **Double submit/retry:** cần cơ chế chống commit token lặp hoặc idempotency phù hợp. Cách triển khai phải theo pattern repo và chính sách transaction được chốt.

## 4\. Auth, RBAC, Data Scope và lỗi

* Mỗi request phải xác thực phiên và kiểm tra quyền phía server; ẩn nút ở FE không đủ.
* Theo ma trận gốc, Marketing `F` ở Lead \& phân bổ. Với các role khác, áp dụng quyền module trong workbook và kiểm tra chính sách thao tác cụ thể; không suy diễn quyền import chỉ từ giao diện.
* Data Scope: workbook ghi rõ các quyền có dấu `\*` giới hạn dữ liệu sở hữu/nhóm. Marketing có `F` trong module nhưng scope của lead do Marketing tạo và khả năng xem/sửa của role khác cần đối chiếu rule hiện hữu. Không tự nới phạm vi dữ liệu.
* Nếu project dùng JSP redirect/forward, ánh xạ lỗi vào flow sẵn có; các mã HTTP dưới đây chỉ áp dụng nếu endpoint HTTP/JSON thật được dùng.

|Tình huống|HTTP nếu dùng endpoint|Hành vi cần có|
|-|-:|-|
|Phiên hết hạn/chưa đăng nhập|401 hoặc redirect login|Không xử lý/ghi dữ liệu|
|Không đủ quyền|403 hoặc trang forbidden|Không xử lý/ghi dữ liệu|
|Lead/nguồn tham chiếu không tồn tại|404 hoặc validation error phù hợp|Không tiết lộ dữ liệu ngoài scope|
|Dữ liệu không hợp lệ|400 hoặc lỗi form|Field errors rõ ràng; không ghi|
|Trùng theo quy tắc đã duyệt|409 hoặc lỗi dòng|Chỉ dùng khi có quy tắc duplicate chính thức|
|Lỗi hệ thống/DB|500 hoặc trang lỗi chung|Rollback theo transaction; log nội bộ, không lộ stack trace|
|File vượt giới hạn|413 hoặc thông báo upload|Không parse/lưu một phần ngoài chính sách|

## 5\. Mock data dùng chung

**Nguồn:** dữ liệu giả được tạo chỉ để thống nhất FE–BE. `L-TEST-001`, tên người, email, công ty, source đều là placeholder; không phải dữ liệu thật, không phản ánh schema DB thực tế. Các field `name/email/company` chỉ dùng sau khi team xác nhận chúng thuộc model.

### Mock A — Tạo thủ công thành công (AC1/AC3)

```json
{
  "mock": true,
  "status": "SUCCESS",
  "message": "Tạo lead thành công (dữ liệu minh họa).",
  "lead": {
    "id": "L-TEST-001",
    "name": "Nguyễn An Demo",
    "email": "an.demo@example.invalid",
    "company": "Công ty Mẫu Alpha",
    "source": "EVENT",
    "createdAt": "2026-10-14T09:00:00+07:00"
  }
}
```

### Mock B — Import preview rỗng (AC2)

```json
{
  "mock": true,
  "status": "EMPTY",
  "totalRows": 0,
  "validRows": 0,
  "errorRows": 0,
  "rows": \[],
  "message": "Tệp không có dòng dữ liệu để xem trước."
}
```

### Mock C — Validation error, nguồn bị thiếu (AC3)

```json
{
  "mock": true,
  "status": "VALIDATION\_ERROR",
  "fieldErrors": {
    "source": "Nguồn lead là bắt buộc."
  },
  "message": "Vui lòng chọn nguồn lead.",
  "persisted": false
}
```

### Mock D — Forbidden (RBAC)

```json
{
  "mock": true,
  "status": "FORBIDDEN",
  "message": "Bạn không có quyền thực hiện thao tác này."
}
```

### Mock E — Preview có dòng hợp lệ và lỗi theo dòng (AC2/AC3)

```json
{
  "mock": true,
  "status": "PREVIEW\_READY",
  "totalRows": 3,
  "validRows": 2,
  "errorRows": 1,
  "rows": \[
    {
      "rowNumber": 2,
      "status": "VALID",
      "previewValues": {
        "name": "Nguyễn An Demo",
        "email": "an.demo@example.invalid",
        "source": "EVENT"
      },
      "fieldErrors": \[]
    },
    {
      "rowNumber": 3,
      "status": "INVALID",
      "previewValues": {
        "name": "Trần Bình Demo",
        "email": "binh.demo@example.invalid",
        "source": ""
      },
      "fieldErrors": \[
        {
          "field": "source",
          "message": "Nguồn lead là bắt buộc."
        }
      ]
    },
    {
      "rowNumber": 4,
      "status": "VALID",
      "previewValues": {
        "name": "Lê Chi Demo",
        "email": "chi.demo@example.invalid",
        "source": "CONFERENCE"
      },
      "fieldErrors": \[]
    }
  ],
  "persistenceOccurred": false
}
```

### Mock F — Boundary: file không đúng định dạng hoặc vượt giới hạn

```json
{
  "mock": true,
  "status": "FILE\_REJECTED",
  "code": "UNSUPPORTED\_FILE",
  "message": "Không thể đọc tệp. Hãy dùng định dạng được hỗ trợ và thử lại.",
  "persistenceOccurred": false
}
```

### Mock G — Commit partial (CHỈ minh họa nếu chọn phương án partial)

```json
{
  "mock": true,
  "status": "PARTIAL",
  "importedRows": 2,
  "failedRows": 1,
  "rowResults": \[
    {"rowNumber": 2, "status": "IMPORTED", "leadId": "L-TEST-001"},
    {"rowNumber": 3, "status": "FAILED", "errors": \["Nguồn lead là bắt buộc."]},
    {"rowNumber": 4, "status": "IMPORTED", "leadId": "L-TEST-002"}
  ]
}
```

**Cảnh báo:** Mock G mâu thuẫn với chính sách all-or-nothing; chỉ sử dụng nếu leader chọn partial. Nếu chọn all-or-nothing, phải thay bằng phản hồi FAILED/không bản ghi nào được lưu. Không trộn hai semantics trong cùng contract.

## 6\. Quy tắc dữ liệu và format đề xuất

* Chuỗi: trim khoảng trắng đầu/cuối; không tự đổi nội dung người dùng ngoài chuẩn hóa đã thống nhất.
* `source`: bắt buộc, kiểu biểu diễn CHƯA CHỐT. Không nhận chuỗi trắng; kiểm tra thuộc danh mục/mã hợp lệ theo mô hình thật.
* Ngày giờ ví dụ dùng ISO 8601 có offset, nhưng phải theo convention project; không giả định DB lưu timezone như vậy.
* Số dòng và số lượng dùng integer không âm. Không hiển thị số dòng hợp lệ như số dòng đã lưu.
* Enum `VALID`, `INVALID`, `SUCCESS`, `PARTIAL`, `FAILED`, `EMPTY`, `FORBIDDEN` là giá trị minh họa đề xuất, chưa phải enum trong source.
* Tên field JSON/JSP attribute phải thống nhất giữa FE và BE, dùng đúng một kiểu viết sau khi đối chiếu convention hiện hữu.
* Lỗi field nên có cấu trúc ổn định: `field`, `message`; lỗi import có thêm `rowNumber`. Không trả stack trace hoặc SQL.

## 7\. Kế hoạch thay mock bằng backend thật

1. Antigravity/nhóm đọc repo, tìm luồng Lead hiện có, mapping Servlet/JSP, Service/DAO/model/schema, session/RBAC và pattern upload.
2. So sánh field đề xuất với entity và template thật; loại bỏ field không tồn tại, thêm field chỉ khi có căn cứ nghiệp vụ được duyệt.
3. FE xây form/preview theo contract DRAFT, dùng fixture riêng có cờ mock; không giả vờ persistence thành công ngoài demo.
4. BE triển khai validation server-side, auth/scope, parser an toàn, transaction theo quyết định đã chốt và thông báo lỗi theo từng dòng.
5. Thay adapter mock bằng dữ liệu từ request attributes hoặc endpoint thật. Không để mock chạy trong luồng production.
6. Kiểm tra Network tab (nếu AJAX) hoặc request/form response và server logs (không lộ secrets); so sánh tên field, kiểu, nullable, trạng thái và lỗi với contract.
7. Viết contract/integration tests: create success; source null/blank/invalid; forbidden; preview file hợp lệ; sai header; dòng lỗi; file rỗng/quá giới hạn; duplicate/double submit; rollback/partial theo quyết định.
8. Xác minh persistence trong DB phù hợp môi trường. Nếu test dùng H2, chạy thêm kiểm tra phù hợp trên MySQL trước khi tuyên bố tương thích.
9. Chỉ cập nhật trạng thái AC sau khi có bằng chứng test/kiểm tra thực tế và người phụ trách xác nhận.

## 8\. Contract test matrix

|ID|Input/điều kiện|Expected|Bằng chứng|
|-|-|-|-|
|CT-01|Marketing, form hợp lệ, source hợp lệ|Lưu đúng 1 lead|DB/query kiểm tra + response|
|CT-02|Source null/rỗng|Validation error, không lưu|Test server-side bỏ qua FE|
|CT-03|Source không thuộc danh mục|Từ chối, không lưu|Kiểm tra response và DB|
|CT-04|User chưa đăng nhập|Redirect/401 theo pattern|Request thực tế|
|CT-05|Role không có quyền|403/forbidden, không lưu|Thử POST trực tiếp|
|CT-06|File đúng định dạng, nhiều dòng hợp lệ|Preview đúng số dòng; chưa lưu|So sánh nội dung preview và DB|
|CT-07|Một dòng thiếu source|Đúng rowNumber/field error|Fixture có dòng lỗi xen kẽ|
|CT-08|File rỗng/sai header/hỏng|Thông báo cụ thể, không lưu|Test file fixture|
|CT-09|File vượt giới hạn|Từ chối an toàn|Đo byte/row và xác nhận không lưu|
|CT-10|Commit lặp/lỗi DB giữa chừng|Không nhân đôi; rollback/partial đúng quyết định|Test transaction + DB|
|CT-11|Dữ liệu trùng trong file/DB|Hành vi theo duplicate policy đã chốt|Test boundary|
|CT-12|File kích thước tối đa đã chốt|Đo thời gian/bộ nhớ và xác nhận UX|Báo cáo đo thực tế, không phán đoán|

## 9\. Danh sách kỹ thuật CHỈ LÀ ĐỀ XUẤT — PHẢI ĐỐI CHIẾU REPO

|Thành phần|Tên gợi ý chưa xác minh|Việc phải kiểm tra|
|-|-|-|
|URL/form action|`/leads/create`, `/leads/import/preview`, `/leads/import/commit`|Servlet mapping và convention hiện hữu|
|JSP|`lead-create.jsp`, `lead-import-preview.jsp`|Vị trí/template thực tế|
|Request attributes|`sources`, `formData`, `fieldErrors`, `preview`, `totalRows`|Naming convention và model thực tế|
|DTO|`LeadCreateRequest`, `ImportPreviewResult`, `RowValidationError`|Có DTO tương tự chưa; không thêm trùng|
|Service|`LeadService`, `LeadImportService`|Service hiện hữu, transaction boundary|
|DAO/table/columns|**CHƯA BIẾT**|Đọc schema/entity/migration trước khi đề xuất thay đổi|
|Source model|`source` / `sourceId`|Danh mục/enum/chuỗi thực tế|
|Test|`LeadImport...Test`|Framework test, H2 config, fixture hiện hữu|

## 10\. Ô quyết định / approval

* **Contract status:** DRAFT — CHƯA APPROVED
* **FE reviewer:** CHƯA BIẾT
* **BE reviewer:** CHƯA BIẾT
* **Leader/mentor approver:** CHƯA BIẾT
* **Ngày duyệt:** CHƯA BIẾT
* **Quyết định cần ghi trước khi APPROVED:**

  1. Trường lead bắt buộc ngoài source.
  2. Kiểu source và danh mục hợp lệ.
  3. Định dạng file, template/header, giới hạn byte/dòng.
  4. Chính sách duplicate.
  5. Import all-or-nothing hay partial; có bước xác nhận; role được import.
* **Decision log:** CHƯA CÓ QUYẾT ĐỊNH ĐƯỢC DUYỆT. Sau review, ghi người quyết định, ngày, lựa chọn và ảnh hưởng contract/test tại đây.

**Không dùng contract này như bằng chứng đã code/đã nghiệm thu.** Chỉ dùng làm bản nháp để FE, BE và leader thống nhất rồi mới đưa vào Prompt 02 cho Antigravity.

