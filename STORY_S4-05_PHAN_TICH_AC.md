# STORY S4-05 --- PHÂN TÍCH ACCEPTANCE CRITERIA

## 1. Trạng thái và nguồn sự thật

-   **Story ID:** S4-05
-   **Nguồn nghiệp vụ:** workbook `HỆ THỐNG QUẢN LÝ KHÁCH HÀNG.xlsx`,
    sheet `4. Product Backlog`, dòng có ID chính xác `S4-05`.
-   **Vai trò trong backlog:** Giám đốc kinh doanh
-   **Epic:** EP-04 --- Lead & Phân bổ
-   **Sprint:** 4
-   **Story Points:** 8
-   **Ưu tiên:** Must
-   **Trạng thái trong workbook:** Chưa bắt đầu
-   **Ghi chú/người thực hiện được ghi trong workbook:** Phạm Tiến Hà
    (BE)
-   **Repo/source code:** Chưa được cung cấp; mọi chi tiết kỹ thuật bên
    dưới là đề xuất cần đối chiếu, không phải mô tả code hiện có.

### Nhãn bằng chứng

-   **\[BACKLOG GỐC\]** Trích nguyên văn workbook.
-   **\[GIẢI THÍCH\]** Diễn giải nghiệp vụ để nhóm dễ hiểu.
-   **\[ĐỀ XUẤT\]** Phương án kỹ thuật/mock để FE và BE thảo luận.
-   **\[CHƯA XÁC MINH\]** Cần kiểm tra repo, DB hoặc chạy thử.
-   **\[ĐÃ ĐƯỢC NGƯỜI DÙNG CHỐT\]** Chưa có quyết định bổ sung nào ngoài
    nội dung workbook.

## 2. Trích nguyên văn Story và AC

**\[BACKLOG GỐC\] User Story**

> Là Giám đốc kinh doanh, tôi muốn cấu hình chấm điểm lead theo tiêu chí
> khai báo được, để nhân viên gọi những lead có khả năng nhất trước.

**\[BACKLOG GỐC\] Acceptance Criteria (nguyên văn, đánh số để tiện trao
đổi)**

-   **AC1:** Khai báo tiêu chí và số điểm: ngành nghề phù hợp, quy mô
    doanh nghiệp, nguồn, mức độ quan tâm
-   **AC2:** Điểm được tính lại tự động khi thông tin lead thay đổi
-   **AC3:** Phân loại Nóng, Ấm, Lạnh theo ngưỡng điểm khai báo được
-   **AC4:** Điểm chỉ để sắp xếp ưu tiên, không tự động loại lead

**\[BACKLOG GỐC\]** Workbook không nêu công thức cộng điểm, kiểu dữ liệu
cụ thể của từng tiêu chí, cách xử lý tiêu chí chưa có dữ liệu, ngưỡng
Nóng/Ấm/Lạnh mặc định, lịch sử cấu hình, hoặc việc có cho sửa cấu hình
đang áp dụng hay không. Không được tự coi các chi tiết đó là quyết định
đã duyệt.

## 3. Phân quyền liên quan

### 3.1 Đọc đúng ma trận `2. User Roles`

**\[BACKLOG GỐC\]** Sheet ghi chú: - `F` = toàn quyền. - `W` = ghi trong
phạm vi được giao. - `R` = chỉ xem. - `–` = không truy cập. - `*` = chỉ
trên dữ liệu mình sở hữu, hoặc của nhóm mình quản lý đối với Team
Lead. - Giá vốn và biên lợi nhuận chỉ Giám đốc kinh doanh xem được. -
Ràng buộc phạm vi dữ liệu phải được lọc ở truy vấn phía server, không
chỉ ẩn trên giao diện. - Ghi chú chung của sheet nêu Admin có toàn quyền
trên mọi module.

Ở hàng `Lead & phân bổ`, ma trận hiển thị: Sales Rep `W*`, Marketing
`F`, Cust. Success `–`, Accountant `–`, Team Lead `F`, Director `F`. Cột
Admin không được thể hiện thành cột riêng trong bảng ma trận, nhưng ghi
chú chung nói Admin toàn quyền mọi module.

