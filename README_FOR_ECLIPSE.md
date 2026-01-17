# How to Import into Eclipse (Tomcat 10)

This project is configured as a standard Eclipse **Dynamic Web Project** (version 5.0) for **Java 17** and **Apache Tomcat 10.1**.

## Prerequisites
1.  **Eclipse IDE for Enterprise Java and Web Developers** (2022-03 or newer recommended).
2.  **Java JDK 17** installed.
3.  **Apache Tomcat 10.1** installed (Tomcat 10 is required for `jakarta` namespace support).

## Import Steps
1.  Open Eclipse.
2.  Go to **File > Open Projects from File System...**.
3.  Click **Directory...** and select the **root folder** of this project (the folder containing this README).
4.  Click **Finish**.
    *   Eclipse should recognize it as a Dynamic Web Project.
    *   It may take a moment to build.

## Server Setup
1.  In Eclipse, go to the **Servers** view (Window > Show View > Servers).
2.  Click the link to **create a new server**.
3.  Select **Apache > Tomcat v10.1 Server**.
4.  Point to your local Tomcat 10 installation directory.
5.  Click **Next**.
6.  Add **BookingSystem** to the "Configured" resources list on the right.
7.  Click **Finish**.

## Running the Project
1.  Right-click the project in the **Project Explorer**.
2.  Select **Run As > Run on Server**.
3.  Choose your configured Tomcat 10 server.
4.  The application should open in the internal browser at `http://localhost:8080/BookingSystem/`.

## Troubleshooting
*   **"jakarta cannot be resolved"**: Make sure you are targeting **Tomcat 10.1** (or 10.0) in the Project Properties > Targeted Runtimes.
*   **Database Errors**: ensure the `WebContent/WEB-INF/lib/ojdbc11.jar` is on the build path (it should be automatically).
