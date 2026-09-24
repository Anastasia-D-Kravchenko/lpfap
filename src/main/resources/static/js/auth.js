function saveSession(data) {
  S.token = data.token;
  S.user  = { email: data.email, role: data.role };
  localStorage.setItem(LS_TOKEN, data.token);
  localStorage.setItem(LS_USER, JSON.stringify(S.user));
}

function clearSession() {
  S.token = null; S.user = null; S.profile = null;
  localStorage.removeItem(LS_TOKEN);
  localStorage.removeItem(LS_USER);
}

function logout() {
  clearSession();
  toast('You have been logged out.', 'info');
  setTimeout(() => { window.location.href = '/'; }, 250);
}

function requireAuth() {
  if (!S.token) { window.location.href = '/login.html?next=' + encodeURIComponent(window.location.pathname); return false; }
  return true;
}
function requireRole(role) {
  if (!requireAuth()) return false;
  if (S.user?.role !== role) { window.location.href = '/'; return false; }
  return true;
}

async function refreshProfile() {
  if (!S.token) return null;
  try {
    S.profile = await api('/users/me');
    return S.profile;
  } catch {
    clearSession();
    return null;
  }
}

function renderNav(activeKey) {
  const nav = document.getElementById('topNav');
  if (!nav) return;
  const role = S.user?.role;
  const u    = S.profile || S.user;
  const name = u?.fullName || u?.email?.split('@')[0] || 'User';

  let links = `<a class="nav-link${activeKey==='animals'?' active':''}" href="/">Animals</a>`;
  if (role === 'USER') {
    links += `<a class="nav-link${activeKey==='dashboard'?' active':''}" href="/dashboard.html">My Applications</a>`;
    links += `<a class="nav-link${activeKey==='profile'?' active':''}"   href="/profile.html">Profile</a>`;
  } else if (role === 'ADMIN') {
    links += `<a class="nav-link${activeKey==='admin'?' active':''}"     href="/admin/animals.html">Admin Panel</a>`;
  }

  const avatar = S.profile?.avatarUrl
    ? `<img class="avatar" src="${S.profile.avatarUrl}" alt="${name}" style="object-fit:cover">`
    : `<span class="avatar${role==='ADMIN'?' yellow':''}">${initials(name)}</span>`;

  const right = S.token
    ? `<a class="nav-user" href="${role==='ADMIN'?'/admin/animals.html':'/profile.html'}">
         ${avatar}
         <span>${name}</span>
       </a>
       <button class="btn btn-outline btn-sm" onclick="logout()">Logout</button>`
    : `<a class="btn btn-outline btn-sm" href="/login.html">Login</a>
       <a class="btn btn-primary btn-sm" href="/login.html?tab=register">Register</a>`;

  nav.outerHTML = `
    <nav class="nav">
      <a class="nav-brand" href="/" style="display:flex;align-items:center;gap:8px">
        <img src="/img/pet-logo-design-paw-vector-animal-shop-business_53876-136741.avif"
             alt="LPFAP" style="height:32px;width:32px;border-radius:8px;object-fit:cover">
        <span>LPFAP</span>
      </a>
      <div class="nav-links">${links}</div>
      <div class="nav-right">${right}</div>
    </nav>`;
}
