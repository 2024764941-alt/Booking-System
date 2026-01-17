<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Select Booking Method - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
        <style>
            .method-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
                gap: 2.5rem;
                margin-top: 4rem;
                max-width: 900px;
                margin-left: auto;
                margin-right: auto;
            }

            .method-card {
                background: #FFFFFF;
                padding: 4rem 2.5rem;
                text-align: center;
                border: 1px solid #F0F0F0;
                transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
                cursor: pointer;
                text-decoration: none;
                color: inherit;
                position: relative;
                overflow: hidden;
            }

            .method-card::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                width: 100%;
                height: 1px;
                background: linear-gradient(90deg, transparent, #000, transparent);
                transform: scaleX(0);
                transition: transform 0.6s ease;
                opacity: 0.5;
            }

            .method-card:hover {
                transform: translateY(-8px);
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.04);
                border-color: #E0E0E0;
            }

            .method-card:hover::before {
                transform: scaleX(1);
            }

            .method-icon {
                height: 64px;
                width: 64px;
                margin: 0 auto 2rem auto;
                color: #1a1a1a;
                transition: transform 0.4s ease;
            }

            .method-card:hover .method-icon {
                transform: scale(1.1);
            }

            .method-card h3 {
                font-family: 'Playfair Display', serif;
                font-size: 1.75rem;
                margin-bottom: 1rem;
                color: #1a1a1a;
                letter-spacing: -0.02em;
            }

            .method-card p {
                font-family: 'Lato', sans-serif;
                color: #555;
                font-weight: 300;
                line-height: 1.8;
                font-size: 1.05rem;
            }

            h2.section-title {
                font-family: 'Playfair Display', serif;
                font-size: 3rem;
                color: #1a1a1a;
                margin-bottom: 1rem;
            }

            p.section-subtitle {
                font-family: 'Lato', sans-serif;
                color: #777;
                font-weight: 300;
                letter-spacing: 0.05em;
                text-transform: uppercase;
                font-size: 0.85rem;
            }
        </style>
    </head>

    <body>
        <div id="app">
            <!-- Page ID -->
            <div id="booking-method-page"></div>

            <section id="booking-method" class="page active">

                <jsp:include page="header.jsp" />

                <div class="container" style="padding-bottom: 6rem;">
                    <!-- Header -->
                    <div
                        style="text-align: center; margin-top: 4rem; opacity: 0; animation: fadeInUp 0.8s ease-out forwards;">
                        <h2 class="section-title">How would you like to book?</h2>
                        <p class="section-subtitle">Choose the path that suits your vision</p>
                    </div>

                    <div class="method-grid">
                        <!-- Option 1: By Date -->
                        <a href="booking-date.jsp" class="method-card" id="method-date-btn"
                            style="opacity: 0; animation: fadeInUp 0.8s ease-out 0.2s forwards;">
                            <div class="method-icon">
                                <svg xmlns="http://www.w3.org/2001/svg" fill="none" viewBox="0 0 24 24"
                                    stroke="currentColor" stroke-width="1">
                                    <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                                </svg>
                            </div>
                            <h3>By Date</h3>
                            <p>Have a specific timeline? Select your preferred date to see available designers.</p>
                        </a>

                        <!-- Option 2: By Category -->
                        <a href="booking-category.jsp" class="method-card" id="method-category-btn"
                            style="opacity: 0; animation: fadeInUp 0.8s ease-out 0.4s forwards;">
                            <div class="method-icon">
                                <svg xmlns="http://www.w3.org/2001/svg" fill="none" viewBox="0 0 24 24"
                                    stroke="currentColor" stroke-width="1">
                                    <path stroke-linecap="round" stroke-linejoin="round"
                                        d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01" />
                                </svg>
                            </div>
                            <h3>By Style</h3>
                            <p>Driven by aesthetics? Browse curated design styles to find the perfect match.</p>
                        </a>
                    </div>
                </div>

                <jsp:include page="footer.jsp" />
            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>