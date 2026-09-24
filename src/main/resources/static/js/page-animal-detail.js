let currentAnimal = null;
let appType = 'ADOPTION';
let lastSubmittedAppId = null;
const FROM_ADMIN = qs('from') === 'admin';

async function init() {
  await refreshProfile();
  renderNav('animals');

  if (FROM_ADMIN && S.user?.role === 'ADMIN') {
    const breadcrumb = document.querySelector('.breadcrumb');
    if (breadcrumb) {
      breadcrumb.innerHTML =
        `<a href="/admin/animals.html">Admin · Animals</a> › <span id="detailBreadcrumb">Loading…</span>`;
    }
    const backBtn = document.querySelector('.detail-back');
    if (backBtn) { backBtn.textContent = '← Back to admin panel'; backBtn.setAttribute('href','/admin/animals.html'); }
  }

  const id = qs('id');
  if (!id) { window.location.href = '/'; return; }

  await loadAnimal(id);
}

async function loadAnimal(id) {
  try {
    const a = await api('/animals/' + id);
    currentAnimal = a;
    document.getElementById('detailBreadcrumb').textContent = a.name;
    document.title = a.name + ' · LPFAP';

    const isAdmin     = S.user?.role === 'ADMIN';
    const isLoggedIn  = !!S.token;
    const isAvailable = a.status === 'AVAILABLE';

    const adminNote = isAdmin
      ? '<div style="font-size:11px;color:#94a3b8;margin-top:4px">As an admin you can still adopt/foster as a user.</div>'
      : '';

    const applySection = isLoggedIn && isAvailable
      ? `<div class="apply-box">
           <h4>Apply for ${a.name}</h4>
           <p>Choose how you'd like to help this animal.</p>
           <div class="apply-action-btns">
             <button class="btn btn-primary" onclick="openAppModal('ADOPTION')">🏠 Apply to Adopt</button>
             <button class="btn btn-outline" onclick="openAppModal('FOSTER')">💛 Apply to Foster</button>
           </div>
           ${adminNote}
         </div>`
      : isLoggedIn
      ? `<div class="apply-box"><h4>This animal is not available</h4><p>Status: ${a.status}</p></div>`
      : `<div class="apply-box">
           <h4>Log in to apply for ${a.name}</h4>
           <p>You must have an account to submit adoption or foster applications.</p>
           <div class="apply-btns">
             <a class="btn btn-primary" href="/login.html?next=${encodeURIComponent(location.pathname + location.search)}">Log In</a>
             <a class="btn btn-outline" href="/login.html?tab=register">Sign Up</a>
           </div>
         </div>`;

    const allPhotos = [a.photoUrl, ...(a.photos || [])].filter(Boolean);
    const galleryStrip = allPhotos.length > 1
      ? `<div class="detail-thumbs">${allPhotos.map((u, i) => `
           <div class="detail-thumb${i===0?' active':''}" onclick="setMainPhoto(this,'${u}')">
             <img src="${u}" alt="${i+1}">
           </div>`).join('')}</div>`
      : '';

    const infoView = isAdmin ? renderAdminEditCard(a) : renderPublicInfoCard(a);

    document.getElementById('detailContent').innerHTML = `
      <div class="detail-grid">
        <div>
          <div class="detail-main-img">
            <img id="mainPhoto" src="${animalImg(a)}" alt="${a.name}"
                 onerror="this.src='https://placehold.co/600x440/e2e8f0/94a3b8?text=${encodeURIComponent(a.name)}'">
          </div>
          ${galleryStrip}
          ${isAdmin ? renderAdminGalleryControls(a) : ''}
        </div>
        ${infoView}
      </div>
      ${applySection}`;
  } catch {
    document.getElementById('detailContent').innerHTML =
      '<div class="empty"><div class="empty-icon">⚠️</div>Failed to load animal details.</div>';
  }
}

function setMainPhoto(thumb, src) {
  document.querySelectorAll('.detail-thumb').forEach(t => t.classList.remove('active'));
  thumb.classList.add('active');
  document.getElementById('mainPhoto').src = src;
}

function renderPublicInfoCard(a) {
  return `
    <div class="detail-info">
      <div class="detail-name-row">
        <div class="detail-name">${a.name}</div>
        ${statusBadge(a.status)}
      </div>
      <table class="detail-table">
        <tr><td>Species / Breed</td><td>${a.species}${a.breed ? ' · ' + a.breed : ''}</td></tr>
        <tr><td>Age</td><td>${formatAge(a.age)}</td></tr>
        <tr><td>Sex</td><td>${a.sex}</td></tr>
        <tr><td>Shelter</td><td>${a.shelterName || '—'}</td></tr>
        <tr><td>Intake Date</td><td>${formatDate(a.intakeDate)}</td></tr>
        <tr><td>Medical Status</td><td>${a.medicalStatus || '—'}</td></tr>
      </table>
      ${a.description ? `<div class="detail-desc"><h4>Description</h4><p>${a.description}</p></div>` : ''}
    </div>`;
}

