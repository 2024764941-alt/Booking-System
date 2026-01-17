package com.dottstudio.controller;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.dottstudio.util.PasswordUtils;
import com.dottstudio.util.UserDAO;
import com.dottstudio.model.User;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        System.out.println(">>> DEBUG: Login Attempt for email: " + email);

        try {
            // Need to hash password before checking DB if using stored hash comparison in
            // query
            // However, UserDAO.login currently takes raw password and query checks
            // 'PasswordHash = ?'
            // This implies UserDAO expects the HASHED password if the DB stores hash.
            // Or UserDAO should handle hashing.
            // Let's assume UserDAO.login should handle verification or raw check.
            // Looking at previous UserDAO code: `stmt.setString(2, password);` matches
            // against `PasswordHash`.
            // So we must hash it here first if DB has hashes.

            // MICROSERVICE INTEGRATION: Call Auth Service
            String authServiceUrl = "http://localhost:8081/auth/api/login";
            String jsonPayload = "{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"; // Naive JSON construction

            java.net.URL url = new java.net.URL(authServiceUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Servlet often expects params, but our new API expects params too or JSON?
            // Wait, my Auth Service LoginServlet does: request.getParameter("email"). 
            // So I should send form-urlencoded, NOT JSON body for the *request*.
            // Let's adjust payload style.
            
            String urlParameters = "email=" + java.net.URLEncoder.encode(email, "UTF-8") + 
                                   "&password=" + java.net.URLEncoder.encode(password, "UTF-8");

            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(urlParameters.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println(">>> Auth Service Response Code: " + responseCode);

            if (responseCode == 200) {
                // Read JSON Response with UTF-8 encoding
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                
                String jsonResponse = content.toString();
                System.out.println(">>> Auth Service Response: " + jsonResponse);
                
                // Manual JSON Parsing (Naive)
                // Expected: {"success": true, "user": {"id": 1, "email": "...", "role": "ADMIN", ...}}
                if (jsonResponse.contains("\"success\": true")) {
                    // Extract ID
                    int id = Integer.parseInt(extractJsonValue(jsonResponse, "\"id\":"));
                    String roleName = extractJsonString(jsonResponse, "\"role\":");
                    String fName = extractJsonString(jsonResponse, "\"firstName\":");
                    String lName = extractJsonString(jsonResponse, "\"lastName\":");
                    
                    // Create User Object for Session
                    User user = new User();
                    user.setId(id);
                    user.setEmail(email);
                    user.setRoleName(roleName);
                    user.setFirstName(fName);
                    user.setLastName(lName);
                    
                    String phone = extractJsonString(jsonResponse, "\"phone\":");
                    user.setPhone(phone);
                    // Note: RoleID matching might be needed for other parts of app, 
                    // but RoleName is often used for redirection.
                    
                    System.out.println(">>> DEBUG: Remote Login SUCCESS for " + email + " Role: " + roleName);
                    HttpSession session = request.getSession();
                    session.setAttribute("user", user);

                    // Redirect based on Role
                    if ("ADMIN".equalsIgnoreCase(roleName)) {
                        response.sendRedirect("admin-dashboard.jsp");
                    } else if ("DESIGNER".equalsIgnoreCase(roleName)) {
                        response.sendRedirect("designer-dashboard.jsp");
                    } else {
                        response.sendRedirect("index.jsp");
                    }
                } else {
                     response.sendRedirect("login.jsp?error=Invalid Credentials (Auth)");
                }

            } else {
                System.out.println(">>> DEBUG: Login FAILED (Remote " + responseCode + ")");
                response.sendRedirect("login.jsp?error=Invalid Credentials");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("login.jsp?error=System Error: "
                    + java.net.URLEncoder.encode(e.toString(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    // Helper to extract numeric value
    private String extractJsonValue(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) return "0";
        start += key.length();
        while (Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
        return json.substring(start, end);
    }

    // Helper to extract string value
    private String extractJsonString(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) return "";
        start = json.indexOf("\"", start + key.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}


