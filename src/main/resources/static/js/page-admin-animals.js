let editAnimalId = null;
let allAnimals = [];

const ANIMAL_PHOTO_CHOICES = [
  '/img/GettyImages-598175960-cute-dog-headshot.jpg',
  '/img/cute-happy-pup.jpeg',
  '/img/download.jpeg',
  '/img/adorable-brown-white-basenji-dog-smiling-giving-high-five-isolated-white_346278-1657.avif',
  '/img/eyJidWNrZXQiOiJjb250ZW50Lmhzd3N0YXRpYy5jb20iLCJrZXkiOiJnaWZcL2dldHR5aW1hZ2VzLTE0MTc5NTY1MDMuanBnIiwiZWRpdHMiOnsicmVzaXplIjp7IndpZHRoIjo4Mjh9fX0=.webp',
  '/img/Cat-on-couch.jpg',
  '/img/GettyImages-953005498-4ca60c6c2c5f4b0e881b2746ad5f17ef.jpg',
  '/img/spruce-pets-cutest-cats-16384c1bdb824b1d87861d62b2a73f4c.jpg',
  '/img/coniglietto_appartamento-cure.jpg',
  '/img/Image-A.webp',
  '/img/green-orange-faced-lovebird-standing-on-the-tree-in-garden-503871724-5c45ad159f4046fda8936dc461f8a607.jpg',
  '/img/vibrant-macaw-perched-branch-rainforest-generated-by-ai_188544-47067.avif',
  '/img/Rats-with-Teddy-Bears-14_grande.webp'
];

async function init() {
  if (!(await adminShellInit('animals'))) return;
  buildPhotoPicker();
  load();
}

function buildPhotoPicker() {
  const box = document.getElementById('anPhotoPicker');
  if (!box) return;
  box.innerHTML = ANIMAL_PHOTO_CHOICES.map(url => `
    <img src="${url}" alt="" onclick="pickAnimalPhoto('${url}')"
         style="width:50px;height:50px;border-radius:8px;object-fit:cover;
                cursor:pointer;border:2px solid transparent;transition:.15s"
         onmouseover="this.style.borderColor='#1d4ed8'"
         onmouseout="this.style.borderColor='transparent'">
  `).join('');
}
function pickAnimalPhoto(url) {
  document.getElementById('anPhoto').value = url;
  updateAnPhotoPreview();
}
function updateAnPhotoPreview() {
  const url = document.getElementById('anPhoto').value.trim();
  const img = document.getElementById('anPhotoPreview');
  img.src = url || 'https://placehold.co/96x96/e2e8f0/94a3b8?text=No+image';
}

async function load() {
  try {
    const data = await api('/animals/all');
    allAnimals = Array.isArray(data) ? data : (data.content || []);
    render(allAnimals);
  } catch {
    document.getElementById('adminAnimalTable').innerHTML = '<div class="empty">Failed to load animals.</div>';
  }
}

function filterAdminAnimals() {
  const q      = (document.getElementById('anSearch')?.value||'').toLowerCase();
  const status = document.getElementById('anStatusFilter')?.value || '';
  const filtered = allAnimals
    .filter(a => !status || a.status === status)
    .filter(a => a.name.toLowerCase().includes(q)
              || (a.breed||'').toLowerCase().includes(q)
              || String(a.id).includes(q));
  render(filtered);
}

function render(animals) {
  document.getElementById('adminAnimalTable').innerHTML = animals.length
    ? `<table><thead><tr><th>ID</th><th>NAME</th><th>SPECIES</th><th>AGE</th><th>STATUS</th><th>ACTIONS</th></tr></thead>
       <tbody>${animals.map(a => `
         <tr>
           <td style="color:#94a3b8;font-size:12px">#${String(a.id).padStart(4,'0')}</td>
           <td><div class="animal-cell"><img class="animal-cell-img" src="${animalImg(a)}" alt="${a.name}" onerror="this.src='https://placehold.co/40x40/e2e8f0/94a3b8?text=?'">
             <div><div class="animal-cell-name">${a.name}</div><div class="animal-cell-breed">${a.breed||a.species}</div></div></div></td>
           <td>${a.species}</td><td>${formatAge(a.age)}</td>
           <td>${statusBadge(a.status)}</td>
           <td><div style="display:flex;gap:6px">
             <a class="icon-btn edit" href="/animal.html?id=${a.id}&from=admin" title="Open in inline editor">🔍</a>
             <div class="icon-btn edit" onclick="openAnimalModal(${a.id})" title="Edit (modal)">✏️</div>
             <div class="icon-btn del"  onclick="archiveAnimal(${a.id},'${a.name}')" title="Archive">🗑️</div>
           </div></td>
         </tr>`).join('')}
       </tbody></table>`
    : '<div class="empty"><div class="empty-icon">🐾</div>No animals found.</div>';
}

