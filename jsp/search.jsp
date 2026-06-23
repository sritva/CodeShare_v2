<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet, java.util.List" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    List<Snippet> snippets = (List<Snippet>) request.getAttribute("snippets");
    String keyword  = request.getAttribute("keyword")  != null ? (String) request.getAttribute("keyword")  : "";
    String language = request.getAttribute("language") != null ? (String) request.getAttribute("language") : "all";

    String pageTitle = "Search - CodeShare";
    String extraHead = null;
    String activeNav = "search";
    String langSelected = language;
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<%@ include file="includes/nav.jspf" %>

<div class="container container-wide">
    <div class="page-header">
        <h1>Search snippets</h1>
        <p>Find public snippets by title or programming language</p>
    </div>

    <div class="card search-bar">
        <form action="<%= request.getContextPath() %>/search" method="get">
            <div class="form-row">
                <div class="form-group">
                    <label for="keyword">Keyword</label>
                    <input type="text" id="keyword" name="keyword" class="form-control"
                           placeholder="Search by title..." value="<%= keyword %>">
                </div>
                <div class="form-group" style="max-width: 200px;">
                    <label for="language">Language</label>
                    <select id="language" name="language" class="form-control">
                        <%@ include file="includes/language-options.jspf" %>
                    </select>
                </div>
                <div class="form-group" style="flex: 0;">
                    <label>&nbsp;</label>
                    <button type="submit" class="btn btn-primary">Search</button>
                </div>
            </div>
        </form>
    </div>

    <div class="card">
        <% if (snippets == null || snippets.isEmpty()) { %>
            <div class="empty-state">
                <p>No snippets found matching your search.</p>
                <a href="<%= request.getContextPath() %>/home" class="btn btn-secondary">Browse all snippets</a>
            </div>
        <% } else { %>
            <p class="search-results-count"><strong><%= snippets.size() %></strong> result<%= snippets.size() == 1 ? "" : "s" %> found</p>
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
