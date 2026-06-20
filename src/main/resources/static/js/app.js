/**
 * app.js — CropPlanner Upgraded
 * Features: Weather widget, AI planting tips, Crop journal/notes, Harvest history chart
 */

// ── Toast ─────────────────────────────────────────────────────────────────────
function toast(message, type = 'success') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        document.body.appendChild(container);
    }
    const colors = { success: '#1B7A3E', error: '#C62828', info: '#1565C0' };
    const icons  = { success: '✓', error: '✕', info: 'ℹ' };
    const t = document.createElement('div');
    t.className = 'toast';
    t.style.background = colors[type] || colors.success;
    t.innerHTML = `<span>${icons[type]||icons.success}</span> ${message}`;
    container.appendChild(t);
    setTimeout(() => { t.style.opacity='0'; t.style.transition='opacity .4s'; }, 3200);
    setTimeout(() => t.remove(), 3700);
}

// ── Loading helper ────────────────────────────────────────────────────────────
function setLoading(btn, loading, orig) {
    btn.disabled = loading;
    btn.textContent = loading ? 'Please wait…' : orig;
}

// ── Boot ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    attachForms();
    checkSession();
});

function attachForms() {
    const reg   = document.getElementById('registerForm');
    const login = document.getElementById('loginForm');
    const sched = document.getElementById('scheduleForm');
    if (reg)   reg.addEventListener('submit',   handleRegister);
    if (login) login.addEventListener('submit', handleLogin);
    if (sched) sched.addEventListener('submit', createSchedule);
}

// ── Session ───────────────────────────────────────────────────────────────────
async function checkSession() {
    try {
        const res  = await fetch('/api/auth/session', { credentials: 'include' });
        const data = await res.json();
        if (data.loggedIn) {
            window._currentUser = data;
            updateNav(true, data.fullName);
            if (document.getElementById('dashboard-content')) loadDashboard();
        } else {
            updateNav(false);
            if (window.location.pathname.includes('dashboard.html'))
                window.location.href = '/login.html';
        }
    } catch (e) {
        if (window.location.pathname.includes('dashboard.html'))
            window.location.href = '/login.html';
    }
}

function updateNav(loggedIn, name) {
    document.querySelectorAll('.guest-only').forEach(el => el.style.display = loggedIn ? 'none' : '');
    document.querySelectorAll('.auth-only').forEach(el  => el.style.display = loggedIn ? ''     : 'none');
    const span = document.getElementById('userNameDisplay');
    if (span && name) span.textContent = name;
}

// ── Register ──────────────────────────────────────────────────────────────────
async function handleRegister(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const orig = btn.textContent;
    setLoading(btn, true, orig);
    const payload = {
        fullName: document.getElementById('fullName').value.trim(),
        email:    document.getElementById('email').value.trim(),
        password: document.getElementById('password').value,
        phone:    document.getElementById('phone')?.value.trim() || '',
        location: document.getElementById('location')?.value.trim() || ''
    };
    try {
        const res  = await fetch('/api/auth/register', { method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body:JSON.stringify(payload) });
        const data = await res.json();
        if (res.ok && data.status === 'success') {
            toast('Account created! Redirecting to login…', 'success');
            setTimeout(() => window.location.href = '/login.html', 1500);
        } else {
            toast(data.message || 'Registration failed.', 'error');
            setLoading(btn, false, orig);
        }
    } catch { toast('Cannot connect to server.', 'error'); setLoading(btn, false, orig); }
}

// ── Login ─────────────────────────────────────────────────────────────────────
async function handleLogin(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button[type="submit"]');
    const orig = btn.textContent;
    setLoading(btn, true, orig);
    const payload = { email: document.getElementById('email').value.trim(), password: document.getElementById('password').value };
    try {
        const res  = await fetch('/api/auth/login', { method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body:JSON.stringify(payload) });
        const data = await res.json();
        if (res.ok && data.status === 'success') {
            sessionStorage.setItem('userFullName', data.fullName);
            toast('Login successful!', 'success');
            setTimeout(() => window.location.href = '/dashboard.html', 800);
        } else {
            toast(data.message || 'Invalid email or password.', 'error');
            setLoading(btn, false, orig);
        }
    } catch { toast('Cannot connect to server.', 'error'); setLoading(btn, false, orig); }
}

