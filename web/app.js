// Smart Hospital DSA Simulator Logic

// ==========================================
// DATA CLASS STRUCTURES (REPLICATING JAVA)
// ==========================================

class Patient {
    constructor(id, name, age, gender, phone) {
        this.patientId = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phone;
        this.registrationDate = new Date().toISOString().split('T')[0];
        this.visitHistory = []; // Linked List of VisitRecords
    }
}

class EmergencyPatient extends Patient {
    constructor(id, name, age, gender, phone, registrationDate, severity, arrivalOrder) {
        super(id, name, age, gender, phone);
        this.registrationDate = registrationDate;
        this.severityLevel = parseInt(severity);
        this.arrivalOrder = arrivalOrder;
    }
}

class Doctor {
    constructor(id, name, specialization) {
        this.doctorId = id;
        this.name = name;
        this.specialization = specialization;
        this.available = true;
    }
}

class Appointment {
    constructor(id, patientId, doctorId, date, time) {
        this.appointmentId = id;
        this.patientId = parseInt(patientId);
        this.doctorId = parseInt(doctorId);
        this.appointmentDate = date;
        this.appointmentTime = time;
        this.status = "SCHEDULED"; // SCHEDULED, COMPLETED, CANCELLED
    }
}

class VisitRecord {
    constructor(id, patientId, doctorId, doctorName, diagnosis, treatment, prescription) {
        this.visitId = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.visitDate = new Date().toISOString().split('T')[0];
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
    }
}

// ==========================================
// SYSTEM STATE (REPLICATING HMS IMPLEMENTATION)
// ==========================================

const state = {
    patientMap: new Map(), // HashMap<Integer, Patient>
    doctors: [],           // ArrayList<Doctor>
    appointments: [],      // ArrayList<Appointment>
    cancelledAppointments: [], // Stack<Appointment>
    emergencyQueue: [],    // PriorityQueue<EmergencyPatient>
    normalQueue: [],       // Queue<Patient> (LinkedList)
    
    // Auto-increment ID counters
    nextPatientId: 1,
    nextDoctorId: 1,
    nextAppointmentId: 1,
    nextVisitId: 1,
    emergencyArrivalCounter: 1
};

// Log entry helper
function addLog(message, type = 'info') {
    const logBox = document.getElementById('activity-log');
    const entry = document.createElement('div');
    entry.className = `log-entry ${type}`;
    const timestamp = new Date().toLocaleTimeString();
    entry.innerHTML = `<strong>[${timestamp}]</strong> ${message}`;
    logBox.appendChild(entry);
    logBox.scrollTop = logBox.scrollHeight;
}

// Update stats boxes
function updateDashboardStats() {
    document.getElementById('stat-patients').textContent = state.patientMap.size;
    document.getElementById('stat-doctors').textContent = state.doctors.length;
    document.getElementById('stat-emergency-queue').textContent = state.emergencyQueue.length;
    document.getElementById('stat-normal-queue').textContent = state.normalQueue.length;
    
    const activeApps = state.appointments.filter(app => app.status === "SCHEDULED").length;
    document.getElementById('stat-appointments').textContent = activeApps;
}

// ==========================================
// REGISTRATION AND LOOKUP
// ==========================================

function registerPatient(name, age, gender, phone) {
    // Validate duplicate phone numbers (optional check)
    const id = state.nextPatientId++;
    const p = new Patient(id, name, age, gender, phone);
    state.patientMap.set(id, p);
    addLog(`Patient Registered: ${name} (Assigned ID: ${id}) [HashMap Insert]`, 'success');
    
    updateDashboardStats();
    renderPatientsTable();
    renderVisualizers();
    populateDoctorFormDropdowns();
    return p;
}

function addDoctor(name, specialization) {
    const id = state.nextDoctorId++;
    const d = new Doctor(id, name, specialization);
    state.doctors.push(d);
    addLog(`Doctor Registered: Dr. ${name} (${specialization}) [ArrayList Add]`, 'success');
    
    updateDashboardStats();
    renderDoctorsTable();
    populateDoctorFormDropdowns();
    return d;
}

// ==========================================
// QUEUE MANAGEMENT
// ==========================================

function isPatientInAnyQueue(patientId) {
    const pId = parseInt(patientId);
    const inEmergency = state.emergencyQueue.some(p => p.patientId === pId);
    const inNormal = state.normalQueue.some(p => p.patientId === pId);
    return inEmergency || inNormal;
}

