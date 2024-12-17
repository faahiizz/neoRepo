
// Get the button
       let mybutton = document.getElementById("backToTop");

       // When the user scrolls down 20px from the top of the document, show the button
       window.onscroll = function() {scrollFunction()};

       function scrollFunction() {
           if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
               mybutton.style.display = "block";
           } else {
               mybutton.style.display = "none";
           }
       }

       // When the user clicks on the button, scroll to the top of the document
       function topFunction() {
           document.body.scrollTop = 0; // For Safari
           document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
       }



function openDrawer() {
        document.getElementById("myDrawer").style.width = "250px";
        document.addEventListener('click', closeDrawerOnOutsideClick);
    }

    function closeDrawer() {
        document.getElementById("myDrawer").style.width = "0";
        document.removeEventListener('click', closeDrawerOnOutsideClick);
    }

    function closeDrawerOnOutsideClick(event) {
        if (!document.getElementById("myDrawer").contains(event.target) && !document.getElementById("drawer-button").contains(event.target)) {
            closeDrawer();
        }
    }


function saveExpenses() {
	       updateDashboard();
	       document.getElementById('charts-section').classList.remove('hidden');
	       document.getElementById('summary-section').classList.remove('hidden');
		   const month=document.getElementById('month').value;
		   const income=document.getElementById('income').value;

	       const expensesData = getExpensesData();
	       const emptyFields = [];

	       expensesData.forEach(expense => {
	           if (!expense.category || !expense.amount) {
	               emptyFields.push(expense);
	           }
	       });

	       if (emptyFields.length > 0) {
	           alert('Please fill in all expense fields before saving.');
	           return;
	       }

	       if (expensesData.length === 0) {
	           alert('No expenses to save.');
	           return;
	       }
		   expensesData.pu

	      /* // Send expensesData to your backend using fetch
	       fetch('DashboardServlet', {
	           method: 'POST',
	           headers: {
	               'Content-Type': 'application/json'
	           },
	           body: JSON.stringify({ expenses: expensesData })
	       })*/
	       .then(response => response.json())
	       .then(data => {
	           console.log('Expenses data:', data);
	           alert('Expenses saved successfully!');
	       })
	       .catch((error) => {
	           console.error('Error:', error);
	       });

	       // Optionally, log the expensesData to the console
	       console.log('Expenses data:', expensesData);
	   }

	   function fetchUsername() {
	       fetch('GetUsernameServlet')
	           .then(response => response.json())
	           .then(data => {
				if (data.username && data.name) {
	               document.getElementById('profile-button').setAttribute('data-username', data.username);
				   document.getElementById("usernameDisplay").innerText = data.name;
				   }else {
			       console.error("Error fetching user data:", data.error);
			      }
				   
	           })
	           .catch(error => console.error('Error fetching username:', error));
	   }


	   function viewProfile() {
	       const username = document.getElementById('profile-button').getAttribute('data-username');
	       if (username) {
	           window.location.href = `Profile.html?username=${encodeURIComponent(username)}`;
	       } else {
	           alert('Username not found. Please log in again.');
	       }
	   }


	const expenseChartCtx = document.getElementById('expenseChart').getContext('2d');
	   const incomeExpenseChartCtx = document.getElementById('incomeExpenseChart').getContext('2d');

	   let expenseChart = new Chart(expenseChartCtx, {
	       type: 'doughnut',
	       data: {
	           labels: [],
	           datasets: [{
	               data: [],
	               backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF']
	           }]
	       }
	   });

	   let incomeExpenseChart = new Chart(incomeExpenseChartCtx, {
	       type: 'bar',
	       data: {
	           labels: ['Income', 'Expenses'],
	           datasets: [{
	               label: 'Amount',
	               data: [0, 0],
	               backgroundColor: ['#4BC0C0', '#FF6384']
	           }]
	       }
	   });

	   function addExpense() {
	       const expensesDiv = document.getElementById('expenses');
	       const newExpense = document.createElement('div');
	       newExpense.className = 'input-group mb-3';
	       newExpense.innerHTML = `
	           <input type="text" class="form-control expense-category" placeholder="Expense Category">
	           <input type="number" class="form-control expense-amount" placeholder="Amount" oninput="updateDashboard()">
	           <button class="btn btn-danger remove-expense" type="button" onclick="removeExpense(this)">Remove</button>
	       `;
	       expensesDiv.appendChild(newExpense);
	   }

	   function removeExpense(button) {
	       button.parentElement.remove();
	       updateDashboard();
	   }

	   function updateDashboard() {
	       const income = parseFloat(document.getElementById('income').value) || 0;
	       const expenseElements = document.querySelectorAll('#expenses .input-group');
	       const expenses = {};

	       expenseElements.forEach(expenseElement => {
	           const category = expenseElement.querySelector('.expense-category').value;
	           const amount = parseFloat(expenseElement.querySelector('.expense-amount').value) || 0;
	           if (category && amount) {
	               if (!expenses[category]) {
	                   expenses[category] = 0;
	               }
	               expenses[category] += amount;
	           }
	       });

	       const totalExpenses = Object.values(expenses).reduce((a, b) => a + b, 0);
	       const savings = income - totalExpenses;

	       expenseChart.data.labels = Object.keys(expenses);
	       expenseChart.data.datasets[0].data = Object.values(expenses);
	       expenseChart.update();

	       incomeExpenseChart.data.datasets[0].data = [income, totalExpenses];
	       incomeExpenseChart.update();
	   }

	   function checkMonthAndIncome() {
	       const month = document.getElementById('month').value;
	       const income = document.getElementById('income').value;
	       if (month && income) {
	           document.getElementById('expenses-section').classList.remove('hidden');
	       }
	   }

	   


	   function addToTable() {
	       const month = document.getElementById('month').value;
	       const income = parseFloat(document.getElementById('income').value) || 0;
	       const expenseAmounts = document.querySelectorAll('.expense-amount');
	       let totalExpenses = 0;
	       expenseAmounts.forEach(amount => {
	           totalExpenses += parseFloat(amount.value) || 0;
	       });

	       if (month && income > 0 && totalExpenses > 0) {
	           const savings = income - totalExpenses;

	           const tableBody = document.getElementById('summaryTableBody');
	           const newRow = document.createElement('tr');
	           newRow.innerHTML = `
	               <td>${month}</td>
	               <td>${income.toFixed(2)}</td>
	               <td>${totalExpenses.toFixed(2)}</td>
	               <td>${savings.toFixed(2)}</td>
	               <td><button class="btn btn-warning" onclick="editRow(this)">Edit</button></td>
	               <td><button class="btn btn-warning view-expenses" onclick="viewExpenses('${month}')">View Expenses</button></td>
	               <td><button class="btn btn-danger delete" onclick="removeRow(this, '${month}')">Delete</button></td>
	           `;
	           newRow.setAttribute('data-expenses', JSON.stringify(getExpensesData()));
	           tableBody.appendChild(newRow);

	           // Prepare data to send to the servlet
	           const data = {
	               month: month,
	               income: income,
	               totalExpenses: totalExpenses,
	               savings: savings,
	               expenses: getExpensesData()
	           };

	           // Fetch API call to DashboardServlet
	           fetch('FinanceServlet', {
	               method: 'POST',
	               headers: {
	                   'Content-Type': 'application/json'
	               },
	               body: JSON.stringify(data)
	           })
	           .then(response => response.json())
	           .then(data => {
	               console.log('Expenses data:', data);
	               alert('Expenses saved successfully!');
	               updateChart(month, income, totalExpenses, savings);
	           })
	           .catch((error) => {
	               console.error('Error:', error);
	           });

	           // Clear fields after adding to table
	           document.getElementById('month').value = '';
	           document.getElementById('income').value = '';
	           document.getElementById('expenses').innerHTML = '';
	           updateDashboard(); // Optionally update dashboard after clearing fields
	       } else {
	           alert('Please select a month, enter valid income and expenses.');
	       }
	   }

	   function getExpensesData() {
	       const expenseElements = document.querySelectorAll('#expenses .input-group');
	       const expenses = [];

	       expenseElements.forEach(expenseElement => {
	           const category = expenseElement.querySelector('.expense-category').value;
	           const amount = parseFloat(expenseElement.querySelector('.expense-amount').value) || 0;
	           if (category && amount) {
	               expenses.push({ category, amount });
	           }
	       });

	       return expenses;
	   }
	   function removeRow(button, month) {
	       const row = button.closest('tr');
	       row.remove();

	       // Send delete request to the backend
	       fetch('FinanceServlet', {
	           method: 'DELETE',
	           headers: {
	               'Content-Type': 'application/json'
	           },
	           body: JSON.stringify({ month: month })
	       })
	       .then(response => response.json())
	       .then(data => {
	           console.log('Delete response:', data);
	           alert('Entry deleted successfully!');
	       })
	       .catch((error) => {
	           console.error('Error:', error);
	       });
	   }

	   // Function to update the Chart.js chart
	   let chart;
	   function updateChart(month, income, totalExpenses, savings) {
	       const ctx = document.getElementById('myChart').getContext('2d');
	       
	       if (!chart) {
	           chart = new Chart(ctx, {
	               type: 'bar',
	               data: {
	                   labels: [month],
	                   datasets: [
	                       {
	                           label: 'Income',
	                           data: [income],
	                           backgroundColor: 'rgba(75, 192, 192, 0.2)',
	                           borderColor: 'rgba(75, 192, 192, 1)',
	                           borderWidth: 1
	                       },
	                       {
	                           label: 'Total Expenses',
	                           data: [totalExpenses],
	                           backgroundColor: 'rgba(255, 99, 132, 0.2)',
	                           borderColor: 'rgba(255, 99, 132, 1)',
	                           borderWidth: 1
	                       },
	                       {
	                           label: 'Savings',
	                           data: [savings],
	                           backgroundColor: 'rgba(54, 162, 235, 0.2)',
	                           borderColor: 'rgba(54, 162, 235, 1)',
	                           borderWidth: 1
	                       }
	                   ]
	               },
	               options: {
	                   scales: {
	                       y: {
	                           beginAtZero: true
	                       }
	                   }
	               }
	           });
	       } else {
	           chart.data.labels.push(month);
	           chart.data.datasets[0].data.push(income);
	           chart.data.datasets[1].data.push(totalExpenses);
	           chart.data.datasets[2].data.push(savings);
	           chart.update();
	       }
	   }


	   document.addEventListener('DOMContentLoaded', function() {
		   fetchUsername();
	       const monthSelect = document.getElementById('month');
	       const currentMonth = new Date().toLocaleString('default', { month: 'long' });
	       const options = monthSelect.options;

	       for (let i = 0; i < options.length; i++) {
	           if (options[i].value === currentMonth) {
	               options[i].selected = true;
	               break;
	           }
	       }
	   });

	   function editRow(button) {
	       const row = button.parentElement.parentElement;
	       const cells = row.querySelectorAll('td');
	       document.getElementById('month').value = cells[0].textContent;
	       document.getElementById('income').value = parseFloat(cells[1].textContent);

	       const expenses = JSON.parse(row.getAttribute('data-expenses') || '[]');
	       const expensesDiv = document.getElementById('expenses');
	       expensesDiv.innerHTML = '';
	       expenses.forEach(expense => {
	           const newExpense = document.createElement('div');
	           newExpense.className = 'input-group mb-3';
	           newExpense.innerHTML = `
	               <input type="text" class="form-control expense-category" value="${expense.category}">
	               <input type="number" class="form-control expense-amount" value="${expense.amount}" oninput="updateDashboard()">
	               <button class="btn btn-danger remove-expense" type="button" onclick="removeExpense(this)">Remove</button>
	           `;
	           expensesDiv.appendChild(newExpense);
	       });

	       updateDashboard();
	   }

	   function viewExpenses(month) {
	       const tableRows = document.querySelectorAll('#summaryTableBody tr');
	       tableRows.forEach(row => {
	           if (row.cells[0].textContent === month) {
	               const expenses = JSON.parse(row.getAttribute('data-expenses') || '[]');
	               alert(`Expenses for ${month}:\n` + expenses.map(exp => `${exp.category}: $${exp.amount.toFixed(2)}`).join('\n'));
	           }
	       });
	   }


	 
	   function contactUs() {
	   	 window.location.href = 'ContactUs.html';
	   }

	   function aboutUs() {
	   	 window.location.href = 'Aboutus.html';
	   }

	   function goHome() {
	       window.location.href = 'Dashboard.html';
	   }

	   
	   

	   function logout() {
	       window.location.href = 'login.html';
	   }