<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String pageTitle = "Create Snippet - CodeShare";
    String activeNav = "create";
    String langSelected = "text";
    String extraHead =
        "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/codemirror.min.css\">" +
        "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/theme/material-darker.min.css\">" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/codemirror.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/mode/clike/clike.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/mode/python/python.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/mode/javascript/javascript.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/mode/xml/xml.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/mode/css/css.min.js\"></script>" +
        "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.65.17/mode/sql/sql.min.js\"></script>";
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
                <input type="text" id="title" name="title" class="form-control" placeholder="e.g. Binary search in Java" maxlength="200" required>
            </div>

            <div class="form-group">
                <label for="language">Language</label>
                <select id="language" name="language" class="form-control">
                    <%@ include file="includes/language-options-form.jspf" %>
                </select>
            </div>

            <div class="form-group">
                <label for="code">Code</label>
                <textarea id="code" name="code" class="form-control" placeholder="Paste your code here..."></textarea>
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

<script>
var langModeMap = {
    java: 'text/x-java',
    python: 'python',
    javascript: 'javascript',
    html: 'xml',
    css: 'css',
    sql: 'text/x-sql',
    c: 'text/x-csrc',
    cpp: 'text/x-c++src',
    text: null
};

var editor = CodeMirror.fromTextArea(document.getElementById('code'), {
    theme: 'material-darker',
    lineNumbers: true,
    indentUnit: 4,
    tabSize: 4,
    indentWithTabs: false,
    lineWrapping: true,
    autofocus: false
});
editor.setSize(null, 400);

document.getElementById('language').addEventListener('change', function() {
    var mode = langModeMap[this.value] || null;
    editor.setOption('mode', mode);
});

document.querySelector('form').addEventListener('submit', function() {
    editor.save();
});
</script>

</body>
</html>
