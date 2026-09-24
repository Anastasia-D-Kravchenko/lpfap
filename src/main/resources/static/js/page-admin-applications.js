let apps = [];

async function init() {
  if (!(await adminShellInit('applications'))) return;
  load();
}

async function load() {
  document.getElementById('adminAppTable').innerHTML = '<div class="loading"><div class="spinner"></div></div>';
  const status = document.getElementById('appFilterStatus')?.value || '';
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  try {
    const url = '/applications' + (params.toString() ? '?' + params : '');
    apps = await api(url);
    renderAdminApps();
  } catch (e) {
    document.getElementById('adminAppTable').innerHTML =
      '<div class="empty">Failed to load applications. ' + (e.message || '') + '</div>';
  }
}

function onStatusChange() { load(); }

function renderAdminApps() {
  const type = document.getElementById('appFilterType')?.value || '';
  const filtered = apps.filter(a => !type || a.type === type);

  document.getElementById('adminAppTable').innerHTML = filtered.length
    ? `<table><thead><tr><th>ID</th><th>APPLICANT</th><th>ANIMAL</th><th>TYPE</th><th>SUBMITTED</th><th>STATUS</th><th>ACTIONS</th></tr></thead>
       <tbody>${filtered.map(a => `
         <tr>
           <td style="color:#94a3b8;font-size:12px">#${String(a.id).padStart(4,'0')}</td>
           <td><div style="font-weight:600;color:#1e293b">${a.applicantName||'—'}</div><div style="font-size:12px;color:#94a3b8">${a.applicantEmail||''}</div></td>
           <td><a class="animal-cell" href="/animal.html?id=${a.animalId}">
             <img class="animal-cell-img"
                  src="${a.animalPhotoUrl || 'https://placehold.co/40x40/e2e8f0/94a3b8?text='+encodeURIComponent((a.animalName||'?').charAt(0))}"
                  alt="${a.animalName||''}"
                  onerror="this.src='https://placehold.co/40x40/e2e8f0/94a3b8?text=?'">
             <span>${a.animalName||'—'}</span></a></td>
           <td>${a.type}</td>
           <td>${formatDate(a.submissionDate)}</td>
           <td>${statusBadge(a.status)}</td>
           <td><div style="display:flex;gap:6px;align-items:center;flex-wrap:wrap">
             <div class="icon-btn edit" onclick="viewApplication(${a.id})" title="View motivation">👁</div>
             ${a.status==='PENDING'
               ? `<div class="icon-btn approve" onclick="approveApp(${a.id})" title="Approve">✓</div>
                  <div class="icon-btn reject" onclick="rejectApp(${a.id})" title="Reject">✕</div>`
               : a.status==='APPROVED' && a.type==='FOSTER'
               ? `<button class="btn btn-yellow btn-sm" onclick="askReturn(${a.id})" title="Ask user to return the animal">Ask return</button>`
               : a.status==='RETURN_REQUESTED'
               ? `<button class="btn btn-success btn-sm" onclick="finalizeReturn(${a.id})" title="Animal will return to AVAILABLE">✓ Approve return</button>`
               : `<span style="color:#94a3b8;font-size:12px">Processed</span>`}
           </div></td>
         </tr>`).join('')}
       </tbody></table>`
    : '<div class="empty"><div class="empty-icon">📋</div>No applications match this filter.</div>';
}

async function approveApp(id) {
  if (!confirm('Approve this application? The animal status will be updated and other pending applications for the same animal will be rejected.')) return;
  try { await api('/applications/'+id+'/approve', { method:'PATCH' }); toast('Application approved! ✅'); load(); }
  catch (e) { toast(e.message, 'error'); }
}
async function rejectApp(id) {
  if (!confirm('Reject this application?')) return;
  try { await api('/applications/'+id+'/reject',  { method:'PATCH' }); toast('Application rejected'); load(); }
  catch (e) { toast(e.message, 'error'); }
}

let currentDetailApp = null;

