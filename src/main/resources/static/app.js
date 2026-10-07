// Zoo Animal Management Frontend Logic with Spring Security & Real RBAC Login/Register
const API_BASE = '/api';

// RBAC Configuration & Credentials
const ROLE_METADATA = {
    ADMIN: { label: 'מנהל ראשי (Admin)', icon: '👑', dotColor: '#a855f7', key: 'admin', defaultCreds: { username: 'admin', password: 'admin123' } },
    VET: { label: 'רופא וטרינר (Vet)', icon: '🩺', dotColor: '#f59e0b', key: 'vet', defaultCreds: { username: 'vet', password: 'vet123' } },
    KEEPER: { label: 'מטפל חיות (Keeper)', icon: '🥩', dotColor: '#10b981', key: 'keeper', defaultCreds: { username: 'keeper', password: 'keeper123' } },
    GUEST: { label: 'אורח (צפייה בלבד)', icon: '👤', dotColor: '#94a3b8', key: 'guest', defaultCreds: null }
};

// Current Session State
let currentUser = {
    authenticated: false,
    username: 'guest',
    fullName: 'אורח',
    role: 'GUEST',
    roleAuthority: 'ROLE_GUEST'
};

// Active credentials stored for HTTP requests
let activeCredentials = null; // { username, password }

// Central API Fetch Wrapper with Basic Auth & 401/403 Handling
async function apiFetch(url, options = {}) {
    const headers = { ...(options.headers || {}) };

    if (activeCredentials && activeCredentials.username && activeCredentials.password) {
        const basicAuth = btoa(`${activeCredentials.username}:${activeCredentials.password}`);
        headers['Authorization'] = `Basic ${basicAuth}`;
    }

    try {
        const response = await fetch(url, { ...options, headers });

        if (response.status === 401) {
            showToast('🔒 שגיאת אימות (401): נדרשת התחברות לביצוע פעולה זו במערכת', 'error');
        } else if (response.status === 403) {
            const roleTitle = currentUser && currentUser.role ? (ROLE_METADATA[currentUser.role]?.label || currentUser.role) : 'אורח';
            showToast(`⛔ שגיאת הרשאה (403): אין לך הרשאה מתאימה לביצוע פעולה זו בתפקיד "${roleTitle}"`, 'error');
        }

        return response;
    } catch (err) {
        showToast('שגיאת תקשורת עם השרת: ' + err.message, 'error');
        throw err;
    }
}

// State
let allAnimals = [];
let metadata = {
    species: [],
    subSpecies: [],
    healthStatuses: [],
    dietTypes: [],
    conservationStatuses: [],
    genders: []
};
let allCages = [];
let currentFilter = {
    species: '',
    health: '',
    diet: '',
    endangered: false,
    cageId: '',
    search: ''
};

// Emoji map for species / subspecies
const EMOJI_MAP = {
    LION: '🦁', TIGER: '🐯', LEOPARD: '🐆', PANTHER: '🐈‍⬛', CHEETAH: '🐆', JAGUAR: '🐆',
    CHIMPANZEE: '🐒', GORILLA: '🦍', ORANGUTAN: '🦧', LEMUR: '🦊',
    EAGLE: '🦅', PARROT: '🦜', FLAMINGO: '🦩', PENGUIN: '🐧', OWL: '🦉',
    PYTHON: '🐍', CROCODILE: '🐊', IGUANA: '🦎', CHAMELEON: '🦎', TORTOISE: '🐢',
    ELEPHANT: '🐘', GIRAFFE: '🦒', ZEBRA: '🦓', KANGAROO: '🦘', PANDA: '🐼', KOALA: '🐨',
    DOLPHIN: '🐬', SEAL: '🦭', SHARK: '🦈', SEA_TURTLE: '🐢', OTTER: '🦦',
    FROG: '🐸', SALAMANDER: '🦎',
    FELINE: '🐱', PRIMATE: '🐵', BIRD: '🐦', REPTILE: '🦎', MAMMAL: '🐾', AQUATIC: '🌊', AMPHIBIAN: '🐸'
};

function getEmoji(animal) {
    if (animal.subSpecies && EMOJI_MAP[animal.subSpecies]) return EMOJI_MAP[animal.subSpecies];
    if (animal.species && EMOJI_MAP[animal.species]) return EMOJI_MAP[animal.species];
    return '🐾';
}

// Preloaded Photorealistic Images Map for All 20 Zoo Animals
const ANIMAL_IMAGE_MAP = {
    'Simba': 'images/simba.jpg',
    'Nala': 'images/nala.jpg',
    'Shere Khan': 'images/shere_khan.jpg',
    'Bagheera': 'images/bagheera.jpg',
    'George': 'images/george.jpg',
    'Koko': 'images/koko.jpg',
    'King Julien': 'images/king_julien.jpg',
    'Majestic': 'images/majestic.jpg',
    'Rio': 'images/rio.jpg',
    'Pinky': 'images/pinky.jpg',
    'Pingu': 'images/pingu.jpg',
    'Flipper': 'images/flipper.jpg',
    'Crush': 'images/crush.jpg',
    'Dumbo': 'images/dumbo.jpg',
    'Melman': 'images/melman.jpg',
    'Marty': 'images/marty.jpg',
    'Po': 'images/po.jpg',
    'Kaa': 'images/kaa.jpg',
    'Pascal': 'images/pascal.jpg',
    'Kermit': 'images/kermit.jpg'
};

function getAnimalImage(animal) {
    if (animal.imageUrl && animal.imageUrl.trim() !== '') {
        return animal.imageUrl;
    }
    if (animal.name && ANIMAL_IMAGE_MAP[animal.name]) {
        return ANIMAL_IMAGE_MAP[animal.name];
    }
    return null;
}

// Initialization
document.addEventListener('DOMContentLoaded', async () => {
    initEventListeners();
    initAuthControls();
    initNavigationAndNewFeatures();
    await checkInitialSession();
    await loadMetadata();
    await loadCages();
    await loadStats();
    await loadAnimals();
    await loadAlerts();
    await loadTasks();
});

// Check Session from localStorage on startup
async function checkInitialSession() {
    const saved = localStorage.getItem('zoo_auth');
    if (!saved) {
        window.location.replace('login.html');
        return;
    }

    try {
        const parsed = JSON.parse(saved);
        if (parsed && parsed.username && parsed.password) {
            activeCredentials = { username: parsed.username, password: parsed.password };
            const res = await apiFetch(`${API_BASE}/auth/me`);
            if (res && res.ok) {
                const data = await res.json();
                if (data && data.authenticated) {
                    applySession(data, activeCredentials);
                    return;
                }
            }
        }
    } catch (e) {
        console.error('Error restoring session', e);
    }

    // If session is invalid, clear and redirect to login.html
    localStorage.removeItem('zoo_auth');
    activeCredentials = null;
    window.location.replace('login.html');
}

// ==========================================
// Auth Controls & UI Wiring
// ==========================================
function initAuthControls() {
    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', handleLogout);
    }

    // Role bar buttons (direct listeners)
    document.querySelectorAll('.btn-role').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const role = btn.dataset.role;
            if (role) {
                quickSwitchRole(role);
            }
        });
    });
}

// Quick switch between default roles from the role bar
async function quickSwitchRole(roleKey) {
    let roleEnum = 'GUEST';
    let creds = null;

    if (roleKey === 'admin') {
        roleEnum = 'ADMIN';
        creds = { username: 'admin', password: 'admin123' };
    } else if (roleKey === 'vet') {
        roleEnum = 'VET';
        creds = { username: 'vet', password: 'vet123' };
    } else if (roleKey === 'keeper') {
        roleEnum = 'KEEPER';
        creds = { username: 'keeper', password: 'keeper123' };
    } else {
        roleEnum = 'GUEST';
        creds = null;
    }

    activeCredentials = creds;

    if (creds) {
        localStorage.setItem('zoo_auth', JSON.stringify(creds));
        try {
            const res = await apiFetch(`${API_BASE}/auth/me`);
            if (res && res.ok) {
                const data = await res.json();
                applySession(data, creds);
                showToast(`התחברת בהצלחה בתפקיד: ${ROLE_METADATA[roleEnum]?.label || roleEnum}`, 'success');
            } else {
                applyFallbackRole(roleEnum, creds.username);
            }
        } catch (e) {
            applyFallbackRole(roleEnum, creds.username);
        }
    } else {
        localStorage.removeItem('zoo_auth');
        applySession({ authenticated: false, username: 'guest', fullName: 'אורח', role: 'GUEST', roleAuthority: 'ROLE_GUEST' }, null);
        showToast('מחובר כעת כאורח (צפייה בלבד)', 'info');
    }
}