**\[GIẢI THÍCH\]** Story này là cấu hình quy tắc chấm điểm, không đồng
nghĩa với quyền xem/sửa mọi lead. `F/W/R` là mức thao tác trên module;
phạm vi bản ghi (của tôi/của nhóm/tất cả) là ràng buộc riêng. Director
là vai trò của Story và có `F` ở module Lead & phân bổ. Quyền truy cập
cấu hình cụ thể vẫn cần kiểm tra nếu repo có permission key chi tiết
hơn.

**\[ĐỀ XUẤT\]** Chỉ Director (và Admin theo quy tắc chung của workbook)
được tạo/sửa ngưỡng và điểm cấu hình. Người dùng khác có thể nhìn thấy
điểm/nhãn lead trong phạm vi quyền hiện có nếu sản phẩm cho phép, nhưng
không được sửa cấu hình. Đây là đề xuất cần leader chốt, không phải AC
đã ghi.

## 4. Phân tích từng AC

### AC1 --- Khai báo tiêu chí và số điểm

**\[BACKLOG GỐC\]** "Khai báo tiêu chí và số điểm: ngành nghề phù hợp,
quy mô doanh nghiệp, nguồn, mức độ quan tâm".

**\[GIẢI THÍCH\]** Director cần một màn hình cấu hình để xác định tiêu
chí nào được chấm và số điểm tương ứng. Bốn nhóm tiêu chí được nêu rõ là
ngành nghề phù hợp, quy mô doanh nghiệp, nguồn và mức độ quan tâm.
Workbook chưa xác định mỗi tiêu chí là danh sách giá trị hay điểm nhập
tự do, có cho phép nhiều giá trị hay không, và điểm có thể âm hay chỉ
không âm.

**\[ĐỀ XUẤT\] Luồng FE** 1. Mở trang "Cấu hình chấm điểm lead" (tên
trang đề xuất). 2. Hiển thị cấu hình hiện tại, các tiêu chí và điểm
tương ứng; nút Lưu/Hủy. 3. Validate cơ bản ở FE để báo lỗi nhanh, nhưng
không thay validation server. 4. Khi lưu, khóa nút và hiện trạng thái
đang lưu; thành công thì hiển thị thông báo và dữ liệu đã được server
trả lại. 5. Có trạng thái loading, lỗi tải, lỗi lưu, và empty state nếu
chưa có cấu hình.

**\[ĐỀ XUẤT\] Luồng BE/DB** 1. Xác thực phiên đăng nhập. 2. Kiểm tra
role/permission cấu hình; không tin role gửi từ browser. 3. Kiểm tra
tiêu chí hợp lệ, điểm đúng kiểu số và nằm trong giới hạn đã được chốt.
4. Lưu toàn bộ cấu hình theo một transaction nếu cấu hình gồm nhiều bản
ghi. 5. Trả cấu hình chuẩn hóa; không trả dữ liệu không cần thiết. 6.
Tên bảng/service/field cụ thể: **CHƯA XÁC MINH**.

**Lỗi/biên cần xét** - Thiếu tiêu chí bắt buộc; điểm rỗng, chữ thay vì
số, số ngoài giới hạn. - Tiêu chí trùng lặp hoặc giá trị danh mục không
hợp lệ. - Người không có quyền gọi trực tiếp URL/POST. - Hai Director
lưu gần như đồng thời: cần quyết định chính sách ghi đè hoặc phát hiện
cấu hình cũ. - DB lỗi giữa lúc lưu nhiều tiêu chí: không để trạng thái
lưu dở dang. - Double-click nút Lưu: không tạo bản ghi trùng hoặc trạng
thái không nhất quán.

**Test AC1** - Director lưu cấu hình hợp lệ; tải lại trang vẫn thấy đúng
cấu hình đã lưu trong môi trường thật. - Điểm nhập sai định dạng bị từ
chối ở server và hiển thị lỗi có thể hiểu. - User không có quyền gửi
POST trực tiếp vẫn bị từ chối; DB không đổi. - Mô phỏng lỗi DB giữa
transaction; không có cấu hình lưu một nửa. - Hai lần gửi cùng request
không tạo dữ liệu trùng ngoài dự kiến.

**IN SCOPE:** Cấu hình tiêu chí/điểm cho bốn nhóm tiêu chí được backlog
nêu. **OUT OF SCOPE trừ khi được chốt thêm:** Tự tạo tiêu chí tùy ý
ngoài bốn nhóm; AI dự đoán khả năng mua; phân bổ lead; loại/xóa lead;
báo cáo hiệu quả chiến dịch.

