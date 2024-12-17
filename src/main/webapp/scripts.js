document.getElementById('email').addEventListener('blur', function() {
    const email = this.value;
    if (validateEmail(email)) {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', 'ForgotPasswordServlet', true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4 && xhr.status === 200) {
                if (xhr.responseText === 'valid') {
                    document.getElementById('captcha-container').classList.remove('hidden');
                    document.querySelector('.captcha-image').src = 'CaptchaServlet?' + new Date().getTime();
                } else {
                    showEmailAlert('Email not found.');
                    document.getElementById('captcha-container').classList.add('hidden');
                }
            }
        };
        xhr.send('action=validateEmail&email=' + encodeURIComponent(email));
    } else {
        showEmailAlert('Invalid email format.');
        document.getElementById('captcha-container').classList.add('hidden');
    }
});

document.getElementById('captcha').addEventListener('blur', function() {
    const captchaValue = this.value;
    validateCaptcha(captchaValue);
});

document.getElementById('forgot-password-form').addEventListener('submit', function(event) {
    event.preventDefault();
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const email = document.getElementById('email').value;

    if (newPassword.length < 6) {
        showEmailAlert('Password must be at least 6 characters long.');
        return;
    }

    if (newPassword !== confirmPassword) {
        showEmailAlert('Passwords do not match.');
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'ForgotPasswordServlet', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            if (xhr.responseText === 'success') {
                showEmailAlert('Password updated successfully.');
            } else {
                showEmailAlert('Error updating password.');
            }
        }
    };
    xhr.send('action=updatePassword&email=' + encodeURIComponent(email) + '&newPassword=' + encodeURIComponent(newPassword));
});

function validateEmail(email) {
    const re = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/;
    return re.test(email);
}

function validateCaptcha(captchaValue) {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'ForgotPasswordServlet', true);
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            if (xhr.responseText === 'valid') {
                document.getElementById('new-password-container').classList.remove('hidden');
            } else {
                showEmailAlert('Invalid CAPTCHA.');
                document.getElementById('new-password-container').classList.add('hidden');
            }
        }
    };
    xhr.send('action=validateCaptcha&captcha=' + encodeURIComponent(captchaValue));
}

function showEmailAlert(message) {
    const alertBox = document.createElement('div');
    alertBox.className = 'email-alert';
    alertBox.textContent = message;
    document.body.appendChild(alertBox);
    setTimeout(() => alertBox.remove(), 3000);
}

function goBack() {
    window.history.back();
}
/**
 * 
 */