function applyFallbackRole(roleEnum, username) {
    const meta = ROLE_METADATA[roleEnum];
    applySession({
        authenticated: true,
        username: username,
        fullName: username,
        role: roleEnum,
        roleAuthority: meta ? meta.key : 'ROLE_GUEST'
    }, activeCredentials);
}

function applySession(userData, creds) {
    currentUser = {
        authenticated: !!userData.authenticated,
        username: userData.username || 'guest',
        fullName: userData.fullName || userData.username || 'אורח',
        role: userData.role || (userData.roleAuthority === 'ROLE_ADMIN' ? 'ADMIN' : userData.roleAuthority === 'ROLE_VET' ? 'VET' : userData.roleAuthority === 'ROLE_KEEPER' ? 'KEEPER' : 'GUEST'),
        roleAuthority: userData.roleAuthority || 'ROLE_GUEST'
    };
    activeCredentials = creds;

    updateAuthUI();
}

function updateAuthUI() {
    const guestControls = document.getElementById('authGuestControls');
    const userControls = document.getElementById('authUserControls');
    const greetingAvatar = document.getElementById('userGreetingAvatar');
    const greetingName = document.getElementById('userGreetingName');
    const greetingRole = document.getElementById('userGreetingRole');
    const roleBadgeLabel = document.getElementById('currentRoleLabel');
    const roleBadgeDot = document.querySelector('.badge-status-dot');

    const meta = ROLE_METADATA[currentUser.role] || ROLE_METADATA.GUEST;

    // Update Role Selector Buttons
    document.querySelectorAll('.btn-role').forEach(b => {
        b.classList.toggle('active', b.dataset.role === meta.key);
    });

    // Update Role Selector Badge
    if (roleBadgeLabel) {
        roleBadgeLabel.textContent = `מחובר כ-${meta.label}`;
    }
    if (roleBadgeDot) {
        roleBadgeDot.style.background = meta.dotColor;
        roleBadgeDot.style.boxShadow = `0 0 8px ${meta.dotColor}`;
    }

    // Update Header Controls
    if (currentUser.authenticated) {
        if (guestControls) guestControls.style.display = 'none';
        if (userControls) userControls.style.display = 'inline-flex';

        if (greetingAvatar) greetingAvatar.textContent = meta.icon;
        if (greetingName) greetingName.textContent = currentUser.fullName;
        if (greetingRole) greetingRole.textContent = meta.label;
    } else {
        if (guestControls) guestControls.style.display = 'inline-flex';
        if (userControls) userControls.style.display = 'none';
    }
}

// ==========================================
// Logout & Session Handling
// ==========================================
async function handleLogout() {
    try {
        await apiFetch(`${API_BASE}/auth/logout`, { method: 'POST' });
    } catch (e) {
        console.warn('Logout API error', e);
    }

    localStorage.removeItem('zoo_auth');
    activeCredentials = null;
    showToast('🚪 מתנתק מהמערכת ומעביר לדף ההתחברות...', 'info');
    setTimeout(() => {
        window.location.replace('login.html');
    }, 450);
}

// ==========================================
// Regular Event Listeners & CRUD
// ==========================================
function initEventListeners() {
    // Search
    const searchInput = document.getElementById('searchInput');
    let debounceTimer;
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentFilter.search = e.target.value.trim();
                loadAnimals();
            }, 300);
        });
    }

    // Health filter
    const healthFilter = document.getElementById('healthFilter');
    if (healthFilter) {
        healthFilter.addEventListener('change', (e) => {
            currentFilter.health = e.target.value;
            loadAnimals();
        });
    }

    // Diet filter
    const dietFilter = document.getElementById('dietFilter');
    if (dietFilter) {
        dietFilter.addEventListener('change', (e) => {
            currentFilter.diet = e.target.value;
            loadAnimals();
        });
    }

    // Cage filter
    const cageFilter = document.getElementById('cageFilter');
    if (cageFilter) {
        cageFilter.addEventListener('change', (e) => {
            currentFilter.cageId = e.target.value;
            loadAnimals();
        });
    }

    // Endangered toggle
    const endangeredToggle = document.getElementById('endangeredToggle');
    if (endangeredToggle) {
        endangeredToggle.addEventListener('change', (e) => {
            currentFilter.endangered = e.target.checked;
            loadAnimals();
        });
    }

    // Add animal button
    const btnAddAnimal = document.getElementById('btnAddAnimal');
    if (btnAddAnimal) {
        btnAddAnimal.addEventListener('click', () => {
            openAnimalModal();
        });
    }

    // Refresh button
    const btnRefresh = document.getElementById('btnRefresh');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            loadStats();
            loadAnimals();
            showToast('הנתונים רועננו בהצלחה', 'success');
        });
    }

    // Animal form species change (filters subspecies)
    const modalAnimalSpecies = document.getElementById('modalAnimalSpecies');
    if (modalAnimalSpecies) {
        modalAnimalSpecies.addEventListener('change', (e) => {
            populateSubSpeciesDropdown(e.target.value);
        });
    }

    // Save animal submit
    const animalForm = document.getElementById('animalForm');
    if (animalForm) {
        animalForm.addEventListener('submit', handleSaveAnimal);
    }

    // Feed form submit
    const feedForm = document.getElementById('feedForm');
    if (feedForm) {
        feedForm.addEventListener('submit', handleSaveFeeding);
    }

    // Medical form submit
    const medicalForm = document.getElementById('medicalForm');
    if (medicalForm) {
        medicalForm.addEventListener('submit', handleSaveMedical);
    }
}

// API Calls
async function loadMetadata() {
    try {
        const res = await apiFetch(`${API_BASE}/animals/metadata`);
        if (res && res.ok) {
            metadata = await res.json();
            renderSpeciesTabs();
            populateMetadataDropdowns();
        }
    } catch (err) {
        console.error('Failed to load metadata', err);
    }
}

async function loadCages() {
    try {
        const res = await apiFetch(`${API_BASE}/cages`);
        if (res && res.ok) {
            allCages = await res.json();
            populateCagesDropdown();
        }
    } catch (err) {
        console.error('Failed to load cages', err);
    }
}

async function loadStats() {
    try {
        const res = await apiFetch(`${API_BASE}/animals/stats`);
        if (res && res.ok) {
            const stats = await res.json();
            const totalEl = document.getElementById('statTotalAnimals');
            const endEl = document.getElementById('statEndangered');
            const hungryEl = document.getElementById('statHungry');
            const medEl = document.getElementById('statMedicalAttention');
            const weightEl = document.getElementById('statAvgWeight');
            const ageEl = document.getElementById('statAvgAge');

            if (totalEl) totalEl.textContent = stats.totalAnimals;
            if (endEl) endEl.textContent = stats.endangeredCount;
            if (hungryEl) hungryEl.textContent = stats.hungryCount;
            if (medEl) medEl.textContent = (stats.sickOrInjuredCount + stats.quarantinedCount + stats.observationCount);
            if (weightEl) weightEl.textContent = `${stats.averageWeightKg} ק"ג`;
            if (ageEl) ageEl.textContent = `${stats.averageAge} שנים`;
        }
    } catch (err) {
        console.error('Failed to load stats', err);
    }
}

async function loadAnimals() {
    try {
        const params = new URLSearchParams();
        if (currentFilter.species) params.append('species', currentFilter.species);
        if (currentFilter.health) params.append('health', currentFilter.health);
        if (currentFilter.diet) params.append('diet', currentFilter.diet);
        if (currentFilter.endangered) params.append('endangered', 'true');
        if (currentFilter.cageId) params.append('cageId', currentFilter.cageId);
        if (currentFilter.search) params.append('q', currentFilter.search);

        const url = `${API_BASE}/animals?${params.toString()}`;
        const res = await apiFetch(url);
        if (res && res.ok) {
            allAnimals = await res.json();
            renderAnimalsList(allAnimals);
        }
    } catch (err) {
        console.error('Failed to load animals', err);
        showToast('שגיאה בטעינת החיות', 'error');
    }
}

