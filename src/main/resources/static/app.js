// Zoo Animal Management Frontend Logic
const API_BASE = '/api';

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

// Initialization
document.addEventListener('DOMContentLoaded', async () => {
    initEventListeners();
    await loadMetadata();
    await loadCages();
    await loadStats();
    await loadAnimals();
});

// Event Listeners
function initEventListeners() {
    // Search
    const searchInput = document.getElementById('searchInput');
    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            currentFilter.search = e.target.value.trim();
            loadAnimals();
        }, 300);
    });

    // Health filter
    document.getElementById('healthFilter').addEventListener('change', (e) => {
        currentFilter.health = e.target.value;
        loadAnimals();
    });

    // Diet filter
    document.getElementById('dietFilter').addEventListener('change', (e) => {
        currentFilter.diet = e.target.value;
        loadAnimals();
    });

    // Cage filter
    document.getElementById('cageFilter').addEventListener('change', (e) => {
        currentFilter.cageId = e.target.value;
        loadAnimals();
    });

    // Endangered toggle
    document.getElementById('endangeredToggle').addEventListener('change', (e) => {
        currentFilter.endangered = e.target.checked;
        loadAnimals();
    });

    // Add animal button
    document.getElementById('btnAddAnimal').addEventListener('click', () => {
        openAnimalModal();
    });

    // Refresh button
    document.getElementById('btnRefresh').addEventListener('click', () => {
        loadStats();
        loadAnimals();
        showToast('הנתונים רועננו בהצלחה', 'success');
    });

    // Animal form species change (filters subspecies)
    document.getElementById('modalAnimalSpecies').addEventListener('change', (e) => {
        populateSubSpeciesDropdown(e.target.value);
    });

    // Save animal submit
    document.getElementById('animalForm').addEventListener('submit', handleSaveAnimal);

    // Feed form submit
    document.getElementById('feedForm').addEventListener('submit', handleSaveFeeding);

    // Medical form submit
    document.getElementById('medicalForm').addEventListener('submit', handleSaveMedical);
}

