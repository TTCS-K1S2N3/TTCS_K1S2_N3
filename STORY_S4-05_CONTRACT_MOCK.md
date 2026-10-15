# STORY S4-05 --- CONTRACT FE--BE VÀ MOCK DATA CHUNG

## 1. Thông tin và trạng thái

-   **Story:** S4-05
-   **Epic:** EP-04 --- Lead & Phân bổ
-   **Sprint:** 4
-   **Vai trò:** Giám đốc kinh doanh
-   **Ưu tiên / Point / trạng thái backlog:** Must / 8 / Chưa bắt đầu
-   **AC:** AC1 khai báo tiêu chí/điểm; AC2 tự tính lại khi lead đổi;
    AC3 phân loại Nóng/Ấm/Lạnh theo ngưỡng; AC4 không tự loại lead.
-   **Nguồn nghiệp vụ:** `4. Product Backlog`; phân quyền đối chiếu
    `2. User Roles`.
-   **Repo chưa được cung cấp.** Tất cả route, field, DTO, service,
    table và status bên dưới là **ĐỀ XUẤT --- ANTIGRAVITY PHẢI ĐỐI CHIẾU
    REPO**.

## 2. Nguyên tắc hợp đồng

1.  Dùng Servlet/JSP/form submit nếu phù hợp với cấu trúc hiện có; không
    ép dự án chuyển sang REST.
2.  BE là nguồn quyết định cuối cùng cho quyền, validation, điểm và
    nhãn.
3.  Không nhận role hoặc Data Scope từ dữ liệu do browser tự gửi để
    quyết định quyền.
4.  Điểm thấp không được tự xóa, loại hoặc đổi trạng thái lead.
5.  Mock chỉ là dữ liệu giả cho FE/contract discussion; không chứng minh
    DB persistence hay AC đã hoàn thành.
6.  Không thêm field/table vào schema thật trước khi kiểm tra repo và
    được nhóm duyệt.
7.  Các quyết định chưa có trong backlog phải được chốt trước khi chuyển
    DRAFT thành APPROVED.

## 3. Các quyết định còn chờ

-   **\[CẦN CHỐT\]** Điểm gắn với từng giá trị hay từng nhóm tiêu chí.
-   **\[CẦN CHỐT\]** Thang điểm, điểm null/thiếu dữ liệu và giới hạn tối
    đa.
-   **\[CẦN CHỐT\]** Ngưỡng Nóng/Ấm/Lạnh và quy tắc điểm đúng bằng
    ngưỡng.
-   **\[CẦN CHỐT\]** Cấu hình thay đổi thì tính lại lead cũ ngay hay
    theo yêu cầu/luồng cập nhật.
-   **\[CẦN CHỐT\]** Role được sửa cấu hình; có sort mặc định theo điểm
    hay không.

Để minh họa mock nhất quán, phần dưới dùng giả định tạm: điểm số nguyên
không âm; tiêu chí có điểm mẫu; điểm cao ưu tiên hơn; `hotMin=70`,
`warmMin=40`; Nóng nếu `score >= 70`, Ấm nếu `40 <= score < 70`, Lạnh
nếu `score < 40`; tiêu chí thiếu dữ liệu nhận 0 điểm. **Đây là dữ
liệu/giả định minh họa, không phải quyết định nghiệp vụ đã duyệt.**

## 4. Contract A --- Xem cấu hình chấm điểm (AC1)

**Mã hành động:** `SCORING_CONFIG_VIEW`\
**Kiểu:** JSP server-render là phương án ưu tiên nếu phù hợp repo.

**\[ĐỀ XUẤT --- ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO\]** - URL dự kiến:
`GET /lead-scoring/config` - Controller/Servlet dự kiến:
`LeadScoringConfigServlet` (tên minh họa). - Query params: không bắt
buộc. - Response: forward tới JSP cấu hình với request attributes.

### Request / auth

-   Cookie/session đăng nhập hiện hành; không nhận `role=Director` từ
    query.
-   Server kiểm tra session hợp lệ và quyền xem cấu hình.
-   Nếu chưa đăng nhập: theo convention dự án, thường redirect login
    hoặc trả 401 nếu endpoint AJAX.
-   Nếu không được quyền: 403 hoặc trang báo không có quyền theo
    convention hiện tại.

