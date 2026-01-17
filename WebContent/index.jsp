<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>DotsStudio - Interior Design Consultation Booking</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <!-- Landing Page -->
            <section id="landing" class="page active">

                <jsp:include page="header.jsp" />

                <div class="hero"
                    style="background: linear-gradient(rgba(0, 0, 0, 0.3), rgba(0, 0, 0, 0.3)), url('category_images/background.jpg') no-repeat center center; background-size: cover;">
                    <div class="hero-content">
                        <h1>DotsStudio</h1>
                        <p class="tagline">Designing Spaces That Inspire</p>
                        <p class="description">At DotsStudio, we specialize in creating bespoke interior designs that
                            reflect your unique style and enhance your living spaces. Our expert consultants are here to
                            bring your vision to life.</p>
                        <button id="book-consultation-btn" class="btn">Book Consultation</button>
                    </div>
                </div>

                <!-- Philosophy Section -->
                <section class="philosophy" id="philosophy">
                    <div class="philosophy-grid">
                        <div class="philosophy-left">
                            <p class="philosophy-left-label">Our Philosophy</p>
                            <h2>Timeless Design for Modern Living</h2>
                        </div>
                        <div class="philosophy-right">
                            <p>At DotsStudio, we believe that exceptional design transcends trends. Every space tells a
                                story—yours. We create environments that are not just beautiful, but deeply functional
                                and
                                meaningful. Our approach balances aesthetic elegance with practical living, ensuring
                                your
                                home becomes a reflection of your personality and lifestyle.</p>
                            <p><a href="#" class="read-more">Read Our Full Philosophy</a></p>
                        </div>
                    </div>
                </section>

                <!-- Portfolio Section -->
                <section class="portfolio" id="portfolio">
                    <div class="section-header">
                        <h2>Featured Projects</h2>
                        <p class="section-subtitle">A curated selection of transformations</p>
                    </div>
                    <div class="portfolio-grid">
                        <div class="portfolio-item">
                            <div class="portfolio-item-image">
                                <img src="category_images/id1.jpeg" alt="Modern Urban Living">
                            </div>
                            <div class="portfolio-item-meta">
                                <h3>Modern Urban Living</h3>
                                <p class="portfolio-item-location">Mont Kiara, Kuala Lumpur</p>
                            </div>
                        </div>
                        <div class="portfolio-item">
                            <div class="portfolio-item-image">
                                <img src="category_images/id2.jpeg" alt="Serene Retreat">
                            </div>
                            <div class="portfolio-item-meta">
                                <h3>Serene Retreat</h3>
                                <p class="portfolio-item-location">Georgetown, Penang</p>
                            </div>
                        </div>
                        <div class="portfolio-item">
                            <div class="portfolio-item-image">
                                <img src="category_images/od3.jpeg" alt="Contemporary Workspace">
                            </div>
                            <div class="portfolio-item-meta">
                                <h3>Contemporary Workspace</h3>
                                <p class="portfolio-item-location">Petaling Jaya, Selangor</p>
                            </div>
                        </div>
                        <div class="portfolio-item">
                            <div class="portfolio-item-image">
                                <img src="category_images/id4.jpeg" alt="Luxury Villa">
                            </div>
                            <div class="portfolio-item-meta">
                                <h3>Luxury Villa</h3>
                                <p class="portfolio-item-location">Langkawi, Kedah</p>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Process Section -->
                <section class="process" id="process">
                    <div class="section-header">
                        <h2>Our Process</h2>
                        <p class="section-subtitle">From vision to realization</p>
                    </div>
                    <div class="process-grid">
                        <div class="process-item">
                            <div class="process-number">01</div>
                            <h3>Discovery & Consultation</h3>
                            <p>We begin by listening. Understanding your lifestyle, aesthetic preferences, and
                                functional
                                needs allows us to create a design that truly resonates with who you are.</p>
                        </div>
                        <div class="process-item">
                            <div class="process-number">02</div>
                            <h3>Design Development</h3>
                            <p>Our designers craft multiple concepts, exploring materials, color palettes, and spatial
                                arrangements. Your feedback guides each iteration, ensuring the final design exceeds
                                your
                                expectations.</p>
                        </div>
                        <div class="process-item">
                            <div class="process-number">03</div>
                            <h3>Implementation & Realization</h3>
                            <p>We manage every detail of the execution—from sourcing materials to coordinating
                                contractors.
                                Your vision becomes reality with meticulous attention to quality and craftsmanship.</p>
                        </div>
                    </div>
                </section>

                <jsp:include page="footer.jsp" />

            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>