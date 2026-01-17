<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Select Date - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <!-- Added ID for page detection -->
            <div id="booking-date-page"></div>

            <section id="booking-date" class="page active">

                <jsp:include page="header.jsp" />

                <div class="container">
                    <div class="booking-progress">
                        <!-- Dynamic Progress Bar via script.js -->
                    </div>
                    <h2>Select Appointment Date</h2>
                    <input type="date" id="appointment-date" min="2026-01-01">

                    <div style="display: flex; justify-content: space-between; gap: 20px; margin-top: 20px;">
                        <a href="javascript:void(0)" onclick="handleBookingBack()" class="btn cancel btn-red"
                            style="background-color: #dc3545 !important; color: white !important;">Back</a>
                        <button id="next-to-designer" class="btn">Next</button>
                    </div>
                </div>

                <jsp:include page="footer.jsp" />
            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>