### AC2 --- Tự tính lại khi thông tin lead thay đổi

**\[BACKLOG GỐC\]** "Điểm được tính lại tự động khi thông tin lead thay
đổi".

**\[GIẢI THÍCH\]** Khi một trường lead thuộc các tiêu chí thay đổi, điểm
phải được tính lại mà không yêu cầu người dùng bấm nút "Tính điểm"
riêng. "Tự động" có thể là tính ngay trong luồng lưu lead hoặc xử lý bất
đồng bộ; workbook không chỉ rõ độ trễ chấp nhận được.

**\[ĐỀ XUẤT\] Luồng FE** - FE gửi cập nhật lead như luồng hiện có của dự
án. - Sau khi lưu thành công, hiển thị điểm/nhãn mới từ response hoặc
tải lại dữ liệu. - Không tự tính điểm chính thức chỉ bằng JavaScript; FE
chỉ được preview nếu sau này được chốt riêng.

**\[ĐỀ XUẤT\] Luồng BE/DB** 1. Nhận request cập nhật lead. 2. Kiểm tra
authentication, quyền sửa lead và Data Scope (sở hữu/nhóm/tất cả theo
vai trò). 3. Validate các trường lead. 4. Tính điểm từ dữ liệu lead mới
và cấu hình hiện hành. 5. Lưu thông tin lead và điểm/nhãn trong cùng
transaction nếu điểm được lưu; hoặc tính khi đọc nếu thiết kế hiện tại
dùng cách đó. 6. Trả kết quả để FE hiển thị.

**\[CHƯA XÁC MINH\]** Repo/DB chưa cho biết điểm sẽ được lưu hay tính
lúc đọc, trigger nào cập nhật lead, tên field nào thuộc tiêu chí, hoặc
lead được tạo/cập nhật qua những luồng nào.

**Lỗi/biên cần xét** - Lead thiếu một hoặc nhiều trường tiêu chí. - Cập
nhật trường không liên quan: có cần tính lại hay không cần tối ưu? - Cấu
hình thay đổi đồng thời với cập nhật lead. - Cập nhật lead thất bại:
không được để điểm mới gắn với dữ liệu lead cũ. - Hai cập nhật đồng
thời: tránh điểm cuối cùng không tương ứng với phiên bản dữ liệu cuối. -
Người dùng sửa lead ngoài phạm vi: server phải từ chối dù biết ID. - Lỗi
tính điểm hoặc DB: rollback thay đổi liên quan theo chính sách
transaction đã chọn.

**Test AC2** - Sửa từng trường tiêu chí; kiểm tra điểm/nhãn sau lưu theo
công thức được mentor chốt. - Sửa trường không thuộc tiêu chí; xác nhận
hành vi theo quyết định hiệu năng. - Gửi dữ liệu thiếu tiêu chí; xác
nhận chính sách điểm thiếu dữ liệu. - Ép lỗi DB khi cập nhật; kiểm tra
lead và điểm không lệch nhau. - Cập nhật lead ngoài Data Scope; nhận từ
chối và dữ liệu không đổi.

**IN SCOPE:** Điểm cập nhật theo thông tin lead thay đổi. **OUT OF SCOPE
trừ khi được chốt thêm:** Chấm điểm bằng lịch sử tương tác, dữ liệu bên
ngoài, mô hình ML, tự phân công nhân viên hoặc tự loại lead.

### AC3 --- Phân loại Nóng/Ấm/Lạnh theo ngưỡng khai báo được

**\[BACKLOG GỐC\]** "Phân loại Nóng, Ấm, Lạnh theo ngưỡng điểm khai báo
được".

**\[GIẢI THÍCH\]** Điểm số được chuyển thành một trong ba nhãn. Cần có
ngưỡng cấu hình và quy tắc biên không chồng lấn, không bỏ sót điểm.
Workbook chưa quy định điểm cao là Nóng hay các giá trị ngưỡng cụ thể,
dù cách hiểu phổ biến là điểm cao hơn tương ứng mức ưu tiên cao hơn.