// Rendering
function renderSpeciesTabs() {
    const container = document.getElementById('speciesTabs');
    if (!container) return;

    container.innerHTML = `<button class="tab-btn active" data-species="">🐾 כל המינים</button>`;

    if (metadata.species) {
        metadata.species.forEach(s => {
            const btn = document.createElement('button');
            btn.className = 'tab-btn';
            btn.dataset.species = s.key;
            btn.textContent = `${EMOJI_MAP[s.key] || '🐾'} ${s.label}`;
            btn.addEventListener('click', () => {
                container.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                currentFilter.species = s.key;
                loadAnimals();
            });
            container.appendChild(btn);
        });
    }

    container.querySelector('.tab-btn').addEventListener('click', (e) => {
        container.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        e.target.classList.add('active');
        currentFilter.species = '';
        loadAnimals();
    });
}

function populateMetadataDropdowns() {
    // Health filter
    const healthSelect = document.getElementById('healthFilter');
    if (healthSelect && metadata.healthStatuses) {
        healthSelect.innerHTML = `<option value="">כל המצבים הבריאותיים</option>`;
        metadata.healthStatuses.forEach(h => {
            healthSelect.innerHTML += `<option value="${h.key}">${h.label}</option>`;
        });
    }

    // Diet filter
    const dietSelect = document.getElementById('dietFilter');
    if (dietSelect && metadata.dietTypes) {
        dietSelect.innerHTML = `<option value="">כל סוגי התזונה</option>`;
        metadata.dietTypes.forEach(d => {
            dietSelect.innerHTML += `<option value="${d.key}">${d.label}</option>`;
        });
    }

    // Form species
    const modalSpecies = document.getElementById('modalAnimalSpecies');
    if (modalSpecies && metadata.species) {
        modalSpecies.innerHTML = `<option value="">בחר משפחה / מין</option>`;
        metadata.species.forEach(s => {
            modalSpecies.innerHTML += `<option value="${s.key}">${s.label}</option>`;
        });
    }

    // Form diet
    const modalDiet = document.getElementById('modalAnimalDiet');
    if (modalDiet && metadata.dietTypes) {
        modalDiet.innerHTML = '';
        metadata.dietTypes.forEach(d => {
            modalDiet.innerHTML += `<option value="${d.key}">${d.label}</option>`;
        });
    }

    // Form health
    const modalHealth = document.getElementById('modalAnimalHealth');
    if (modalHealth && metadata.healthStatuses) {
        modalHealth.innerHTML = '';
        metadata.healthStatuses.forEach(h => {
            modalHealth.innerHTML += `<option value="${h.key}">${h.label}</option>`;
        });
    }

    // Form conservation
    const modalCons = document.getElementById('modalAnimalConservation');
    if (modalCons && metadata.conservationStatuses) {
        modalCons.innerHTML = '';
        metadata.conservationStatuses.forEach(c => {
            modalCons.innerHTML += `<option value="${c.key}">${c.label}</option>`;
        });
    }

    // Form gender
    const modalGender = document.getElementById('modalAnimalGender');
    if (modalGender && metadata.genders) {
        modalGender.innerHTML = '';
        metadata.genders.forEach(g => {
            modalGender.innerHTML += `<option value="${g.key}">${g.label}</option>`;
        });
    }

    // Medical modal health statuses
    const medHealthSelect = document.getElementById('medHealthStatus');
    if (medHealthSelect && metadata.healthStatuses) {
        medHealthSelect.innerHTML = '';
        metadata.healthStatuses.forEach(h => {
            medHealthSelect.innerHTML += `<option value="${h.key}">${h.label}</option>`;
        });
    }
}

function populateSubSpeciesDropdown(selectedSpecies, selectedSubSpecies = '') {
    const subSelect = document.getElementById('modalAnimalSubSpecies');
    if (!subSelect) return;
    subSelect.innerHTML = `<option value="">בחר תת-מין</option>`;
    const filtered = (metadata.subSpecies || []).filter(ss => !selectedSpecies || ss.species === selectedSpecies);
    filtered.forEach(ss => {
        const isSel = ss.key === selectedSubSpecies ? 'selected' : '';
        subSelect.innerHTML += `<option value="${ss.key}" ${isSel}>${ss.label}</option>`;
    });
}

function populateCagesDropdown() {
    const cageFilter = document.getElementById('cageFilter');
    if (cageFilter) {
        cageFilter.innerHTML = `<option value="">כל הכלובים</option>`;
        allCages.forEach(c => {
            cageFilter.innerHTML += `<option value="${c.id}">כלוב ${c.id} (${c.species})</option>`;
        });
    }

    const modalCage = document.getElementById('modalAnimalCage');
    if (modalCage) {
        modalCage.innerHTML = `<option value="">ללא כלוב כרגע</option>`;
        allCages.forEach(c => {
            modalCage.innerHTML += `<option value="${c.id}">כלוב ${c.id} (${c.species})</option>`;
        });
    }
}

