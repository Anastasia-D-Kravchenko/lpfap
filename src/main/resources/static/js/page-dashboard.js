async function init() {
  if (!requireAuth()) return;
  await refreshProfile();
  renderNav('dashboard');
  load();
}

function decisionCell(a) {
  if (a.status === 'PENDING')          return '<span style="color:#94a3b8;font-size:12px">Awaiting review</span>';
  if (a.status === 'APPROVED')         return `<span style="color:#10b981;font-weight:600">✓ Approved</span><div style="font-size:11px;color:#94a3b8">${formatDate(a.decisionDate)}</div>`;
  if (a.status === 'REJECTED')         return `<span style="color:#ef4444;font-weight:600">✕ Rejected</span><div style="font-size:11px;color:#94a3b8">${formatDate(a.decisionDate)}</div>`;
  if (a.status === 'WITHDRAWN')        return `<span style="color:#64748b">Withdrawn</span><div style="font-size:11px;color:#94a3b8">${formatDate(a.decisionDate)}</div>`;
  if (a.status === 'RETURN_REQUESTED') return `<span style="color:#f59e0b">↩ Return in progress</span>`;
  if (a.status === 'RETURNED')         return `<span style="color:#64748b">Returned</span><div style="font-size:11px;color:#94a3b8">${formatDate(a.decisionDate)}</div>`;
  return '—';
}

function actionCell(a) {
  if (a.status === 'PENDING')
    return `<button class="action-btn" onclick="withdrawApp(${a.id})">Withdraw</button>`;
  if (a.status === 'APPROVED' && a.type === 'FOSTER')
    return `<button class="btn btn-yellow btn-sm" onclick="requestReturn(${a.id})">↩ Return animal</button>`;
  if (a.status === 'RETURN_REQUESTED')
    return `<div style="display:flex;gap:6px;flex-wrap:wrap">
              <button class="btn btn-outline btn-sm" onclick="declineReturn(${a.id})"
                      title="Keep fostering this animal">Keep animal</button>
              <span style="color:#f59e0b;font-size:11px;align-self:center">Awaiting shelter</span>
            </div>`;
  return `<span style="color:#94a3b8;font-size:12px">—</span>`;
}

async function load() {
  const u    = S.profile || S.user;
  const name = u?.fullName || u?.email?.split('@')[0] || 'User';
  document.getElementById('dashGreeting').textContent = 'Hello, ' + name + '! 👋';
  document.getElementById('dashStats').innerHTML = '<div class="loading" style="grid-column:1/-1"><div class="spinner"></div></div>';
  document.getElementById('myAppsTable').innerHTML = '';
  try {
    const apps     = await api('/applications/my');
    const total    = apps.length;
    const pending  = apps.filter(a => a.status==='PENDING').length;
    const approved = apps.filter(a => a.status==='APPROVED').length;

    document.getElementById('dashStats').innerHTML = `
      <div class="stat-card"><div class="stat-icon gray">📋</div><div><div class="stat-label">Total Applications</div><div class="stat-num">${total}</div></div></div>
      <div class="stat-card"><div class="stat-icon yellow">⏳</div><div><div class="stat-label">Pending Review</div><div class="stat-num">${pending}</div></div></div>
      <div class="stat-card"><div class="stat-icon green">✅</div><div><div class="stat-label">Approved</div><div class="stat-num">${approved}</div></div></div>`;

    if (!apps.length) {
      document.getElementById('myAppsTable').innerHTML =
        `<div class="empty"><div class="empty-icon">🐾</div>No applications yet. <a href="/" style="color:#1d4ed8;font-weight:600">Browse animals to get started!</a></div>`;
      return;
    }
    document.getElementById('myAppsTable').innerHTML = `
      <table>
        <thead><tr><th>ANIMAL</th><th>TYPE</th><th>SUBMITTED</th><th>STATUS</th><th>DECISION</th><th>ACTIONS</th></tr></thead>
        <tbody>${apps.map(a => `
          <tr>
            <td><a class="animal-cell" href="/animal.html?id=${a.animalId}">
              <img class="animal-cell-img"
                   src="${a.animalPhotoUrl || 'https://placehold.co/40x40/e2e8f0/94a3b8?text='+encodeURIComponent((a.animalName||'?').charAt(0))}"
                   alt="${a.animalName||''}">
              <div><div class="animal-cell-name">${a.animalName||'—'}</div></div>
            </a></td>
            <td>${a.type}</td>
            <td>${formatDate(a.submissionDate)}</td>
            <td>${statusBadge(a.status)}</td>
            <td>${decisionCell(a)}</td>
            <td>${actionCell(a)}</td>
          </tr>`).join('')}
        </tbody>
      </table>`;
  } catch (e) { toast('Failed to load applications: ' + e.message, 'error'); }
}

async function withdrawApp(id) {
  if (!confirm('Withdraw this application?')) return;
  try {
    await api('/applications/'+id+'/withdraw', { method:'PATCH' });
    toast('Application withdrawn');
    load();
  } catch (e) { toast(e.message, 'error'); }
}

async function requestReturn(id) {
  if (!confirm('Ask the shelter to take this animal back?\n\nThis ends your fostering arrangement.')) return;
  try {
    await api('/applications/'+id+'/request-return', { method:'PATCH' });
    toast('Return requested — the shelter has been notified.');
    load();
  } catch (e) { toast(e.message, 'error'); }
}

async function declineReturn(id) {
  if (!confirm('Keep this animal? The return request will be cancelled and the animal remains in your care.')) return;
  try {
    await api('/applications/'+id+'/decline-return', { method:'PATCH' });
    toast('Got it — you keep fostering this animal.');
    load();
  } catch (e) { toast(e.message, 'error'); }
}

init();