### JSP request attributes đề xuất

  ------------------------------------------------------------------------------------------------
  Attribute                Kiểu                                Nullable?         Ý nghĩa
  ------------------------ ----------------------------------- ----------------- -----------------
  `scoringConfig`          DTO đề xuất                         Không nếu đã có   Bản cấu hình hiện
                                                               cấu hình          hành

  `criteriaRows`           List`<CriterionConfigDTO>`{=html}   Không; list rỗng  Các tiêu chí và
                                                               được phép         điểm

  `thresholds`             ThresholdDTO                        Có nếu chưa cấu   Ngưỡng
                                                               hình              Nóng/Ấm/Lạnh

  `errorMessage`           String                              Có                Lỗi tải cấu hình

  `canEditScoringConfig`   Boolean                             Không             Cờ UI từ server;
                                                                                 BE vẫn phải kiểm
                                                                                 tra khi POST
  ------------------------------------------------------------------------------------------------

**Không phải field/schema đã xác minh.** Tên attribute chỉ là gợi ý;
phải tuân theo quy ước repo.

### Mock success --- FE

``` json
{
  "configVersion": 3,
  "criteria": [
    {"key": "INDUSTRY_FIT", "label": "Ngành nghề phù hợp", "enabled": true, "points": 25},
    {"key": "COMPANY_SIZE", "label": "Quy mô doanh nghiệp", "enabled": true, "points": 20},
    {"key": "SOURCE", "label": "Nguồn", "enabled": true, "points": 15},
    {"key": "INTEREST_LEVEL", "label": "Mức độ quan tâm", "enabled": true, "points": 40}
  ],
  "thresholds": {"warmMin": 40, "hotMin": 70},
  "mockOnly": true
}
```

JSON trên chỉ là ví dụ payload cho thảo luận; không bắt buộc endpoint
JSON phải tồn tại. Với JSP, dữ liệu tương đương được gắn vào request
attributes.

### Mock empty

``` json
{
  "configVersion": null,
  "criteria": [],
  "thresholds": null,
  "message": "Chưa có cấu hình chấm điểm. Cần khởi tạo cấu hình trước khi sử dụng.",
  "mockOnly": true
}
```

### Mock error / forbidden

``` json
{
  "error": {
    "code": "CONFIG_LOAD_FAILED",
    "message": "Không tải được cấu hình chấm điểm. Vui lòng thử lại."
  },
  "mockOnly": true
}
```

``` json
{
  "error": {
    "code": "FORBIDDEN",
    "message": "Bạn không có quyền truy cập chức năng này."
  },
  "mockOnly": true
}
```

## 5. Contract B --- Lưu cấu hình (AC1, AC3)

**Mã hành động:** `SCORING_CONFIG_SAVE`

**\[ĐỀ XUẤT --- ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO\]** - URL dự kiến:
`POST /lead-scoring/config` - Cách gửi: form-urlencoded hoặc POST form
JSP nếu project đang dùng form submit; JSON chỉ khi repo có convention
fetch/AJAX. - Không được dùng route này trước khi xác minh cấu trúc hiện
tại.

### Input mẫu (logic payload, không khẳng định API JSON)

``` json
{
  "criteria": [
    {"key": "INDUSTRY_FIT", "enabled": true, "points": 25},
    {"key": "COMPANY_SIZE", "enabled": true, "points": 20},
    {"key": "SOURCE", "enabled": true, "points": 15},
    {"key": "INTEREST_LEVEL", "enabled": true, "points": 40}
  ],
  "thresholds": {"warmMin": 40, "hotMin": 70},
  "expectedConfigVersion": 3,
  "mockOnly": true
}
```

### Kiểu dữ liệu và validation dự kiến

  --------------------------------------------------------------------------------------
  Field logic               Kiểu                  Required         Nullable Validation
                                                                            đề xuất
  ------------------------- ------------- ---------------- ---------------- ------------
  `criteria`                List                        Có            Không Chỉ chứa
                                                                            tiêu chí
                                                                            được phép;
                                                                            không trùng
                                                                            key

  `criteria[].key`          String/enum                 Có            Không Một trong 4
                                                                            tiêu chí của
                                                                            AC

  `criteria[].enabled`      Boolean                     Có            Không Boolean
                                                                            thật, không
                                                                            nhận chuỗi
                                                                            tùy tiện

  `criteria[].points`       Integer         Có nếu enabled        Không khi Giới hạn
                                                                    enabled phải do
                                                                            mentor chốt

  `thresholds.warmMin`      Integer       Có theo giả định        Chưa chốt Không âm
                                                                            theo giả
                                                                            định mock

  `thresholds.hotMin`       Integer       Có theo giả định        Chưa chốt Phải lớn hơn
                                                                            `warmMin`
                                                                            theo giả
                                                                            định

  `expectedConfigVersion`   Integer        Tùy chọn/khuyến      Có thể null Dùng phát
                                                      nghị                  hiện cập
                                                                            nhật đồng
                                                                            thời nếu
                                                                            thiết kế hỗ
                                                                            trợ
  --------------------------------------------------------------------------------------

