const API = '/api/v1';

const LS_TOKEN = 'lpfap_token';
const LS_USER  = 'lpfap_user';

const S = {
  token:   localStorage.getItem(LS_TOKEN) || null,
  user:    JSON.parse(localStorage.getItem(LS_USER) || 'null'),
  profile: null
};

function api(path, opts = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(S.token ? { Authorization: 'Bearer ' + S.token } : {})
  };
  return fetch(API + path, { ...opts, headers: { ...headers, ...opts.headers } })
    .then(async r => {
      const data = await r.json().catch(() => ({}));
      if (r.status === 401 && S.token) {
        localStorage.removeItem(LS_TOKEN);
        localStorage.removeItem(LS_USER);
        S.token = null; S.user = null; S.profile = null;
        if (!location.pathname.endsWith('/login.html')) {
          const next = encodeURIComponent(location.pathname + location.search);
          location.href = '/login.html?next=' + next + '&expired=1';
        }
        throw new Error('Session expired — please log in again');
      }
      if (!r.ok) throw new Error(data.error || data.message || 'Request failed');
      return data;
    });
}

function toast(msg, type = 'success') {
  let el = document.getElementById('toast');
  if (!el) {
    el = document.createElement('div');
    el.id = 'toast';
    el.className = 'toast';
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.className = 'toast ' + type + ' show';
  clearTimeout(el._t);
  el._t = setTimeout(() => (el.className = 'toast'), 3000);
}
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }
function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }

function formatAge(months) {
  if (months < 12) return months + ' Month' + (months !== 1 ? 's' : '');
  const y = Math.floor(months / 12);
  const m = months % 12;
  return y + ' Year' + (y !== 1 ? 's' : '') + (m ? ' ' + m + 'mo' : '');
}
function formatDate(s) {
  return s ? new Date(s).toLocaleDateString('en-GB',
    { day: 'numeric', month: 'short', year: 'numeric' }) : '—';
}
function statusBadge(s) {
  const m = {
    AVAILABLE:'available', FOSTERED:'fostered', ADOPTED:'adopted', ARCHIVED:'archived',
    PENDING:'pending', APPROVED:'approved', REJECTED:'rejected', WITHDRAWN:'withdrawn'
  };
  return `<span class="status-badge badge-${m[s]||'available'}">${s}</span>`;
}
function initials(name) {
  return name ? name.split(' ').map(w => w[0]).join('').substring(0,2).toUpperCase() : '?';
}
function animalImg(a) {
  return a.photoUrl
    || `https://placehold.co/400x240/e2e8f0/94a3b8?text=${encodeURIComponent(a.name||'?')}`;
}

function qs(name) {
  return new URLSearchParams(window.location.search).get(name);
}
