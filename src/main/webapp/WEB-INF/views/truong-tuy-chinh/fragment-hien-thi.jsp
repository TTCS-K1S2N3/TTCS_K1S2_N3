<%--
    Fragment: Hiển thị Trường Tuỳ Chỉnh trong Form KhachHang / CoHoi - S2-08 FE
    Sử dụng bằng: <jsp:include page="/WEB-INF/views/truong-tuy-chinh/fragment-hien-thi.jsp"/>
    Attribute cần set vào request trước khi include:
     - dsTruongTuyChinhHienThi : List<TruongTuyChinhDTO> (các trường đang hoạt động)
     - giaTriTuyChinhHienTai   : Map<String,String> (key=tenTruong, value=giaTri - dùng khi sửa)
    Mọi trường có batBuoc=true sẽ có attribute required trong HTML.
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty dsTruongTuyChinhHienThi}">
    <div class="ttc-form-section">
        <p class="ttc-form-section-title">Thông tin tuỳ chỉnh</p>

        <c:forEach var="t" items="${dsTruongTuyChinhHienThi}">
            <div class="form-group">
                <label for="ttc_${t.tenTruong}">
                    <c:out value="${t.nhanHien}"/>
                    <c:if test="${t.batBuoc}">
                        <span class="required">*</span>
                    </c:if>
                </label>

                <c:set var="giaTriHienTai"
                       value="${not empty giaTriTuyChinhHienTai ? giaTriTuyChinhHienTai[t.tenTruong] : ''}"/>

                <c:choose>

                    <%-- Kiểu: Văn bản --%>
                    <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">
                        <input type="text"
                               id="ttc_${t.tenTruong}"
                               name="ttc_${t.tenTruong}"
                               maxlength="500"
                               <c:if test="${t.batBuoc}">required</c:if>
                               value="<c:out value='${giaTriHienTai}'/>">
                    </c:when>

                    <%-- Kiểu: Số --%>
                    <c:when test="${t.kieuDuLieu == 'SO'}">
                        <input type="number"
                               id="ttc_${t.tenTruong}"
                               name="ttc_${t.tenTruong}"
                               step="any"
                               <c:if test="${t.batBuoc}">required</c:if>
                               value="<c:out value='${giaTriHienTai}'/>">
                    </c:when>

                    <%-- Kiểu: Ngày --%>
                    <c:when test="${t.kieuDuLieu == 'NGAY'}">
                        <input type="date"
                               id="ttc_${t.tenTruong}"
                               name="ttc_${t.tenTruong}"
                               <c:if test="${t.batBuoc}">required</c:if>
                               value="<c:out value='${giaTriHienTai}'/>">
                    </c:when>

                    <%-- Kiểu: Danh sách chọn --%>
                    <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">
                        <select id="ttc_${t.tenTruong}"
                                name="ttc_${t.tenTruong}"
                                <c:if test="${t.batBuoc}">required</c:if>>
                            <c:if test="${not t.batBuoc}">
                                <option value="">-- Không chọn --</option>
                            </c:if>
                            <c:forEach var="opt" items="${t.danhSachLuaChon}">
                                <option value="<c:out value='${opt}'/>"
                                    <c:if test="${opt == giaTriHienTai}">selected</c:if>>
                                    <c:out value="${opt}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </c:when>

                </c:choose>
            </div>
        </c:forEach>
    </div>
</c:if>
