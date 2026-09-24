const AVATAR_CHOICES = [
  '/img/pet-logo-design-paw-vector-animal-shop-business_53876-136741.avif',
  '/img/GettyImages-598175960-cute-dog-headshot.jpg',
  '/img/Cat-on-couch.jpg',
  '/img/coniglietto_appartamento-cure.jpg',
  '/img/cute-happy-pup.jpeg',
  '/img/spruce-pets-cutest-cats-16384c1bdb824b1d87861d62b2a73f4c.jpg',
  '/img/green-orange-faced-lovebird-standing-on-the-tree-in-garden-503871724-5c45ad159f4046fda8936dc461f8a607.jpg',
  '/img/vibrant-macaw-perched-branch-rainforest-generated-by-ai_188544-47067.avif',
  '/img/Rats-with-Teddy-Bears-14_grande.webp'
];

async function init() {
  if (!requireAuth()) return;
  await refreshProfile();
  renderNav('profile');
  buildAvatarPicker();
  load();
}

function buildAvatarPicker() {
  const box = document.getElementById('avatarPicker');
  box.innerHTML = AVATAR_CHOICES.map(url => `
    <img src="${url}" alt="" onclick="pickAvatar('${url}')"
         style="width:44px;height:44px;border-radius:8px;object-fit:cover;
                cursor:pointer;border:2px solid transparent;transition:.15s"
         onmouseover="this.style.borderColor='#1d4ed8'"
         onmouseout="this.style.borderColor='transparent'">
  `).join('');
}
function pickAvatar(url) {
  document.getElementById('editAvatarUrl').value = url;
  updateAvatarPreview();
}

async function uploadAvatar(ev) {
  const file = ev.target.files && ev.target.files[0];
  if (!file) return;
  const status = document.getElementById('avatarUploadStatus');
  status.style.color = '#64748b';
  status.textContent = 'Uploading…';
  try {
    const form = new FormData();
    form.append('file', file);
    const r = await fetch('/api/v1/users/me/avatar', {
      method: 'POST',
      headers: { Authorization: 'Bearer ' + S.token },
      body: form
    });
    const data = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(data.error || 'Upload failed');
    document.getElementById('editAvatarUrl').value = data.avatarUrl || '';
    updateAvatarPreview();
    status.style.color = '#10b981';
    status.textContent = '✓ Uploaded';
    toast('Photo uploaded — don\'t forget to Save Changes!', 'success');
  } catch (e) {
    status.style.color = '#ef4444';
    status.textContent = '✗ ' + e.message;
    toast('Upload failed: ' + e.message, 'error');
  } finally {
    ev.target.value = '';
  }
}

function clearAvatar() {
  document.getElementById('editAvatarUrl').value = '';
  document.getElementById('avatarUploadStatus').textContent = '';
  updateAvatarPreview();
}

function updateAvatarPreview() {
  const url = document.getElementById('editAvatarUrl').value.trim();
  const img = document.getElementById('avatarPreview');
  if (url) {
    img.src = url;
    img.style.display = '';
  } else {
    img.src = `https://placehold.co/72x72/1d4ed8/ffffff?text=${encodeURIComponent(
      initials(document.getElementById('editName').value || S.profile?.email || '?'))}`;
  }
}

async function load() {
  try {
    S.profile = await api('/users/me');
    const u = S.profile;
    const name = u.fullName || u.email;

    const avatarBig = document.getElementById('profileAvatarBig');
    if (u.avatarUrl) {
      avatarBig.innerHTML = `<img src="${u.avatarUrl}" alt="${name}"
        style="width:100%;height:100%;border-radius:50%;object-fit:cover">`;
      avatarBig.style.padding = '0';
    } else {
      avatarBig.textContent = initials(name);
    }
    document.getElementById('profileName').textContent      = name;
    document.getElementById('profileEmail').textContent     = u.email;
    document.getElementById('profileRoleBadge').innerHTML   =
      `<span class="profile-role-badge profile-role-${u.role.toLowerCase()}">${u.role}</span>`;

    let apps = [];
    try { apps = await api('/applications/my'); } catch {}
    const pending  = apps.filter(a => a.status==='PENDING').length;
    const approved = apps.filter(a => a.status==='APPROVED').length;
    document.getElementById('profileStats').innerHTML = `
      <div class="profile-stat"><div class="profile-stat-num">${apps.length}</div><div class="profile-stat-label">Applications</div></div>
      <div class="profile-stat"><div class="profile-stat-num">${approved}</div><div class="profile-stat-label">Approved</div></div>`;

    document.getElementById('profileInfoRows').innerHTML = `
      <div class="profile-info-row"><span class="profile-info-label">Member since</span><span class="profile-info-val">${formatDate(u.registrationDate)}</span></div>
      <div class="profile-info-row"><span class="profile-info-label">Phone</span><span class="profile-info-val">${u.phone||'—'}</span></div>
      <div class="profile-info-row" style="border:none"><span class="profile-info-label">Pending</span><span class="profile-info-val">${pending}</span></div>`;

    document.getElementById('editName').value      = u.fullName || '';
    document.getElementById('editEmail').value     = u.email;
    document.getElementById('editPhone').value     = u.phone || '';
    document.getElementById('editBio').value       = u.bio || '';
    document.getElementById('editAvatarUrl').value = u.avatarUrl || '';
    document.getElementById('editRole').value      = u.role;
    document.getElementById('editSince').value     = formatDate(u.registrationDate);
    updateAvatarPreview();
  } catch (e) { toast('Failed to load profile: '+e.message, 'error'); }
}

async function saveProfile() {
  const fullName  = document.getElementById('editName').value.trim();
  const phone     = document.getElementById('editPhone').value.trim();
  const bio       = document.getElementById('editBio').value.trim();
  const avatarUrl = document.getElementById('editAvatarUrl').value.trim();
  if (!fullName) { toast('Full name is required', 'error'); return; }
  try {
    S.profile = await api('/users/me',
      { method:'PATCH', body: JSON.stringify({ fullName, phone, bio, avatarUrl }) });
    S.user = { ...S.user, fullName: S.profile.fullName };
    localStorage.setItem(LS_USER, JSON.stringify(S.user));
    renderNav('profile');
    load();
    toast('Profile updated successfully! ✅');
  } catch (e) { toast(e.message, 'error'); }
}

init();