const SPECIES = ['DOG','CAT','RABBIT','BIRD','HAMSTER','GUINEA_PIG','FERRET','REPTILE','FISH','HORSE','OTHER'];
const SEXES   = ['MALE','FEMALE','UNKNOWN'];

function renderAdminEditCard(a) {
  return `
    <div class="detail-info">
      <div class="detail-name-row">
        <input class="form-input" id="edName" value="${escapeAttr(a.name)}" style="font-size:22px;font-weight:800;flex:1;min-width:160px">
        ${statusBadge(a.status)}
      </div>
      <div class="form-row" style="margin-top:6px">
        <div class="form-group"><label class="form-label">Species</label>
          <select class="form-select" id="edSpecies">${SPECIES.map(s => `<option value="${s}"${s===a.species?' selected':''}>${s}</option>`).join('')}</select>
        </div>
        <div class="form-group"><label class="form-label">Sex</label>
          <select class="form-select" id="edSex">${SEXES.map(s => `<option value="${s}"${s===a.sex?' selected':''}>${s}</option>`).join('')}</select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-group"><label class="form-label">Breed</label>
          <input class="form-input" id="edBreed" value="${escapeAttr(a.breed || '')}">
        </div>
        <div class="form-group"><label class="form-label">Age (months)</label>
          <input class="form-input" id="edAge" type="number" min="0" value="${a.age ?? ''}">
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">Status</label>
        <select class="form-select" id="edStatus">
          ${['AVAILABLE','FOSTERED','ADOPTED','ARCHIVED'].map(s => `<option value="${s}"${s===a.status?' selected':''}>${s}</option>`).join('')}
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">Primary photo URL</label>
        <input class="form-input" id="edPhoto" value="${escapeAttr(a.photoUrl || '')}" oninput="document.getElementById('mainPhoto').src=this.value||'${animalImg(a)}'">
      </div>
      <div class="form-group">
        <label class="form-label">Medical Status</label>
        <input class="form-input" id="edMedical" value="${escapeAttr(a.medicalStatus || '')}">
      </div>
      <div class="form-group">
        <label class="form-label">Description</label>
        <textarea class="form-textarea" id="edDesc" rows="4">${escapeText(a.description || '')}</textarea>
      </div>
      <div style="display:flex;gap:10px;margin-top:8px;flex-wrap:wrap">
        <button class="btn btn-primary" onclick="saveAdminEdits()">💾 Save Changes</button>
        <button class="btn btn-outline" onclick="loadAnimal(${a.id})">↺ Cancel</button>
        <button class="btn btn-danger" onclick="adminArchive(${a.id},'${escapeAttr(a.name)}')">🗑 Archive</button>
      </div>
    </div>`;
}

function renderAdminGalleryControls(a) {
  return `
    <div style="margin-top:14px;padding:12px;background:#fff;border:1px solid #e2e8f0;border-radius:10px">
      <div style="font-size:12px;font-weight:700;color:#1e293b;margin-bottom:8px">📷 Add photo to gallery</div>
      <label class="btn btn-outline btn-sm" style="cursor:pointer;margin:0">
        📁 Upload photo
        <input id="detailPhotoFile" type="file" accept="image/*" style="display:none" onchange="uploadDetailPhoto(event)">
      </label>
      <span id="detailUploadStatus" style="font-size:12px;color:#64748b;margin-left:10px"></span>
    </div>`;
}

async function saveAdminEdits() {
  const id = currentAnimal.id;
  const body = {
    name: document.getElementById('edName').value.trim(),
    species: document.getElementById('edSpecies').value,
    breed: document.getElementById('edBreed').value.trim() || null,
    age: parseInt(document.getElementById('edAge').value) || 0,
    sex: document.getElementById('edSex').value,
    photoUrl: document.getElementById('edPhoto').value.trim() || null,
    medicalStatus: document.getElementById('edMedical').value.trim() || null,
    description: document.getElementById('edDesc').value.trim() || null,
    shelterId: 1
  };
  const newStatus = document.getElementById('edStatus').value;
  try {
    await api('/animals/' + id, { method:'PUT', body: JSON.stringify(body) });
    if (newStatus !== currentAnimal.status) {
      await api('/animals/' + id + '/status', { method:'PATCH', body: JSON.stringify({ status:newStatus }) });
    }
    toast('Animal updated! ✅');
    await loadAnimal(id);
  } catch (e) { toast(e.message, 'error'); }
}