async function openAnimalModal(id = null) {
  editAnimalId = id;
  document.getElementById('animalModalTitle').textContent = id ? 'Edit Animal' : 'Add New Animal';
  const uploadRow = document.getElementById('anUploadRow');
  const gallery   = document.getElementById('anGallery');
  if (id) {
    try {
      const a = await api('/animals/'+id);
      document.getElementById('anName').value    = a.name||'';
      document.getElementById('anSpecies').value = a.species||'DOG';
      document.getElementById('anBreed').value   = a.breed||'';
      document.getElementById('anAge').value     = a.age||'';
      document.getElementById('anSex').value     = a.sex||'MALE';
      document.getElementById('anPhoto').value   = a.photoUrl||'';
      document.getElementById('anMedical').value = a.medicalStatus||'';
      document.getElementById('anDesc').value    = a.description||'';
      if (uploadRow) uploadRow.style.display = '';
      renderGallery(a.photos || []);
    } catch { toast('Failed to load animal','error'); return; }
  } else {
    ['anName','anBreed','anAge','anPhoto','anMedical','anDesc']
      .forEach(i => document.getElementById(i).value = '');
    document.getElementById('anSpecies').value = 'DOG';
    document.getElementById('anSex').value     = 'MALE';
    if (uploadRow) uploadRow.style.display = 'none';
    if (gallery)   gallery.innerHTML = '';
  }
  document.getElementById('anUploadStatus').textContent = '';
  updateAnPhotoPreview();
  openModal('animalModal');
}

function renderGallery(urls) {
  const box = document.getElementById('anGallery');
  if (!box) return;
  if (!urls.length) { box.innerHTML = '<div style="font-size:11px;color:#94a3b8">No gallery photos yet.</div>'; return; }
  box.innerHTML = '<div style="font-size:11px;font-weight:700;color:#64748b;margin-bottom:6px">GALLERY (' + urls.length + ')</div>'
    + '<div style="display:flex;gap:8px;flex-wrap:wrap">'
    + urls.map(u => `<img src="${u}" alt="" style="width:60px;height:60px;border-radius:8px;object-fit:cover;border:1px solid #e2e8f0">`).join('')
    + '</div>';
}

async function uploadAnimalPhoto(ev) {
  const file = ev.target.files && ev.target.files[0];
  if (!file || !editAnimalId) return;
  const status = document.getElementById('anUploadStatus');
  status.style.color = '#64748b'; status.textContent = 'Uploading…';
  try {
    const form = new FormData(); form.append('file', file);
    const r = await fetch('/api/v1/animals/' + editAnimalId + '/photos',
      { method:'POST', headers:{ Authorization:'Bearer '+S.token }, body: form });
    const data = await r.json().catch(() => ({}));
    if (!r.ok) throw new Error(data.error || 'Upload failed');
    status.style.color = '#10b981'; status.textContent = '✓ Uploaded';
    if (data.photoUrl) document.getElementById('anPhoto').value = data.photoUrl;
    updateAnPhotoPreview();
    renderGallery(data.photos || []);
    toast('Photo added to gallery');
  } catch (e) {
    status.style.color = '#ef4444'; status.textContent = '✗ ' + e.message;
    toast('Upload failed: ' + e.message, 'error');
  } finally { ev.target.value = ''; }
}

async function saveAnimal() {
  const body = {
    name:          document.getElementById('anName').value.trim(),
    species:       document.getElementById('anSpecies').value,
    breed:         document.getElementById('anBreed').value.trim()  || null,
    age:           parseInt(document.getElementById('anAge').value) || 0,
    sex:           document.getElementById('anSex').value,
    photoUrl:      document.getElementById('anPhoto').value.trim()   || null,
    medicalStatus: document.getElementById('anMedical').value.trim() || null,
    description:   document.getElementById('anDesc').value.trim()    || null,
    shelterId:     1
  };
  if (!body.name) { toast('Name is required','error'); return; }
  try {
    if (editAnimalId) {
      await api('/animals/'+editAnimalId, { method:'PUT', body: JSON.stringify(body) });
      toast('Animal updated! ✅');
    } else {
      await api('/animals', { method:'POST', body: JSON.stringify(body) });
      toast('Animal added! 🐾');
    }
    closeModal('animalModal');
    load();
  } catch (e) { toast(e.message,'error'); }
}

async function archiveAnimal(id, name) {
  if (!confirm('Archive "' + name + '"? It will be hidden from public listings.')) return;
  try {
    await api('/animals/'+id, { method:'DELETE' });
    toast(name+' archived');
    load();
  } catch (e) { toast(e.message,'error'); }
}

init();