function addPatientToEmergency(patientId, severity) {
    const pId = parseInt(patientId);
    const p = state.patientMap.get(pId);
    if (!p) {
        addLog(`Error: Patient ID ${pId} not found in registry.`, 'error');
        return false;
    }
    if (isPatientInAnyQueue(pId)) {
        addLog(`Error: Patient ID ${pId} is already waiting in a queue.`, 'warning');
        return false;
    }

    const arrival = state.emergencyArrivalCounter++;
    const ep = new EmergencyPatient(p.patientId, p.name, p.age, p.gender, p.phoneNumber, p.registrationDate, severity, arrival);
    ep.visitHistory = p.visitHistory; // Keep visit history reference
    
    state.emergencyQueue.push(ep);
    // Sort Queue based on Comparator Logic (First: severity Level, Second: arrival Order)
    sortEmergencyQueue();
    
    addLog(`Emergency Queue Enqueue: ${p.name} (Severity: ${severity}) [PriorityQueue Enqueue]`, 'warning');
    
    updateDashboardStats();
    renderQueuesBoards();
    renderVisualizers();
    checkNextPatientForTreatment();
    return true;
}

function sortEmergencyQueue() {
    state.emergencyQueue.sort((a, b) => {
        if (a.severityLevel !== b.severityLevel) {
            return a.severityLevel - b.severityLevel;
        }
        return a.arrivalOrder - b.arrivalOrder;
    });
}

function addPatientToNormal(patientId) {
    const pId = parseInt(patientId);
    const p = state.patientMap.get(pId);
    if (!p) {
        addLog(`Error: Patient ID ${pId} not found in registry.`, 'error');
        return false;
    }
    if (isPatientInAnyQueue(pId)) {
        addLog(`Error: Patient ID ${pId} is already waiting in a queue.`, 'warning');
        return false;
    }

    state.normalQueue.push(p);
    addLog(`Normal Queue Enqueue: ${p.name} [FIFO Queue Enqueue]`, 'info');
    
    updateDashboardStats();
    renderQueuesBoards();
    renderVisualizers();
    checkNextPatientForTreatment();
    return true;
}

// ==========================================
// SCHEDULING AND CANCELLATIONS
// ==========================================

function isDoctorBooked(doctorId, date, time) {
    const docId = parseInt(doctorId);
    return state.appointments.some(app => 
        app.doctorId === docId && 
        app.status === "SCHEDULED" && 
        app.appointmentDate === date && 
        app.appointmentTime === time
    );
}

function scheduleAppointment(patientId, doctorId, date, time) {
    const pId = parseInt(patientId);
    const docId = parseInt(doctorId);
    
    const patient = state.patientMap.get(pId);
    const doctor = state.doctors.find(d => d.doctorId === docId);
    
    if (!patient) {
        addLog(`Booking Error: Patient ID ${pId} does not exist.`, 'error');
        return null;
    }
    if (!doctor) {
        addLog(`Booking Error: Doctor ID ${docId} does not exist.`, 'error');
        return null;
    }
    
    const today = new Date().toISOString().split('T')[0];
    if (date < today) {
        addLog(`Booking Error: Cannot book appointments in the past.`, 'warning');
        return null;
    }
    
    if (isDoctorBooked(docId, date, time)) {
        addLog(`Booking Error: Dr. ${doctor.name} is already booked at ${date} ${time}.`, 'warning');
        return null;
    }

    const appId = state.nextAppointmentId++;
    const app = new Appointment(appId, pId, docId, date, time);
    state.appointments.push(app);
    addLog(`Scheduled Appointment ID ${appId}: ${patient.name} with Dr. ${doctor.name} [ArrayList Add]`, 'info');
    
    updateDashboardStats();
    renderAppointmentsTable();
    renderVisualizers();
    return app;
}

function cancelAppointment(appointmentId) {
    const appId = parseInt(appointmentId);
    const app = state.appointments.find(a => a.appointmentId === appId);
    if (!app) {
        addLog(`Cancellation Error: Appointment ID ${appId} not found.`, 'error');
        return false;
    }
    if (app.status !== "SCHEDULED") {
        addLog(`Cancellation Error: Appointment is already ${app.status}.`, 'warning');
        return false;
    }

    app.status = "CANCELLED";
    state.cancelledAppointments.push(app); // Push onto Stack
    addLog(`Cancelled Appointment ID ${appId} [Stack Push]`, 'warning');
    
    updateDashboardStats();
    renderAppointmentsTable();
    renderVisualizers();
    return true;
}