function renderAnimalsList(animals) {
    const grid = document.getElementById('animalsGrid');
    if (!grid) return;

    if (!animals || animals.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔍</div>
                <h3>לא נמצאו חיות התואמות את החיפוש</h3>
                <p>נסה לשנות את הסינון או להוסיף חיה חדשה לגן החיות.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = animals.map(animal => renderAnimalCard(animal)).join('');
}

function renderAnimalCard(animal) {
    const emoji = getEmoji(animal);
    const imgUrl = getAnimalImage(animal);
    const speciesLabel = getLabel(metadata.species, animal.species) || animal.species;
    const subSpeciesLabel = getLabel(metadata.subSpecies, animal.subSpecies) || animal.subSpecies || '';
    const healthLabel = getLabel(metadata.healthStatuses, animal.healthStatus) || animal.healthStatus;
    const dietLabel = getLabel(metadata.dietTypes, animal.dietType) || animal.dietType;
    const consLabel = getLabel(metadata.conservationStatuses, animal.conservationStatus) || animal.conservationStatus;
    const isEndangered = animal.conservationStatus && ['VULNERABLE', 'ENDANGERED', 'CRITICALLY_ENDANGERED'].includes(animal.conservationStatus);

    // Feeding time calculation
    const fedTimeStr = formatTimeAgo(animal.lastFedTime);
    const isHungry = isAnimalHungry(animal.lastFedTime);

    const coverHtml = imgUrl ? `
        <div class="animal-card-cover">
            <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(animal.name)}" class="animal-cover-img" loading="lazy" onerror="this.style.display='none'">
            <div class="animal-cover-overlay"></div>
            <span class="badge ${isEndangered ? 'badge-endangered' : 'badge-least-concern'} cover-badge">
                ${isEndangered ? '⚠️' : '🌿'} ${consLabel}
            </span>
            <div class="cover-ai-tag">✨ NaNoBanana 2.1</div>
        </div>
    ` : `
        <div class="animal-card-cover empty-cover">
            <div class="empty-cover-emoji">${emoji}</div>
            <span class="badge ${isEndangered ? 'badge-endangered' : 'badge-least-concern'} cover-badge">
                ${isEndangered ? '⚠️' : '🌿'} ${consLabel}
            </span>
        </div>
    `;

    return `
        <div class="animal-card" id="animal-card-${animal.id}">
            ${coverHtml}
            <div class="animal-card-content">
                <div>
                    <div class="animal-header">
                        <div class="animal-title-wrap">
                            <div class="animal-avatar">${emoji}</div>
                            <div>
                                <div class="animal-name">${escapeHtml(animal.name)}</div>
                                <div class="animal-species-text">${subSpeciesLabel} &bull; ${speciesLabel}</div>
                            </div>
                        </div>
                        <div>
                            <span class="health-dot ${animal.healthStatus}"></span>
                            <span style="font-size: 0.82rem; font-weight:600;">${healthLabel}</span>
                        </div>
                    </div>

                    <div class="badge-row">
                        <span class="badge badge-diet">🍽️ ${dietLabel}</span>
                        <span class="badge badge-gender">${animal.gender === 'MALE' ? '♂ זכר' : animal.gender === 'FEMALE' ? '♀ נקבה' : 'לא ידוע'}</span>
                        ${animal.cage ? `<span class="badge badge-cage">🛖 כלוב #${animal.cage.id}</span>` : `<span class="badge badge-cage">ללא כלוב</span>`}
                    </div>

                    <div class="animal-specs">
                        <div class="spec-item">
                            <span class="spec-label">גיל:</span>
                            <span class="spec-val">${animal.age != null ? animal.age + ' שנים' : 'לא צוין'}</span>
                        </div>
                        <div class="spec-item">
                            <span class="spec-label">משקל:</span>
                            <span class="spec-val">${animal.weightKg != null ? animal.weightKg + ' ק"ג' : 'לא צוין'}</span>
                        </div>
                        <div class="spec-item">
                            <span class="spec-label">שבב זיהוי:</span>
                            <span class="spec-val">${escapeHtml(animal.microchipId || 'ללא שבב')}</span>
                        </div>
                        <div class="spec-item">
                            <span class="spec-label">ארץ מוצא:</span>
                            <span class="spec-val">${escapeHtml(animal.originCountry || 'לא צוין')}</span>
                        </div>
                        <div class="spec-item" style="grid-column: 1 / -1;">
                            <span class="spec-label">מזון מועדף:</span>
                            <span class="spec-val">${escapeHtml(animal.favoriteFood || 'מזון מותאם')}</span>
                        </div>
                    </div>

                    <div class="feeding-bar ${isHungry ? 'hungry' : ''}">
                        <div>
                            <span>🕒 האכלה אחרונה: </span>
                            <strong>${fedTimeStr}</strong>
                        </div>
                        ${isHungry ? `<span class="hungry-badge">⚠️ זקוק להאכלה!</span>` : `<span style="color:#34d399; font-size:0.78rem;">✓ שבע</span>`}
                    </div>
                </div>

                <div class="card-actions">
                    <button class="btn btn-emerald btn-sm" onclick="openFeedModal(${animal.id}, '${escapeHtml(animal.name)}', '${escapeHtml(animal.favoriteFood || '')}')">
                        🥩 האכל חיה
                    </button>
                    <button class="btn btn-amber btn-sm" onclick="openMedicalModal(${animal.id}, '${escapeHtml(animal.name)}', '${animal.healthStatus}')">
                        🩺 בדיקה רפואית
                    </button>
                    <button class="btn btn-secondary btn-sm" onclick="openHistoryModal(${animal.id})">
                        📋 היסטוריה
                    </button>
                    <button class="btn btn-secondary btn-sm" onclick="openEditAnimalModal(${animal.id})">
                        ✏️ עריכה
                    </button>
                    <button class="btn btn-rose btn-sm" onclick="handleDeleteAnimal(${animal.id}, '${escapeHtml(animal.name)}')">
                        🗑️ מחק
                    </button>
                </div>
            </div>
        </div>
    `;
}

function getLabel(list, key) {
    if (!list || !key) return '';
    const item = list.find(i => i.key === key);
    return item ? item.label : key;
}

function formatTimeAgo(dateStr) {
    if (!dateStr) return 'טרם הואכל';
    const d = new Date(dateStr);
    const now = new Date();
    const diffHours = Math.floor((now - d) / (1000 * 60 * 60));
    if (diffHours < 1) return 'לפני פחות משעה';
    if (diffHours === 1) return 'לפני שעה';
    if (diffHours < 24) return `לפני ${diffHours} שעות`;
    const days = Math.floor(diffHours / 24);
    return `לפני ${days} ימים`;
}

function isAnimalHungry(dateStr) {
    if (!dateStr) return true;
    const d = new Date(dateStr);
    const diffHours = (new Date() - d) / (1000 * 60 * 60);
    return diffHours >= 8;
}

// Modals
function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('open');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('open');
}

// Add/Edit Animal Modal
function openAnimalModal(animal = null) {
    const isEdit = !!animal;
    const titleEl = document.getElementById('animalModalTitle');
    if (titleEl) titleEl.textContent = isEdit ? `עריכת חיה: ${animal.name}` : 'הוספת חיה חדשה לגן החיות';

    document.getElementById('modalAnimalId').value = isEdit ? animal.id : '';
    document.getElementById('modalAnimalName').value = isEdit ? animal.name : '';
    document.getElementById('modalAnimalSpecies').value = isEdit ? animal.species : '';

    populateSubSpeciesDropdown(isEdit ? animal.species : '', isEdit ? animal.subSpecies : '');

    document.getElementById('modalAnimalGender').value = isEdit ? animal.gender : 'MALE';
    document.getElementById('modalAnimalAge').value = isEdit && animal.age != null ? animal.age : '';
    document.getElementById('modalAnimalWeight').value = isEdit && animal.weightKg != null ? animal.weightKg : '';
    document.getElementById('modalAnimalDiet').value = isEdit ? animal.dietType : 'OMNIVORE';
    document.getElementById('modalAnimalFood').value = isEdit ? animal.favoriteFood || '' : '';
    document.getElementById('modalAnimalOrigin').value = isEdit ? animal.originCountry || '' : '';
    document.getElementById('modalAnimalConservation').value = isEdit ? animal.conservationStatus : 'LEAST_CONCERN';
    document.getElementById('modalAnimalChip').value = isEdit ? animal.microchipId || '' : '';
    document.getElementById('modalAnimalSchedule').value = isEdit ? animal.feedingSchedule || '' : '';
    document.getElementById('modalAnimalHealth').value = isEdit ? animal.healthStatus : 'HEALTHY';
    document.getElementById('modalAnimalCage').value = isEdit && animal.cage ? animal.cage.id : '';
    const imgInput = document.getElementById('modalAnimalImage');
    if (imgInput) imgInput.value = isEdit ? (animal.imageUrl || '') : '';
    document.getElementById('modalAnimalNotes').value = isEdit ? animal.notes || '' : '';

    openModal('animalModal');
}

function openEditAnimalModal(id) {
    const animal = allAnimals.find(a => a.id === id);
    if (animal) {
        openAnimalModal(animal);
    }
}

async function handleSaveAnimal(e) {
    e.preventDefault();
    const id = document.getElementById('modalAnimalId').value;
    const isEdit = !!id;

    const cageIdVal = document.getElementById('modalAnimalCage').value;
    const cageObj = cageIdVal ? { id: parseInt(cageIdVal) } : null;

    const payload = {
        name: document.getElementById('modalAnimalName').value.trim(),
        species: document.getElementById('modalAnimalSpecies').value,
        subSpecies: document.getElementById('modalAnimalSubSpecies').value || null,
        gender: document.getElementById('modalAnimalGender').value,
        age: document.getElementById('modalAnimalAge').value ? parseInt(document.getElementById('modalAnimalAge').value) : null,
        weightKg: document.getElementById('modalAnimalWeight').value ? parseFloat(document.getElementById('modalAnimalWeight').value) : null,
        dietType: document.getElementById('modalAnimalDiet').value,
        favoriteFood: document.getElementById('modalAnimalFood').value.trim(),
        originCountry: document.getElementById('modalAnimalOrigin').value.trim(),
        conservationStatus: document.getElementById('modalAnimalConservation').value,
        microchipId: document.getElementById('modalAnimalChip').value.trim(),
        feedingSchedule: document.getElementById('modalAnimalSchedule').value.trim(),
        healthStatus: document.getElementById('modalAnimalHealth').value,
        cage: cageObj,
        imageUrl: (document.getElementById('modalAnimalImage')?.value || '').trim() || null,
        notes: document.getElementById('modalAnimalNotes').value.trim()
    };

    try {
        const url = isEdit ? `${API_BASE}/animals/${id}` : `${API_BASE}/animals`;
        const method = isEdit ? 'PUT' : 'POST';
        const res = await apiFetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            if (res.status !== 401 && res.status !== 403) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || 'שגיאה בשמירת פרטי החיה');
            }
            return;
        }

        closeModal('animalModal');
        showToast(isEdit ? 'פרטי החיה עודכנו בהצלחה!' : 'חיה חדשה נוספה לגן החיות בהצלחה!', 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        if (err.message) showToast(err.message, 'error');
    }
}

// Feed Modal
function openFeedModal(animalId, animalName, favoriteFood) {
    document.getElementById('feedAnimalId').value = animalId;
    document.getElementById('feedModalTitle').textContent = `🥩 האכלת ${animalName}`;
    document.getElementById('feedFoodItem').value = favoriteFood || '';
    document.getElementById('feedAmountKg').value = '2.5';
    document.getElementById('feedCaretaker').value = currentUser.fullName || 'מטפל ראשי';
    document.getElementById('feedNotes').value = '';
    openModal('feedModal');
}

async function handleSaveFeeding(e) {
    e.preventDefault();
    const animalId = document.getElementById('feedAnimalId').value;
    const payload = {
        foodItem: document.getElementById('feedFoodItem').value.trim(),
        amountKg: parseFloat(document.getElementById('feedAmountKg').value),
        fedBy: document.getElementById('feedCaretaker').value.trim(),
        notes: document.getElementById('feedNotes').value.trim()
    };

    try {
        const res = await apiFetch(`${API_BASE}/animals/${animalId}/feed`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            if (res.status !== 401 && res.status !== 403) {
                throw new Error('שגיאה בביצוע האכלה');
            }
            return;
        }

        closeModal('feedModal');
        showToast('החיה הואכלה בהצלחה!', 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        if (err.message) showToast(err.message, 'error');
    }
}

// Medical Checkup Modal
function openMedicalModal(animalId, animalName, currentHealth) {
    document.getElementById('medAnimalId').value = animalId;
    document.getElementById('medicalModalTitle').textContent = `🩺 בדיקה רפואית: ${animalName}`;
    document.getElementById('medHealthStatus').value = currentHealth;
    document.getElementById('medDiagnosis').value = '';
    document.getElementById('medTreatment').value = '';
    document.getElementById('medDoctor').value = currentUser.fullName || 'ד"ר שרה מילר';
    document.getElementById('medNotes').value = '';
    openModal('medicalModal');
}

async function handleSaveMedical(e) {
    e.preventDefault();
    const animalId = document.getElementById('medAnimalId').value;
    const payload = {
        healthStatus: document.getElementById('medHealthStatus').value,
        diagnosis: document.getElementById('medDiagnosis').value.trim(),
        treatment: document.getElementById('medTreatment').value.trim(),
        performedBy: document.getElementById('medDoctor').value.trim(),
        notes: document.getElementById('medNotes').value.trim()
    };

    try {
        const res = await apiFetch(`${API_BASE}/animals/${animalId}/medical`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            if (res.status !== 401 && res.status !== 403) {
                throw new Error('שגיאה ברישום הבדיקה הרפואית');
            }
            return;
        }

        closeModal('medicalModal');
        showToast('הרשומה הרפואית נשמרה בהצלחה ומצב החיה עודכן!', 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        if (err.message) showToast(err.message, 'error');
    }
}

// History Modal
async function openHistoryModal(animalId) {
    const animal = allAnimals.find(a => a.id === animalId);
    if (!animal) return;

    document.getElementById('historyModalTitle').textContent = `📋 היסטוריה: ${animal.name}`;
    const container = document.getElementById('historyTimeline');
    container.innerHTML = '<p style="color:var(--text-muted);">טוען נתונים...</p>';
    openModal('historyModal');

    try {
        const [medRes, feedRes] = await Promise.all([
            apiFetch(`${API_BASE}/animals/${animalId}/medical`),
            apiFetch(`${API_BASE}/animals/${animalId}/feedings`)
        ]);

        const medHistory = (medRes && medRes.ok) ? await medRes.json() : [];
        const feedHistory = (feedRes && feedRes.ok) ? await feedRes.json() : [];

        // Combine and sort by date descending
        const items = [];
        medHistory.forEach(m => {
            items.push({
                type: 'medical',
                date: new Date(m.timestamp),
                title: `🩺 בדיקה רפואית (${getLabel(metadata.healthStatuses, m.healthStatus)})`,
                desc: `אבחנה: ${escapeHtml(m.diagnosis)} | טיפול: ${escapeHtml(m.treatment || 'ללא')} | וטרינר: ${escapeHtml(m.performedBy || 'לא צוין')}`,
                notes: m.notes
            });
        });

        feedHistory.forEach(f => {
            items.push({
                type: 'feeding',
                date: new Date(f.timestamp),
                title: `🥩 האכלה: ${escapeHtml(f.foodItem)} (${f.amountKg} ק"ג)`,
                desc: `הוזן ע"י: ${escapeHtml(f.fedBy || 'מטפל')}`,
                notes: f.notes
            });
        });

        items.sort((a, b) => b.date - a.date);

        if (items.length === 0) {
            container.innerHTML = '<p style="color:var(--text-muted); text-align:center; padding: 20px;">אין היסטוריה מתועדת לחיה זו עדיין.</p>';
            return;
        }

        container.innerHTML = items.map(item => `
            <div class="timeline-item">
                <span class="timeline-dot" style="background:${item.type === 'medical' ? 'var(--accent-amber)' : 'var(--accent-emerald)'}"></span>
                <span class="timeline-date">${item.date.toLocaleString('he-IL')}</span>
                <div class="timeline-title">${item.title}</div>
                <div class="timeline-body">${item.desc}</div>
                ${item.notes ? `<div style="font-size:0.78rem; color:var(--text-muted); margin-top:2px;">הערות: ${escapeHtml(item.notes)}</div>` : ''}
            </div>
        `).join('');

    } catch (err) {
        container.innerHTML = '<p style="color:var(--accent-rose);">שגיאה בטעינת היסטוריה</p>';
    }
}

