<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
    <% // Get the requested URI String requestedUri=(String) request.getAttribute("jakarta.servlet.error.request_uri");
        // If it's an HTML file, redirect to the JSP equivalent if (requestedUri !=null &&
        requestedUri.endsWith(".html")) { String jspUri=requestedUri.replace(".html", ".jsp" );
        response.sendRedirect(request.getContextPath() + jspUri.substring(jspUri.lastIndexOf("/"))); } else { // For
        other 404s, show a generic error page %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Page Not Found - DotsStudio</title>
            <link rel="stylesheet" href="style.css">
            <style>
                .error-container {
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    text-align: center;
                    padding: 2rem;
                }

                .error-content {
                    max-width: 600px;
                }

                .error-code {
                    font-size: 6rem;
                    font-weight: bold;
                    color: var(--accent-color, #c6a87c);
                    margin: 0;
                }

                .error-message {
                    font-size: 1.5rem;
                    margin: 1rem 0;
                }

                .error-description {
                    color: #666;
                    margin: 1rem 0 2rem;
                }
            </style>
        </head>

        <body>
            <div class="error-container">
                <div class="error-content">
                    <h1 class="error-code">404</h1>
                    <p class="error-message">Page Not Found</p>
                    <p class="error-description">
                        The page you're looking for doesn't exist or has been moved.
                    </p>
                    <a href="index.jsp" class="btn">Return to Home</a>
                </div>
            </div>
        </body>

        </html>
        <% } %>