### Mock success

``` json
{
  "success": true,
  "message": "Đã lưu cấu hình chấm điểm.",
  "configVersion": 4,
  "config": {
    "criteria": [
      {"key": "INDUSTRY_FIT", "enabled": true, "points": 25},
      {"key": "COMPANY_SIZE", "enabled": true, "points": 20},
      {"key": "SOURCE", "enabled": true, "points": 15},
      {"key": "INTEREST_LEVEL", "enabled": true, "points": 40}
    ],
    "thresholds": {"warmMin": 40, "hotMin": 70}
  },
  "mockOnly": true
}
```

### Mock validation error

``` json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Vui lòng kiểm tra lại cấu hình.",
    "fieldErrors": {
      "thresholds.hotMin": "Ngưỡng Nóng phải lớn hơn ngưỡng Ấm.",
      "criteria[1].points": "Điểm phải là số nguyên trong giới hạn đã quy định."
    }
  },
  "mockOnly": true
}
```

### Mock conflict

``` json
{
  "success": false,
  "error": {
    "code": "CONFIG_VERSION_CONFLICT",
    "message": "Cấu hình đã được người khác cập nhật. Hãy tải lại trước khi lưu."
  },
  "mockOnly": true
}
```

### Quy tắc lỗi HTTP nếu dùng AJAX

Các mã dưới đây là đề xuất; phải đối chiếu convention dự án: - `400` dữ
liệu request sai cú pháp. - `401` chưa đăng nhập. - `403` không đủ
quyền. - `404` cấu hình không tồn tại nếu thiết kế coi đó là tài nguyên
riêng. - `409` xung đột phiên bản cấu hình. - `422` validation nghiệp vụ
nếu dự án dùng mã này. - `500` lỗi server/DB, không trả stack trace hay
SQL cho browser.

Nếu dùng JSP form submit, thể hiện cùng lỗi bằng request
attribute/message và forward/redirect phù hợp; không cần dựng JSON
endpoint chỉ để tuân theo tài liệu này.

## 6. Contract C --- Cập nhật lead và tính lại điểm (AC2, AC4)

**Mã hành động:** `LEAD_UPDATE_RECALCULATE_SCORE`

**\[ĐỀ XUẤT --- ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO\]** Không đề xuất tạo
route cập nhật lead mới nếu repo đã có Servlet xử lý. Hãy mở luồng
update lead thật và gắn bước tính điểm vào đúng service/transaction hiện
có.

### Input logic minh họa

``` json
{
  "leadId": 1205,
  "industry": "SOFTWARE",
  "companySize": "MEDIUM",
  "source": "WEBINAR",
  "interestLevel": "HIGH",
  "mockOnly": true
}
```

`leadId`, `industry`, `companySize`, `source`, `interestLevel` là tên
minh họa, chưa xác minh field thật. Cần map sang field/enum thật trong
repo; không tự thêm cột vào DB.

### Output logic minh họa

``` json
{
  "success": true,
  "lead": {
    "id": 1205,
    "score": 100,
    "temperature": "HOT",
    "status": "NEW"
  },
  "message": "Đã cập nhật lead và tính lại điểm.",
  "mockOnly": true
}
```

`status` chỉ để minh họa rằng trạng thái không tự đổi do điểm; enum thực
tế phải lấy từ repo. Không khẳng định response thật sẽ có cấu trúc này.

### Quy tắc xử lý dự kiến

1.  Kiểm tra session.
2.  Lấy lead theo ID từ DB; không chỉ tin dữ liệu lead do client gửi.
3.  Kiểm tra quyền sửa lead và Data Scope trên bản ghi ở server.
4.  Validate các field theo danh mục thực tế.
5.  Tính điểm theo cấu hình hiện hành và quy tắc null đã được duyệt.
6.  Lưu thông tin lead và điểm theo cùng transaction nếu điểm được lưu
    vật lý.