function undoLastCancellation() {
    if (state.cancelledAppointments.length === 0) {
        addLog("Undo Error: No cancelled appointments in the Stack.", "error");
        alert("The cancellation Stack is empty!");
        return false;
    }

    const app = state.cancelledAppointments[state.cancelledAppointments.length - 1];
    
    // Check if patient and doctor still exist in map/array
    if (!state.patientMap.has(app.patientId)) {
        addLog(`Undo Error: Cannot restore appointment. Patient ID ${app.patientId} has been deleted.`, 'error');
        state.cancelledAppointments.pop(); // Pop invalid entry
        renderVisualizers();
        return false;
    }
    const docExists = state.doctors.some(d => d.doctorId === app.doctorId);
    if (!docExists) {
        addLog(`Undo Error: Cannot restore appointment. Doctor ID ${app.doctorId} has been deleted.`, 'error');
        state.cancelledAppointments.pop();
        renderVisualizers();
        return false;
    }

    // Check if doctor's slot is still available
    if (isDoctorBooked(app.doctorId, app.appointmentDate, app.appointmentTime)) {
        addLog(`Undo Warning: Cannot restore Appointment ID ${app.appointmentId}. Doctor slot ${app.appointmentDate} ${app.appointmentTime} has been booked.`, 'warning');
        alert("Unable to restore appointment because the slot is no longer available.");
        return false;
    }

    // Restore
    state.cancelledAppointments.pop(); // Pop from Stack
    app.status = "SCHEDULED";
    addLog(`Undo Success: Restored Appointment ID ${app.appointmentId} [Stack Pop]`, 'success');
    
    updateDashboardStats();
    renderAppointmentsTable();
    renderVisualizers();
    return true;
}

// ==========================================
// TREATMENT AND VISITS
// ==========================================

let activeTreatedPatient = null;
let activeTreatedQueueType = ''; // 'emergency' or 'normal'

function checkNextPatientForTreatment() {
    const box = document.getElementById('treatment-active-box');
    const emptyMsg = document.getElementById('treatment-empty-msg');
    const detailsCard = document.getElementById('active-treatment-patient');
    
    if (state.emergencyQueue.length > 0) {
        activeTreatedPatient = state.emergencyQueue[0];
        activeTreatedQueueType = 'emergency';
        emptyMsg.classList.add('hidden');
        detailsCard.classList.remove('hidden');
        
        document.getElementById('active-patient-name').textContent = activeTreatedPatient.name;
        document.getElementById('active-patient-badge').className = `badge emergency-crit-${activeTreatedPatient.severityLevel}`;
        document.getElementById('active-patient-badge').textContent = `Emergency Priority ${activeTreatedPatient.severityLevel}`;
        document.getElementById('active-patient-info').textContent = `Patient ID: ${activeTreatedPatient.patientId} | Age: ${activeTreatedPatient.age} | Phone: ${activeTreatedPatient.phoneNumber}`;
    } else if (state.normalQueue.length > 0) {
        activeTreatedPatient = state.normalQueue[0];
        activeTreatedQueueType = 'normal';
        emptyMsg.classList.add('hidden');
        detailsCard.classList.remove('hidden');
        
        document.getElementById('active-patient-name').textContent = activeTreatedPatient.name;
        document.getElementById('active-patient-badge').className = `badge normal`;
        document.getElementById('active-patient-badge').textContent = `Normal Patient`;
        document.getElementById('active-patient-info').textContent = `Patient ID: ${activeTreatedPatient.patientId} | Age: ${activeTreatedPatient.age} | Phone: ${activeTreatedPatient.phoneNumber}`;
    } else {
        activeTreatedPatient = null;
        activeTreatedQueueType = '';
        emptyMsg.classList.remove('hidden');
        detailsCard.classList.add('hidden');
    }
}

function completeTreatment(doctorId, diagnosis, treatment, prescription) {
    if (!activeTreatedPatient) return;
    
    const docId = parseInt(doctorId);
    const doctor = state.doctors.find(d => d.doctorId === docId);
    if (!doctor) {
        alert("Selected doctor does not exist.");
        return;
    }
    
    // Dequeue patient from the correct queue
    if (activeTreatedQueueType === 'emergency') {
        state.emergencyQueue.shift(); // Remove head of sorted priority queue
    } else {
        state.normalQueue.shift(); // Remove head of FIFO queue
    }
    
    // Record visit
    const recordId = state.nextVisitId++;
    const record = new VisitRecord(recordId, activeTreatedPatient.patientId, doctor.doctorId, doctor.name, diagnosis, treatment, prescription);
    
    // Fetch patient from registry and append history record
    const patientObj = state.patientMap.get(activeTreatedPatient.patientId);
    if (patientObj) {
        patientObj.visitHistory.push(record); // Append to list
    }
    
    addLog(`Patient Treated: ${activeTreatedPatient.name} by Dr. ${doctor.name}. Visit logged (ID: ${recordId}) [LinkedList Append]`, 'success');
    
    // Reset form fields
    document.getElementById('treatment-form').reset();
    
    // Refresh tables and queues
    updateDashboardStats();
    renderQueuesBoards();
    renderPatientsTable();
    renderVisualizers();
    checkNextPatientForTreatment();
}

