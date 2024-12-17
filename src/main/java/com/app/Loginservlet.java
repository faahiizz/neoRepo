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

@WebServlet("/Loginservlet")
public class Loginservlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Loginservlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String loginIdentifier = request.getParameter("loginIdentifier");
        String password = request.getParameter("password");

        if (loginIdentifier == null || loginIdentifier.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            out.print("<script type='text/javascript'>");
            out.print("alert('Username/Email/Phone and password cannot be empty.');");
            out.print("window.location = 'login.html';");
            out.print("</script>");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");

            String query = "SELECT * FROM users WHERE (username = ? OR email = ? OR phone = ?) AND password = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, loginIdentifier);
            pstmt.setString(2, loginIdentifier);
            pstmt.setString(3, loginIdentifier);
            pstmt.setString(4, password);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("username", rs.getString("username")); // Fetch the username from the result set
                
                response.sendRedirect("Dashboard.html");
            } else {
                out.print("<script type='text/javascript'>");
                out.print("alert('Login Failed. Please enter the correct username/email/phone number and password.');");
                out.print("window.location = 'login.html';");
                out.print("</script>");
            }

        } catch (ClassNotFoundException e) {
            out.print("<script type='text/javascript'>");
            out.print("alert('Login Failed due to server exception');");
            out.print("window.location.href = 'login.html';");
            out.print("</script>");
            e.printStackTrace(); // Print stack trace for debugging
        } catch (SQLException e) {
            out.print("<script type='text/javascript'>");
            out.print("alert('Login Failed due to database exception: " + e.getMessage() + "');");
            out.print("window.location.href = 'login.html';");
            out.print("</script>");
            e.printStackTrace(); // Print stack trace for debugging
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
