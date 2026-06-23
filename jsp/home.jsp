<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet, com.codeshare.model.User, java.util.List" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    User user = (User) session.getAttribute("user");
    List<Snippet> snippets = (List<Snippet>) request.getAttribute("snippets");

    String pageTitle = "Home - CodeShare";
    String extraHead = null;
    String activeNav = "home";
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<%@ include file="includes/nav.jspf" %>

<div class="container container-wide">
    <div class="page-header">
        <h1>Hello, <%= user.getUsername() %></h1>
        <p>Browse public code snippets shared by the community</p>
    </div>

    <div class="card">
        <div class="card-header">
            <h1 style="font-size: 1.125rem; margin: 0;">All public snippets</h1>
        </div>

        <% if (snippets == null || snippets.isEmpty()) { %>
            <div class="empty-state">
                <p>No public snippets yet.</p>
                <a href="<%= request.getContextPath() %>/create-snippet" class="btn btn-primary">Create the first snippet</a>
            </div>
        <% } else { %>
            <div class="table-wrap">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Language</th>
                            <th>Posted by</th>
                            <th>Created</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Snippet s : snippets) { %>
                        <tr>
                            <td><a href="<%= request.getContextPath() %>/view-snippet?id=<%= s.getId() %>"><%= s.getTitle() %></a></td>
                            <td><span class="badge badge-lang"><%= s.getLanguage() %></span></td>
                            <td><%= s.getUsername() %></td>
                            <td><%= s.getCreatedAt() %></td>
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