// Delete Animal
async function handleDeleteAnimal(id, name) {
    if (!confirm(`האם אתה בטוח שברצונך למחוק את החיה "${name}" ממערכת גן החיות?`)) {
        return;
    }

    try {
        const res = await apiFetch(`${API_BASE}/animals/${id}`, {
            method: 'DELETE'
        });

        if (!res.ok) {
            if (res.status !== 401 && res.status !== 403) {
                throw new Error('שגיאה במחיקת החיה');
            }
            return;
        }

        showToast(`החיה "${name}" נמחקה בהצלחה`, 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        if (err.message) showToast(err.message, 'error');
    }
}

// Utility
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    let icon = '✓';
    if (type === 'error') icon = '⚠️';
    if (type === 'info') icon = 'ℹ️';
    toast.innerHTML = `<span>${icon}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4500);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Global state for new modules
let allTasks = [];
let allAlertsData = null;
let currentTaskFilter = 'ALL';

// ==========================================
// Navigation & New Modules Initialization
// ==========================================
function initNavigationAndNewFeatures() {
    // Navigation tabs
    document.querySelectorAll('.nav-tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const view = btn.dataset.view;
            if (view) switchView(view);
        });
    });

    // Alert Bell & Drawer
    const btnAlertsBell = document.getElementById('btnAlertsBell');
    if (btnAlertsBell) {
        btnAlertsBell.addEventListener('click', () => toggleAlertsDrawer(true));
    }

    const btnCloseAlerts = document.getElementById('btnCloseAlerts');
    if (btnCloseAlerts) {
        btnCloseAlerts.addEventListener('click', () => toggleAlertsDrawer(false));
    }

    const drawerOverlay = document.getElementById('alertsDrawerOverlay');
    if (drawerOverlay) {
        drawerOverlay.addEventListener('click', (e) => {
            if (e.target === drawerOverlay) toggleAlertsDrawer(false);
        });
    }

    // Export CSV
    const btnExportCsv = document.getElementById('btnExportCsv');
    if (btnExportCsv) {
        btnExportCsv.addEventListener('click', exportAnimalsToCSV);
    }

    // Print Report
    const btnPrintReport = document.getElementById('btnPrintReport');
    if (btnPrintReport) {
        btnPrintReport.addEventListener('click', () => window.print());
    }

    // New Cage Modal
    const btnOpenAddCage = document.getElementById('btnOpenAddCage');
    if (btnOpenAddCage) {
        btnOpenAddCage.addEventListener('click', openAddCageModal);
    }

    const cageForm = document.getElementById('cageForm');
    if (cageForm) {
        cageForm.addEventListener('submit', handleCageSubmit);
    }

    // New Task Modal
    const btnOpenAddTask = document.getElementById('btnOpenAddTask');
    if (btnOpenAddTask) {
        btnOpenAddTask.addEventListener('click', openAddTaskModal);
    }

    const taskForm = document.getElementById('taskForm');
    if (taskForm) {
        taskForm.addEventListener('submit', handleTaskSubmit);
    }
}

// Switch between views (Animals, Cages, Tasks, Analytics)
function switchView(viewName) {
    document.querySelectorAll('.nav-tab-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.view === viewName);
    });

    document.querySelectorAll('.app-view').forEach(v => {
        v.style.display = 'none';
        v.classList.remove('active');
    });

    const targetView = document.getElementById(`view${capitalize(viewName)}`);
    if (targetView) {
        targetView.style.display = 'block';
        targetView.classList.add('active');
    }

    if (viewName === 'cages') {
        loadCagesView();
    } else if (viewName === 'tasks') {
        loadTasks();
    } else if (viewName === 'analytics') {
        loadAnalytics();
    } else if (viewName === 'animals') {
        loadStats();
        loadAnimals();
    }
}

function capitalize(s) {
    if (!s) return '';
    return s.charAt(0).toUpperCase() + s.slice(1);
}

// ==========================================
// 1. Smart Alerts & Reminders Center
// ==========================================
async function loadAlerts() {
    try {
        const res = await apiFetch(`${API_BASE}/alerts`);
        if (!res.ok) return;

        const data = await res.json();
        allAlertsData = data;

        const badge = document.getElementById('alertsBadgeCount');
        const activeCount = (data.criticalCount || 0) + (data.warningCount || 0);

        if (badge) {
            if (activeCount > 0) {
                badge.textContent = activeCount;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }

        renderAlerts(data);
    } catch (e) {
        console.warn('Error fetching alerts:', e);
    }
}

function toggleAlertsDrawer(open) {
    const overlay = document.getElementById('alertsDrawerOverlay');
    if (overlay) {
        overlay.classList.toggle('open', !!open);
        if (open) loadAlerts();
    }
}

function renderAlerts(data) {
    const list = document.getElementById('alertsDrawerList');
    const subtitle = document.getElementById('alertsSubtitle');
    if (!list) return;

    if (subtitle) {
        subtitle.textContent = `${data.totalAlerts || 0} התראות פעילות (${data.criticalCount || 0} קריטיות)`;
    }

    const alerts = data.alerts || [];
    if (alerts.length === 0) {
        list.innerHTML = `
            <div style="text-align:center; padding:50px 20px; color:var(--text-muted);">
                <div style="font-size:3rem; margin-bottom:12px;">✅</div>
                <div style="font-size:1.1rem; font-weight:700; color:var(--text-primary);">הכל תקין בגן החיות!</div>
                <p style="font-size:0.85rem; margin-top:4px;">כל החיות הואכלו בזמן, אין בידודים חריגים והמתחמים בתפוסה מותרת.</p>
            </div>
        `;
        return;
    }

    list.innerHTML = alerts.map(a => {
        const typeClass = (a.type || 'info').toLowerCase();
        let btnHtml = '';

        if (a.actionType === 'FEED' && a.animalId) {
            btnHtml = `<button type="button" class="btn btn-emerald btn-sm" onclick="toggleAlertsDrawer(false); openFeedModal(${a.animalId})">🥩 ${escapeHtml(a.actionText || 'האכל')}</button>`;
        } else if (a.actionType === 'MEDICAL' && a.animalId) {
            btnHtml = `<button type="button" class="btn btn-amber btn-sm" onclick="toggleAlertsDrawer(false); openMedicalModal(${a.animalId})">🩺 ${escapeHtml(a.actionText || 'בדיקה')}</button>`;
        } else if (a.actionType === 'TASK') {
            btnHtml = `<button type="button" class="btn btn-primary btn-sm" onclick="toggleAlertsDrawer(false); switchView('tasks')">📋 ${escapeHtml(a.actionText || 'משימה')}</button>`;
        } else if (a.actionType === 'CAGE') {
            btnHtml = `<button type="button" class="btn btn-primary btn-sm" onclick="toggleAlertsDrawer(false); switchView('cages')">🏡 ${escapeHtml(a.actionText || 'כלוב')}</button>`;
        } else if (a.animalId) {
            btnHtml = `<button type="button" class="btn btn-secondary btn-sm" onclick="toggleAlertsDrawer(false); openEditAnimalModal(${a.animalId})">👁️ צפה בחיה</button>`;
        }

        return `
            <div class="alert-item-card ${typeClass}">
                <div class="alert-item-top">
                    <span class="alert-item-icon">${escapeHtml(a.icon || '⚠️')}</span>
                    <div style="flex:1;">
                        <div class="alert-item-title">${escapeHtml(a.title)}</div>
                        <div class="alert-item-message">${escapeHtml(a.message)}</div>
                    </div>
                </div>
                ${btnHtml ? `<div class="alert-item-footer"><div></div><div>${btnHtml}</div></div>` : ''}
            </div>
        `;
    }).join('');
}

// ==========================================
// 2. Cages Management View
// ==========================================
async function loadCagesView() {
    const grid = document.getElementById('cagesListGrid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column:1/-1; text-align:center; padding:40px; color:var(--text-muted);">טוען מתחמים וכלובים...</div>';

    try {
        const [cagesRes, animalsRes] = await Promise.all([
            apiFetch(`${API_BASE}/cages`),
            apiFetch(`${API_BASE}/animals`)
        ]);

        if (!cagesRes.ok || !animalsRes.ok) return;

        const cages = await cagesRes.json();
        const animals = await animalsRes.json();
        allCages = cages;

        // Group animals by cage ID
        const cageAnimalsMap = {};
        animals.forEach(a => {
            if (a.cage && a.cage.id) {
                if (!cageAnimalsMap[a.cage.id]) cageAnimalsMap[a.cage.id] = [];
                cageAnimalsMap[a.cage.id].push(a);
            }
        });

        if (cages.length === 0) {
            grid.innerHTML = `
                <div style="grid-column:1/-1; text-align:center; padding:60px 20px; color:var(--text-muted);">
                    <div style="font-size:3rem; margin-bottom:12px;">🏡</div>
                    <div style="font-size:1.1rem; font-weight:700; color:var(--text-primary);">אין מתחמים להצגה</div>
                    <p style="margin-top:6px;">לחץ על "הוספת מתחם / כלוב חדש" למעלה ליצירת מתחם חדש בגן.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = cages.map(c => {
            const assigned = cageAnimalsMap[c.id] || [];
            const capacity = c.capacity || 6;
            const occupancyPercent = Math.min(100, Math.round((assigned.length / capacity) * 100));
            const fillClass = occupancyPercent >= 100 ? 'rose' : occupancyPercent >= 70 ? 'amber' : 'green';

            const speciesHebrew = c.species ? (metadata.species.find(s => s.code === c.species)?.name || c.species) : 'כללי';
            const speciesIcon = c.species && EMOJI_MAP[c.species] ? EMOJI_MAP[c.species] : '🏡';

            const animalsChipsHtml = assigned.length > 0
                ? assigned.map(a => `<span class="cage-animal-chip">${getEmoji(a)} ${escapeHtml(a.name)}</span>`).join('')
                : '<span style="font-size:0.8rem; color:var(--text-muted);">הכלוב ריק כרגע מחיות</span>';

            return `
                <div class="cage-card" id="cage-card-${c.id}">
                    <div class="cage-card-top">
                        <div class="cage-name-wrap">
                            <span class="cage-icon">${speciesIcon}</span>
                            <div>
                                <div class="cage-name">${escapeHtml(c.name || `כלוב #${c.id}`)}</div>
                                <div class="cage-species-tag">${escapeHtml(speciesHebrew)} (${c.status || 'ACTIVE'})</div>
                            </div>
                        </div>
                        <div style="text-align:left;">
                            <span style="font-size:1.1rem; font-weight:800; color:var(--text-primary);">${assigned.length}</span>
                            <span style="font-size:0.8rem; color:var(--text-muted);"> / ${capacity}</span>
                        </div>
                    </div>

                    <div>
                        <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted); margin-bottom:4px;">
                            <span>תפוסת מתחם</span>
                            <span style="font-weight:700;">${occupancyPercent}%</span>
                        </div>
                        <div class="occupancy-track">
                            <div class="occupancy-fill ${fillClass}" style="width: ${occupancyPercent}%;"></div>
                        </div>
                    </div>

                    <div class="climate-stats">
                        <span class="climate-pill">🌡️ ${c.temperatureCelsius != null ? c.temperatureCelsius : 24.0}°C</span>
                        <span class="climate-pill">💧 ${c.humidityPercent != null ? c.humidityPercent : 55.0}% לחות</span>
                        <span class="climate-pill">📍 ${escapeHtml(c.locationZone || 'מתחם מרכזי')}</span>
                    </div>

                    <div>
                        <div style="font-size:0.78rem; font-weight:700; color:var(--text-secondary); margin-bottom:6px;">חיות שוהות במתחם:</div>
                        <div class="cage-animals-list">
                            ${animalsChipsHtml}
                        </div>
                    </div>

                    <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:auto; padding-top:10px; border-top:1px solid var(--border-color);">
                        <button type="button" class="btn btn-secondary btn-sm" onclick="openAddAnimalInCage(${c.id})">
                            <span>➕</span> הוסף חיה לכאן
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        console.error('Error loading cages view', e);
        grid.innerHTML = '<div style="grid-column:1/-1; text-align:center; color:#f87171; padding:30px;">שגיאה בטעינת מתחמים</div>';
    }
}

