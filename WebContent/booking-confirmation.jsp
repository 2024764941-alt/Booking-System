<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Confirmation - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <div id="booking-confirmation-page"></div>

            <section id="booking-confirmation" class="page active">

                <jsp:include page="header.jsp" />

                <div class="container">
                    <h2>Booking Confirmed</h2>
                    <div class="receipt">
                        <p><strong>Booking ID:</strong> <span id="confirm-id"></span></p>
                        <p><strong>Client Name:</strong> <span id="confirm-name"></span></p>
                        <p><strong>Email:</strong> <span id="confirm-email"></span></p>
                        <p><strong>Phone:</strong> <span id="confirm-phone"></span></p>
                        <p><strong>Designer:</strong> <span id="confirm-designer"></span></p>
                        <p><strong>Date:</strong> <span id="confirm-date"></span></p>
                        <p><strong>Time:</strong> <span id="confirm-time"></span></p>
                        <p><strong>Notes:</strong> <span id="confirm-notes"></span></p>
                        <p><strong>Status:</strong> Confirmed</p>
                    </div>
                    <button id="print-booking" class="btn">Print / Download</button>
                    <a href="index.jsp" class="btn secondary">Back to Home</a>
                </div>

                <jsp:include page="footer.jsp" />
            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>