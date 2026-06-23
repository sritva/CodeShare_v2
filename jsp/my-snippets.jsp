<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet, java.util.List" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    List<Snippet> snippets = (List<Snippet>) request.getAttribute("snippets");

    String pageTitle = "My Snippets - CodeShare";
    String extraHead = null;
    String activeNav = "my-snippets";
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<%@ include file="includes/nav.jspf" %>

<div class="container container-wide">
    <div class="page-header">
        <h1>My snippets</h1>
        <p>Manage your public and private code snippets</p>
    </div>

    <div class="card">
        <% if (snippets == null || snippets.isEmpty()) { %>
            <div class="empty-state">
                <p>You haven't created any snippets yet.</p>
                <a href="<%= request.getContextPath() %>/create-snippet" class="btn btn-primary">Create your first snippet</a>
            </div>
        <% } else { %>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Language</th>
                            <th>Visibility</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Snippet s : snippets) { %>
                        <tr>
                            <td><a href="<%= request.getContextPath() %>/view-snippet?id=<%= s.getId() %>"><%= s.getTitle() %></a></td>
                            <td><span class="badge badge-lang"><%= s.getLanguage() %></span></td>
                            <td>
                                <% if (s.isPublic()) { %>
                                    <span class="badge badge-public">Public</span>
                                <% } else { %>
                                    <span class="badge badge-private">Private</span>
                                <% } %>
                            </td>
                            <td><%= s.getCreatedAt() %></td>
                            <td class="actions">
                                <a href="<%= request.getContextPath() %>/edit-snippet?id=<%= s.getId() %>" class="btn btn-secondary btn-sm">Edit</a>
                                <form action="<%= request.getContextPath() %>/delete-snippet" method="post"
                                      onsubmit="return confirm('Delete this snippet?');">
                                    <input type="hidden" name="id" value="<%= s.getId() %>">
                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                </form>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </div>
</div>

<footer class="site-footer">CodeShare &mdash; Share code, learn together</footer>

</body>
</html>