function completeAppointment(appointmentId, diagnosis, treatment, prescription) {
    const appId = parseInt(appointmentId);
    const app = state.appointments.find(a => a.appointmentId === appId);
    if (!app) {
        alert("Appointment ID not found.");
        return;
    }
    if (app.status !== "SCHEDULED") {
        alert("Appointment is already completed or cancelled.");
        return;
    }

    const patient = state.patientMap.get(app.patientId);
    const doctor = state.doctors.find(d => d.doctorId === app.doctorId);
    
    if (!patient || !doctor) {
        alert("Linked patient or doctor no longer exists.");
        return;
    }

    const vId = state.nextVisitId++;
    const record = new VisitRecord(vId, patient.patientId, doctor.doctorId, doctor.name, diagnosis, treatment, prescription);
    patient.visitHistory.push(record);
    
    app.status = "COMPLETED";
    addLog(`Completed Appointment ID ${appId} for ${patient.name}. Visit logged (ID: ${vId}) [LinkedList Append]`, 'success');
    
    updateDashboardStats();
    renderAppointmentsTable();
    renderPatientsTable();
    renderVisualizers();
}

// ==========================================
// RENDERERS (POPULATING HTML TABLES & VIEWS)
// ==========================================

function renderPatientsTable() {
    const tbody = document.getElementById('patients-table-body');
    tbody.innerHTML = '';
    
    state.patientMap.forEach((p) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${p.patientId}</strong></td>
            <td>${p.name}</td>
            <td>${p.age} / ${p.gender}</td>
            <td>${p.phoneNumber}</td>
            <td>${p.registrationDate}</td>
            <td><button class="action-btn btn-small" onclick="viewHistory(${p.patientId})">${p.visitHistory.length} Record(s)</button></td>
            <td><button class="action-btn danger-btn btn-small" onclick="deletePatientRecord(${p.patientId})">❌ Delete</button></td>
        `;
        tbody.appendChild(tr);
    });
    
    if (state.patientMap.size === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-msg" style="text-align:center;">No patients registered.</td></tr>`;
    }
}

function deletePatientRecord(patientId) {
    const pId = parseInt(patientId);
    if (confirm(`Are you sure you want to delete Patient ID #${pId}? This clears their appointments and queue statuses.`)) {
        state.patientMap.delete(pId);
        
        // Remove from queues
        state.emergencyQueue = state.emergencyQueue.filter(ep => ep.patientId !== pId);
        state.normalQueue = state.normalQueue.filter(p => p.patientId !== pId);
        
        // Clear active appointments
        state.appointments = state.appointments.filter(app => !(app.patientId === pId && app.status === "SCHEDULED"));
        state.cancelledAppointments = state.cancelledAppointments.filter(app => app.patientId !== pId);
        
        addLog(`Deleted Patient ID #${pId} [HashMap Remove]`, 'warning');
        
        updateDashboardStats();
        renderPatientsTable();
        renderQueuesBoards();
        renderAppointmentsTable();
        renderVisualizers();
        checkNextPatientForTreatment();
    }
}

function renderDoctorsTable() {
    const tbody = document.getElementById('doctors-table-body');
    tbody.innerHTML = '';
    
    state.doctors.forEach((d) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${d.doctorId}</strong></td>
            <td>Dr. ${d.name}</td>
            <td>${d.specialization}</td>
            <td><span class="status-indicator" style="background-color: ${d.available ? 'var(--accent-emerald)' : 'var(--text-muted)'};"></span> ${d.available ? 'Available' : 'Unavailable'}</td>
            <td>
                <button class="action-btn btn-small" onclick="toggleDoctorAvailability(${d.doctorId})">Toggle Status</button>
            </td>
        `;
        tbody.appendChild(tr);
    });

    if (state.doctors.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-msg" style="text-align:center;">No doctors registered.</td></tr>`;
    }
}

function toggleDoctorAvailability(doctorId) {
    const d = state.doctors.find(doc => doc.doctorId === doctorId);
    if (d) {
        d.available = !d.available;
        addLog(`Toggled availability of Dr. ${d.name} to ${d.available ? 'Available' : 'Unavailable'}.`, 'info');
        renderDoctorsTable();
    }
}