function openAddAnimalInCage(cageId) {
    switchView('animals');
    openAnimalModal();
    const select = document.getElementById('modalAnimalCage');
    if (select) select.value = String(cageId);
}

function openAddCageModal() {
    if (currentUser.role !== 'ADMIN') {
        showToast('רק מנהל מערכת (ADMIN) רשאי להוסיף מתחמים וכלובים חדשים', 'error');
        return;
    }

    const speciesSelect = document.getElementById('cageSpeciesInput');
    if (speciesSelect && metadata.species) {
        speciesSelect.innerHTML = metadata.species.map(s => `
            <option value="${s.code}">${escapeHtml(s.name)}</option>
        `).join('');
    }

    document.getElementById('cageNameInput').value = '';
    document.getElementById('cageCapacityInput').value = '6';
    document.getElementById('cageTempInput').value = '24.0';
    document.getElementById('cageHumidityInput').value = '55';
    document.getElementById('cageZoneInput').value = '';

    openModal('cageModal');
}

async function handleCageSubmit(e) {
    e.preventDefault();

    const name = document.getElementById('cageNameInput').value.trim();
    const species = document.getElementById('cageSpeciesInput').value;
    const capacity = parseInt(document.getElementById('cageCapacityInput').value, 10) || 6;
    const temperatureCelsius = parseFloat(document.getElementById('cageTempInput').value) || 24.0;
    const humidityPercent = parseFloat(document.getElementById('cageHumidityInput').value) || 55.0;
    const locationZone = document.getElementById('cageZoneInput').value.trim();

    try {
        const res = await apiFetch(`${API_BASE}/cages`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name,
                species,
                capacity,
                temperatureCelsius,
                humidityPercent,
                locationZone,
                status: 'ACTIVE'
            })
        });

        if (res.ok) {
            closeModal('cageModal');
            showToast(`המתחם "${name}" נוצר בהצלחה!`, 'success');
            await loadCages();
            await loadCagesView();
        } else {
            showToast('שגיאה ביצירת המתחם', 'error');
        }
    } catch (err) {
        showToast('שגיאה ביצירת כלוב: ' + err.message, 'error');
    }
}

