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
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProfileServlet.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        System.out.println(username);
        LOGGER.log(Level.INFO, "Received username: {0}", username); // Log the received username
        JSONObject userData = new JSONObject();

        String jdbcURL = "jdbc:mysql://localhost:3306/sakila";
        String dbUser = "root";
        String dbPassword = "Faheem@04";

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "SELECT name, username, email, phone FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    userData.put("name", resultSet.getString("name"));
                    userData.put("username", resultSet.getString("username"));
                    userData.put("email", resultSet.getString("email"));
                    userData.put("phone", resultSet.getString("phone"));
                } else {
                    LOGGER.log(Level.WARNING, "No user found with username: {0}", username);
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database access error", e);
            throw new ServletException("Database access error", e);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(userData.toString());
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");

        String jdbcURL = "jdbc:mysql://localhost:3306/sakila";
        String dbUser = "root";
        String dbPassword = "Faheem@04";

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            String sql = "UPDATE users SET name = ?, email = ?, phone = ? WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setString(2, email);
                statement.setString(3, phone);
                statement.setString(4, username);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    LOGGER.log(Level.INFO, "Profile updated successfully for username: {0}", username);
                } else {
                    LOGGER.log(Level.WARNING, "No user found with username: {0}", username);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database access error", e);
            throw new ServletException("Database access error", e);
        }

        response.sendRedirect("Profile.html?username=" + username);
    }
}
