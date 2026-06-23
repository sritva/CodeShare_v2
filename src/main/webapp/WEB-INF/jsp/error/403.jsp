<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String pageTitle = "403 Access Denied - CodeShare";
    String extraHead = null;
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="../includes/head.jspf" %>
<body>

<div class="page-center">
    <div class="card" style="text-align: center; max-width: 500px;">
        <div class="card-header">
            <h1 style="font-size: 3.5rem; margin: 0; color: #ffa940;">403</h1>
            <h2 style="margin: 10px 0 5px 0;">Access Denied</h2>
            <p style="margin: 0; color: #888;">You don't have permission to access this resource.</p>
        </div>

        <div style="padding: 30px 20px;">
            <p><%= request.getAttribute("message") != null ? request.getAttribute("message") : "You do not have permission to access this resource." %></p>
            
            <div style="margin-top: 30px;">
                <a href="<%= request.getContextPath() %>/home" class="btn btn-primary" style="display: inline-block; padding: 10px 20px; text-decoration: none;">
                    Back to Home
                </a>
            </div>
        </div>
    </div>
</div>

</body>
</html>