// ==========================================
// 3. Tasks & Shift Operations View
// ==========================================
async function loadTasks() {
    try {
        const res = await apiFetch(`${API_BASE}/tasks`);
        if (!res.ok) return;

        const tasks = await res.json();
        allTasks = tasks;

        // Count pending tasks for badge
        const pendingCount = tasks.filter(t => t.status === 'PENDING').length;
        const navBadge = document.getElementById('navTasksBadge');
        if (navBadge) {
            if (pendingCount > 0) {
                navBadge.textContent = pendingCount;
                navBadge.style.display = 'inline-block';
            } else {
                navBadge.style.display = 'none';
            }
        }

        renderTasks();
    } catch (e) {
        console.warn('Error loading tasks:', e);
    }
}

function filterTasks(status) {
    currentTaskFilter = status;
    document.querySelectorAll('.task-filter-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.taskFilter === status);
    });
    renderTasks();
}

function renderTasks() {
    const grid = document.getElementById('tasksListGrid');
    if (!grid) return;

    let filtered = allTasks;
    if (currentTaskFilter !== 'ALL') {
        filtered = allTasks.filter(t => t.status === currentTaskFilter);
    }

    if (filtered.length === 0) {
        grid.innerHTML = `
            <div style="grid-column:1/-1; text-align:center; padding:60px 20px; color:var(--text-muted);">
                <div style="font-size:3rem; margin-bottom:12px;">📋</div>
                <div style="font-size:1.1rem; font-weight:700; color:var(--text-primary);">אין משימות בסטטוס זה</div>
                <p style="margin-top:6px;">לחץ על "+ משימה חדשה לצוות" להוספת משימה יומית חדשה.</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = filtered.map(t => {
        const priorityClass = (t.priority || 'medium').toLowerCase();
        let priorityLabel = 'בינונית';
        if (t.priority === 'URGENT') priorityLabel = '⚡ דחופה';
        else if (t.priority === 'HIGH') priorityLabel = 'גבוהה';
        else if (t.priority === 'LOW') priorityLabel = 'נמוכה';

        const roleHebrew = t.assignedRole ? (ROLE_METADATA[t.assignedRole]?.label || t.assignedRole) : 'כל הצוות';
        const isCompleted = t.status === 'COMPLETED';
        const isInProgress = t.status === 'IN_PROGRESS';

        let actionBtns = '';
        if (!isCompleted) {
            if (!isInProgress) {
                actionBtns += `<button type="button" class="btn btn-primary btn-sm" onclick="updateTaskStatus(${t.id}, 'IN_PROGRESS')">⏳ התחל ביצוע</button>`;
            }
            actionBtns += `<button type="button" class="btn btn-emerald btn-sm" onclick="updateTaskStatus(${t.id}, 'COMPLETED')">✅ סמן כהושלם</button>`;
        } else {
            actionBtns += `<span style="font-size:0.8rem; color:#86efac; font-weight:700;">✓ המשימה הושלמה</span>`;
        }

        if (currentUser.role === 'ADMIN') {
            actionBtns += `<button type="button" class="btn btn-rose btn-sm" onclick="deleteTask(${t.id})" title="מחק משימה">🗑️</button>`;
        }

        return `
            <div class="task-card ${isCompleted ? 'completed' : ''}" id="task-card-${t.id}">
                <div class="task-card-header">
                    <div class="task-title">${escapeHtml(t.title)}</div>
                    <span class="task-priority-tag ${priorityClass}">${priorityLabel}</span>
                </div>

                ${t.description ? `<div class="task-desc">${escapeHtml(t.description)}</div>` : ''}

                <div class="task-meta-row">
                    <span>👤 ${escapeHtml(roleHebrew)} ${t.assignedToName ? `(${escapeHtml(t.assignedToName)})` : ''}</span>
                    <span>⏰ ${escapeHtml(t.dueDate || 'היום')}</span>
                </div>

                ${t.animalName ? `<div style="font-size:0.8rem; color:var(--primary); font-weight:600;">🐾 קשור לחיה: ${escapeHtml(t.animalName)}</div>` : ''}

                <div class="task-actions-row">
                    ${actionBtns}
                </div>
            </div>
        `;
    }).join('');
}

function openAddTaskModal() {
    document.getElementById('taskTitle').value = '';
    document.getElementById('taskDesc').value = '';
    document.getElementById('taskRole').value = currentUser.role !== 'GUEST' ? currentUser.role : 'KEEPER';
    document.getElementById('taskAssignee').value = currentUser.fullName !== 'אורח' ? currentUser.fullName : '';
    document.getElementById('taskAnimalName').value = '';
    document.getElementById('taskPriority').value = 'MEDIUM';
    document.getElementById('taskDueDate').value = 'היום בשעה 14:00';

    openModal('taskModal');
}

async function handleTaskSubmit(e) {
    e.preventDefault();

    const title = document.getElementById('taskTitle').value.trim();
    const description = document.getElementById('taskDesc').value.trim();
    const assignedRole = document.getElementById('taskRole').value;
    const assignedToName = document.getElementById('taskAssignee').value.trim();
    const animalName = document.getElementById('taskAnimalName').value.trim();
    const priority = document.getElementById('taskPriority').value;
    const dueDate = document.getElementById('taskDueDate').value.trim();

    try {
        const res = await apiFetch(`${API_BASE}/tasks`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title,
                description,
                assignedRole,
                assignedToName,
                animalName,
                priority,
                status: 'PENDING',
                dueDate
            })
        });

        if (res.ok) {
            closeModal('taskModal');
            showToast('המשימה נוצרה בהצלחה!', 'success');
            await loadTasks();
            await loadAlerts();
        } else {
            showToast('שגיאה ביצירת המשימה', 'error');
        }
    } catch (err) {
        showToast('שגיאה ביצירת משימה: ' + err.message, 'error');
    }
}

async function updateTaskStatus(id, newStatus) {
    try {
        const res = await apiFetch(`${API_BASE}/tasks/${id}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: newStatus })
        });

        if (res.ok) {
            showToast(newStatus === 'COMPLETED' ? 'המשימה סומנה כהושלמה בהצלחה!' : 'סטטוס המשימה עודכן', 'success');
            await loadTasks();
            await loadAlerts();
        }
    } catch (err) {
        showToast('שגיאה בעדכון משימה: ' + err.message, 'error');
    }
}

