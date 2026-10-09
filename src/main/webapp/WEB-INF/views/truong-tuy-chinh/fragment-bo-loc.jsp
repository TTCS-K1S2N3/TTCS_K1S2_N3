<%--
    Fragment: Bộ Lọc Trường Tuỳ Chỉnh - S2-08 FE
    Sử dụng bằng: <jsp:include page="/WEB-INF/views/truong-tuy-chinh/fragment-bo-loc.jsp"/>
    Attribute cần set vào request trước khi include:
     - dsTruongBoDacTuyChinh : List<TruongTuyChinhDTO> (các trường có hienThiBoDac=true)
     - giaTriBoDacTuyChinh   : Map<String,String> (giá trị đang lọc từ query param)
    Fragment này được nhúng vào filter-bar của danh-sach-khach-hang.jsp và danh-sach-co-hoi.jsp
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty dsTruongBoDacTuyChinh}">
    <div class="ttc-filter-section">
        <div class="ttc-filter-section-title" style="display: flex; align-items: center; gap: 4px;">
            <span class="material-symbols-outlined icon-xs" aria-hidden="true">tune</span>
            <span>Bộ lọc tuỳ chỉnh</span>
        </div>
        <div class="ttc-filter-row">
            <c:forEach var="t" items="${dsTruongBoDacTuyChinh}">
                <c:set var="giaTriLoc"
                       value="${not empty giaTriBoDacTuyChinh ? giaTriBoDacTuyChinh[t.tenTruong] : ''}"/>

                <div class="ttc-filter-group">
                    <label for="ttcf_${t.tenTruong}">
                        <c:out value="${t.nhanHien}"/>
                    </label>

                    <c:choose>

                        <%-- Kiểu văn bản: input text --%>
                        <c:when test="${t.kieuDuLieu == 'VAN_BAN'}">
                            <input type="text"
                                   id="ttcf_${t.tenTruong}"
                                   name="ttcf_${t.tenTruong}"
                                   placeholder="Lọc theo ${t.nhanHien}..."
                                   maxlength="200"
                                   value="<c:out value='${giaTriLoc}'/>">
                        </c:when>

                        <%-- Kiểu số: input number --%>
                        <c:when test="${t.kieuDuLieu == 'SO'}">
                            <input type="number"
                                   id="ttcf_${t.tenTruong}"
                                   name="ttcf_${t.tenTruong}"
                                   placeholder="${t.nhanHien}"
                                   step="any"
                                   style="min-width:110px"
                                   value="<c:out value='${giaTriLoc}'/>">
                        </c:when>

                        <%-- Kiểu ngày: input date (từ - đến) --%>
                        <c:when test="${t.kieuDuLieu == 'NGAY'}">
                            <input type="date"
                                   id="ttcf_${t.tenTruong}_tu"
                                   name="ttcf_${t.tenTruong}_tu"
                                   title="${t.nhanHien} từ"
                                   value="<c:out value='${giaTriBoDacTuyChinh[t.tenTruong.concat(\"_tu\")]}'/>"
                                   style="min-width:130px">
                            <input type="date"
                                   id="ttcf_${t.tenTruong}_den"
                                   name="ttcf_${t.tenTruong}_den"
                                   title="${t.nhanHien} đến"
                                   value="<c:out value='${giaTriBoDacTuyChinh[t.tenTruong.concat(\"_den\")]}'/>"
                                   style="min-width:130px">
                        </c:when>

                        <%-- Kiểu danh sách chọn: select --%>
                        <c:when test="${t.kieuDuLieu == 'DANH_SACH_CHON'}">
                            <select id="ttcf_${t.tenTruong}"
                                    name="ttcf_${t.tenTruong}">
                                <option value="">-- Tất cả --</option>
                                <c:forEach var="opt" items="${t.danhSachLuaChon}">
                                    <option value="<c:out value='${opt}'/>"
                                        <c:if test="${opt == giaTriLoc}">selected</c:if>>
                                        <c:out value="${opt}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </c:when>

                    </c:choose>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>