**\[ĐỀ XUẤT --- CHỜ CHỐT\]** Nếu dùng thang điểm tăng dần, có thể quy
định `score >= hotMin` là Nóng; `warmMin <= score < hotMin` là Ấm;
`score < warmMin` là Lạnh. Đây chỉ là mẫu minh họa, không phải ngưỡng
được duyệt. Cần chốt điểm tối thiểu/tối đa, ngưỡng mặc định và xử lý
bằng đúng ngưỡng.

**Luồng FE:** màn hình cấu hình có hai ngưỡng dễ hiểu; cảnh báo nếu
ngưỡng đảo hoặc bằng nhau không hợp lệ; danh sách lead hiển thị nhãn
nhất quán với điểm; màu sắc không phải cách duy nhất để truyền đạt nhãn.

**Luồng BE/DB:** server kiểm tra ngưỡng hợp lệ, phân loại bằng một
hàm/quy tắc dùng chung, không để mỗi màn hình tự phân loại khác nhau.
Nếu lưu nhãn, cập nhật đồng bộ với điểm; nếu tính khi đọc, cùng một hàm
được dùng ở mọi luồng.

**Lỗi/biên** - Ngưỡng âm, rỗng, không phải số, đảo thứ tự. - Điểm đúng
bằng ngưỡng nóng/ấm. - Điểm null do dữ liệu thiếu hoặc cấu hình chưa tồn
tại. - Cấu hình mới làm nhiều lead đổi nhãn; cần xác định có tính lại
toàn bộ ngay không. - Nhiều request đọc trong lúc cập nhật cấu hình;
tránh nhãn/điểm không đồng nhất.

**Test AC3** - Test điểm dưới ngưỡng Ấm, bằng ngưỡng Ấm, giữa hai
ngưỡng, bằng ngưỡng Nóng, và trên ngưỡng Nóng. - Thử lưu ngưỡng đảo thứ
tự; server từ chối. - Sửa ngưỡng trong môi trường test; kiểm tra nhãn
các lead theo chính sách đã duyệt. - Kiểm tra cùng lead trên danh sách
và trang chi tiết cho kết quả nhất quán.

**IN SCOPE:** Ba phân loại Nóng/Ấm/Lạnh theo ngưỡng cấu hình. **OUT OF
SCOPE:** Thêm nhãn thứ tư, tự chuyển trạng thái lead, gửi thông báo,
hoặc tự phân bổ lead nếu không có Story/AC khác.

### AC4 --- Điểm chỉ để ưu tiên, không tự động loại lead

**\[BACKLOG GỐC\]** "Điểm chỉ để sắp xếp ưu tiên, không tự động loại
lead".

**\[GIẢI THÍCH\]** Lead điểm thấp vẫn phải tồn tại và không bị tự động
xóa, ẩn khỏi danh sách, đóng, hoặc đổi sang trạng thái bị loại chỉ vì
điểm thấp. Có thể dùng điểm để sắp xếp danh sách ưu tiên, nhưng quy tắc
sort mặc định và quyền người dùng thay đổi sort chưa được chỉ rõ.

**Luồng FE:** hiển thị điểm/nhãn và có thể hỗ trợ sắp xếp giảm dần nếu
được chốt; không lọc bỏ lead lạnh mặc định mà người dùng không biết. Nếu
có bộ lọc nhãn, đó phải là thao tác lọc rõ ràng do người dùng chọn.

**Luồng BE/DB:** việc cập nhật điểm không được gọi logic xóa/loại/chuyển
trạng thái. Danh sách vẫn trả lead lạnh theo Data Scope và các bộ lọc do
người dùng chủ động áp dụng. Kiểm tra toàn bộ luồng chấm điểm để không
có side effect ngoài AC.

**Test AC4** - Tạo/cập nhật lead điểm rất thấp; xác nhận lead vẫn tồn
tại và còn xuất hiện trong danh sách không lọc, nếu user có quyền xem. -
Xác nhận trạng thái lead không đổi chỉ vì điểm. - Sắp xếp theo điểm
không làm mất bản ghi; chuyển trang vẫn đủ dữ liệu. - Kiểm tra query,
export (nếu đã có trong phạm vi khác) không tự loại lead chỉ vì nhãn
Lạnh.