7.  Không đổi trạng thái, xóa hoặc loại lead chỉ vì điểm thấp.
8.  Trả dữ liệu đã được server xác nhận để FE cập nhật màn hình.

### Mock success

``` json
{
  "success": true,
  "lead": {
    "id": 1205,
    "industry": "SOFTWARE",
    "companySize": "MEDIUM",
    "source": "WEBINAR",
    "interestLevel": "HIGH",
    "score": 100,
    "temperature": "HOT",
    "status": "NEW"
  },
  "mockOnly": true
}
```

### Mock boundary / thiếu dữ liệu

``` json
{
  "success": true,
  "lead": {
    "id": 1206,
    "industry": null,
    "companySize": "SMALL",
    "source": null,
    "interestLevel": "LOW",
    "score": 20,
    "temperature": "COLD",
    "status": "NEW"
  },
  "note": "Chỉ minh họa giả định trường thiếu dữ liệu nhận 0 điểm; cần mentor xác nhận.",
  "mockOnly": true
}
```

### Mock validation error

``` json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Dữ liệu lead không hợp lệ.",
    "fieldErrors": {
      "source": "Nguồn lead không thuộc danh mục hợp lệ."
    }
  },
  "mockOnly": true
}
```

### Mock forbidden / not found / server error

``` json
{
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "Bạn không có quyền cập nhật lead này."
  },
  "mockOnly": true
}
```

``` json
{
  "success": false,
  "error": {
    "code": "NOT_FOUND",
    "message": "Không tìm thấy lead hoặc lead không khả dụng trong phạm vi của bạn."
  },
  "mockOnly": true
}
```

``` json
{
  "success": false,
  "error": {
    "code": "SERVER_ERROR",
    "message": "Chưa thể cập nhật lead. Vui lòng thử lại."
  },
  "mockOnly": true
}
```

Thông báo không tồn tại/ngoài phạm vi cần theo quy ước bảo mật của dự
án; không để lộ bản ghi ngoài quyền.

## 7. Contract D --- Hiển thị/sắp xếp lead theo điểm (AC3, AC4)

**Mã hành động:** `LEAD_LIST_SCORE_DISPLAY`

**\[ĐỀ XUẤT --- ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO\]** - Không tạo endpoint
danh sách mới nếu đã có màn hình danh sách lead. - Nếu dùng JSP, BE
truyền list DTO đã được lọc theo quyền/scope. - Nếu có sort, tên tham số
sort phải theo quy ước hiện tại và server phải whitelist field sort;
không nối chuỗi SQL từ input.

### DTO hiển thị đề xuất

  -----------------------------------------------------------------------
  Field logic       Kiểu              Nullable          Ghi chú
  ----------------- ----------------- ----------------- -----------------
  `id`              Long/Integer      Không             ID logic của lead

  `displayName`     String            Theo dữ liệu thật Có thể cần
                                                        fallback khi rỗng

  `score`           Integer           Chưa chốt         Có thể null nếu
                                                        chưa chấm được

  `temperature`     Enum/String       Có nếu chưa chấm  `HOT`, `WARM`,
                                                        `COLD` là mã mock

  `status`          Enum/String       Theo model thật   Không được đổi tự
                                                        động vì score

  `updatedAt`       Date/time         Theo model thật   Format hiển thị
                                                        theo convention
                                                        ứng dụng
  -----------------------------------------------------------------------

### Mock list success

``` json
{
  "items": [
    {"id": 1205, "displayName": "Công ty Mây Xanh (MOCK)", "score": 100, "temperature": "HOT", "status": "NEW"},
    {"id": 1207, "displayName": "Công ty Gió Biển (MOCK)", "score": 55, "temperature": "WARM", "status": "CONTACTING"},
    {"id": 1206, "displayName": "Công ty Sao Nhỏ (MOCK)", "score": 20, "temperature": "COLD", "status": "NEW"}
  ],
  "page": 1,
  "pageSize": 20,
  "totalItems": 3,
  "mockOnly": true
}
```

### Mock empty

``` json
{
  "items": [],
  "page": 1,
  "pageSize": 20,
  "totalItems": 0,
  "message": "Chưa có lead phù hợp với điều kiện lọc.",
  "mockOnly": true
}
```

Empty state không được hiểu là BE tự loại lead lạnh. Nếu user chọn bộ
lọc "Nóng" thì empty có thể do bộ lọc chủ động.

### Quy tắc hiển thị

-   Nhãn hiển thị cả chữ và có thể kèm màu/icon; không dựa vào màu đơn
    độc.