// ── Logout ────────────────────────────────────────────────────────────────────
async function logout() {
    try { await fetch('/api/auth/logout', { credentials: 'include' }); } catch (_) {}
    sessionStorage.clear();
    window.location.href = '/index.html';
}

// ── Dashboard ─────────────────────────────────────────────────────────────────
async function loadDashboard() {
    const nameEl = document.getElementById('userNameDisplay');
    if (nameEl) nameEl.textContent = window._currentUser?.fullName || sessionStorage.getItem('userFullName') || 'Farmer';
    await loadStats();
    await loadCrops();
    await loadSchedules();
    loadWeather();
}

async function loadStats() {
    try {
        const res   = await fetch('/api/schedules/dashboard', { credentials: 'include' });
        if (!res.ok) return;
        const stats = await res.json();
        setText('totalSchedules',   stats.totalSchedules);
        setText('upcomingHarvests', stats.upcomingHarvests);
        setText('currentSeason',    stats.currentSeason);
        setText('farmerLocation',   stats.farmerLocation || '—');
        setText('currentSeasonStat', stats.currentSeason);
        if (stats.farmerName) setText('userNameDisplay', stats.farmerName);
    } catch (e) { console.error('loadStats:', e); }
}

async function loadCrops() {
    try {
        const res   = await fetch('/api/crops', { credentials: 'include' });
        if (!res.ok) return;
        const crops = await res.json();
        window._allCrops = crops;
        const sel = document.getElementById('cropSelect');
        if (!sel) return;
        sel.innerHTML = '<option value="" disabled selected>Select a crop…</option>';
        crops.forEach(c => {
            sel.innerHTML += `<option value="${c.id}" data-name="${c.name}" data-season="${c.season}" data-days="${c.growthDurationDays}">${c.name} (${c.season} – ${c.growthDurationDays} days)</option>`;
        });
    } catch (e) { console.error('loadCrops:', e); }
}

async function loadSchedules() {
    try {
        const res = await fetch('/api/schedules', { credentials: 'include' });
        if (res.status === 401) { window.location.href = '/login.html'; return; }
        if (!res.ok) return;
        const schedules = await res.json();
        renderSchedules(schedules);
        renderHarvestChart(schedules);
    } catch (e) { console.error('loadSchedules:', e); }
}

function renderSchedules(schedules) {
    const body = document.getElementById('scheduleTableBody');
    if (!body) return;
    if (!schedules || schedules.length === 0) {
        body.innerHTML = `<tr><td colspan="6" style="padding:36px;text-align:center;color:var(--text-light)">
            No schedules yet. Click <strong>Plan New Crop</strong> to get started! 🌱
        </td></tr>`;
        return;
    }
    body.innerHTML = schedules.map(s => {
        const isHarvested = s.status === 'Harvested';
        const badge = isHarvested
            ? '<span class="badge badge-success">✓ Harvested</span>'
            : '<span class="badge badge-warning">🌱 Growing</span>';
        const actions = isHarvested
            ? `<button class="action-btn action-btn-notes" onclick="openNotesModal(${s.id},'${escHtml(s.cropName)}')">📓 Notes</button>
               <button class="action-btn action-btn-delete" onclick="deleteSchedule(${s.id})">✕ Delete</button>`
            : `<button class="action-btn action-btn-harvest" onclick="markHarvested(${s.id})">✓ Harvested</button>
               <button class="action-btn action-btn-notes" onclick="openNotesModal(${s.id},'${escHtml(s.cropName)}')">📓 Notes</button>
               <button class="action-btn action-btn-delete" onclick="deleteSchedule(${s.id})">✕ Delete</button>`;
        return `<tr>
            <td>
                <strong>${s.cropName}</strong>
                <span class="season-badge ${s.cropSeason}" style="margin-left:6px">${s.cropSeason}</span>
            </td>
            <td>${s.plantingDate}</td>
            <td><strong>${s.expectedHarvestDate}</strong></td>
            <td>${s.growthDays} days</td>
            <td>${badge}</td>
            <td style="display:flex;gap:6px;flex-wrap:wrap;padding-top:10px">${actions}</td>
        </tr>`;
    }).join('');
}

