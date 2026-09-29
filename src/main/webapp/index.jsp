<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session != null && session.getAttribute("nguoiDungHienTai") != null) {
        response.sendRedirect(request.getContextPath() + "/khach-hang");
    } else {
        response.sendRedirect(request.getContextPath() + "/dang-nhap");
    }
%>