-   Điểm null phải hiển thị "Chưa chấm" hoặc nội dung đã được chốt,
    không tự biến thành 0 ở FE nếu chưa có quy tắc.
-   Dữ liệu dài phải wrap/truncate có thể xem đầy đủ; responsive tối
    thiểu theo layout hiện có.
-   Loading/error/empty phải phân biệt rõ.
-   Nếu có phân trang, total/page/pageSize phải nhất quán; không được
    biến thành danh sách chỉ gồm lead nóng.

## 8. Quy tắc tính điểm minh họa duy nhất cho mock

**Giả định minh họa --- chưa duyệt:** cộng điểm của các tiêu chí được
bật; giá trị mẫu khớp đúng lựa chọn được cấu hình; trường thiếu nhận 0;
điểm cao hơn ưu tiên hơn.

  ------------------------------------------------------------------------
  Lead mock Ngành     Quy mô    Nguồn     Quan tâm     Điểm minh Nhãn
            nghề                                             họa 
  --------- --------- --------- --------- --------- ------------ ---------
  1205      25        20        15        40                 100 Nóng

  1207      10        10        15        20                  55 Ấm

  1206      0         10        0         10                  20 Lạnh
  ------------------------------------------------------------------------

Các số trên chỉ phục vụ ví dụ. Chúng không phải tiêu chí hay điểm được
workbook xác định. Không dùng các số này làm acceptance oracle cho
nghiệm thu thật cho đến khi mentor chốt công thức.

## 9. RBAC, Data Scope và an toàn

**\[BACKLOG GỐC\]** Director có `F` với module Lead & phân bổ; Marketing
`F`; Team Lead `F`; Sales Rep `W*`; Cust. Success và Accountant `–`. `*`
là phạm vi dữ liệu sở hữu/nhóm theo vai trò. Ghi chú chung nói Admin có
toàn quyền mọi module. Đây là quyền module, không tự xác định toàn bộ
permission key cho từng hành động.

**\[ĐỀ XUẤT\]** - Server xác minh người dùng đã đăng nhập. - Chỉ role
được duyệt mới gọi thao tác lưu cấu hình. - Update lead phải kiểm tra cả
quyền module và quyền với bản ghi cụ thể. - Ẩn nút trên FE chỉ là UX,
không phải kiểm soát bảo mật. - Không nhận `userId`, `role`, `teamId` từ
client làm bằng chứng quyền sở hữu. - Lỗi không được chứa stack trace,
SQL, mật khẩu hoặc thông tin bí mật. - Nếu config save gồm nhiều bản
ghi, dùng transaction; khi thất bại không để lưu một phần. - Kiểm tra
CSRF/session theo convention đang có trong repo.

## 10. Contract test chung

  ------------------------------------------------------------------------------
  ID             Input/điều     Bước           Kết quả mong   Bằng chứng
                 kiện                          đợi            
  -------------- -------------- -------------- -------------- ------------------
  CT-01          Director, cấu  Lưu cấu hình   Server chấp    Request/response
                 hình hợp lệ                   nhận; reload   hoặc JSP message +
                                               vẫn có dữ liệu DB/test assertion
                                               trong môi      
                                               trường thật    

  CT-02          Ngưỡng sai thứ POST cấu hình  Server từ      Status/message +
                 tự                            chối, lỗi gắn  không đổi DB
                                               đúng field     

  CT-03          User không có  Gọi trực tiếp  403/trang từ   Server log/test +
                 quyền          POST cấu hình  chối theo      DB không đổi
                                               convention     

  CT-04          Lead hợp lệ có Sửa một trường Điểm/nhãn tính DB/result sau
                 đủ tiêu chí    tiêu chí       lại theo công  reload
                                               thức được      
                                               duyệt          

  CT-05          Lead có trường Cập nhật lead  Kết quả theo   Assertion rõ điểm
                 tiêu chí null                 chính sách     và nhãn
                                               null đã chốt   

  CT-06          Lead ngoài     Gửi request    Từ chối; không Test với hai
                 Data Scope     sửa bằng ID    tiết lộ dữ     user/nhóm
                                               liệu trái      
                                               quyền          

  CT-07          Lead điểm thấp Tính lại điểm  Lead vẫn tồn   Query/test
                                               tại, trạng     assertion
                                               thái không tự  
                                               đổi            

  CT-08          DB lỗi giữa    Cố ý gây lỗi   Rollback theo  Transaction test
                 lúc lưu        test           thiết kế;      
                                               không lưu dở   

  CT-09          Hai người lưu  Gửi hai lần    Theo chính     Test lặp lại
                 cùng phiên bản cập nhật cạnh  sách           
                                tranh          concurrency đã 
                                               chốt; không âm 
                                               thầm ghi đè    
                                               nếu dùng       
                                               versioning     

  CT-10          Danh sách dài  Mở list và     Phân           UI + kiểm tra tổng
                                phân trang     trang/sort     bản ghi
                                               đúng; lead     
                                               lạnh không bị  
                                               tự loại        
  ------------------------------------------------------------------------------

