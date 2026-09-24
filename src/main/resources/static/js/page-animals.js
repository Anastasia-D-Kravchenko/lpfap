let currentPage = 0;

async function init() {
  await refreshProfile();
  renderNav('animals');
  loadAnimals();
}

function onFilterChange() { currentPage = 0; loadAnimals(); }

async function loadAnimals() {
  const grid = document.getElementById('animalsGrid');
  grid.innerHTML = '<div class="loading" style="grid-column:1/-1"><div class="spinner"></div><div>Loading animals...</div></div>';
  try {
    const species = document.getElementById('filterSpecies').value;
    const age     = document.getElementById('filterAge').value;
    const search  = document.getElementById('filterSearch').value.toLowerCase().trim();
    const params  = new URLSearchParams({ page: currentPage, size: 12 });
    if (species) params.set('species', species);
    if (age)     params.set('age', age);

    const data = await api('/animals?' + params);
    let animals = data.content || data || [];
    if (search) animals = animals.filter(a =>
      a.name.toLowerCase().includes(search) || (a.breed||'').toLowerCase().includes(search));

    renderAnimals(animals);
    renderPagination(data.totalPages || data.page?.totalPages || 1, currentPage);
  } catch {
    grid.innerHTML = '<div class="empty" style="grid-column:1/-1"><div class="empty-icon">⚠️</div>Failed to load animals. Is the server running?</div>';
  }
}

function renderAnimals(animals) {
  const grid = document.getElementById('animalsGrid');
  if (!animals.length) {
    grid.innerHTML = '<div class="empty" style="grid-column:1/-1"><div class="empty-icon">🐾</div>No animals found matching your filters.</div>';
    return;
  }
  grid.innerHTML = animals.map(a => `
    <a class="animal-card" href="/animal.html?id=${a.id}">
      <div class="card-img-wrap">
        <img src="${animalImg(a)}" alt="${a.name}" loading="lazy"
             onerror="this.src='https://placehold.co/400x220/e2e8f0/94a3b8?text=${encodeURIComponent(a.name)}'">
        <span class="status-badge badge-available">AVAILABLE</span>
      </div>
      <div class="card-body">
        <div class="card-name">${a.name}</div>
        <div class="card-sub">${a.breed || a.species} · ${formatAge(a.age)}</div>
        <div class="card-link">View Profile →</div>
      </div>
    </a>`).join('');
}

function renderPagination(total, current) {
  const pg = document.getElementById('pagination');
  if (total <= 1) { pg.innerHTML = ''; return; }
  let html = '';
  for (let i = 0; i < total; i++) {
    if (i === 0 || i === total - 1 || Math.abs(i - current) <= 1) {
      html += `<div class="pg-btn${i===current?' active':''}" onclick="goPage(${i})">${i+1}</div>`;
    } else if (!html.endsWith('…</div>')) {
      html += `<div class="pg-btn" style="pointer-events:none;border:none;color:#cbd5e1">…</div>`;
    }
  }
  pg.innerHTML = html;
}
function goPage(n) { currentPage = n; loadAnimals(); window.scrollTo(0, 0); }

document.head.insertAdjacentHTML('beforeend',
  '<style>.animal-card{display:block;text-decoration:none;color:inherit}</style>');

init();