async function viewApplication(id) {
  try {
    const a = await api('/applications/'+id);
    currentDetailApp = a;
    document.getElementById('appDetailTitle').textContent = 'Application #' + String(a.id).padStart(4,'0');
    document.getElementById('appDetailSub').textContent   = a.type + ' · ' + a.status;

    document.getElementById('appDetailApplicant').textContent = a.applicantName || '—';
    document.getElementById('appDetailEmail').textContent     = a.applicantEmail || '—';
    document.getElementById('appDetailApplicantPhone').textContent = a.applicantPhone ? '📞 ' + a.applicantPhone : '';
    document.getElementById('appDetailApplicantBio').textContent   = a.applicantBio || '';
    const avatar = document.getElementById('appDetailApplicantAvatar');
    avatar.src = a.applicantAvatarUrl
      || `https://placehold.co/96x96/1d4ed8/ffffff?text=${encodeURIComponent(initials(a.applicantName||a.applicantEmail||'?'))}`;

    const animalCell = document.getElementById('appDetailAnimal');
    if (a.animalId) {
      animalCell.innerHTML = `
        <a href="/animal.html?id=${a.animalId}" style="display:inline-flex;align-items:center;gap:8px">
          <img src="${a.animalPhotoUrl || 'https://placehold.co/32x32/e2e8f0/94a3b8?text=?'}"
               style="width:32px;height:32px;border-radius:8px;object-fit:cover;border:1px solid #e2e8f0">
          <span style="font-weight:600">${a.animalName || '—'}</span>
          <span style="color:#94a3b8;font-size:11px">#${a.animalId}</span>
        </a>`;
    } else { animalCell.textContent = '—'; }
    document.getElementById('appDetailType').textContent      = a.type;
    document.getElementById('appDetailStatus').innerHTML      = statusBadge(a.status);
    document.getElementById('appDetailSubmitted').textContent = formatDate(a.submissionDate);
    document.getElementById('appDetailDecision').textContent  = a.decisionDate ? formatDate(a.decisionDate) : '—';
    document.getElementById('appDetailMotivation').textContent = a.motivationText || '—';

    const photosWrap = document.getElementById('appDetailPhotosWrap');
    const photosBox  = document.getElementById('appDetailPhotos');
    if (a.photos && a.photos.length) {
      photosWrap.style.display = '';
      photosBox.innerHTML = a.photos.map(u => `
        <a href="${u}" target="_blank" rel="noopener">
          <img src="${u}" alt="" style="width:108px;height:108px;border-radius:10px;object-fit:cover;border:1px solid #e2e8f0">
        </a>`).join('');
    } else {
      photosWrap.style.display = 'none';
      photosBox.innerHTML = '';
    }

    const footer = document.getElementById('appDetailFooter');
    if (a.status === 'PENDING') {
      footer.innerHTML = `
        <button class="btn btn-outline" onclick="closeModal('appDetailModal')">Close</button>
        <button class="btn btn-danger" onclick="closeModal('appDetailModal');rejectApp(${a.id})">Reject</button>
        <button class="btn btn-success" onclick="closeModal('appDetailModal');approveApp(${a.id})">Approve</button>`;
    } else if (a.status === 'APPROVED' && a.type === 'FOSTER') {
      footer.innerHTML = `
        <button class="btn btn-outline" onclick="closeModal('appDetailModal')">Close</button>
        <button class="btn btn-yellow" onclick="closeModal('appDetailModal');askReturn(${a.id})">Ask user to return</button>`;
    } else if (a.status === 'RETURN_REQUESTED') {
      footer.innerHTML = `
        <button class="btn btn-outline" onclick="closeModal('appDetailModal')">Close</button>
        <button class="btn btn-success" onclick="closeModal('appDetailModal');finalizeReturn(${a.id})">Finalise return</button>`;
    } else {
      footer.innerHTML = `<button class="btn btn-outline" onclick="closeModal('appDetailModal')">Close</button>`;
    }
    openModal('appDetailModal');
  } catch (e) { toast('Failed to load application: '+e.message, 'error'); }
}

async function viewApplicantFromApp() {
  const a = currentDetailApp;
  if (!a) return;
  document.getElementById('upmName').textContent  = a.applicantName  || '—';
  document.getElementById('upmEmail').textContent = a.applicantEmail || '—';
  document.getElementById('upmRole').textContent  = 'USER';
  document.getElementById('upmPhone').textContent = a.applicantPhone || '—';
  document.getElementById('upmRegistered').textContent = '—';
  document.getElementById('upmAvatar').src = a.applicantAvatarUrl
    || `https://placehold.co/96x96/1d4ed8/ffffff?text=${encodeURIComponent(initials(a.applicantName||'?'))}`;
  const bioWrap = document.getElementById('upmBioWrap');
  if (a.applicantBio) {
    bioWrap.style.display = '';
    document.getElementById('upmBio').textContent = a.applicantBio;
  } else { bioWrap.style.display = 'none'; }
  openModal('userProfileModal');
}

async function askReturn(id) {
  if (!confirm('Ask the foster owner to return this animal?')) return;
  try { await api('/applications/'+id+'/admin-request-return', { method:'PATCH' });
        toast('Return requested'); load(); }
  catch (e) { toast(e.message, 'error'); }
}
async function finalizeReturn(id) {
  if (!confirm('Finalise the return? The animal will become AVAILABLE again.')) return;
  try { await api('/applications/'+id+'/finalize-return', { method:'PATCH' });
        toast('Animal returned'); load(); }
  catch (e) { toast(e.message, 'error'); }
}

init();
