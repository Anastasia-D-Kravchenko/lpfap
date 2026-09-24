let allUsers = [];

async function init() {
  if (!(await adminShellInit('users'))) return;
  load();
}

async function load() {
  try {
    allUsers = await api('/admin/users');
    document.getElementById('usersTable').innerHTML = allUsers.length
      ? `<table><thead><tr><th>ID</th><th>NAME</th><th>EMAIL</th><th>PHONE</th><th>ROLE</th><th>REGISTERED</th><th>ACTIONS</th></tr></thead>
         <tbody>${allUsers.map(u => `
           <tr style="cursor:pointer" onclick="viewUser(${u.id})">
             <td style="color:#94a3b8;font-size:12px">${u.id}</td>
             <td><div style="display:flex;align-items:center;gap:8px">
               ${u.avatarUrl
                 ? `<img class="avatar" style="width:28px;height:28px;object-fit:cover" src="${u.avatarUrl}" alt="">`
                 : `<div class="avatar" style="width:28px;height:28px;font-size:10px;background:${u.role==='ADMIN'?'#f59e0b':'#1d4ed8'}">${initials(u.fullName||u.email)}</div>`}
               <span style="font-weight:600">${u.fullName||'—'}</span></div></td>
             <td style="color:#64748b">${u.email}</td>
             <td style="color:#64748b">${u.phone||'—'}</td>
             <td><span class="profile-role-badge profile-role-${u.role.toLowerCase()}" style="margin:0">${u.role}</span></td>
             <td style="color:#64748b">${formatDate(u.registrationDate)}</td>
             <td onclick="event.stopPropagation()"><div style="display:flex;gap:6px">
               <button class="btn btn-outline btn-sm" onclick="viewUser(${u.id})">View</button>
               <button class="btn btn-outline btn-sm" onclick="toggleRole(${u.id},'${u.role}','${u.email}')">${u.role==='ADMIN'?'→ USER':'→ ADMIN'}</button>
             </div></td>
           </tr>`).join('')}
         </tbody></table>`
      : '<div class="empty">No users found.</div>';
  } catch { document.getElementById('usersTable').innerHTML = '<div class="empty">Failed to load users.</div>'; }
}

async function toggleRole(id, current, email) {
  const newRole = current === 'ADMIN' ? 'USER' : 'ADMIN';
  if (!confirm('Change role of '+email+' to '+newRole+'?')) return;
  try {
    await api('/admin/users/'+id+'/role', { method:'PATCH', body: JSON.stringify({ role: newRole }) });
    toast('Role updated!');
    load();
  } catch (e) { toast(e.message,'error'); }
}

async function viewUser(id) {
  const u = allUsers.find(x => x.id === id);
  if (!u) return;
  document.getElementById('userDetailName').textContent  = u.fullName || u.email;
  document.getElementById('userDetailEmail').textContent = u.email;
  document.getElementById('userDetailRole').innerHTML    =
    `<span class="profile-role-badge profile-role-${u.role.toLowerCase()}" style="margin:0">${u.role}</span>`;
  document.getElementById('userDetailPhone').textContent      = u.phone || '—';
  document.getElementById('userDetailRegistered').textContent = formatDate(u.registrationDate);

  const avatar = document.getElementById('userDetailAvatar');
  if (u.avatarUrl) {
    avatar.src = u.avatarUrl;
  } else {
    avatar.src = `https://placehold.co/96x96/${u.role==='ADMIN'?'f59e0b':'1d4ed8'}/ffffff?text=${encodeURIComponent(initials(u.fullName||u.email))}`;
  }

  const bioWrap = document.getElementById('userDetailBioWrap');
  if (u.bio) { bioWrap.style.display = ''; document.getElementById('userDetailBio').textContent = u.bio; }
  else       { bioWrap.style.display = 'none'; }

  const newRole = u.role === 'ADMIN' ? 'USER' : 'ADMIN';
  const btn = document.getElementById('userDetailRoleBtn');
  btn.textContent = u.role === 'ADMIN' ? '↓ Demote to USER' : '↑ Promote to ADMIN';
  btn.onclick = async () => {
    if (!confirm('Change role of '+u.email+' to '+newRole+'?')) return;
    try {
      await api('/admin/users/'+u.id+'/role', { method:'PATCH', body: JSON.stringify({ role: newRole }) });
      toast('Role updated!');
      closeModal('userDetailModal');
      load();
    } catch (e) { toast(e.message,'error'); }
  };

  document.getElementById('userDetailApps').textContent = '…';
  document.getElementById('userDetailAppList').innerHTML = '<div class="loading"><div class="spinner"></div></div>';
  try {
    const allApps = await api('/applications');
    const apps = allApps.filter(a => a.applicantEmail === u.email);
    document.getElementById('userDetailApps').textContent = apps.length;
    document.getElementById('userDetailAppList').innerHTML = apps.length
      ? `<table><thead><tr><th>ANIMAL</th><th>TYPE</th><th>SUBMITTED</th><th>STATUS</th></tr></thead>
         <tbody>${apps.map(a => `
           <tr>
             <td>${a.animalName || '—'}</td>
             <td>${a.type}</td>
             <td>${formatDate(a.submissionDate)}</td>
             <td>${statusBadge(a.status)}</td>
           </tr>`).join('')}
         </tbody></table>`
      : '<div class="empty" style="padding:20px">No applications yet.</div>';
  } catch {
    document.getElementById('userDetailAppList').innerHTML =
      '<div class="empty" style="padding:20px">Could not load applications.</div>';
  }
  openModal('userDetailModal');
}

init();
