<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    Snippet snippet = (Snippet) request.getAttribute("snippet");
    if (snippet == null) {
        response.sendRedirect(request.getContextPath() + "/my-snippets");
        return;
    }

    String pageTitle = "Edit Snippet - CodeShare";
    String extraHead = null;
    String activeNav = "my-snippets";
    String langSelected = snippet.getLanguage() != null ? snippet.getLanguage() : "text";
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<%@ include file="includes/nav.jspf" %>

<div class="container">
    <div class="page-header">
        <h1>Edit snippet</h1>
        <p>Update your snippet details and code</p>
    </div>

    <div class="card">
        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-error"><%= request.getAttribute("error") %></div>
        <% } %>

        <form action="<%= request.getContextPath() %>/edit-snippet" method="post">
            <input type="hidden" name="id" value="<%= snippet.getId() %>">

            <div class="form-group">
                <label for="title">Title</label>
                <input type="text" id="title" name="title" class="form-control"
                       value="<%= snippet.getTitle() %>" required>
            </div>

            <div class="form-group">
                <label for="language">Language</label>
                <select id="language" name="language" class="form-control">
                    <%@ include file="includes/language-options-form.jspf" %>
                </select>
            </div>

            <div class="form-group">
                <label for="code">Code</label>
                <textarea id="code" name="code" class="form-control" required><%= snippet.getCode() %></textarea>
            </div>

            <div class="form-check">
                <input type="checkbox" id="isPublic" name="isPublic" <%= snippet.isPublic() ? "checked" : "" %>>
                <label for="isPublic">Make this snippet public</label>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Save changes</button>
                <a href="<%= request.getContextPath() %>/my-snippets" class="btn btn-secondary">Cancel</a>
            </div>
        </form>
    </div>
</div>

<footer class="site-footer">CodeShare &mdash; Share code, learn together</footer>

</body>
</html>
