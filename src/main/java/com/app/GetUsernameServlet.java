package com.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/GetUsernameServlet")
public class GetUsernameServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        
        HttpSession session = request.getSession(false);

        try (PrintWriter out = response.getWriter()) {
            if (session != null) {
                String username = (String) session.getAttribute("username");

                if (username != null) {
                    // Database connection details
                    String url = "jdbc:mysql://localhost:3306/sakila";
                    String user = "root";
                    String password = "Faheem@04";

                    // SQL query to get the name and username
                    String query = "SELECT name, username FROM users WHERE username = ?";
                    
                    try (Connection con = DriverManager.getConnection(url, user, password);
                         PreparedStatement ps = con.prepareStatement(query)) {
                         
                        ps.setString(1, username);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String name = rs.getString("name");
                                String fetchedUsername = rs.getString("username");

                                out.print("{\"name\":\"" + name + "\", \"username\":\"" + fetchedUsername + "\"}");
                            } else {
                                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                out.print("{\"error\":\"User not found\"}");
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.print("{\"error\":\"Database error\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"No username found\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"No session found\"}");
            }
        }
    }
}
