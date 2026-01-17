<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Select Style - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
        <style>
            .category-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
                gap: 1.5rem;
                margin-top: 2rem;
            }

            .category-card {
                background: white;
                padding: 1rem;
                text-align: center;
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
                cursor: pointer;
                transition: all 0.2s;
                border: 1px solid #eee;
                overflow: hidden;
            }

            .category-image {
                width: 100%;
                height: 150px;
                object-fit: cover;
                border-radius: 4px;
                margin-bottom: 1rem;
            }

            .category-card:hover {
                transform: translateY(-3px);
                box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
                border-color: var(--primary-color);
            }

            .category-card.selected {
                border-color: var(--primary-color);
                background-color: var(--bg-light);
            }

            .category-card h3 {
                margin: 0;
                font-size: 1.2rem;
                color: var(--text-dark);
            }
        </style>
    </head>

    <body>
        <div id="app">
            <div id="booking-category-page"></div>

            <section class="page active">
                <jsp:include page="header.jsp" />

                <div class="container">
                    <div class="booking-progress">
                        <!-- Dynamic Progress Bar via script.js -->
                    </div>

                    <h2>Select Design Style</h2>
                    <div id="category-grid" class="category-grid">
                        <!-- Populated by script.js -->
                        <div style="text-align: center; width: 100%; grid-column: 1 / -1;">
                            <p>Loading styles...</p>
                        </div>
                    </div>

                    <div style="margin-top: 2rem; display: flex; justify-content: space-between; gap: 1rem;">
                        <a href="booking-method.jsp" class="btn cancel btn-red"
                            style="background-color: #dc3545 !important; color: white !important;">Back</a>
                        <button id="next-to-designer-cat" class="btn">Next</button>
                    </div>
                </div>

                <jsp:include page="footer.jsp" />
            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>