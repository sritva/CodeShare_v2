<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.codeshare.model.Snippet, com.codeshare.model.User" %>
<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
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
        "<script src=\"https://cdn.jsdelivr.net/npm/marked/marked.min.js\"></script>" +
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
            <span><strong>Language:</strong> <span class="lang-label lang-<%= snippet.getLanguage().toLowerCase() %>"><%= snippet.getLanguage() %></span></span>
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
            <div style="display: flex; gap: 8px;">
                <button type="button" class="btn btn-secondary btn-sm" onclick="toggleExplanationWindow()">Explain code</button>
                <button type="button" id="copyBtn" class="btn btn-secondary btn-sm btn-copy" onclick="copyCode()">Copy code</button>
            </div>
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

    <!-- AI Code Explanation Window below the source code window -->
    <div id="aiExplanationWindow" class="explanation-window">
        <div class="explanation-window-header">
            <h3 class="explanation-window-title">
                <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="color: var(--primary); vertical-align: middle; margin-right: 4px;"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                Explanation
            </h3>
            <button type="button" class="explanation-close-btn" onclick="closeExplanationWindow()">&times;</button>
        </div>
        <div class="explanation-window-body" id="explanationWindowBody">
            <!-- Loading or markdown description -->
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

let cachedExplanation = null;
<% if (snippet.getAiExplanation() != null && !snippet.getAiExplanation().trim().isEmpty()) { %>
    cachedExplanation = `<%= snippet.getAiExplanation().replace("`", "\\`").replace("$", "\\$") %>`;
<% } %>

function toggleExplanationWindow() {
    const win = document.getElementById("aiExplanationWindow");
    if (win.classList.contains("show")) {
        closeExplanationWindow();
    } else {
        openExplanationWindow();
    }
}

function openExplanationWindow() {
    const win = document.getElementById("aiExplanationWindow");
    const body = document.getElementById("explanationWindowBody");
    
    if (win.classList.contains("show")) return;
    
    win.style.height = "auto";
    
    if (cachedExplanation) {
        body.innerHTML = `
            <div id="aiExplanationText" class="markdown-body">
                ${marked.parse(cachedExplanation)}
            </div>
        `;
        animateExpand(win);
        return;
    }
    
    const snippetId = <%= snippet.getId() %>;
    body.innerHTML = `
        <div class="ai-loading-wrap" style="padding: 24px; text-align: center;">
            <div class="ai-loader-pulse"></div>
            <p style="margin: 12px 0 0 0; font-size: 12px; color: var(--muted); font-weight: 500;">Gemini is analyzing the code structure and preparing an explanation...</p>
        </div>
    `;
    
    animateExpand(win);

    fetch("<%= request.getContextPath() %>/view-snippet/explain", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "id=" + encodeURIComponent(snippetId)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(errText => {
                throw new Error(errText || "Error status code: " + response.status);
            });
        }
        return response.text();
    })
    .then(markdownText => {
        cachedExplanation = markdownText;
        body.innerHTML = `
            <div id="aiExplanationText" class="markdown-body">
                ${marked.parse(markdownText)}
            </div>
        `;
        adjustHeight(win);
    })
    .catch(error => {
        console.error("AI Explainer failed:", error);
        body.innerHTML = `
            <div class="alert alert-error" style="margin: 0; padding: 12px; border-radius: 3px; font-size: 12px;">
                <strong style="font-weight: bold;">Error:</strong> ${error.message}
                <div style="margin-top: 10px;">
                    <button type="button" class="btn btn-secondary btn-sm" onclick="openExplanationWindow()">Try Again</button>
                </div>
            </div>
        `;
        adjustHeight(win);
    });
}

function closeExplanationWindow() {
    const win = document.getElementById("aiExplanationWindow");
    if (!win.classList.contains("show")) return;
    
    win.style.height = win.scrollHeight + "px";
    win.offsetHeight; // Force reflow
    
    win.classList.remove("show");
    win.style.height = "0px";
}

function animateExpand(element) {
    element.classList.add("show");
    const height = element.scrollHeight;
    element.style.height = height + "px";
    
    setTimeout(() => {
        if (element.classList.contains("show")) {
            element.style.height = "auto";
        }
    }, 350);
}

function adjustHeight(element) {
    if (!element.classList.contains("show")) return;
    element.style.height = "auto";
    const height = element.scrollHeight;
    element.style.height = element.clientHeight + "px"; // Start from current
    element.offsetHeight; // Reflow
    element.style.height = height + "px";
    
    setTimeout(() => {
        if (element.classList.contains("show")) {
            element.style.height = "auto";
        }
    }, 350);
}
</script>

</body>
</html>