**IN SCOPE:** Bảo đảm chấm điểm không tự loại lead; ưu tiên/sắp xếp chỉ
khi được triển khai phù hợp. **OUT OF SCOPE:** Tự động từ chối, xóa,
archive, đổi trạng thái, hoặc ngăn nhân viên tiếp cận lead lạnh.

## 5. Bảng mapping AC → thiết kế → test

  --------------------------------------------------------------------------------------------
  AC         Diễn giải  Luồng FE đề     Luồng BE/DB   Quyền/phạm   Test tối thiểu   Câu hỏi
             chính xác  xuất            đề xuất       vi                            chưa chốt
  ---------- ---------- --------------- ------------- ------------ ---------------- ----------
  AC1        Khai báo   Form cấu hình,  Auth +        Director `F` Lưu hợp lệ; điểm Điểm cho
             điểm cho 4 validate,       permission,   ở Lead &     sai; user không  từng giá
             nhóm tiêu  lưu/hủy,        validate,     phân bổ;     quyền; lỗi       trị hay
             chí        loading/error   transaction   Admin theo   transaction      một điểm
                                        lưu cấu hình  ghi chú                       cho mỗi
                                                      chung; vai                    nhóm? Giới
                                                      trò khác cần                  hạn điểm?
                                                      chặn sửa cấu                  
                                                      hình                          

  AC2        Tự tính    Sau lưu lead    Validate      Quyền sửa    Sửa từng trường; Tính đồng
             lại khi    hiển thị        scope; tính   lead và Data thiếu dữ liệu;   bộ hay bất
             thông tin  điểm/nhãn mới   theo config;  Scope hiện   rollback; cập    đồng bộ?
             lead đổi                   lưu/tính nhất hữu          nhật đồng thời   Điểm lưu
                                        quán với lead                               hay tính
                                                                                    lúc đọc?

  AC3        Phân loại  Cấu hình        Kiểm tra      Chỉ người    Test biên        Ngưỡng mặc
             theo       ngưỡng; nhãn    ngưỡng; một   được quyền   dưới/bằng/trên   định,
             ngưỡng cấu hiển thị nhất   quy tắc phân  sửa cấu hình ngưỡng           hướng
             hình       quán            loại dùng                                   điểm, điểm
                                        chung                                       biên?

  AC4        Điểm không Không ẩn/xóa    Không đổi     Mọi truy vấn Lead điểm thấp   Sort mặc
             tự loại    lead lạnh mặc   trạng         vẫn lọc theo vẫn tồn tại và   định? Có
             lead       định; sort rõ   thái/xóa theo quyền và     xem được trong   lưu lịch
                        ràng            điểm; giữ     scope        scope            sử điểm
                                        Data Scope                                  không?
  --------------------------------------------------------------------------------------------

## 6. Câu hỏi bắt buộc chốt với mentor/leader (tối đa 5)

1.  **\[CẦN CHỐT\] Mô hình điểm của mỗi tiêu chí là gì?**\
    Phương án A: mỗi nhóm tiêu chí có một số điểm cố định. Phương án B:
    từng giá trị trong nhóm (ví dụ từng nguồn lead) có điểm riêng. A đơn
    giản, B linh hoạt hơn nhưng cần cấu hình/dữ liệu chi tiết hơn.
2.  **\[CẦN CHỐT\] Thang điểm và quy tắc thiếu dữ liệu là gì?**\
    Ví dụ tổng điểm không âm, trường chưa có dữ liệu nhận 0 điểm; nhưng
    cần duyệt rõ để không chấm sai lead chưa đủ thông tin.
3.  **\[CẦN CHỐT\] Ngưỡng Nóng/Ấm/Lạnh và quy tắc điểm bằng ngưỡng?**\
    Cần hai ngưỡng, cách so sánh (`>=` hay `>`), điểm tối đa và mặc
    định. Nếu không chốt, FE/BE có thể phân loại khác nhau.
4.  **\[CẦN CHỐT\] Khi cấu hình đổi, điểm/nhãn của lead cũ cập nhật lúc
    nào?**\
    Phương án A: tính lại hàng loạt ngay; B: tính lại khi lead được
    sửa/đọc. A nhất quán hơn nhưng có chi phí và cần xử lý lỗi hàng
    loạt.