Không đánh dấu PASS trước khi chạy trên source và môi trường thực tế.

## 11. Chuyển từ mock sang backend thật

1.  FE tạo view với dữ liệu mock theo các cấu trúc logic ở trên; ghi rõ
    `mockOnly` trong fixture/dev notes, không gửi cờ mock như dữ liệu
    nghiệp vụ production.
2.  Antigravity kiểm tra repo, xác định Servlet/JSP/form hiện tại, DTO,
    Service, DAO, schema, permission và test pattern.
3.  Leader/mentor chốt câu hỏi nghiệp vụ ở mục 3; cập nhật contract
    trước khi triển khai.
4.  BE implement bằng convention hiện có, không bắt buộc tạo REST nếu
    form submit/forward phù hợp.
5.  FE thay fixture bằng request/attributes thật; giữ nguyên mapping UI
    chỉ khi contract đã được duyệt.
6.  So sánh tên field, kiểu dữ liệu, nullability, enum, message và lỗi
    giữa mock với response thật.
7.  Chạy contract tests và kiểm thử RBAC/Data Scope bằng user có quyền
    và user không có quyền.
8.  Kiểm tra DB sau khi lưu và reload; mock thành công không chứng minh
    persistence.
9.  Kiểm tra AC4 riêng: lead lạnh vẫn tồn tại và không tự chuyển trạng
    thái.
10. Ghi bằng chứng nghiệm thu: bước test, input, kết quả thực tế, người
    chạy, ngày chạy và lỗi còn lại.

## 12. Danh sách kỹ thuật cần đối chiếu trong repo

Tất cả đều **ĐỀ XUẤT --- ANTIGRAVITY PHẢI ĐỐI CHIẾU REPO**: - URL/route:
`/lead-scoring/config`, route cập nhật lead, route danh sách lead. -
Servlet/controller: `LeadScoringConfigServlet` và mọi tên minh họa
khác. - Field: `industry`, `companySize`, `source`, `interestLevel`,
`score`, `temperature`, `status`. - DTO: `CriterionConfigDTO`,
`ThresholdDTO`, `LeadDTO`. - Service: scoring/config/lead service. - DAO
và bảng cấu hình/lead; schema/migration/index. - Enum trạng thái lead,
enum nguồn/ngành/quy mô/mức quan tâm. - Quy ước HTTP status,
redirect/forward, flash messages, validation. - Transaction,
locking/versioning, logging/audit nếu dự án có. - Test framework, H2
setup và lệnh chạy test.

Không được tuyên bố các tên trên đang tồn tại trong repo hoặc tự tạo
schema tương ứng mà chưa đối chiếu.

## 13. Ô QUYẾT ĐỊNH --- DRAFT

  -----------------------------------------------------------------------
  Trường                              Giá trị
  ----------------------------------- -----------------------------------
  Trạng thái                          **DRAFT --- CHƯA APPROVED**

  FE đại diện duyệt                   CHƯA BIẾT

  BE đại diện duyệt                   CHƯA BIẾT

  Leader/mentor duyệt                 CHƯA BIẾT

  Ngày duyệt                          CHƯA BIẾT

  Công thức và điểm                   CHỜ CHỐT

  Quy tắc null                        CHỜ CHỐT

  Ngưỡng Nóng/Ấm/Lạnh                 CHỜ CHỐT

  Thời điểm tính lại lead cũ          CHỜ CHỐT

  Quyền sửa cấu hình                  CHỜ CHỐT

  Các thay đổi sau duyệt              Ghi decision, người quyết định,
                                      ngày và ảnh hưởng FE/BE/test
  -----------------------------------------------------------------------

**Không chuyển sang APPROVED chỉ vì mock chạy được.** Chỉ đổi trạng thái
sau khi FE + BE + leader/mentor thống nhất và ghi rõ quyết định.