function escHtml(str) { return String(str).replace(/'/g,"&#39;").replace(/"/g,"&quot;"); }

// ── Harvest History Chart ─────────────────────────────────────────────────────
function renderHarvestChart(schedules) {
    const wrap = document.getElementById('harvestChartWrap');
    if (!wrap) return;
    const harvested = schedules.filter(s => s.status === 'Harvested');
    if (harvested.length === 0) {
        wrap.innerHTML = '<p class="chart-empty">No harvest data yet. Mark schedules as harvested to see your chart.</p>';
        return;
    }
    // Count by crop name
    const counts = {};
    harvested.forEach(s => { counts[s.cropName] = (counts[s.cropName] || 0) + 1; });
    const max = Math.max(...Object.values(counts));
    const rows = Object.entries(counts)
        .sort((a,b) => b[1]-a[1])
        .map(([name, count]) => {
            const pct = Math.round((count / max) * 100);
            return `<div class="chart-bar-row">
                <div class="chart-bar-label">${name}</div>
                <div class="chart-bar-outer">
                    <div class="chart-bar-inner" style="width:${pct}%">
                        <span>${count}</span>
                    </div>
                </div>
            </div>`;
        }).join('');
    wrap.innerHTML = `<div class="chart-bar-wrap">${rows}</div>`;
    // Animate bars in after paint
    requestAnimationFrame(() => {
        wrap.querySelectorAll('.chart-bar-inner').forEach(bar => {
            const w = bar.style.width; bar.style.width = '0';
            requestAnimationFrame(() => bar.style.width = w);
        });
    });
}

// ── Create Schedule ───────────────────────────────────────────────────────────
async function createSchedule(e) {
    e.preventDefault();
    const btn  = e.target.querySelector('button[type="submit"]');
    const orig = btn.textContent;
    setLoading(btn, true, orig);
    const cropId       = document.getElementById('cropSelect').value;
    const plantingDate = document.getElementById('plantingDate').value;
    const notes        = document.getElementById('scheduleNotes')?.value.trim() || '';
    if (!cropId || !plantingDate) {
        toast('Please select a crop and date.', 'error');
        setLoading(btn, false, orig); return;
    }
    try {
        const res  = await fetch('/api/schedules', { method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body:JSON.stringify({ cropId: parseInt(cropId,10), plantingDate, notes }) });
        const data = await res.json();
        if (res.ok && data.status === 'success') {
            closeModal('scheduleModal');
            toast(`Schedule saved! Harvest: ${data.data}`, 'success');
            loadSchedules(); loadStats();
        } else { toast(data.message || 'Failed to save schedule.', 'error'); }
    } catch { toast('Server error. Please try again.', 'error'); }
    finally   { setLoading(btn, false, orig); }
}

async function markHarvested(id) {
    if (!confirm('Mark this crop as Harvested?')) return;
    try {
        const res  = await fetch(`/api/schedules/${id}/status`, { method:'PUT', headers:{'Content-Type':'application/json'}, credentials:'include', body:JSON.stringify({ id, status:'Harvested' }) });
        const data = await res.json();
        if (res.ok) { toast('Marked as Harvested! 🌾', 'success'); loadSchedules(); loadStats(); }
        else toast(data.message || 'Update failed.', 'error');
    } catch { toast('Server error.', 'error'); }
}

async function deleteSchedule(id) {
    if (!confirm('Delete this schedule? This cannot be undone.')) return;
    try {
        const res  = await fetch(`/api/schedules/${id}`, { method:'DELETE', credentials:'include' });
        const data = await res.json();
        if (res.ok) { toast('Schedule deleted.', 'info'); loadSchedules(); loadStats(); }
        else toast(data.message || 'Delete failed.', 'error');
    } catch { toast('Server error.', 'error'); }
}

// ── Modal helpers ─────────────────────────────────────────────────────────────
function openModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.add('active');
    const d = document.getElementById('plantingDate');
    if (d) d.valueAsDate = new Date();
}
function closeModal(id) {
    const m = document.getElementById(id);
    if (m) m.classList.remove('active');
}

// ── Crop Notes / Journal ──────────────────────────────────────────────────────
let _currentNotesId = null;

function openNotesModal(scheduleId, cropName) {
    _currentNotesId = scheduleId;
    setText('notesScheduleLabel', cropName);
    document.getElementById('newNoteText').value = '';
    renderNotesList(scheduleId);
    openModal('notesModal');
}

function getNotes(scheduleId) {
    try {
        const raw = localStorage.getItem(`crop_notes_${scheduleId}`);
        return raw ? JSON.parse(raw) : [];
    } catch { return []; }
}

function saveNotes(scheduleId, notes) {
    localStorage.setItem(`crop_notes_${scheduleId}`, JSON.stringify(notes));
}

function renderNotesList(scheduleId) {
    const list  = document.getElementById('notesList');
    if (!list) return;
    const notes = getNotes(scheduleId);
    if (notes.length === 0) {
        list.innerHTML = '<div class="notes-empty">📝 No notes yet. Add your first observation below.</div>';
        return;
    }
    list.innerHTML = notes.slice().reverse().map(n => `
        <div class="note-entry">
            <div class="note-date">${new Date(n.date).toLocaleDateString('en-ZA', { day:'numeric', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' })}</div>
            <div class="note-text">${escHtml(n.text)}</div>
        </div>`).join('');
}

function saveNote() {
    const text = document.getElementById('newNoteText').value.trim();
    if (!text) { toast('Please enter a note.', 'error'); return; }
    const notes = getNotes(_currentNotesId);
    notes.push({ date: new Date().toISOString(), text });
    saveNotes(_currentNotesId, notes);
    document.getElementById('newNoteText').value = '';
    renderNotesList(_currentNotesId);
    toast('Note saved! 📓', 'success');
}

// ── AI Planting Tips ──────────────────────────────────────────────────────────
let _aiTipCache = {};

async function fetchAiTip() {
    const sel = document.getElementById('cropSelect');
    const opt = sel.options[sel.selectedIndex];
    if (!opt || !opt.value) return;

    const cropName   = opt.dataset.name   || opt.text.split(' (')[0];
    const cropSeason = opt.dataset.season || '';
    const cropDays   = opt.dataset.days   || '';

    const box     = document.getElementById('aiTipBox');
    const content = document.getElementById('aiTipContent');
    if (!box || !content) return;
    box.style.display = 'block';

    // Check cache
    if (_aiTipCache[opt.value]) {
        content.innerHTML = `<p>${_aiTipCache[opt.value]}</p>`;
        return;
    }

    content.innerHTML = `<div class="ai-tip-loading"><div class="dot-pulse"><span></span><span></span><span></span></div> Generating tip…</div>`;

    try {
        const response = await fetch('https://api.anthropic.com/v1/messages', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                model: 'claude-sonnet-4-6',
                max_tokens: 1000,
                messages: [{
                    role: 'user',
                    content: `You are an expert agricultural advisor for South African small-scale farmers.
Give a concise, practical planting tip (3-4 sentences) for: ${cropName}.
Season: ${cropSeason}. Growth duration: ${cropDays} days.
Cover: best soil conditions, watering frequency, one common challenge to watch for, and a quick yield tip.
Be friendly, practical, and specific to South African conditions. Plain text only, no markdown.`
                }]
            })
        });
        const data = await response.json();
        const tip  = data.content?.[0]?.text || 'Could not generate tip. Please try again.';
        _aiTipCache[opt.value] = tip;
        content.innerHTML = `<p>${tip}</p>`;
    } catch (err) {
        content.innerHTML = `<p style="color:var(--text-muted)">Could not load AI tip right now. Proceed with your schedule.</p>`;
    }
}

