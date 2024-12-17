package com.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/ForgotPasswordServlet")
public class ForgotPasswordServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/sakila";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Faheem@04";
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordServlet.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ServletException("MySQL JDBC Driver not found.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "validateEmail":
                    validateEmail(request, response);
                    break;
                case "validateCaptcha":
                    validateCaptcha(request, response);
                    break;
                case "updatePassword":
                    updatePassword(request, response);
                    break;
                default:
                    response.getWriter().write("invalid");
            }
        } else {
            response.getWriter().write("invalid");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("getCaptcha".equals(action)) {
            generateCaptchaImage(request, response);
        } else {
            response.getWriter().write("invalid");
        }
    }

    private boolean checkEmailInDatabase(String email) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String query = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
            return false;
        }
    }

    private void validateEmail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");

        if (email == null || email.isEmpty()) {
            response.getWriter().write("invalid");
            return;
        }

        boolean emailExists = checkEmailInDatabase(email);

        if (emailExists) {
            String captchaText = generateCaptcha(6);
//            request.getSession().setAttribute("captchaText", captchaText);
            LOGGER.log(Level.INFO, "Generated CAPTCHA: {0}", captchaText);
            response.getWriter().write("valid");
        } else {
            response.getWriter().write("invalid");
        }
    }

    private void validateCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String userCaptchaInput = request.getParameter("captcha");
        String sessionCaptcha = (String) request.getSession().getAttribute("captchaText");

        LOGGER.log(Level.INFO, "User CAPTCHA input: {0}", userCaptchaInput);
        LOGGER.log(Level.INFO, "Session CAPTCHA text: {0}", sessionCaptcha);

        // CAPTCHA validation
        if (sessionCaptcha == null || !sessionCaptcha.equals(userCaptchaInput)) {
            request.setAttribute("error", "Invalid CAPTCHA");
            request.getRequestDispatcher("/WEB-INF/views/forgotPassword.html").forward(request, response);
            return;
        }

        // CAPTCHA is valid, remove the session attribute
//        request.getSession().removeAttribute("captchaText");

        response.getWriter().write("valid");
    }


    private void updatePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String newPassword = request.getParameter("newPassword");

        if (email == null || newPassword == null || newPassword.length() < 6) {
            response.getWriter().write("invalid");
            return;
        }

        if (updatePasswordInDatabase(email, newPassword)) {
            response.getWriter().write("success");
        } else {
            response.getWriter().write("error");
        }
    }

    private boolean updatePasswordInDatabase(String email, String newPassword) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String query = "UPDATE users SET password = ? WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newPassword); // Ideally, hash the password before storing it
                stmt.setString(2, email);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error: {0}", e.getMessage());
            return false;
        }
    }

    private String generateCaptcha(int length) {
        String captchaChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder captcha = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(captchaChars.length());
            captcha.append(captchaChars.charAt(index));
        }

        return captcha.toString();
    }

    private void generateCaptchaImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int width = 150;
        int height = 50;

        char[] data = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        Font font = new Font("Georgia", Font.BOLD, 18);
        g2d.setFont(font);

        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHints(rh);

        GradientPaint gp = new GradientPaint(0, 0, Color.green, 0, height / 2, Color.black, true);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(new Color(255, 153, 0));

        Random r = new Random();
        StringBuilder captcha = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int index = r.nextInt(data.length);
            captcha.append(data[index]);
        }

        request.getSession().setAttribute("captchaText", captcha.toString());

        int x = 10;
        int y = 25;

        for (int i = 0; i < captcha.length(); i++) {
            x += 15 + (r.nextInt(10));
            y = 25 + r.nextInt(10) - 5;
            g2d.drawChars(captcha.toString().toCharArray(), i, 1, x, y);
        }

        g2d.dispose();

        response.setContentType("image/png");
        ImageIO.write(bufferedImage, "png", response.getOutputStream());
    }
}
