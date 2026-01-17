<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Booking Summary - DotsStudio</title>
        <link rel="preconnect" href="https://fonts.googleapis.com">
        <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
        <link
            href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;500;600;700&family=Lato:wght@300;400;700&display=swap"
            rel="stylesheet">
        <link rel="stylesheet" href="style.css?v=<%= System.currentTimeMillis() %>">
    </head>

    <body>
        <div id="app">
            <div id="booking-summary-page"></div>

            <section id="booking-summary" class="page active">

                <jsp:include page="header.jsp" />

                <div class="container">
                    <div class="booking-progress">
                        <div class="progress-track"></div>
                        <div class="progress-step completed">
                            <div class="step-circle">1</div>
                            <div class="step-label">Date</div>
                        </div>
                        <div class="progress-step completed">
                            <div class="step-circle">2</div>
                            <div class="step-label">Designer</div>
                        </div>
                        <div class="progress-step completed">
                            <div class="step-circle">3</div>
                            <div class="step-label">Time</div>
                        </div>
                        <div class="progress-step active">
                            <div class="step-circle">4</div>
                            <div class="step-label">Summary</div>
                        </div>
                    </div>
                    <h2>Booking Summary</h2>
                    <div class="summary-details">
                        <p><strong>Client Name:</strong> <span id="summary-name"></span></p>
                        <p><strong>Email:</strong> <span id="summary-email"></span></p>
                        <p><strong>Designer:</strong> <span id="summary-designer"></span></p>
                        <p><strong>Date:</strong> <span id="summary-date"></span></p>
                        <p><strong>Time:</strong> <span id="summary-time"></span></p>
                        <div class="form-group">
                            <label for="summary-phone">Phone Number</label>
                            <input type="tel" id="summary-phone">
                        </div>
                        <div class="form-group">
                            <label for="summary-notes">Notes / Consultation Details</label>
                            <textarea id="summary-notes"></textarea>
                        </div>
                    </div>
                    <div style="display: flex; justify-content: space-between; gap: 20px; margin-top: 20px;">
                        <a href="javascript:void(0)" onclick="handleBookingBack()" class="btn secondary">Back</a>
                        <button id="confirm-booking" class="btn">Confirm Booking</button>
                    </div>
                </div>

                <jsp:include page="footer.jsp" />
            </section>
        </div>
        <script src="script.js?v=<%= System.currentTimeMillis() %>"></script>
    </body>

    </html>