
// Load settings from local storage
		document.addEventListener('DOMContentLoaded', function() {
			loadSettings();
			fetchUsername();

			document.getElementById('changePasswordForm').addEventListener('submit', changePassword);
		});

		function loadSettings() {
			// Load dark mode
			const darkModeEnabled = localStorage.getItem('darkModeEnabled') === 'true';
			if (darkModeEnabled) {
				document.body.classList.add('dark-mode');
				document.getElementById('darkModeSwitch').checked = true;
			}

			// Load font
			const selectedFont = localStorage.getItem('selectedFont');
			if (selectedFont) {
				document.body.style.fontFamily = selectedFont;
				document.getElementById('fontSelect').value = selectedFont;
			}

			// Load language
			const selectedLanguage = localStorage.getItem('selectedLanguage');
			if (selectedLanguage) {
				document.getElementById('languageSelect').value = selectedLanguage;
			}

			// Load privacy settings
			const dataSharingEnabled = localStorage.getItem('dataSharingEnabled') === 'true';
			document.getElementById('dataSharingToggle').checked = dataSharingEnabled;
		}










function goHome() {
			window.location.href = 'Dashboard.html'; // Adjust the path as necessary
		}

		function openModal(modalId) {
			document.getElementById(modalId).style.display = "block";
		}

		function closeModal(modalId) {
			document.getElementById(modalId).style.display = "none";
		}

		function showOptions() {
			var dropdown = document.getElementById('dropdownMenu');
			if (dropdown.style.display === "block") {
				dropdown.style.display = "none";
			} else {
				dropdown.style.display = "block";
			}
		}

		
		document.addEventListener('DOMContentLoaded', function() {
            // Fetch the username from the server
            function fetchUsername() {
                fetch('GetUsernameServlet')
                    .then(response => response.json())
                    .then(data => {
                        if (data.username && data.name) {
                            document.getElementById('username').value = data.username;
                            document.getElementById("usernameDisplay").innerText = data.name;
                            /* document.getElementById('profile-button').setAttribute('data-username', data.username); */
         				   
                        } else {
                            console.error("Error fetching user data:", data.error);
                        }
                    })
                    .catch(error => console.error('Error fetching username:', error));
            }

            // Function to handle password change form submission
            function changePassword(event) {
                event.preventDefault();
                const username = document.getElementById('username').value;
                const currentPassword = document.getElementById('currentPassword').value;
                const newPassword = document.getElementById('newPassword').value;
                const confirmPassword = document.getElementById('confirmPassword').value;

                if (newPassword !== confirmPassword) {
                    alert('Passwords do not match!');
                } else if (newPassword.length < 6) {
                    alert('Password must be at least 6 characters long!');
                } else {
                    fetch('ChangePasswordServlet', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        body: new URLSearchParams({
                            'username': username,
                            'currentPassword': currentPassword,
                            'newPassword': newPassword,
                            'confirmPassword': confirmPassword
                        })
                    })
                    .then(response => response.text())
                    .then(text => {
                        if (text.includes('successfully')) {
                            alert('Password changed successfully!');
                            document.getElementById('changePasswordForm').reset();
                            closeModal('changePasswordModal');
                        } else {
                            alert('Failed to change password: ' + text);
                        }
                    })
                    .catch(error => console.error('Error:', error));
                }
            }

            fetchUsername();
            document.getElementById('changePasswordForm').addEventListener('submit', changePassword);
        });


		function saveLanguage(event) {
			event.preventDefault();
			const selectedLanguage = document.getElementById('languageSelect').value;
			localStorage.setItem('selectedLanguage', selectedLanguage);
			alert('Language saved: ' + selectedLanguage);
			closeModal('languageSettingsModal');
		}

		function saveFont(event) {
			event.preventDefault();
			const selectedFont = document.getElementById('fontSelect').value;
			document.body.style.fontFamily = selectedFont;
			localStorage.setItem('selectedFont', selectedFont);
			closeModal('fontSettingsModal');
		}

		function saveDarkMode(event) {
			event.preventDefault();
			const darkModeEnabled = document.getElementById('darkModeSwitch').checked;
			if (darkModeEnabled) {
				document.body.classList.add('dark-mode');
			} else {
				document.body.classList.remove('dark-mode');
			}
			localStorage.setItem('darkModeEnabled', darkModeEnabled);
			closeModal('darkModeModal');
		}

		function savePrivacySettings(event) {
					event.preventDefault();
					const dataSharingEnabled = document.getElementById('dataSharingToggle').checked;
					localStorage.setItem('dataSharingEnabled', dataSharingEnabled);
					closeModal('privacySettingsModal');
				}

				function resetSettings() {
					if (confirm('Are you sure you want to reset all settings?')) {
						localStorage.removeItem('darkModeEnabled');
						localStorage.removeItem('selectedFont');
						localStorage.removeItem('selectedLanguage');
						localStorage.removeItem('dataSharingEnabled');
						alert('Settings have been reset.');
						loadSettings();
					}
				}


		window.onclick = function(event) {
			var modals = document.querySelectorAll('.modal');
			var dropdown = document.getElementById('dropdownMenu');
			modals.forEach(function(modal) {
				if (event.target == modal) {
					modal.style.display = "none";
				}
			});
			if (event.target != dropdown && !dropdown.contains(event.target)) {
				dropdown.style.display = "none";
			}
		}
		function viewProfile() {
			window.location.href = 'Profile.html';
		}
		function aboutUs() {
			window.location.href = 'Aboutus.html';
		}/**
 * 
 */