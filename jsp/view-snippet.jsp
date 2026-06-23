<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet, com.codeshare.model.User" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/jsp/login.jsp");
        return;
    }
    Snippet snippet = (Snippet) request.getAttribute("snippet");
    if (snippet == null) {
        response.sendRedirect(request.getContextPath() + "/home");
        return;
    }
    User user = (User) session.getAttribute("user");

    String pageTitle = snippet.getTitle() + " - CodeShare";
    String activeNav = "";
    String extraHead =
        "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css\">" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-java.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-python.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-javascript.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-markup.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-sql.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-css.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-c.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-cpp.min.js\"></script>";
%>
<!DOCTYPE html>
<html lang="en">
<%@ include file="includes/head.jspf" %>
<body>

<%@ include file="includes/nav.jspf" %>

<div class="container container-wide">
    <div class="page-header">
        <h1><%= snippet.getTitle() %></h1>
        <div class="snippet-meta">
            <span><strong>Language:</strong> <span class="badge badge-lang"><%= snippet.getLanguage() %></span></span>
            <span><strong>By:</strong> <%= snippet.getUsername() %></span>
            <span><strong>Posted:</strong> <%= snippet.getCreatedAt() %></span>
            <span><strong>Visibility:</strong>
                <% if (snippet.isPublic()) { %>
                    <span class="badge badge-public">Public</span>
                <% } else { %>
                    <span class="badge badge-private">Private</span>
                <% } %>
            </span>
        </div>
    </div>

    <div class="card">
        <div class="code-toolbar">
            <span style="font-size: 0.875rem; font-weight: 600; color: var(--muted);">Source code</span>
            <button type="button" id="copyBtn" class="btn btn-secondary btn-sm btn-copy" onclick="copyCode()">Copy code</button>
        </div>
        <div class="code-block-wrap">
            <pre><code class="language-<%= snippet.getLanguage() %>" id="codeBlock"><%= snippet.getCode() %></code></pre>
        </div>

        <div class="snippet-actions">
            <% if (snippet.getUser() != null && snippet.getUser().getId().equals(user.getId())) { %>
                <a href="<%= request.getContextPath() %>/edit-snippet?id=<%= snippet.getId() %>" class="btn btn-primary">Edit snippet</a>
            <% } %>
            <a href="<%= request.getContextPath() %>/home" class="btn btn-secondary">&larr; Back to all snippets</a>
        </div>
    </div>
</div>

<footer class="site-footer">CodeShare &mdash; Share code, learn together</footer>

<script>
function copyCode() {
    const code = document.getElementById("codeBlock").innerText;
    const btn = document.getElementById("copyBtn");
    navigator.clipboard.writeText(code).then(function() {
        btn.textContent = "Copied!";
        btn.classList.add("copied");
        setTimeout(function() {
            btn.textContent = "Copy code";
            btn.classList.remove("copied");
        }, 2000);
    });
}
</script>

</body>
</html>