5.  **\[CẦN CHỐT\] Ai được sửa cấu hình và sort mặc định ra sao?**\
    Đề xuất Director/Admin được sửa; điểm giảm dần chỉ là sort mặc định
    nếu leader chấp thuận. Cần chốt để không suy rộng quyền từ quyền
    module.

## 7. Decision Log và giả định

  ------------------------------------------------------------------------
  ID                Quyết định/giả     Trạng thái        Hệ quả nếu đổi
                    định                                 
  ----------------- ------------------ ----------------- -----------------
  D-01              Bốn nhóm tiêu chí  \[BACKLOG GỐC\]   Không tự thêm
                    là ngành nghề, quy                   tiêu chí ngoài AC
                    mô doanh nghiệp,                     
                    nguồn, mức độ quan                   
                    tâm                                  

  D-02              Director là người  \[BACKLOG GỐC\]   Không đồng nghĩa
                    dùng chính của                       các role khác có
                    chức năng cấu hình                   quyền sửa

  D-03              Director và Admin  \[ĐỀ XUẤT --- CHỜ Nếu thay đổi, cập
                    được sửa cấu hình  CHỐT\]            nhật UI,
                                                         permission và
                                                         test

  D-04              Điểm cao tương ứng \[GIẢ ĐỊNH ---    Có thể đảo logic
                    mức ưu tiên cao    CHỜ CHỐT\]        nhãn và sort
                    hơn                                  

  D-05              Tính điểm phía     \[ĐỀ XUẤT\]       FE không được tự
                    server là nguồn                      coi preview là
                    kết quả chính thức                   kết quả chính
                                                         thức

  D-06              Ngưỡng/công        \[CHƯA XÁC MINH\] Chưa được code
                    thức/field/table                     theo tên giả định
                    cụ thể                               như schema thật
  ------------------------------------------------------------------------

**Trạng thái tài liệu: DRAFT --- chưa được mentor/FE/BE duyệt.** Không
có mục nào được đánh dấu APPROVED.

## 8. Checklist nghiệm thu thực tế

-   [ ] Đối chiếu Story/AC với đúng dòng `S4-05` trong workbook.
-   [ ] Mentor/leader chốt 5 câu hỏi nghiệp vụ ở mục 6.
-   [ ] Xác minh quyền cấu hình và quyền đọc điểm trong repo.
-   [ ] Xác minh luồng tạo/sửa lead hiện tại và tất cả entry point cập
    nhật lead.
-   [ ] Xác minh field thật cho ngành nghề, quy mô, nguồn, mức độ quan
    tâm.
-   [ ] Chốt công thức, giới hạn điểm, quy tắc null và ngưỡng.
-   [ ] Chạy test AC1--AC4 trên DB/test environment; ghi lại input, kết
    quả thực tế và bằng chứng.
-   [ ] Kiểm tra user không quyền và Data Scope bằng request trực tiếp,
    không chỉ qua UI.
-   [ ] Kiểm tra lead điểm thấp không bị ẩn/xóa/đổi trạng thái.
-   [ ] Không ghi PASS nếu chưa chạy kiểm thử thực tế.

## 9. Bắt buộc đối chiếu source trước khi code

Antigravity/nhóm cần kiểm tra repo thật để xác minh: 1. Cấu trúc
JSP/Servlet và quy ước route/form hiện có; không mặc định bắt buộc REST
JSON. 2. Servlet/controller xử lý tạo/sửa lead; service/DAO liên quan.
3. Trường dữ liệu lead thực tế và enum/danh mục hiện có. 4. Cơ chế
session/authentication, permission key và Data Scope ở server. 5. Cách
project dùng MySQL khi chạy website và H2 trong một số test. 6. Quy ước
transaction, migration/schema và seed dữ liệu. 7. Test framework, test
pattern và lệnh chạy test thực tế. 8. Cách hiển thị message/validation
trong JSP; CSRF/session convention nếu có. 9. Cơ chế phân trang, tìm
kiếm và sắp xếp danh sách lead. 10. Không tự tạo bảng/field/API chỉ vì
tài liệu draft đề xuất; nếu cần schema mới, nêu rõ migration và xin
duyệt.

**Kết luận:** Tài liệu này phân tích nghiệp vụ từ workbook và đưa ra
thiết kế dự kiến để thảo luận. Nó không chứng minh code đã tồn tại, AC
đã đạt, hay contract đã được duyệt.
