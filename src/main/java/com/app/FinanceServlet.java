
package com.app;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

@WebServlet("/FinanceServlet")
public class FinanceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(false);
		String username = (session != null) ? (String) session.getAttribute("username") : null;

		if (username == null) {
			out.print("{\"message\": \"Session expired. Please log in again.\"}");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String action = request.getParameter("action");
		String financeIdStr = request.getParameter("financeId");

		if ("getExpenses".equals(action) && financeIdStr != null) {
			int financeId;
			try {
				financeId = Integer.parseInt(financeIdStr);
			} catch (NumberFormatException e) {
				out.print("{\"message\": \"Invalid finance ID.\"}");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");

				String query = "SELECT expense_name AS category, expense_amount AS amount FROM expenses WHERE finance_id = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, financeId);
				rs = pstmt.executeQuery();

				List<Expense> expenseList = new ArrayList<>();
				while (rs.next()) {
					Expense expense = new Expense();
					expense.setCategory(rs.getString("category"));
					expense.setAmount(rs.getDouble("amount"));
					expenseList.add(expense);
				}

				Gson gson = new Gson();
				String jsonResponse = gson.toJson(expenseList);
				out.print(jsonResponse);
				response.setStatus(HttpServletResponse.SC_OK);

			} catch (ClassNotFoundException | SQLException e) {
				out.print("{\"message\": \"Error fetching expenses.\"}");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (pstmt != null)
						pstmt.close();
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else if ("deleteExpenses".equals(action) && financeIdStr != null) {
			int financeId;
			try {
				financeId = Integer.parseInt(financeIdStr);
			} catch (NumberFormatException e) {
				out.print("{\"message\": \"Invalid finance ID.\"}");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			Connection conn = null;
			PreparedStatement pstmt = null;

			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");

				String query = "DELETE FROM expenses WHERE finance_id = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, financeId);

				int rowsAffected = pstmt.executeUpdate();

				if (rowsAffected > 0) {

					String queryNeo = "DELETE FROM finance_data WHERE id = " + financeId;
					boolean isDeleted = Utilities.executeDelete(queryNeo);
					if (isDeleted) {
						
						out.print("{\"message\": \"Expenses deleted successfully.\"}");
					}

				} else {
					out.print("{\"message\": \"No expenses found for the specified finance ID.\"}");
				}
				response.setStatus(HttpServletResponse.SC_OK);

			} catch (ClassNotFoundException | SQLException e) {
				out.print("{\"message\": \"Error deleting expenses.\"}");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			} finally {
				try {
					if (pstmt != null)
						pstmt.close();
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			// Handle other GET requests (e.g., fetching finance data)
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;

			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");

				String query = "SELECT id, month, income, total_expenses, savings FROM finance_data WHERE username = ?";
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, username);
				rs = pstmt.executeQuery();

				List<FinanceData> financeDataList = new ArrayList<>();
				while (rs.next()) {
					FinanceData financeData = new FinanceData();
					financeData.setId(rs.getInt("id"));
					financeData.setMonth(rs.getString("month"));
					financeData.setIncome(rs.getDouble("income"));
					financeData.setTotalExpenses(rs.getDouble("total_expenses"));
					financeData.setSavings(rs.getDouble("savings"));
					financeDataList.add(financeData);
				}

				Gson gson = new Gson();
				String jsonResponse = gson.toJson(financeDataList);
				out.print(jsonResponse);
				response.setStatus(HttpServletResponse.SC_OK);

			} catch (ClassNotFoundException | SQLException e) {
				out.print("{\"message\": \"Error fetching finance data.\"}");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (pstmt != null)
						pstmt.close();
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();

		HttpSession session = request.getSession(false);
		String username = (session != null) ? (String) session.getAttribute("username") : null;

		if (username == null) {
			out.print("{\"message\": \"Session expired. Please log in again.\"}");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = request.getReader()) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}

		Gson gson = new Gson();
		FinanceData financeData = gson.fromJson(sb.toString(), FinanceData.class);

		if (financeData.getMonth() == null || financeData.getIncome() <= 0 || financeData.getTotalExpenses() <= 0
				|| financeData.getSavings() < 0) {
			out.print("{\"message\": \"Invalid financial data.\"}");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Connection conn = null;
		PreparedStatement pstmtFinance = null;
		PreparedStatement pstmtExpenses = null;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sakila", "root", "Faheem@04");

			String financeQuery = "INSERT INTO finance_data (username, month, income, total_expenses, savings) VALUES (?, ?, ?, ?, ?)";
			pstmtFinance = conn.prepareStatement(financeQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			pstmtFinance.setString(1, username);
			pstmtFinance.setString(2, financeData.getMonth());
			pstmtFinance.setDouble(3, financeData.getIncome());
			pstmtFinance.setDouble(4, financeData.getTotalExpenses());
			pstmtFinance.setDouble(5, financeData.getSavings());

			pstmtFinance.executeUpdate();

			ResultSet generatedKeys = pstmtFinance.getGeneratedKeys();
			int financeId = -1;
			if (generatedKeys.next()) {
				financeId = generatedKeys.getInt(1);
			}

			String expensesQuery = "INSERT INTO expenses (finance_id, expense_name, expense_amount) VALUES (?, ?, ?)";
			pstmtExpenses = conn.prepareStatement(expensesQuery);

			for (Expense expense : financeData.getExpenses()) {
				pstmtExpenses.setInt(1, financeId);
				pstmtExpenses.setString(2, expense.getCategory());
				pstmtExpenses.setDouble(3, expense.getAmount());
				pstmtExpenses.addBatch();
			}

			pstmtExpenses.executeBatch();

			out.print("{\"message\": \"Data saved successfully.\"}");
			response.setStatus(HttpServletResponse.SC_OK);

		} catch (ClassNotFoundException | SQLException e) {
			out.print("{\"message\": \"Data Saving Failed due to an error.\"}");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		} finally {
			try {
				if (pstmtFinance != null)
					pstmtFinance.close();
				if (pstmtExpenses != null)
					pstmtExpenses.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static class FinanceData {
		private int id;
		private String month;
		private double income;
		private double totalExpenses;
		private double savings;
		private List<Expense> expenses;

		// Getters and setters

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getMonth() {
			return month;
		}

		public void setMonth(String month) {
			this.month = month;
		}

		public double getIncome() {
			return income;
		}

		public void setIncome(double income) {
			this.income = income;
		}

		public double getTotalExpenses() {
			return totalExpenses;
		}

		public void setTotalExpenses(double totalExpenses) {
			this.totalExpenses = totalExpenses;
		}

		public double getSavings() {
			return savings;
		}

		public void setSavings(double savings) {
			this.savings = savings;
		}

		public List<Expense> getExpenses() {
			return expenses;
		}

		public void setExpenses(List<Expense> expenses) {
			this.expenses = expenses;
		}
	}

	public static class Expense {
		private String category;
		private double amount;

		// Getters and setters

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}
	}
}
