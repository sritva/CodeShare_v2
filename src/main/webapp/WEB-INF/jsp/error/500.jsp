<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String pageTitle = "500 Server Error - CodeShare";
    String extraHead = null;
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="../includes/head.jspf" %>
<body>

<div class="page-center">
    <div class="card" style="text-align: center; max-width: 500px;">
        <div class="card-header">
            <h1 style="font-size: 3.5rem; margin: 0; color: #e74c3c;">500</h1>
            <h2 style="margin: 10px 0 5px 0;">Server Error</h2>
            <p style="margin: 0; color: #888;">Something went wrong on our end.</p>
        </div>

        <div style="padding: 30px 20px;">
            <p><%= request.getAttribute("message") != null ? request.getAttribute("message") : "An unexpected error occurred. Please try again later." %></p>
            
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
