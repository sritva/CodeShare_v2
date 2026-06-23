<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet, java.util.List" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    List<Snippet> snippets = (List<Snippet>) request.getAttribute("snippets");
    String keyword  = request.getAttribute("keyword")  != null ? (String) request.getAttribute("keyword")  : "";
    String language = request.getAttribute("language") != null ? (String) request.getAttribute("language") : "all";
    Long totalResults = (Long) request.getAttribute("totalResults");

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
            <p class="search-results-count"><strong><%= totalResults != null ? totalResults : (snippets != null ? snippets.size() : 0) %></strong> result<%= (totalResults != null ? totalResults : (snippets != null ? snippets.size() : 0)) == 1 ? "" : "s" %> found</p>
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
                            <td><span class="lang-label lang-<%= s.getLanguage().toLowerCase() %>"><%= s.getLanguage() %></span></td>
                            <td><%= s.getUsername() %></td>
                            <td><%= s.getCreatedAt() %></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
<%
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages  = (Integer) request.getAttribute("totalPages");
    Boolean hasPrevious = (Boolean) request.getAttribute("hasPrevious");
    Boolean hasNext     = (Boolean) request.getAttribute("hasNext");
    if (currentPage == null) currentPage = 0;
    if (totalPages  == null) totalPages  = 1;
    if (hasPrevious == null) hasPrevious = false;
    if (hasNext     == null) hasNext     = false;
    if (totalResults == null) totalResults = (snippets != null ? (long) snippets.size() : 0L);
    String pageBase = request.getContextPath() + "/search?keyword=" + java.net.URLEncoder.encode(keyword != null ? keyword : "", "UTF-8")
                    + "&language=" + java.net.URLEncoder.encode(language != null ? language : "all", "UTF-8");
%>
            <% if (totalPages > 1) { %>
            <div class="pagination">
                <% if (hasPrevious) { %>
                    <a href="<%= pageBase %>&page=<%= currentPage - 1 %>" class="btn btn-secondary btn-sm">&larr; Previous</a>
                <% } else { %>
                    <button class="btn btn-secondary btn-sm" disabled>&larr; Previous</button>
                <% } %>
                <span class="pagination-info">Page <%= currentPage + 1 %> of <%= totalPages %></span>
                <% if (hasNext) { %>
                    <a href="<%= pageBase %>&page=<%= currentPage + 1 %>" class="btn btn-secondary btn-sm">Next &rarr;</a>
                <% } else { %>
                    <button class="btn btn-secondary btn-sm" disabled>Next &rarr;</button>
                <% } %>
            </div>
            <% } %>
        <% } %>
    </div>
</div>

<footer class="site-footer">CodeShare &mdash; Share code, learn together</footer>

</body>
</html>
