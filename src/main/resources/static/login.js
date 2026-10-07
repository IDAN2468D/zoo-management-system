// Dedicated Login & Register Controller for Zoo Management
const API_BASE = '/api';

const ROLE_HEBREW_TITLES = {
    ADMIN: 'מנהל מערכת (Admin)',
    VET: 'רופא וטרינר (Veterinarian)',
    KEEPER: 'מטפל חיות (Keeper)',
    GUEST: 'אורח'
};

document.addEventListener('DOMContentLoaded', () => {
    initAuthForms();
    checkExistingSession();
});

// Check if user already logged in
async function checkExistingSession() {
    const saved = localStorage.getItem('zoo_auth');
    if (!saved) return;

    try {
        const creds = JSON.parse(saved);
        if (creds && creds.username && creds.password) {
            const basicAuth = btoa(`${creds.username}:${creds.password}`);
            const res = await fetch(`${API_BASE}/auth/me`, {
                headers: { 'Authorization': `Basic ${basicAuth}` }
            });

            if (res.ok) {
                const data = await res.json();
                if (data && data.authenticated) {
                    const sessionBox = document.getElementById('existingSessionBox');
                    const userNameSpan = document.getElementById('existingUserName');
                    const userRoleSpan = document.getElementById('existingUserRole');

                    if (sessionBox && userNameSpan && userRoleSpan) {
                        userNameSpan.textContent = data.fullName || data.username;
                        userRoleSpan.textContent = ROLE_HEBREW_TITLES[data.role] || data.role || 'מורשה';
                        sessionBox.style.display = 'block';
                    }
                }
            }
        }
    } catch (e) {
        console.warn('Session verification check failed:', e);
    }
}

function initAuthForms() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    const btnExistingLogout = document.getElementById('btnExistingLogout');
    if (btnExistingLogout) {
        btnExistingLogout.addEventListener('click', () => {
            localStorage.removeItem('zoo_auth');
            const sessionBox = document.getElementById('existingSessionBox');
            if (sessionBox) sessionBox.style.display = 'none';
            showAlert('התנתקת מהחשבון הקודם. כעת תוכל להתחבר מחדש.', 'info');
        });
    }
}

// Switch between Login and Register tabs
function switchTab(tab) {
    const btnLogin = document.getElementById('tabBtnLogin');
    const btnRegister = document.getElementById('tabBtnRegister');
    const paneLogin = document.getElementById('paneLogin');
    const paneRegister = document.getElementById('paneRegister');

    hideAlert();

    if (tab === 'login') {
        if (btnLogin) btnLogin.classList.add('active');
        if (btnRegister) btnRegister.classList.remove('active');
        if (paneLogin) paneLogin.style.display = 'block';
        if (paneRegister) paneRegister.style.display = 'none';
    } else {
        if (btnRegister) btnRegister.classList.add('active');
        if (btnLogin) btnLogin.classList.remove('active');
        if (paneRegister) paneRegister.style.display = 'block';
        if (paneLogin) paneLogin.style.display = 'none';
    }
}

// Quick 1-click fill
function quickFill(username, password) {
    const uInput = document.getElementById('loginUsername');
    const pInput = document.getElementById('loginPassword');

    if (uInput) uInput.value = username;
    if (pInput) pInput.value = password;

    const form = document.getElementById('loginForm');
    if (form) {
        form.requestSubmit();
    }
}

// Submit Login
async function handleLogin(e) {
    e.preventDefault();
    hideAlert();

    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;

    const submitBtn = document.getElementById('btnSubmitLogin');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>⏳</span> מתחבר למערכת...';
    }

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await res.json();

        if (res.ok && data.authenticated) {
            // Save credentials
            const creds = { username, password };
            localStorage.setItem('zoo_auth', JSON.stringify(creds));

            showAlert(`🎉 שלום ${data.fullName || data.username}! התחברת בהצלחה. מעביר אותך לדף הבית...`, 'success');
            
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 650);
        } else {
            showAlert(data.message || 'שם משתמש או סיסמה שגויים, אנא נסה שוב', 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<span>🚀</span> התחבר ועבור לדף הבית';
            }
        }
    } catch (err) {
        showAlert('שגיאת תקשורת עם שרת גן החיות: ' + err.message, 'error');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<span>🚀</span> התחבר ועבור לדף הבית';
        }
    }
}

// Submit Register
async function handleRegister(e) {
    e.preventDefault();
    hideAlert();

    const fullName = document.getElementById('regFullName').value.trim();
    const username = document.getElementById('regUsername').value.trim();
    const password = document.getElementById('regPassword').value;
    const roleRadio = document.querySelector('input[name="regRole"]:checked');
    const role = roleRadio ? roleRadio.value : 'KEEPER';

    const submitBtn = document.getElementById('btnSubmitRegister');
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>⏳</span> רושם חשבון חדש...';
    }

    try {
        const res = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, username, password, role })
        });

        const data = await res.json();

        if (res.ok && data.authenticated) {
            // Auto login with new user
            const creds = { username, password };
            localStorage.setItem('zoo_auth', JSON.stringify(creds));

            showAlert(`✨ ברוך הבא, ${data.fullName}! החשבון נוצר בהצלחה. מעביר אותך לדף הבית...`, 'success');

            setTimeout(() => {
                window.location.href = 'index.html';
            }, 750);
        } else {
            showAlert(data.message || 'שגיאה ביצירת החשבון, בדוק אם שם המשתמש תפוס', 'error');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<span>✨</span> צור חשבון ועבור לדף הבית';
            }
        }
    } catch (err) {
        showAlert('שגיאה בתקשורת עם השרת: ' + err.message, 'error');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<span>✨</span> צור חשבון ועבור לדף הבית';
        }
    }
}

// Alert notification box helpers
function showAlert(text, type = 'error') {
    const alertBox = document.getElementById('loginAlert');
    const alertText = document.getElementById('loginAlertText');
    const alertIcon = document.getElementById('loginAlertIcon');

    if (!alertBox || !alertText || !alertIcon) return;

    alertBox.className = `login-alert ${type} show`;
    alertText.textContent = text;
    alertIcon.textContent = type === 'success' ? '✅' : type === 'info' ? 'ℹ️' : '⚠️';
}

function hideAlert() {
    const alertBox = document.getElementById('loginAlert');
    if (alertBox) {
        alertBox.className = 'login-alert';
    }
}