async function adminArchive(id, name) {
  if (!confirm('Archive "' + name + '"?')) return;
  try {
    await api('/animals/' + id, { method:'DELETE' });
    toast(name + ' archived');
    window.location.href = '/admin/animals.html';
  } catch (e) { toast(e.message, 'error'); }
}

async function uploadDetailPhoto(ev) {
  const file = ev.target.files && ev.target.files[0];
  if (!file || !currentAnimal) return;
  const status = document.getElementById('detailUploadStatus');
  status.style.color = '#64748b'; status.textContent = 'Uploading…';
  try {
    const form = new FormData(); form.append('file', file);
    const r = await fetch('/api/v1/animals/' + currentAnimal.id + '/photos',
      { method:'POST', headers:{ Authorization:'Bearer '+S.token }, body: form });
    const data = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(data.error || 'Upload failed');
    status.style.color = '#10b981'; status.textContent = '✓ Uploaded';
    await loadAnimal(currentAnimal.id);
    toast('Photo added');
  } catch (e) {
    status.style.color = '#ef4444'; status.textContent = '✗ ' + e.message;
    toast('Upload failed: ' + e.message, 'error');
  } finally { ev.target.value = ''; }
}

function escapeAttr(s) { return String(s).replace(/"/g,'&quot;').replace(/</g,'&lt;'); }
function escapeText(s) { return String(s).replace(/</g,'&lt;'); }

function openAppModal(type) {
  appType = type;
  const a = currentAnimal;
  document.getElementById('appModalTitle').textContent = 'Application for: ' + a.name;
  document.getElementById('appModalSub').textContent   = (a.breed || a.species) + ' · ID: #' + a.id;
  const u = S.profile || S.user;
  document.getElementById('appName').value  = u?.fullName || '';
  document.getElementById('appEmail').value = u?.email    || '';
  document.getElementById('appPhone').value = u?.phone    || '';
  document.getElementById('appMotivation').value = '';
  document.getElementById('appCharCount').textContent = '0 / 50';
  document.getElementById('appMotivationError').textContent = '';
  const after = document.getElementById('appAfterSubmit');
  if (after) after.style.display = 'none';
  selectAppType(type);
  openModal('appModal');
}
function selectAppType(type) {
  appType = type;
  document.getElementById('optAdoption').className = 'app-type-opt' + (type==='ADOPTION'?' selected':'');
  document.getElementById('optFoster').className   = 'app-type-opt' + (type==='FOSTER'?' selected':'');
  document.getElementById('optAdoption').querySelector('input').checked = type==='ADOPTION';
  document.getElementById('optFoster').querySelector('input').checked   = type==='FOSTER';
}
function updateCharCount() {
  const len = document.getElementById('appMotivation').value.length;
  const el  = document.getElementById('appCharCount');
  el.textContent = len + ' / 50';
  el.style.color = len >= 50 ? '#10b981' : '#94a3b8';
}
async function submitApplication() {
  const motivation = document.getElementById('appMotivation').value.trim();
  document.getElementById('appMotivationError').textContent = '';
  if (motivation.length < 50) {
    document.getElementById('appMotivationError').textContent = 'Please write at least 50 characters.';
    return;
  }
  try {
    const resp = await api('/applications', {
      method:'POST',
      body: JSON.stringify({ type: appType, motivationText: motivation, animalId: currentAnimal.id })
    });
    lastSubmittedAppId = resp.id;
    toast('Application submitted! Add photos below (or skip).');
    const after = document.getElementById('appAfterSubmit');
    if (after) after.style.display = '';
    document.getElementById('appSubmitFooter').innerHTML =
      `<button class="btn btn-primary" onclick="closeModal('appModal');window.location.href='/dashboard.html'">Done</button>`;
  } catch (e) { toast(e.message, 'error'); }
}
async function uploadApplicationPhoto(ev) {
  const file = ev.target.files && ev.target.files[0];
  if (!file || !lastSubmittedAppId) return;
  const status = document.getElementById('appPhotoStatus');
  status.style.color = '#64748b'; status.textContent = 'Uploading…';
  try {
    const form = new FormData(); form.append('file', file);
    const r = await fetch('/api/v1/applications/' + lastSubmittedAppId + '/photos',
      { method:'POST', headers:{ Authorization:'Bearer '+S.token }, body: form });
    const data = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(data.error || 'Upload failed');
    status.style.color = '#10b981'; status.textContent = '✓ Added (' + (data.photos?.length || 0) + ' photos)';
    const list = document.getElementById('appPhotoList');
    if (list) list.innerHTML = (data.photos || []).map(u => `<img src="${u}" style="width:60px;height:60px;border-radius:8px;object-fit:cover;margin-right:6px">`).join('');
  } catch (e) {
    status.style.color = '#ef4444'; status.textContent = '✗ ' + e.message;
  } finally { ev.target.value = ''; }
}

init();
