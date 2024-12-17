
package com.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/AccountcreationServlet")
public class AccountcreationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle GET requests if needed, currently not used in your example
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String firstname = request.getParameter("firstname");
        String lastname = request.getParameter("lastname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword"); // Assuming there's a confirm password field

        // Concatenate firstname and lastname
        String name = (firstname != null ? firstname : "") + " " + (lastname != null ? lastname : "");

        // JDBC connection and insert logic
        try {
            // Load JDBC driver (optional for newer JDBC drivers)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04")) {
                // Prepare insert statement
                String query = "INSERT INTO users (name, username, email, phone, password) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, username);
                    pstmt.setString(3, email);
                    pstmt.setString(4, phone);
                    pstmt.setString(5, password);

                    // Execute update
                    int rows = pstmt.executeUpdate();

                    if (rows > 0) {
                        // Account creation successful, redirect to login page
                        out.print("<h1>Registration Successful</h1><br>");
                        response.sendRedirect("login.html");
                        return; // Ensure no further output after redirect
                    } else {
                        out.print("<h1>Registration Failed</h1><br>");
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            out.print("<h1>Registration Failed due to an error</h1><br>");
            e.printStackTrace(); // Log exception for debugging
        }
    }
}
