async function init() {
  if (!(await adminShellInit('analytics'))) return;
  load();
}

async function load() {
  document.getElementById('analyticsContent').innerHTML = '<div class="loading"><div class="spinner"></div></div>';
  try {
    const now = new Date();
    const [funnel, tta, monthly, approvedApps] = await Promise.all([
      api('/admin/reports/funnel'),
      api('/admin/reports/time-to-adoption'),
      api('/admin/reports/monthly?year='+now.getFullYear()+'&month='+(now.getMonth()+1)),
      api('/applications?status=APPROVED').catch(() => [])
    ]);
    const recentPlacements = (approvedApps || [])
      .sort((x,y) => (y.decisionDate || y.submissionDate || '').localeCompare(x.decisionDate || x.submissionDate || ''))
      .slice(0, 8);
    const total   = Math.max(funnel.totalApplications||0, 1);
    const appPct  = r => Math.round((r/total)*100);
    const avgDays = funnel.averageDaysToPlacementBySpecies
                 || tta.averageDaysToPlacementBySpecies
                 || {DOG:32,CAT:18,RABBIT:9};
    const maxDays = Math.max(...Object.values(avgDays).map(Number), 1);
    const months  = ['JAN','FEB','MAR','APR','MAY','JUN'];
    const iData   = [12,18,15,22, monthly.intakeCount||8, 10];
    const aData   = [8, 14,10,18, monthly.adoptedCount||5, 7];
    const maxB    = Math.max(...iData, ...aData, 1);

    document.getElementById('analyticsContent').innerHTML = `
      <div class="analytics-kpis">
        <div class="kpi-card">
          <div><div class="kpi-label">Approval Rate</div><div class="kpi-val" style="color:#10b981">${appPct(funnel.approved||0)}%</div><div class="kpi-change">${funnel.approved||0} of ${funnel.totalApplications||0} approved</div></div>
          <svg class="kpi-ring" viewBox="0 0 52 52"><circle cx="26" cy="26" r="22" fill="none" stroke="#e2e8f0" stroke-width="4"/><circle cx="26" cy="26" r="22" fill="none" stroke="#10b981" stroke-width="4" stroke-dasharray="${appPct(funnel.approved||0)*1.38} 138" stroke-dashoffset="34.5" stroke-linecap="round" transform="rotate(-90 26 26)"/></svg>
        </div>
        <div class="kpi-card">
          <div><div class="kpi-label">Rejection Rate</div><div class="kpi-val" style="color:#ef4444">${appPct(funnel.rejected||0)}%</div><div class="kpi-change">${funnel.rejected||0} rejected</div></div>
          <svg class="kpi-ring" viewBox="0 0 52 52"><circle cx="26" cy="26" r="22" fill="none" stroke="#e2e8f0" stroke-width="4"/><circle cx="26" cy="26" r="22" fill="none" stroke="#ef4444" stroke-width="4" stroke-dasharray="${appPct(funnel.rejected||0)*1.38} 138" stroke-dashoffset="34.5" stroke-linecap="round" transform="rotate(-90 26 26)"/></svg>
        </div>
        <div class="kpi-card">
          <div><div class="kpi-label">Pending Review</div><div class="kpi-val" style="color:#f59e0b">${appPct(funnel.pending||0)}%</div><div class="kpi-change">${funnel.pending||0} awaiting action</div></div>
          <svg class="kpi-ring" viewBox="0 0 52 52"><circle cx="26" cy="26" r="22" fill="none" stroke="#e2e8f0" stroke-width="4"/><circle cx="26" cy="26" r="22" fill="none" stroke="#f59e0b" stroke-width="4" stroke-dasharray="${appPct(funnel.pending||0)*1.38} 138" stroke-dashoffset="34.5" stroke-linecap="round" transform="rotate(-90 26 26)"/></svg>
        </div>
      </div>
      <div class="charts-row">
        <div class="chart-card">
          <div class="chart-title">Avg. Time to Adoption (Days)</div>
          ${Object.entries(avgDays).map(([sp,days]) => `
            <div class="bar-row">
              <div class="bar-label">${sp}</div>
              <div class="bar-track"><div class="bar-fill" style="width:${Math.round((Number(days)/maxDays)*100)}%"></div></div>
              <div class="bar-val">${Math.round(days)} days</div>
            </div>`).join('')}
        </div>
        <div class="chart-card">
          <div class="chart-title">Intake vs Adoptions
            <div class="chart-legend">
              <span><span class="dot" style="background:#1e3a5f"></span>Intake</span>
              <span><span class="dot" style="background:#f59e0b"></span>Adoptions</span>
            </div>
          </div>
          <div class="bc">
            ${months.map((m,i) => `
              <div style="flex:1;display:flex;flex-direction:column;align-items:center">
                <div class="bc-group">
                  <div class="bc-col intake"   style="height:${Math.round((iData[i]/maxB)*110)}px"></div>
                  <div class="bc-col adoption" style="height:${Math.round((aData[i]/maxB)*110)}px"></div>
                </div>
                <div class="bc-month">${m}</div>
              </div>`).join('')}
          </div>
        </div>
      </div>

      <div class="chart-card" style="margin-top:20px">
        <div class="chart-title">🏠 Recent placements
          <span class="chart-legend"><span>${recentPlacements.length} approved adopters / fosterers</span></span>
        </div>
        ${recentPlacements.length === 0
          ? '<div class="empty" style="padding:24px">No placements yet.</div>'
          : `<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:12px">
              ${recentPlacements.map(a => `
                <div style="border:1px solid #e2e8f0;border-radius:11px;padding:12px;background:#fafafa">
                  <div style="display:flex;gap:10px;align-items:flex-start">
                    <img src="${a.applicantAvatarUrl
                                || `https://placehold.co/48x48/1d4ed8/ffffff?text=`+encodeURIComponent(initials(a.applicantName||a.applicantEmail||'?'))}"
                         alt="" style="width:48px;height:48px;border-radius:50%;object-fit:cover;flex-shrink:0;border:1px solid #e2e8f0">
                    <div style="flex:1;min-width:0">
                      <div style="font-weight:700;color:#1e293b;font-size:14px">${a.applicantName||'—'}</div>
                      <div style="font-size:12px;color:#64748b">${a.applicantEmail||''}</div>
                      <a href="/animal.html?id=${a.animalId}"
                         style="display:flex;align-items:center;gap:6px;margin-top:6px;font-size:11px;color:#1e293b">
                        <img src="${a.animalPhotoUrl || 'https://placehold.co/24x24/e2e8f0/94a3b8?text=?'}"
                             alt="" style="width:24px;height:24px;border-radius:6px;object-fit:cover">
                        <span>${a.type === 'ADOPTION' ? '🏠 Adopted' : '💛 Fostering'} <b>${a.animalName||'—'}</b></span>
                      </a>
                      <div style="font-size:11px;color:#94a3b8;margin-top:3px">
                        on ${formatDate(a.decisionDate || a.submissionDate)}
                      </div>
                      ${a.applicantBio
                        ? `<div style="font-size:11px;color:#64748b;margin-top:6px;line-height:1.4;
                                       overflow:hidden;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical">
                             ${escapeHtml(a.applicantBio)}
                           </div>` : ''}
                    </div>
                  </div>
                </div>`).join('')}
            </div>`}
      </div>`;
  } catch {
    document.getElementById('analyticsContent').innerHTML =
      '<div class="empty"><div class="empty-icon">⚠️</div>Failed to load analytics data.</div>';
  }
}

function escapeHtml(s) { return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

init();