// API Calls
async function loadMetadata() {
    try {
        const res = await fetch(`${API_BASE}/animals/metadata`);
        if (res.ok) {
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
        const res = await fetch(`${API_BASE}/cages`);
        if (res.ok) {
            allCages = await res.json();
            populateCagesDropdown();
        }
    } catch (err) {
        console.error('Failed to load cages', err);
    }
}

async function loadStats() {
    try {
        const res = await fetch(`${API_BASE}/animals/stats`);
        if (res.ok) {
            const stats = await res.json();
            document.getElementById('statTotalAnimals').textContent = stats.totalAnimals;
            document.getElementById('statEndangered').textContent = stats.endangeredCount;
            document.getElementById('statHungry').textContent = stats.hungryCount;
            document.getElementById('statMedicalAttention').textContent = (stats.sickOrInjuredCount + stats.quarantinedCount + stats.observationCount);
            document.getElementById('statAvgWeight').textContent = `${stats.averageWeightKg} ק"ג`;
            document.getElementById('statAvgAge').textContent = `${stats.averageAge} שנים`;
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
        const res = await fetch(url);
        if (res.ok) {
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
    container.innerHTML = `<button class="tab-btn active" data-species="">🐾 כל המינים</button>`;

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
    healthSelect.innerHTML = `<option value="">כל המצבים הבריאותיים</option>`;
    metadata.healthStatuses.forEach(h => {
        healthSelect.innerHTML += `<option value="${h.key}">${h.label}</option>`;
    });

    // Diet filter
    const dietSelect = document.getElementById('dietFilter');
    dietSelect.innerHTML = `<option value="">כל סוגי התזונה</option>`;
    metadata.dietTypes.forEach(d => {
        dietSelect.innerHTML += `<option value="${d.key}">${d.label}</option>`;
    });

    // Form species
    const modalSpecies = document.getElementById('modalAnimalSpecies');
    modalSpecies.innerHTML = `<option value="">בחר משפחה / מין</option>`;
    metadata.species.forEach(s => {
        modalSpecies.innerHTML += `<option value="${s.key}">${s.label}</option>`;
    });

    // Form diet
    const modalDiet = document.getElementById('modalAnimalDiet');
    modalDiet.innerHTML = '';
    metadata.dietTypes.forEach(d => {
        modalDiet.innerHTML += `<option value="${d.key}">${d.label}</option>`;
    });

    // Form health
    const modalHealth = document.getElementById('modalAnimalHealth');
    modalHealth.innerHTML = '';
    metadata.healthStatuses.forEach(h => {
        modalHealth.innerHTML += `<option value="${h.key}">${h.label}</option>`;
    });

    // Form conservation
    const modalCons = document.getElementById('modalAnimalConservation');
    modalCons.innerHTML = '';
    metadata.conservationStatuses.forEach(c => {
        modalCons.innerHTML += `<option value="${c.key}">${c.label}</option>`;
    });

    // Form gender
    const modalGender = document.getElementById('modalAnimalGender');
    modalGender.innerHTML = '';
    metadata.genders.forEach(g => {
        modalGender.innerHTML += `<option value="${g.key}">${g.label}</option>`;
    });

    // Medical modal health statuses
    const medHealthSelect = document.getElementById('medHealthStatus');
    medHealthSelect.innerHTML = '';
    metadata.healthStatuses.forEach(h => {
        medHealthSelect.innerHTML += `<option value="${h.key}">${h.label}</option>`;
    });
}

function populateSubSpeciesDropdown(selectedSpecies, selectedSubSpecies = '') {
    const subSelect = document.getElementById('modalAnimalSubSpecies');
    subSelect.innerHTML = `<option value="">בחר תת-מין</option>`;
    const filtered = metadata.subSpecies.filter(ss => !selectedSpecies || ss.species === selectedSpecies);
    filtered.forEach(ss => {
        const isSel = ss.key === selectedSubSpecies ? 'selected' : '';
        subSelect.innerHTML += `<option value="${ss.key}" ${isSel}>${ss.label}</option>`;
    });
}

function populateCagesDropdown() {
    const cageFilter = document.getElementById('cageFilter');
    cageFilter.innerHTML = `<option value="">כל הכלובים</option>`;
    allCages.forEach(c => {
        cageFilter.innerHTML += `<option value="${c.id}">כלוב ${c.id} (${c.species})</option>`;
    });

    const modalCage = document.getElementById('modalAnimalCage');
    modalCage.innerHTML = `<option value="">ללא כלוב כרגע</option>`;
    allCages.forEach(c => {
        modalCage.innerHTML += `<option value="${c.id}">כלוב ${c.id} (${c.species})</option>`;
    });
}

function renderAnimalsList(animals) {
    const grid = document.getElementById('animalsGrid');
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
    const speciesLabel = getLabel(metadata.species, animal.species) || animal.species;
    const subSpeciesLabel = getLabel(metadata.subSpecies, animal.subSpecies) || animal.subSpecies || '';
    const healthLabel = getLabel(metadata.healthStatuses, animal.healthStatus) || animal.healthStatus;
    const dietLabel = getLabel(metadata.dietTypes, animal.dietType) || animal.dietType;
    const consLabel = getLabel(metadata.conservationStatuses, animal.conservationStatus) || animal.conservationStatus;
    const isEndangered = animal.conservationStatus && ['VULNERABLE', 'ENDANGERED', 'CRITICALLY_ENDANGERED'].includes(animal.conservationStatus);

    // Feeding time calculation
    const fedTimeStr = formatTimeAgo(animal.lastFedTime);
    const isHungry = isAnimalHungry(animal.lastFedTime);

    return `
        <div class="animal-card" id="animal-card-${animal.id}">
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
                    <span class="badge ${isEndangered ? 'badge-endangered' : 'badge-least-concern'}">
                        ${isEndangered ? '⚠️' : '🌿'} ${consLabel}
                    </span>
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
    document.getElementById(id).classList.add('open');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

// Add/Edit Animal Modal
function openAnimalModal(animal = null) {
    const isEdit = !!animal;
    document.getElementById('animalModalTitle').textContent = isEdit ? `עריכת חיה: ${animal.name}` : 'הוספת חיה חדשה לגן החיות';
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
        notes: document.getElementById('modalAnimalNotes').value.trim()
    };

    try {
        const url = isEdit ? `${API_BASE}/animals/${id}` : `${API_BASE}/animals`;
        const method = isEdit ? 'PUT' : 'POST';
        const res = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'שגיאה בשמירת פרטי החיה');
        }

        closeModal('animalModal');
        showToast(isEdit ? 'פרטי החיה עודכנו בהצלחה!' : 'חיה חדשה נוספה לגן החיות בהצלחה!', 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Feed Modal
function openFeedModal(animalId, animalName, favoriteFood) {
    document.getElementById('feedAnimalId').value = animalId;
    document.getElementById('feedModalTitle').textContent = `🥩 האכלת ${animalName}`;
    document.getElementById('feedFoodItem').value = favoriteFood || '';
    document.getElementById('feedAmountKg').value = '2.5';
    document.getElementById('feedCaretaker').value = 'מטפל ראשי';
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
        const res = await fetch(`${API_BASE}/animals/${animalId}/feed`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('שגיאה בביצוע האכלה');

        closeModal('feedModal');
        showToast('החיה הואכלה בהצלחה!', 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Medical Checkup Modal
function openMedicalModal(animalId, animalName, currentHealth) {
    document.getElementById('medAnimalId').value = animalId;
    document.getElementById('medicalModalTitle').textContent = `🩺 בדיקה רפואית: ${animalName}`;
    document.getElementById('medHealthStatus').value = currentHealth;
    document.getElementById('medDiagnosis').value = '';
    document.getElementById('medTreatment').value = '';
    document.getElementById('medDoctor').value = 'ד"ר שרה מילר';
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
        const res = await fetch(`${API_BASE}/animals/${animalId}/medical`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!res.ok) throw new Error('שגיאה ברישום הבדיקה הרפואית');

        closeModal('medicalModal');
        showToast('הרשומה הרפואית נשמרה בהצלחה ומצב החיה עודכן!', 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        showToast(err.message, 'error');
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
            fetch(`${API_BASE}/animals/${animalId}/medical`),
            fetch(`${API_BASE}/animals/${animalId}/feedings`)
        ]);

        const medHistory = medRes.ok ? await medRes.json() : [];
        const feedHistory = feedRes.ok ? await feedRes.json() : [];

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
        const res = await fetch(`${API_BASE}/animals/${id}`, {
            method: 'DELETE'
        });

        if (!res.ok) throw new Error('שגיאה במחיקת החיה');

        showToast(`החיה "${name}" נמחקה בהצלחה`, 'success');
        await loadStats();
        await loadAnimals();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

// Utility
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${type === 'success' ? '✓' : '⚠️'}</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.remove();
    }, 4000);
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
