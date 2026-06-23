<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String pageTitle = "Register - CodeShare";
    String extraHead = null;
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<div class="page-center">
    <div class="card card-auth">
        <div class="card-header">
            <h1>Create account</h1>
            <p>Join CodeShare and start sharing snippets</p>
        </div>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="<%= request.getContextPath() %>/register" method="post">
            <div class="form-group">
                <label for="username">Username</label>
                <input type="text" id="username" name="username" class="form-control"
                       minlength="3" maxlength="30" pattern="[a-zA-Z0-9_]+"
                       title="3–30 characters: letters, digits, or underscores"
                       required autofocus>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" class="form-control" minlength="6" required>
            </div>
            <div class="form-group">
                <label for="confirmPassword">Confirm password</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" minlength="6" required>
            </div>
            <div class="form-actions" style="border-top: none; padding-top: 0; margin-top: 0;">
                <button type="submit" class="btn btn-primary" style="width: 100%;">Create account</button>
            </div>
        </form>

        <div class="auth-footer">
            Already have an account? <a href="<%= request.getContextPath() %>/login">Log in</a>
        </div>
    </div>
</div>

</body>
</html>
