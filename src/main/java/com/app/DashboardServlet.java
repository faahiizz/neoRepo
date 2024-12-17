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

@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get or create session
        HttpSession session = request.getSession(true);
        String username = (session != null) ? (String) session.getAttribute("username") : null;

        if (username == null) {
            out.print("{\"message\": \"Session expired. Please log in again.\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        handleDashboardRequest(request, response, username, out, session);
    }

    private void handleDashboardRequest(HttpServletRequest request, HttpServletResponse response,
                                        String username, PrintWriter out, HttpSession session) throws IOException {
        String month = request.getParameter("month");
        String incomeStr = request.getParameter("income");

        if (month == null || incomeStr == null) {
            out.print("{\"message\": \"Missing parameters.\"}");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        double income;
        try {
            income = Double.parseDouble(incomeStr);
        } catch (NumberFormatException e) {
            out.print("{\"message\": \"Invalid income format.\"}");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        double totalExpenses = 0.0; // You might want to get this from some other source
        double savings = income - totalExpenses;

        Connection conn = null;
        PreparedStatement pstmtCheckUser = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");

            // Check if the user exists
            String checkUserQuery = "SELECT * FROM users WHERE username = ?";
            pstmtCheckUser = conn.prepareStatement(checkUserQuery);
            pstmtCheckUser.setString(1, username);
            rs = pstmtCheckUser.executeQuery();

            if (!rs.next()) {
                out.print("{\"message\": \"Invalid user. Please log in again.\"}");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            // Store the data in the session
            session.setAttribute("month", month);
            session.setAttribute("income", income);
            session.setAttribute("totalExpenses", totalExpenses);
            session.setAttribute("savings", savings);

            out.print("{\"message\": \"Data processed successfully!\"}");

        } catch (ClassNotFoundException | SQLException e) {
            out.print("{\"message\": \"Data processing failed due to an error.\"}");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmtCheckUser != null) pstmtCheckUser.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
