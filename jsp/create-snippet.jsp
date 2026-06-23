<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }

    String pageTitle = "Create Snippet - CodeShare";
    String extraHead = null;
    String activeNav = "create";
    String langSelected = "text";
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<%@ include file="includes/nav.jspf" %>

<div class="container">
    <div class="page-header">
        <h1>Create snippet</h1>
        <p>Share a new piece of code with the community or keep it private</p>
    </div>

    <div class="card">
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="<%= request.getContextPath() %>/create-snippet" method="post">
            <div class="form-group">
                <label for="title">Title</label>
                <input type="text" id="title" name="title" class="form-control" placeholder="e.g. Binary search in Java" required>
            </div>

            <div class="form-group">
                <label for="language">Language</label>
                <select id="language" name="language" class="form-control">
                    <%@ include file="includes/language-options-form.jspf" %>
                </select>
            </div>

            <div class="form-group">
                <label for="code">Code</label>
                <textarea id="code" name="code" class="form-control" placeholder="Paste your code here..." required></textarea>
            </div>

            <div class="form-check">
                <input type="checkbox" id="isPublic" name="isPublic" checked>
                <label for="isPublic">Make this snippet public</label>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Create snippet</button>
                <a href="<%= request.getContextPath() %>/my-snippets" class="btn btn-secondary">Cancel</a>
            </div>
        </form>
    </div>
</div>

<footer class="site-footer">CodeShare &mdash; Share code, learn together</footer>

</body>
</html>
