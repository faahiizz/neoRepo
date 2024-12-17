package com.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.mysql.cj.jdbc.result.ResultSetMetaData;

public class Utilities {

	// Database URL, username, and passwords
	private static final String DB_URL = "jdbc:mysql://localhost:3306/sakila";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "Faheem@04";

	// Password Related Parameter
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES";
	private static final String SECRET_KEY_STRING = "s3cr3tK3yForAES!"; // 16-byte key for AES-128

	// Method to get a database connection
	public static Connection getConnection() {
		Connection connection = null;
		try {
			// Load the JDBC driver (MySQL example)
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Establish the connection
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			System.out.println("Connection Done.!");
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
			System.out.println("Connection Failed.!");
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
			System.out.println("Connection Failed.!");
		}
		return connection;
	}

	// Method to execute SELECT queries
	public static ResultSet executeSelect(String query) {
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			// method call
			connection = getConnection();
			if (connection != null) {
				statement = (Statement) connection.createStatement();
				resultSet = ((java.sql.Statement) statement).executeQuery(query);
			}
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}

		return resultSet;
	}

	// Method to execute UPDATE queries
	public static boolean executeUpdate(String query) {
		try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {

			int rowsAffected = statement.executeUpdate(query);
			return rowsAffected > 0;

		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
			return false;
		}
	}

	// Method to execute INSERT queries
	public static boolean executeInsert(String query) {
		return executeUpdate(query);
	}

	// Method to execute DELETE queries
	public static boolean executeDelete(String query) {
		return executeUpdate(query);
	}

	// Method to execute SELECT queries and return column names
	public static List<String> getColumnNames(String query) {
		List<String> columnNames = new ArrayList<>();

		try (Connection connection = getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query)) {

			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				columnNames.add(metaData.getColumnName(i));
			}

		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}

		return columnNames;
	}
	
	//Method To Change Password
	public static boolean changePassword(String username, String currentPassword, String newPassword) throws SQLException {
        boolean isSuccess = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
        	Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection to the database
             conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");
           
            // Verify current password
            String verifyQuery = "SELECT password FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(verifyQuery);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(currentPassword)) {
                    // Update to new password
                    String updateQuery = "UPDATE users SET password = ? WHERE username = ?";
                    pstmt = conn.prepareStatement(updateQuery);
                    pstmt.setString(1, newPassword); // Consider hashing the password before storing it
                    pstmt.setString(2, username);
                    int rowsUpdated = pstmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        isSuccess = true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
        	if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close(); // Assume this method closes the connection, statement, and result set
        }

        return isSuccess;
    }
	

	// Vaidate User using username & password-Modify your table names & column names
	public static boolean validateUser(String userName, String password) {
		try {
			String strQuery = "SELECT username,password FROM users where username='" + userName + "'";
			ResultSet rs = executeSelect(strQuery);
			if (rs.next()) {
				String dBUsername = rs.getString("username").equals("") ? "-"
						: rs.getString("username").toString().trim();
				String dBPassword = rs.getString("password").equals("") ? "-"
						: rs.getString("password").toString().trim();
				if (userName.equals(dBUsername) && password.equals(dBPassword)) {
					System.out.println("Validation Done.!");
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// Convert the secret key string to a SecretKey object
	private static SecretKey getSecretKey() {
		byte[] decodedKey = SECRET_KEY_STRING.getBytes();
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
	}

	// Encrypt a password using AES
	public static String encrypt(String password) throws Exception {
		SecretKey secretKey = getSecretKey();
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		byte[] encryptedBytes = cipher.doFinal(password.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	// Decrypt an encrypted password using AES
	public static String decrypt(String encryptedPassword) throws Exception {
		SecretKey secretKey = getSecretKey();
		Cipher cipher = Cipher.getInstance(TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
		byte[] decryptedBytes = cipher.doFinal(decodedBytes);
		return new String(decryptedBytes);
	}

	// Method to store an image into the database
	public static boolean storeImage(String imageName, byte[] imageData) {
		String query = "INSERT INTO imgtable (image_name, image_data) VALUES (?, ?)";

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(query)) {

			statement.setString(1, imageName);
			statement.setBytes(2, imageData);

			int rowsAffected = statement.executeUpdate();
			return rowsAffected > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	// Method to convert image path to byte data
	public static byte[] getImageBytes(String imagePath) {
		File imageFile = new File(imagePath);
		byte[] imageData = null;

		try (FileInputStream fis = new FileInputStream(imageFile)) {
			imageData = new byte[(int) imageFile.length()];
			fis.read(imageData);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return imageData;
	}

	// Method to execute a Any SELECT query and return HTML table
	public static String getHtmlTable(String query) {
		StringBuilder htmlTable = new StringBuilder();
		List<String> columnNames = getColumnNames(query);

		try (Connection connection = getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query)) {

			// Start the HTML table
			htmlTable.append("<table border='1' style='width:100%; border-collapse:collapse;'>");

			// Table header
			htmlTable.append("<thead><tr>");
			for (String columnName : columnNames) {
				htmlTable.append("<th style='background-color:#f2f2f2; padding:8px;'>").append(columnName)
						.append("</th>");
			}
			htmlTable.append("</tr></thead>");

			// Table body
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr>");
				for (String columnName : columnNames) {
					htmlTable.append("<td style='padding:8px;'>").append(resultSet.getString(columnName))
							.append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody>");

			// End the HTML table
			htmlTable.append("</table>");

		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}

		return htmlTable.toString();
	}

	// Method to convert ResultSet to HTML table with specific styles 1
	public static String applyStyles1(ResultSet resultSet) {
		StringBuilder htmlTable = new StringBuilder();

		try {
			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			// Start the HTML table
			htmlTable.append("<table border='1' style='width:100%; border-collapse:collapse;'>");

			// Table header
			htmlTable.append("<thead><tr style='background-color:#87CEEB;'>");
			for (int i = 1; i <= columnCount; i++) {
				htmlTable.append("<th style='padding:8px; border:1px solid #ddd;'>").append(metaData.getColumnName(i))
						.append("</th>");
			}
			htmlTable.append("</tr></thead>");

			// Table body
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr>");
				for (int i = 1; i <= columnCount; i++) {
					htmlTable.append("<td style='padding:8px; border:1px solid #ddd;'>").append(resultSet.getString(i))
							.append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody>");

			// End the HTML table
			htmlTable.append("</table>");

		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return htmlTable.toString();
	}

	// Method to convert ResultSet to HTML table with Style 2
	public static String applyStyles2(ResultSet resultSet) {
		StringBuilder htmlTable = new StringBuilder();
		try {
			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			htmlTable.append("<table style='width:100%; border-collapse:collapse; border: 1px solid #ddd;'>");
			htmlTable.append("<thead style='background-color:#4CAF50; color:white;'>");
			htmlTable.append("<tr>");
			for (int i = 1; i <= columnCount; i++) {
				htmlTable.append("<th style='padding: 8px; text-align: left; border: 1px solid #ddd;'>")
						.append(metaData.getColumnName(i)).append("</th>");
			}
			htmlTable.append("</tr></thead>");
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr style='border-bottom: 1px solid #ddd;'>");
				for (int i = 1; i <= columnCount; i++) {
					htmlTable.append("<td style='padding: 8px; text-align: left; border: 1px solid #ddd;'>")
							.append(resultSet.getString(i)).append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody></table>");
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return htmlTable.toString();
	}

	// Method to convert ResultSet to HTML table with Style 3
	public static String applyStyles3(ResultSet resultSet) {
		StringBuilder htmlTable = new StringBuilder();
		try {
			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			htmlTable.append("<table style='width:100%; border-collapse:collapse;'>");
			htmlTable.append("<thead style='background-color:#007BFF; color:white;'>");
			htmlTable.append("<tr>");
			for (int i = 1; i <= columnCount; i++) {
				htmlTable.append("<th style='padding: 12px; text-align: left; border-bottom: 2px solid #ddd;'>")
						.append(metaData.getColumnName(i)).append("</th>");
			}
			htmlTable.append("</tr></thead>");
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr style='border-bottom: 1px solid #ddd;'>");
				for (int i = 1; i <= columnCount; i++) {
					htmlTable.append("<td style='padding: 12px; text-align: left; border-bottom: 1px solid #ddd;'>")
							.append(resultSet.getString(i)).append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody></table>");
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return htmlTable.toString();
	}

	// Method to convert ResultSet to HTML table with Style 4
	public static String applyStyles4(ResultSet resultSet) {
		StringBuilder htmlTable = new StringBuilder();
		try {
			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			htmlTable.append(
					"<table style='width:100%; border-collapse:collapse; font-family:Tahoma, Geneva, sans-serif;'>");
			htmlTable.append("<thead style='background-color:#FFC107; color:black;'>");
			htmlTable.append("<tr>");
			for (int i = 1; i <= columnCount; i++) {
				htmlTable.append("<th style='padding: 15px; text-align: left; border: 1px solid #ccc;'>")
						.append(metaData.getColumnName(i)).append("</th>");
			}
			htmlTable.append("</tr></thead>");
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr style='border-bottom: 1px solid #ccc;'>");
				for (int i = 1; i <= columnCount; i++) {
					htmlTable.append("<td style='padding: 15px; text-align: left; border: 1px solid #ccc;'>")
							.append(resultSet.getString(i)).append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody></table>");

			// Add some additional styles and hover effects
			htmlTable.append("<style>").append("table { border: 1px solid #ccc; margin: 20px 0; }")
					.append("thead th { background-color: #FFC107; color: black; }")
					.append("tbody tr:hover { background-color: #f9f9f9; }")
					.append("tbody tr:nth-child(even) { background-color: #fff; }")
					.append("tbody tr:nth-child(odd) { background-color: #f2f2f2; }")
					.append("th, td { padding: 15px; text-align: left; border: 1px solid #ccc; }").append("</style>");
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return htmlTable.toString();
	}

	// Method to convert ResultSet to HTML table with Style 5
	public static String applyStyles5(ResultSet resultSet) {
		StringBuilder htmlTable = new StringBuilder();
		try {
			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			htmlTable.append("<table style='width:100%; border-collapse:collapse; font-family:Arial, sans-serif;'>");
			htmlTable.append("<thead style='background-color:#17A2B8; color:white;'>");
			htmlTable.append("<tr>");
			for (int i = 1; i <= columnCount; i++) {
				htmlTable.append("<th style='padding: 15px; text-align: left; border: 1px solid #ddd;'>")
						.append(metaData.getColumnName(i)).append("</th>");
			}
			htmlTable.append("</tr></thead>");
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr style='border-bottom: 1px solid #ddd; transition: background-color 0.3s;'>");
				for (int i = 1; i <= columnCount; i++) {
					htmlTable.append("<td style='padding: 15px; text-align: left; border: 1px solid #ddd;'>")
							.append(resultSet.getString(i)).append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody></table>");

			// Add some additional styles and hover effects
			htmlTable.append("<style>").append("table { box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); }")
					.append("thead th { background-color: #17A2B8; color: white; }")
					.append("tbody tr:hover { background-color: #f1f1f1; }")
					.append("tbody tr:nth-child(even) { background-color: #f9f9f9; }")
					.append("th, td { padding: 15px; text-align: left; border: 1px solid #ddd; }").append("</style>");
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return htmlTable.toString();
	}

	// Method to convert ResultSet to HTML table with Style 6
	public static String applyStyles6(ResultSet resultSet) {
		StringBuilder htmlTable = new StringBuilder();
		try {
			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			htmlTable.append(
					"<table style='width:100%; border-collapse:collapse; font-family:Verdana, Geneva, sans-serif;'>");
			htmlTable.append("<thead style='background-color:#4CAF50; color:white;'>");
			htmlTable.append("<tr>");
			for (int i = 1; i <= columnCount; i++) {
				htmlTable.append("<th style='padding: 10px; text-align: center; border: 1px solid #4CAF50;'>")
						.append(metaData.getColumnName(i)).append("</th>");
			}
			htmlTable.append("</tr></thead>");
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr style='border-bottom: 1px solid #ddd;'>");
				for (int i = 1; i <= columnCount; i++) {
					htmlTable.append("<td style='padding: 10px; text-align: center; border: 1px solid #ddd;'>")
							.append(resultSet.getString(i)).append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody></table>");

			// Add some additional styles and hover effects
			htmlTable.append("<style>").append("table { border: 1px solid #ddd; margin: 20px 0; }")
					.append("thead th { background-color: #4CAF50; color: white; }")
					.append("tbody tr:hover { background-color: #f1f1f1; }")
					.append("tbody tr:nth-child(even) { background-color: #f2f2f2; }")
					.append("th, td { padding: 10px; text-align: center; border: 1px solid #ddd; }").append("</style>");
		} catch (SQLException e) {
			System.err.println("SQL Exception: " + e.getMessage());
		}
		return htmlTable.toString();
	}

	// Alert message from servlet to JSP(Alert from Backend)
	public static String generateAlertMessage(String message) {
		StringBuilder htmlContent = new StringBuilder();
		htmlContent.append("<html><body>");
		htmlContent.append("<script type='text/javascript'>");
		htmlContent.append("alert('").append(message).append("');");
		htmlContent.append("</script>");
		htmlContent.append("<h2>Servlet with Alert Message</h2>");
		htmlContent.append("</body></html>");
		return htmlContent.toString();
	}

	// Method to get the current date in a specified format
	public static String getCurrentDate(String format) {
		// Define the format patterns
		Map<String, String> formatPatterns = new HashMap<>();
		formatPatterns.put("ddmmyyyy", "ddMMyyyy");
		formatPatterns.put("mmddyyyy", "MMddyyyy");
		formatPatterns.put("yyyyddmm", "yyyyddMM");
		formatPatterns.put("yyyy-mm-dd", "yyyy-MM-dd");
		formatPatterns.put("yyyy/mm/dd", "yyyy/MM/dd");
		formatPatterns.put("dd-mm-yyyy", "dd-MM-yyyy");
		formatPatterns.put("mm-dd-yyyy", "MM-dd-yyyy");
		formatPatterns.put("dd/mm/yyyy", "dd/MM/yyyy");

		// Default format pattern
		String pattern = formatPatterns.getOrDefault(format.toLowerCase(), "ddMMyyyy");

		// Get the current date
		LocalDate currentDate = LocalDate.now();

		// Format the date
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(pattern);
		return currentDate.format(dateFormat);
	}

	// Method to get the current time in a specified format
	public static String getCurrentTime(String format) {
		// Define the format patterns
		Map<String, String> formatPatterns = new HashMap<>();
		formatPatterns.put("hhmmss", "HHmmss");
		formatPatterns.put("hh:mm:ss", "HH:mm:ss");
		formatPatterns.put("hh-mm-ss", "HH-mm-ss");
		formatPatterns.put("hh:mm a", "hh:mm a");
		formatPatterns.put("hh:mm:ss a", "hh:mm:ss a");

		// Default format pattern
		String pattern = formatPatterns.getOrDefault(format.toLowerCase(), "HHmmss");

		// Get the current time
		LocalTime currentTime = LocalTime.now();

		// Format the time
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(pattern);
		return currentTime.format(timeFormat);
	}

	// Read the contets of File Using file path
	public static String readFileAsString(String filePath) {
		StringBuilder content = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				content.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	// Validate Email-ID
	public static boolean isValidEmail(String email) {
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pat = Pattern.compile(emailRegex);
		if (email == null)
			return false;
		return pat.matcher(email).matches();
	}

	// Validate Phone Number
	public static boolean isValidIndianPhoneNumber(String phoneNumber) {
		String phoneRegex = "^[789]\\d{9}$";
		Pattern pat = Pattern.compile(phoneRegex);
		if (phoneNumber == null) {
			return false;
		}
		return pat.matcher(phoneNumber).matches();
	}

	// Validate Its a Strong Password Or Not
	public static boolean isStrongPassword(String password) {
		if (password.length() < 8)
			return false;
		boolean hasLetter = false;
		boolean hasDigit = false;
		for (char c : password.toCharArray()) {
			if (Character.isLetter(c)) {
				hasLetter = true;
			} else if (Character.isDigit(c)) {
				hasDigit = true;
			}
			if (hasLetter && hasDigit) {
				return true;
			}
		}
		return false;
	}

	// Captcha Generation
	public static String generateCaptcha(int length) {
		String captchaChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder captcha = new StringBuilder(length);
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(captchaChars.length());
			captcha.append(captchaChars.charAt(index));
		}

		return captcha.toString();
	}

	// Generate OTP: Just by giving number of digits OTP you want.?
	public static String generateOTP(int length) {
		String digits = "0123456789";
		StringBuilder otp = new StringBuilder(length);
		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int index = random.nextInt(digits.length());
			otp.append(digits.charAt(index));
		}

		return otp.toString();
	}

	// URL Validation
	public static boolean isValidURL(String url) {
		String urlRegex = "^(http|https)://[^\\s/$.?#].[^\\s]*$";
		Pattern pat = Pattern.compile(urlRegex);
		if (url == null) {
			return false;
		}
		return pat.matcher(url).matches();
	}

	// Validate Username
	public static boolean isValidUsername(String username) {
		String usernameRegex = "^[a-zA-Z0-9_]{3,15}$";
		Pattern pat = Pattern.compile(usernameRegex);
		if (username == null) {
			return false;
		}
		return pat.matcher(username).matches();
	}

	// Date Validation
	public static boolean isValidDate(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			sdf.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	// Method to execute a paginated SELECT query and return HTML table
	public static String getPaginatedHtmlTable(String query, int page, int pageSize) {
		StringBuilder htmlTable = new StringBuilder();
		List<String> columnNames = new ArrayList<>();
		String paginatedQuery = query + " LIMIT " + (page - 1) * pageSize + ", " + pageSize;

		try (Connection connection = getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(paginatedQuery)) {

			ResultSetMetaData metaData = (ResultSetMetaData) resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				columnNames.add(metaData.getColumnName(i));
			}

			htmlTable.append("<table border='1' style='width:100%; border-collapse:collapse;'>");
			htmlTable.append("<thead><tr>");
			for (String columnName : columnNames) {
				htmlTable.append("<th style='background-color:#f2f2f2; padding:8px;'>").append(columnName)
						.append("</th>");
			}
			htmlTable.append("</tr></thead>");
			htmlTable.append("<tbody>");
			while (resultSet.next()) {
				htmlTable.append("<tr>");
				for (String columnName : columnNames) {
					htmlTable.append("<td style='padding:8px;'>").append(resultSet.getString(columnName))
							.append("</td>");
				}
				htmlTable.append("</tr>");
			}
			htmlTable.append("</tbody>");
			htmlTable.append("</table>");

			// Add pagination controls
			htmlTable.append("<form method='post' action='loginServlet'>");
			if (page > 1) {
				htmlTable.append("<button type='submit' name='page' value='").append(page - 1)
						.append("'>Previous</button> ");
			}
			htmlTable.append("<span>Page ").append(page).append("</span>");
			htmlTable.append(" <button type='submit' name='page' value='").append(page + 1).append("'>Next</button>");
			htmlTable.append("</form>");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return htmlTable.toString();
	}

}
