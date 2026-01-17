package com.dottstudio.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestFetchDesigners {
    public static void main(String[] args) {
        try {
            System.out.println("--- Fetching Designers JSON ---");
            URL url = new URL("http://localhost:8080/dott/api/designers?type=all");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String json = response.toString();
                System.out.println("JSON Response: " + json);

                // Simple parsing check for Harry
                if (json.contains("\"MaxProjects\":3") || json.contains("\"maxProjects\":3")) {
                    System.out.println("SUCCESS: Found maxProjects:3 in JSON");
                } else {
                    System.out.println("FAILURE: Did not find maxProjects:3");
                }
            } else {
                System.out.println("GET request failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
