function init() {
  if (S.token) {
    window.location.href = S.user?.role === 'ADMIN' ? '/admin/animals.html' : '/dashboard.html';
    return;
  }
  renderNav();
  if (qs('tab') === 'register') switchTab('register');
  if (qs('expired')) {
    document.getElementById('loginError').textContent =
      'Your session has expired. Please log in again.';
  }
}

function switchTab(tab) {
  document.getElementById('loginForm').style.display    = tab === 'login'    ? '' : 'none';
  document.getElementById('registerForm').style.display = tab === 'register' ? '' : 'none';
  document.getElementById('tabLogin').className    = 'auth-tab' + (tab === 'login'    ? ' active' : '');
  document.getElementById('tabRegister').className = 'auth-tab' + (tab === 'register' ? ' active' : '');
}

async function doLogin() {
  const email    = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  document.getElementById('loginError').textContent = '';
  if (!email || !password) {
    document.getElementById('loginError').textContent = 'Email and password required.';
    return;
  }
  try {
    const data = await api('/auth/login', { method:'POST', body: JSON.stringify({ email, password }) });
    saveSession(data);
    afterAuth();
  } catch (e) { document.getElementById('loginError').textContent = e.message; }
}

async function doRegister() {
  const fullName = document.getElementById('regName').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const phone    = document.getElementById('regPhone').value.trim();
  const password = document.getElementById('regPassword').value;
  document.getElementById('regError').textContent = '';
  if (!fullName || !email || !password) {
    document.getElementById('regError').textContent = 'All fields except phone are required.';
    return;
  }
  try {
    const data = await api('/auth/register',
      { method:'POST', body: JSON.stringify({ fullName, email, phone, password }) });
    saveSession(data);
    afterAuth();
  } catch (e) { document.getElementById('regError').textContent = e.message; }
}

function afterAuth() {
  const next = qs('next');
  if (next) { window.location.href = next; return; }
  window.location.href = S.user?.role === 'ADMIN' ? '/admin/animals.html' : '/dashboard.html';
}

init();