// ── Weather Widget ────────────────────────────────────────────────────────────
function loadWeather() {
    if (!document.getElementById('weatherWidget')) return;
    if (!navigator.geolocation) { showWeatherFallback(); return; }
    navigator.geolocation.getCurrentPosition(
        pos => fetchWeather(pos.coords.latitude, pos.coords.longitude),
        ()  => showWeatherFallback(),
        { timeout: 8000 }
    );
}

async function fetchWeather(lat, lon) {
    try {
        // Open-Meteo: free, no API key needed
        const url = `https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m&timezone=auto`;
        const res  = await fetch(url);
        const data = await res.json();
        const c    = data.current;

        // Reverse geocode via nominatim
        let locationName = 'Your Location';
        try {
            const geo  = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json`);
            const gData = await geo.json();
            locationName = gData.address?.city || gData.address?.town || gData.address?.village || gData.address?.county || 'Your Location';
        } catch {}

        displayWeather({
            temp:     Math.round(c.temperature_2m),
            feels:    Math.round(c.apparent_temperature),
            humidity: c.relative_humidity_2m,
            wind:     Math.round(c.wind_speed_10m),
            code:     c.weather_code,
            location: locationName
        });
    } catch { showWeatherFallback(); }
}

function weatherCodeToIcon(code) {
    if (code === 0)              return '☀️';
    if (code <= 2)               return '⛅';
    if (code <= 3)               return '☁️';
    if (code <= 48)              return '🌫️';
    if (code <= 57)              return '🌧️';
    if (code <= 67)              return '🌧️';
    if (code <= 77)              return '❄️';
    if (code <= 82)              return '🌦️';
    if (code <= 86)              return '🌨️';
    if (code <= 99)              return '⛈️';
    return '🌤️';
}

function weatherCodeToDesc(code) {
    if (code === 0)  return 'Clear sky';
    if (code <= 2)   return 'Partly cloudy';
    if (code <= 3)   return 'Overcast';
    if (code <= 48)  return 'Foggy';
    if (code <= 57)  return 'Drizzle';
    if (code <= 67)  return 'Rain';
    if (code <= 77)  return 'Snow';
    if (code <= 82)  return 'Rain showers';
    if (code <= 86)  return 'Snow showers';
    if (code <= 99)  return 'Thunderstorm';
    return 'Cloudy';
}

function getFarmingTip(code, temp) {
    if (code === 0 && temp > 25) return '🌞 Hot and sunny — ideal for watering early morning or late afternoon to reduce evaporation.';
    if (code === 0)              return '☀️ Clear conditions — great day for field work, planting, or inspecting your crops.';
    if (code <= 2)               return '⛅ Mild conditions — good day for transplanting seedlings or applying foliar feeds.';
    if (code <= 57)              return '🌧️ Drizzle expected — skip irrigation today, natural moisture is sufficient.';
    if (code <= 67)              return '🌧️ Rain today — hold off on pesticide/fertiliser application until it clears.';
    if (code <= 82)              return '🌦️ Showers likely — secure any loose covers on tunnel crops or seedling trays.';
    if (code <= 99)              return '⛈️ Thunderstorms possible — stay safe, avoid open fields, and check for drainage issues after.';
    return '🌿 Check your crops today and ensure irrigation systems are working properly.';
}

function displayWeather({ temp, feels, humidity, wind, code, location }) {
    document.getElementById('weatherLoading').style.display = 'none';
    document.getElementById('weatherData').style.display    = 'block';
    setText('wTemp',     `${temp}°C`);
    setText('wDesc',     weatherCodeToDesc(code));
    setText('wLocation', location);
    setText('wHumidity', humidity);
    setText('wWind',     wind);
    setText('wFeels',    feels);
    setText('wIcon',     weatherCodeToIcon(code));
    setText('wTip',      getFarmingTip(code, temp));
}

function showWeatherFallback() {
    const widget = document.getElementById('weatherWidget');
    if (!widget) return;
    widget.querySelector('#weatherLoading').textContent = '📍 Allow location access to see live weather for your farm.';
}

// ── Util ──────────────────────────────────────────────────────────────────────
function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}
