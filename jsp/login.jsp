<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String pageTitle = "Login - CodeShare";
    String extraHead = null;
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<div class="page-center">
    <div class="card card-auth">
        <div class="card-header">
            <h1>Welcome back</h1>
            <p>Sign in to your CodeShare account</p>
        </div>

        <% if ("true".equals(request.getParameter("registered"))) { %>
            <div class="alert alert-success">Account created successfully! Please log in.</div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="<%= request.getContextPath() %>/login" method="post">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" class="form-control" required autofocus>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control" required>
            </div>
            <div class="form-actions" style="border-top: none; padding-top: 0; margin-top: 0;">
                <button type="submit" class="btn btn-primary" style="width: 100%;">Log in</button>
            </div>
        </form>

        <div class="auth-footer">
            Don't have an account? <a href="<%= request.getContextPath() %>/jsp/register.jsp">Register</a>
        </div>
    </div>
</div>

</body>
</html>