async function deleteTask(id) {
    if (!confirm('האם אתה בטוח שברצונך למחוק משימה זו?')) return;

    try {
        const res = await apiFetch(`${API_BASE}/tasks/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('המשימה נמחקה', 'info');
            await loadTasks();
            await loadAlerts();
        }
    } catch (err) {
        showToast('שגיאה במחיקת משימה: ' + err.message, 'error');
    }
}

// ==========================================
// 4. Analytics & Visual Distribution Charts
// ==========================================
async function loadAnalytics() {
    const grid = document.getElementById('analyticsGrid');
    if (!grid) return;

    grid.innerHTML = '<div style="grid-column:1/-1; text-align:center; padding:40px; color:var(--text-muted);">מחשב ומנתח נתונים...</div>';

    try {
        const res = await apiFetch(`${API_BASE}/analytics`);
        if (!res.ok) return;

        const data = await res.json();
        renderAnalytics(data);
    } catch (e) {
        console.error('Error loading analytics', e);
        grid.innerHTML = '<div style="grid-column:1/-1; text-align:center; color:#f87171; padding:30px;">שגיאה בטעינת אנליטיקה</div>';
    }
}

function renderAnalytics(data) {
    const grid = document.getElementById('analyticsGrid');
    if (!grid) return;

    const totalAnimals = allAnimals.length || 1;

    // Helper to render a distribution chart card
    function renderDistCard(title, icon, items, colorGradients) {
        const itemsHtml = items.map((it, idx) => {
            const count = it.count || 0;
            const percent = Math.round((count / totalAnimals) * 100);
            const gradient = colorGradients[idx % colorGradients.length];

            return `
                <div class="dist-item">
                    <div class="dist-item-label">
                        <span>${escapeHtml(it.name || it.code)}</span>
                        <span><strong>${count}</strong> חיות (${percent}%)</span>
                    </div>
                    <div class="dist-bar-track">
                        <div class="dist-bar-fill" style="width: ${percent}%; background: ${gradient};"></div>
                    </div>
                </div>
            `;
        }).join('');

        return `
            <div class="chart-card">
                <div class="chart-card-header">
                    <div class="chart-card-title">${icon} ${escapeHtml(title)}</div>
                </div>
                <div class="distribution-list">
                    ${itemsHtml}
                </div>
            </div>
        `;
    }

    const speciesGradients = [
        'linear-gradient(90deg, #f59e0b, #d97706)',
        'linear-gradient(90deg, #3b82f6, #1d4ed8)',
        'linear-gradient(90deg, #10b981, #047857)',
        'linear-gradient(90deg, #8b5cf6, #6d28d9)',
        'linear-gradient(90deg, #ec4899, #be185d)',
        'linear-gradient(90deg, #06b6d4, #0e7490)',
        'linear-gradient(90deg, #14b8a6, #0f766e)'
    ];

    const healthGradients = [
        'linear-gradient(90deg, #10b981, #059669)',
        'linear-gradient(90deg, #f59e0b, #d97706)',
        'linear-gradient(90deg, #ec4899, #be185d)',
        'linear-gradient(90deg, #ef4444, #b91c1c)',
        'linear-gradient(90deg, #6366f1, #4338ca)'
    ];

    const dietGradients = [
        'linear-gradient(90deg, #ef4444, #dc2626)',
        'linear-gradient(90deg, #10b981, #059669)',
        'linear-gradient(90deg, #f59e0b, #d97706)',
        'linear-gradient(90deg, #06b6d4, #0891b2)',
        'linear-gradient(90deg, #8b5cf6, #7c3aed)'
    ];

    const conservationGradients = [
        'linear-gradient(90deg, #10b981, #059669)',
        'linear-gradient(90deg, #f59e0b, #d97706)',
        'linear-gradient(90deg, #fb923c, #ea580c)',
        'linear-gradient(90deg, #f43f5e, #e11d48)',
        'linear-gradient(90deg, #ef4444, #991b1b)'
    ];

    grid.innerHTML = `
        ${renderDistCard('התפלגות לפי מחלקות ומשפחות', '🐾', Array.from(data.speciesDistribution || []), speciesGradients)}
        ${renderDistCard('התפלגות מצב בריאותי ובידוד', '🩺', Array.from(data.healthDistribution || []), healthGradients)}
        ${renderDistCard('התפלגות סוגי תזונה ומשטר מזון', '🥩', Array.from(data.dietDistribution || []), dietGradients)}
        ${renderDistCard('סטטוס שימור וסכנת הכחדה (IUCN)', '🚨', Array.from(data.conservationDistribution || []), conservationGradients)}
    `;
}

// ==========================================
// 5. Data Export (CSV & Print PDF)
// ==========================================
function exportAnimalsToCSV() {
    if (!allAnimals || allAnimals.length === 0) {
        showToast('אין חיות לייצוא כרגע', 'error');
        return;
    }

    const headers = [
        'מזהה',
        'שם החיה',
        'משפחה',
        'תת מין',
        'מין ז/נ',
        'גיל',
        'משקל ק"ג',
        'מצב בריאותי',
        'סוג תזונה',
        'מזון מועדף',
        'ארץ מוצא',
        'סטטוס שימור',
        'מספר שבב',
        'כלוב משויך'
    ];

    const rows = allAnimals.map(a => [
        a.id || '',
        `"${(a.name || '').replace(/"/g, '""')}"`,
        `"${a.species ? (metadata.species.find(s => s.code === a.species)?.name || a.species) : ''}"`,
        `"${a.subSpecies ? (metadata.subSpecies.find(s => s.code === a.subSpecies)?.name || a.subSpecies) : ''}"`,
        `"${a.gender || ''}"`,
        a.age != null ? a.age : '',
        a.weightKg != null ? a.weightKg : '',
        `"${a.healthStatus ? (metadata.healthStatuses.find(s => s.code === a.healthStatus)?.name || a.healthStatus) : ''}"`,
        `"${a.dietType ? (metadata.dietTypes.find(s => s.code === a.dietType)?.name || a.dietType) : ''}"`,
        `"${(a.favoriteFood || '').replace(/"/g, '""')}"`,
        `"${(a.originCountry || '').replace(/"/g, '""')}"`,
        `"${a.conservationStatus ? (metadata.conservationStatuses.find(s => s.code === a.conservationStatus)?.name || a.conservationStatus) : ''}"`,
        `"${(a.microchipId || '').replace(/"/g, '""')}"`,
        a.cage ? `"${(a.cage.name || `כלוב #${a.cage.id}`)}"` : 'ללא'
    ]);

    const csvContent = '\uFEFF' + [headers.join(','), ...rows.map(r => r.join(','))].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `zoo_animals_export_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    showToast('קובץ CSV של נתוני הגן הורד בהצלחה למחשב שלך!', 'success');
}

// Global functions for inline HTML event handlers
window.openModal = openModal;
window.closeModal = closeModal;
window.quickSwitchRole = quickSwitchRole;
window.openFeedModal = openFeedModal;
window.openMedicalModal = openMedicalModal;
window.openHistoryModal = openHistoryModal;
window.openEditAnimalModal = openEditAnimalModal;
window.handleDeleteAnimal = handleDeleteAnimal;
window.switchView = switchView;
window.toggleAlertsDrawer = toggleAlertsDrawer;
window.filterTasks = filterTasks;
window.loadAnalytics = loadAnalytics;
window.updateTaskStatus = updateTaskStatus;
window.deleteTask = deleteTask;
window.openAddAnimalInCage = openAddAnimalInCage;
window.exportAnimalsToCSV = exportAnimalsToCSV;

