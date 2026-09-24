async function adminShellInit(activeKey) {
  if (!requireRole('ADMIN')) return false;
  await refreshProfile();
  renderNav('admin');
  renderAdminSidebar(activeKey);
  return true;
}

function renderAdminSidebar(active) {
  const u = S.profile || S.user;
  const name = u?.fullName || u?.email?.split('@')[0] || 'Admin';

  const sb = document.getElementById('adminSidebar');
  if (!sb) return;
  sb.outerHTML = `
    <div class="sidebar" id="adminSidebar">
      <div class="sidebar-logo">
        <div class="sidebar-logo-title">LPFAP</div>
        <div class="sidebar-logo-sub">Admin Panel</div>
      </div>
      <div class="sidebar-nav">
        <a class="sidebar-link${active==='animals'?' active':''}"      href="/admin/animals.html"     >🐾 Animals</a>
        <a class="sidebar-link${active==='applications'?' active':''}" href="/admin/applications.html">📋 Applications</a>
        <a class="sidebar-link${active==='analytics'?' active':''}"    href="/admin/analytics.html"   >📊 Analytics</a>
        <a class="sidebar-link${active==='users'?' active':''}"        href="/admin/users.html"       >👥 Users</a>
      </div>
      <div class="sidebar-footer">
        <div class="sidebar-user">
          <div class="sidebar-avatar">${initials(name)}</div>
          <div><div class="sidebar-name">${name}</div><div class="sidebar-role">System Manager</div></div>
        </div>
        <div class="sidebar-logout" onclick="logout()">→ Log Out</div>
      </div>
    </div>`;
}

 document.head.insertAdjacentHTML('beforeend',
  '<style>.sidebar-link{text-decoration:none}</style>');