function renderAppointmentsTable(filterPat = null, filterDoc = null) {
    const tbody = document.getElementById('appointments-table-body');
    tbody.innerHTML = '';
    
    let list = state.appointments;
    if (filterPat !== null) list = list.filter(app => app.patientId === filterPat);
    if (filterDoc !== null) list = list.filter(app => app.doctorId === filterDoc);
    
    list.forEach((app) => {
        const patient = state.patientMap.get(app.patientId);
        const doctor = state.doctors.find(d => d.doctorId === app.doctorId);
        
        const pName = patient ? patient.name : `Deleted Patient (#${app.patientId})`;
        const dName = doctor ? `Dr. ${doctor.name}` : `Deleted Doctor (#${app.doctorId})`;
        
        let badgeClass = '';
        if (app.status === 'SCHEDULED') badgeClass = 'badge normal';
        else if (app.status === 'COMPLETED') badgeClass = 'badge emergency-low';
        else badgeClass = 'badge emergency-critical';
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong>#${app.appointmentId}</strong></td>
            <td>${pName} (ID: ${app.patientId})</td>
            <td>${dName}</td>
            <td>${app.appointmentDate} at ${app.appointmentTime}</td>
            <td><span class="${badgeClass}">${app.status}</span></td>
            <td>
                ${app.status === 'SCHEDULED' ? `
                    <button class="action-btn btn-small" onclick="completeAppFromTable(${app.appointmentId})">🩺 Complete</button>
                    <button class="action-btn danger-btn btn-small" onclick="cancelAppointment(${app.appointmentId})">❌ Cancel</button>
                ` : '--'}
            </td>
        `;
        tbody.appendChild(tr);
    });

    if (list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-msg" style="text-align:center;">No matching appointments found.</td></tr>`;
    }
}

function completeAppFromTable(appId) {
    const diagnosis = prompt("Enter Diagnosis:");
    if (diagnosis === null || diagnosis.trim() === '') return;
    const treatment = prompt("Enter Treatment:");
    if (treatment === null || treatment.trim() === '') return;
    const prescription = prompt("Enter Prescription:");
    if (prescription === null || prescription.trim() === '') return;
    
    completeAppointment(appId, diagnosis, treatment, prescription);
}

function renderQueuesBoards() {
    // 1. Emergency Board
    const emContainer = document.getElementById('emergency-queue-list');
    emContainer.innerHTML = '';
    
    state.emergencyQueue.forEach((ep) => {
        const div = document.createElement('div');
        div.className = `queue-patient-node crit-${ep.severityLevel}`;
        div.innerHTML = `
            <div class="node-details">
                <h4>${ep.name} (ID: #${ep.patientId})</h4>
                <p>Severity Level: ${ep.severityLevel} | Arrival Order: ${ep.arrivalOrder}</p>
            </div>
            <span class="badge ${ep.severityLevel === 1 ? 'emergency-critical' : ep.severityLevel === 2 ? 'emergency-serious' : ep.severityLevel === 3 ? 'emergency-moderate' : 'emergency-low'}">
                ${ep.severityLevel === 1 ? 'Critical' : ep.severityLevel === 2 ? 'Serious' : ep.severityLevel === 3 ? 'Moderate' : 'Low'}
            </span>
        `;
        emContainer.appendChild(div);
    });
    if (state.emergencyQueue.length === 0) {
        emContainer.innerHTML = '<p class="empty-msg">No emergency patients waiting.</p>';
    }

    // 2. Normal Board
    const normContainer = document.getElementById('normal-queue-list');
    normContainer.innerHTML = '';
    state.normalQueue.forEach((p, idx) => {
        const div = document.createElement('div');
        div.className = 'queue-patient-node normal-node';
        div.innerHTML = `
            <div class="node-details">
                <h4>${p.name} (ID: #${p.patientId})</h4>
                <p>Position: #${idx + 1} | Registration Date: ${p.registrationDate}</p>
            </div>
            <span class="badge normal">Normal</span>
        `;
        normContainer.appendChild(div);
    });
    if (state.normalQueue.length === 0) {
        normContainer.innerHTML = '<p class="empty-msg">No normal patients waiting.</p>';
    }
}

// Populate treating doctor lists
function populateDoctorFormDropdowns() {
    const dropdown = document.getElementById('treatment-doctor');
    dropdown.innerHTML = '<option value="">-- Choose Doctor --</option>';
    
    state.doctors.forEach((d) => {
        if (d.available) {
            const opt = document.createElement('option');
            opt.value = d.doctorId;
            opt.textContent = `Dr. ${d.name} (${d.specialization})`;
            dropdown.appendChild(opt);
        }
    });
}

// ==========================================
// DSA LIVE STRUCTURE GRAPH RENDERERS
// ==========================================

function renderVisualizers() {
    // 1. HashMap Visualizer (Patient ID mapping to Patient details with hash mapping chains)
    const mapCanvas = document.getElementById('dsa-hashmap-canvas');
    mapCanvas.innerHTML = '';
    
    // Let's implement a visual hash mapping grid using index buckets (modulo 10)
    const buckets = Array.from({length: 10}, () => []);
    
    state.patientMap.forEach((p) => {
        const hash = p.patientId % 10;
        buckets[hash].push(p);
    });

    buckets.forEach((bucketElements, index) => {
        const row = document.createElement('div');
        row.className = 'hashmap-bucket-row';
        row.innerHTML = `<span class="hashmap-index-badge">B-${index}</span>`;
        
        if (bucketElements.length === 0) {
            const emptyLabel = document.createElement('span');
            emptyLabel.className = 'empty-msg';
            emptyLabel.textContent = 'Empty';
            row.appendChild(emptyLabel);
        } else {
            bucketElements.forEach((p, idx) => {
                const node = document.createElement('div');
                node.className = 'hashmap-node';
                node.innerHTML = `<strong>ID: ${p.patientId}</strong>: ${p.name.split(' ')[0]}`;
                row.appendChild(node);
                
                // Render link pointer arrow if chained elements exist
                if (idx < bucketElements.length - 1) {
                    const arrow = document.createElement('span');
                    arrow.className = 'hashmap-arrow';
                    arrow.innerHTML = '&rarr;';
                    row.appendChild(arrow);
                }
            });
        }
        mapCanvas.appendChild(row);
    });

    // 2. Stack Visualizer (Cancelled Appointments Stack)
    const stackCanvas = document.getElementById('dsa-stack-canvas');
    stackCanvas.innerHTML = '';
    
    const stackCylinder = document.createElement('div');
    stackCylinder.className = 'stack-cylinder';
    
    state.cancelledAppointments.forEach((app, idx) => {
        const el = document.createElement('div');
        el.className = 'stack-element';
        const patientName = state.patientMap.has(app.patientId) ? state.patientMap.get(app.patientId).name.split(' ')[0] : `Pat #${app.patientId}`;
        const isTop = idx === state.cancelledAppointments.length - 1;
        el.innerHTML = `
            <strong>App ID: #${app.appointmentId}</strong><br>
            ${patientName}<br>
            <span style="font-size:9px; color:${isTop ? 'yellow' : 'var(--text-secondary)'}; font-weight:bold;">
                ${isTop ? 'TOP OF STACK' : `Depth: ${state.cancelledAppointments.length - idx - 1}`}
            </span>
        `;
        if (isTop) {
            el.style.borderColor = 'var(--accent-amber)';
            el.style.backgroundColor = 'rgba(245, 158, 11, 0.12)';
        }
        stackCylinder.appendChild(el);
    });
    
    if (state.cancelledAppointments.length === 0) {
        stackCylinder.innerHTML = '<p class="empty-msg" style="margin:auto;">Stack is empty.</p>';
    }
    stackCanvas.appendChild(stackCylinder);

    // 3. FIFO Queue Pipeline (Normal Queue LinkedList Pipeline)
    const fqueueCanvas = document.getElementById('dsa-fqueue-canvas');
    fqueueCanvas.innerHTML = '';
    
    const pipelineWrapper = document.createElement('div');
    pipelineWrapper.className = 'queue-pipeline-wrapper';
    
    if (state.normalQueue.length === 0) {
        fqueueCanvas.innerHTML = '<p class="empty-msg" style="margin:20px 0; text-align:center;">Normal queue is empty.</p>';
    } else {
        // Enqueue from right, dequeue from left.
        state.normalQueue.forEach((p, idx) => {
            const node = document.createElement('div');
            node.className = 'pipeline-node';
            
            let label = '';
            if (idx === 0) label = '<br><span style="color:var(--accent-emerald);font-weight:600;">(HEAD / DEQUEUE)</span>';
            else if (idx === state.normalQueue.length - 1) label = '<br><span style="color:var(--accent-rose);font-weight:600;">(TAIL / ENQUEUE)</span>';
            
            node.innerHTML = `
                <h4>${p.name.split(' ')[0]}</h4>
                <span>ID: #${p.patientId}</span>
                ${label}
            `;
            pipelineWrapper.appendChild(node);
            
            // Add arrows between nodes
            if (idx < state.normalQueue.length - 1) {
                const arrow = document.createElement('span');
                arrow.className = 'pipeline-arrow';
                arrow.innerHTML = '&larr;'; // pointer points to next element towards head
                pipelineWrapper.appendChild(arrow);
            }
        });
        fqueueCanvas.appendChild(pipelineWrapper);
    }

    // 4. Priority Queue Visualizer (Min-Heap Array layout mapping)
    const pqueueCanvas = document.getElementById('dsa-pqueue-canvas');
    pqueueCanvas.innerHTML = '';
    
    const heapWrapper = document.createElement('div');
    heapWrapper.className = 'heap-tree-wrapper';
    
    if (state.emergencyQueue.length === 0) {
        pqueueCanvas.innerHTML = '<p class="empty-msg" style="margin:20px 0; text-align:center;">Emergency queue is empty.</p>';
    } else {
        // Render heap sequential array slots
        const arrayGrid = document.createElement('div');
        arrayGrid.className = 'heap-array-grid';
        
        state.emergencyQueue.forEach((ep, idx) => {
            const cell = document.createElement('div');
            cell.className = 'heap-cell';
            const isRoot = idx === 0;
            if (isRoot) {
                cell.style.borderColor = 'var(--accent-amber)';
                cell.style.backgroundColor = 'rgba(245, 158, 11, 0.12)';
            }
            cell.innerHTML = `
                <strong>${ep.name.split(' ')[0]} (ID: ${ep.patientId})</strong>
                <span>Priority: Lvl ${ep.severityLevel}</span>
                <span class="heap-cell-index">Array Index: [${idx}] ${isRoot ? '(ROOT)' : ''}</span>
            `;
            arrayGrid.appendChild(cell);
        });
        
        heapWrapper.appendChild(arrayGrid);
        pqueueCanvas.appendChild(heapWrapper);
    }
}

// ==========================================
// VISIT TIMELINE (LINKED LIST DETAILS POPUP)
// ==========================================

function viewHistory(patientId) {
    const pId = parseInt(patientId);
    const p = state.patientMap.get(pId);
    if (!p) return;

    document.getElementById('modal-patient-name').textContent = `${p.name} - Clinical Visit Timeline`;
    const timeline = document.getElementById('history-timeline');
    timeline.innerHTML = '';
    
    p.visitHistory.forEach((record) => {
        const item = document.createElement('div');
        item.className = 'timeline-item';
        item.innerHTML = `
            <h4>Diagnostic Entry #${record.visitId}</h4>
            <div class="visit-date">Visited: ${record.visitDate} | Consulted by: Dr. ${record.doctorName}</div>
            <p><strong>Diagnosis:</strong> ${record.diagnosis}</p>
            <p><strong>Treatment:</strong> ${record.treatment}</p>
            <p><strong>Prescription:</strong> ${record.prescription}</p>
        `;
        timeline.appendChild(item);
    });

    if (p.visitHistory.length === 0) {
        timeline.innerHTML = '<p class="empty-msg">No clinical history recorded yet.</p>';
    }

    document.getElementById('history-modal').classList.remove('hidden');
}

// Close Modal
document.getElementById('close-modal-btn').addEventListener('click', () => {
    document.getElementById('history-modal').classList.add('hidden');
});
document.getElementById('history-modal').addEventListener('click', (e) => {
    if (e.target.id === 'history-modal') {
        document.getElementById('history-modal').classList.add('hidden');
    }
});

// ==========================================
// SEEDING DUMMY RECORDS (FOR INITIAL WOW)
// ==========================================

function seedSystemData() {
    // 1. Doctors
    addDoctor("Sarah Jenkins", "Cardiology");
    addDoctor("Arjun Mehta", "Neurology");
    addDoctor("Linda Ross", "Orthopedics");
    addDoctor("Charles Kim", "General Medicine");
    addDoctor("Elena Rostova", "Dermatology");
    
    // 2. Patients
    registerPatient("Alice Vance", 30, "Female", "1234567890");
    registerPatient("Charlie Brown", 25, "Male", "9876543210");
    registerPatient("Dave Miller", 45, "Male", "1112223333");
    registerPatient("Fiona Gallagher", 18, "Female", "4445556666");
    registerPatient("George Clark", 67, "Male", "7778889999");
    
    // 3. Queue elements
    addPatientToEmergency(1, 2); // Alice severity 2
    addPatientToEmergency(3, 2); // Dave severity 2
    addPatientToEmergency(2, 1); // Charlie severity 1 (Critical - jumps to head)
    addPatientToNormal(4);       // Fiona normal
    addPatientToNormal(5);       // George normal

    // 4. Appointments
    scheduleAppointment(1, 4, "2026-08-10", "10:00");
    scheduleAppointment(2, 4, "2026-08-10", "11:00");
    scheduleAppointment(3, 2, "2026-08-12", "14:00");
    
    // Trigger initial state
    updateDashboardStats();
    checkNextPatientForTreatment();
}

// ==========================================
// DOM INTERACTION HANDLERS & INGESTION FORM ACTIONS
// ==========================================

document.addEventListener('DOMContentLoaded', () => {
    // 1. Tab switches
    const tabButtons = document.querySelectorAll('.nav-btn');
    const tabPanels = document.querySelectorAll('.tab-panel');
    const tabTitleHeader = document.getElementById('tab-title');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            // Remove active classes
            tabButtons.forEach(b => b.classList.remove('active'));
            tabPanels.forEach(p => p.classList.remove('active'));
            
            // Add active classes
            btn.classList.add('active');
            const targetTab = btn.getAttribute('data-tab');
            document.getElementById(`tab-${targetTab}`).classList.add('active');
            
            // Title text updates
            tabTitleHeader.textContent = btn.innerText;
            
            // Re-render visualizers to prevent scaling bugs
            if (targetTab === 'visualizer') {
                renderVisualizers();
            }
        });
    });

    // 2. Patient Form Handler
    document.getElementById('patient-registration-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const name = document.getElementById('pat-name').value;
        const age = document.getElementById('pat-age').value;
        const gender = document.getElementById('pat-gender').value;
        const phone = document.getElementById('pat-phone').value;

        // Simple validation check
        if (!/^\d{7,15}$/.test(phone)) {
            alert("Phone number must contain only digits and be between 7 to 15 digits.");
            return;
        }

        registerPatient(name, age, gender, phone);
        document.getElementById('patient-registration-form').reset();
    });

    // 3. Doctor Form Handler
    document.getElementById('doctor-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const name = document.getElementById('doc-name').value;
        const spec = document.getElementById('doc-specialization').value;

        addDoctor(name, spec);
        document.getElementById('doctor-form').reset();
    });

    // 4. Appointment Booking Handler
    document.getElementById('appointment-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const patId = document.getElementById('app-patient-id').value;
        const docId = document.getElementById('app-doctor-id').value;
        const date = document.getElementById('app-date').value;
        const time = document.getElementById('app-time').value;

        scheduleAppointment(patId, docId, date, time);
        document.getElementById('appointment-form').reset();
    });

    // 5. Emergency Enqueue Form Handler
    document.getElementById('emergency-queue-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const patId = document.getElementById('em-patient-id').value;
        const severity = document.getElementById('em-severity').value;

        addPatientToEmergency(patId, severity);
        document.getElementById('emergency-queue-form').reset();
    });

    // 6. Normal Enqueue Form Handler
    document.getElementById('normal-queue-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const patId = document.getElementById('norm-patient-id').value;

        addPatientToNormal(patId);
        document.getElementById('normal-queue-form').reset();
    });

    // 7. Treatment Form Submission
    document.getElementById('treatment-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const docId = document.getElementById('treatment-doctor').value;
        const diagnosis = document.getElementById('treatment-diagnosis').value;
        const plan = document.getElementById('treatment-plan').value;
        const prescription = document.getElementById('treatment-prescription').value;

        completeTreatment(docId, diagnosis, plan, prescription);
    });

    // 8. Undo Cancellation Button
    document.getElementById('undo-cancel-btn').addEventListener('click', () => {
        undoLastCancellation();
    });

    // 9. Appointment Search/Filters
    document.getElementById('app-filter-patient').addEventListener('input', applyAppFilters);
    document.getElementById('app-filter-doctor').addEventListener('input', applyAppFilters);
    document.getElementById('clear-app-filters').addEventListener('click', () => {
        document.getElementById('app-filter-patient').value = '';
        document.getElementById('app-filter-doctor').value = '';
        renderAppointmentsTable();
    });

    function applyAppFilters() {
        const patVal = document.getElementById('app-filter-patient').value;
        const docVal = document.getElementById('app-filter-doctor').value;
        
        const filterPat = patVal !== '' ? parseInt(patVal) : null;
        const filterDoc = docVal !== '' ? parseInt(docVal) : null;
        
        renderAppointmentsTable(filterPat, filterDoc);
    }

    // 10. Patient Quick ID search in records
    document.getElementById('patient-search-btn').addEventListener('click', () => {
        const val = document.getElementById('patient-search-input').value.trim();
        const tbody = document.getElementById('patients-table-body');
        
        if (val === '') {
            renderPatientsTable();
            return;
        }

        const id = parseInt(val);
        const p = state.patientMap.get(id);
        tbody.innerHTML = '';
        
        if (p) {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td><strong>#${p.patientId}</strong></td>
                <td>${p.name}</td>
                <td>${p.age} / ${p.gender}</td>
                <td>${p.phoneNumber}</td>
                <td>${p.registrationDate}</td>
                <td><button class="action-btn btn-small" onclick="viewHistory(${p.patientId})">${p.visitHistory.length} Record(s)</button></td>
                <td><button class="action-btn danger-btn btn-small" onclick="deletePatientRecord(${p.patientId})">❌ Delete</button></td>
            `;
            tbody.appendChild(tr);
            addLog(`Searched Patient ID #${id} [HashMap O(1) Fetch]`, 'info');
        } else {
            tbody.innerHTML = `<tr><td colspan="7" class="empty-msg" style="text-align:center;">No patient found with ID #${id}.</td></tr>`;
            addLog(`Search Miss: Patient ID #${id} not found in HashMap.`, 'warning');
        }
    });

    // Initialize with Seed Data
    seedSystemData();
});
