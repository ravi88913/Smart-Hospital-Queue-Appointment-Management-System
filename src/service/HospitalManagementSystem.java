package service;

import model.*;
import util.DataManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class HospitalManagementSystem {
    private HashMap<Integer, Patient> patientMap;
    private PriorityQueue<EmergencyPatient> emergencyQueue;
    private Queue<Patient> normalQueue;
    private ArrayList<Doctor> doctors;
    private ArrayList<Appointment> appointments;
    private Stack<Appointment> cancelledAppointments;

    // Auto-increment ID counters and arrival counters
    private int nextPatientId;
    private int nextDoctorId;
    private int nextAppointmentId;
    private int nextVisitId;
    private long emergencyArrivalCounter;

    public HospitalManagementSystem() {
        this.patientMap = new HashMap<>();
        // Custom Comparator to prioritize by severity level first, and arrival order second.
        this.emergencyQueue = new PriorityQueue<>(new Comparator<EmergencyPatient>() {
            @Override
            public int compare(EmergencyPatient p1, EmergencyPatient p2) {
                if (p1.getSeverityLevel() != p2.getSeverityLevel()) {
                    return Integer.compare(p1.getSeverityLevel(), p2.getSeverityLevel());
                }
                return Long.compare(p1.getArrivalOrder(), p2.getArrivalOrder());
            }
        });
        this.normalQueue = new LinkedList<>();
        this.doctors = new ArrayList<>();
        this.appointments = new ArrayList<>();
        this.cancelledAppointments = new Stack<>();

        this.nextPatientId = 1;
        this.nextDoctorId = 1;
        this.nextAppointmentId = 1;
        this.nextVisitId = 1;
        this.emergencyArrivalCounter = 1L;
    }

    // ==========================================
    // PATIENT MANAGEMENT
    // ==========================================

    public Patient registerPatient(String name, int age, String gender, String phoneNumber) {
        int id = nextPatientId++;
        Patient patient = new Patient(id, name, age, gender, phoneNumber, LocalDate.now());
        patientMap.put(id, patient);
        return patient;
    }

    public Patient searchPatient(int patientId) {
        return patientMap.get(patientId);
    }

    public boolean updatePatient(int patientId, String name, int age, String gender, String phoneNumber) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            patient.setName(name);
            patient.setAge(age);
            patient.setGender(gender);
            patient.setPhoneNumber(phoneNumber);
            return true;
        }
        return false;
    }

    public boolean deletePatient(int patientId) {
        if (!patientMap.containsKey(patientId)) {
            return false;
        }

        // Remove from map
        patientMap.remove(patientId);

        // Remove from normal queue
        normalQueue.removeIf(p -> p.getPatientId() == patientId);

        // Remove from emergency queue
        emergencyQueue.removeIf(p -> p.getPatientId() == patientId);

        // Cancel and remove scheduled appointments of this patient
        appointments.removeIf(app -> app.getPatientId() == patientId && "SCHEDULED".equals(app.getStatus()));

        // Clean stack of cancelled appointments
        cancelledAppointments.removeIf(app -> app.getPatientId() == patientId);

        return true;
    }

    public Collection<Patient> getAllPatients() {
        return patientMap.values();
    }

    // ==========================================
    // QUEUE MANAGEMENT
    // ==========================================

    public boolean isPatientInAnyQueue(int patientId) {
        for (EmergencyPatient ep : emergencyQueue) {
            if (ep.getPatientId() == patientId) {
                return true;
            }
        }
        for (Patient p : normalQueue) {
            if (p.getPatientId() == patientId) {
                return true;
            }
        }
        return false;
    }

    public boolean addEmergencyPatient(int patientId, int severityLevel) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            System.out.println("Error: Patient ID " + patientId + " does not exist.");
            return false;
        }
        if (isPatientInAnyQueue(patientId)) {
            System.out.println("Error: Patient ID " + patientId + " is already in a queue.");
            return false;
        }

        long arrival = emergencyArrivalCounter++;
        EmergencyPatient ep = new EmergencyPatient(
                patient.getPatientId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getRegistrationDate(),
                severityLevel,
                arrival
        );
        // Copy existing history reference to retain historical entries
        ep.setVisitHistory(patient.getVisitHistory());

        emergencyQueue.add(ep);
        return true;
    }

    public boolean addNormalPatient(int patientId) {
        Patient patient = patientMap.get(patientId);
        if (patient == null) {
            System.out.println("Error: Patient ID " + patientId + " does not exist.");
            return false;
        }
        if (isPatientInAnyQueue(patientId)) {
            System.out.println("Error: Patient ID " + patientId + " is already in a queue.");
            return false;
        }

        normalQueue.add(patient);
        return true;
    }

    public PriorityQueue<EmergencyPatient> getEmergencyQueue() {
        return emergencyQueue;
    }

    public Queue<Patient> getNormalQueue() {
        return normalQueue;
    }

    public Patient treatNextPatient() {
        if (!emergencyQueue.isEmpty()) {
            return emergencyQueue.poll();
        } else if (!normalQueue.isEmpty()) {
            return normalQueue.poll();
        }
        return null;
    }

    // ==========================================
    // DOCTOR MANAGEMENT
    // ==========================================

    public Doctor addDoctor(String name, String specialization) {
        int id = nextDoctorId++;
        Doctor doctor = new Doctor(id, name, specialization, true);
        doctors.add(doctor);
        return doctor;
    }

    public ArrayList<Doctor> getDoctors() {
        return doctors;
    }

    public Doctor searchDoctor(int doctorId) {
        for (Doctor d : doctors) {
            if (d.getDoctorId() == doctorId) {
                return d;
            }
        }
        return null;
    }

    // ==========================================
    // APPOINTMENT SCHEDULING
    // ==========================================

    public boolean isDoctorBooked(int doctorId, LocalDate date, LocalTime time) {
        for (Appointment app : appointments) {
            if (app.getDoctorId() == doctorId &&
                "SCHEDULED".equals(app.getStatus()) &&
                app.getAppointmentDate().equals(date) &&
                app.getAppointmentTime().equals(time)) {
                return true;
            }
        }
        return false;
    }

    public Appointment scheduleAppointment(int patientId, int doctorId, LocalDate date, LocalTime time) {
        if (!patientMap.containsKey(patientId)) {
            System.out.println("Error: Patient does not exist.");
            return null;
        }
        Doctor doctor = searchDoctor(doctorId);
        if (doctor == null) {
            System.out.println("Error: Doctor does not exist.");
            return null;
        }
        if (date.isBefore(LocalDate.now())) {
            System.out.println("Error: Appointment date cannot be in the past.");
            return null;
        }
        if (isDoctorBooked(doctorId, date, time)) {
            System.out.println("Error: Doctor is already booked for this slot.");
            return null;
        }

        int id = nextAppointmentId++;
        Appointment app = new Appointment(id, patientId, doctorId, date, time, "SCHEDULED");
        appointments.add(app);
        return app;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public Appointment searchAppointment(int appointmentId) {
        for (Appointment app : appointments) {
            if (app.getAppointmentId() == appointmentId) {
                return app;
            }
        }
        return null;
    }

    public ArrayList<Appointment> viewAppointmentsByPatient(int patientId) {
        ArrayList<Appointment> patientApps = new ArrayList<>();
        for (Appointment app : appointments) {
            if (app.getPatientId() == patientId) {
                patientApps.add(app);
            }
        }
        return patientApps;
    }

    public ArrayList<Appointment> viewAppointmentsByDoctor(int doctorId) {
        ArrayList<Appointment> doctorApps = new ArrayList<>();
        for (Appointment app : appointments) {
            if (app.getDoctorId() == doctorId) {
                doctorApps.add(app);
            }
        }
        return doctorApps;
    }

    public boolean cancelAppointment(int appointmentId) {
        Appointment app = searchAppointment(appointmentId);
        if (app == null) {
            System.out.println("Error: Appointment not found.");
            return false;
        }
        if (!"SCHEDULED".equals(app.getStatus())) {
            System.out.println("Error: Only SCHEDULED appointments can be cancelled.");
            return false;
        }

        app.setStatus("CANCELLED");
        cancelledAppointments.push(app);
        return true;
    }

    public boolean undoLastCancellation() {
        if (cancelledAppointments.isEmpty()) {
            System.out.println("Error: No cancelled appointments to undo.");
            return false;
        }

        Appointment app = cancelledAppointments.peek();
        // Check if doctor and patient still exist
        if (!patientMap.containsKey(app.getPatientId())) {
            System.out.println("Error: Cannot restore appointment. The patient has been deleted.");
            cancelledAppointments.pop(); // Remove it as it is invalid now
            return false;
        }
        if (searchDoctor(app.getDoctorId()) == null) {
            System.out.println("Error: Cannot restore appointment. The doctor has been deleted.");
            cancelledAppointments.pop();
            return false;
        }

        // Check if slot is available
        if (isDoctorBooked(app.getDoctorId(), app.getAppointmentDate(), app.getAppointmentTime())) {
            System.out.println("Unable to restore appointment because the slot is no longer available.");
            return false;
        }

        // Slot is available, restore
        cancelledAppointments.pop();
        app.setStatus("SCHEDULED");
        System.out.println("Successfully restored Appointment ID " + app.getAppointmentId() + ".");
        return true;
    }

    public boolean completeAppointment(int appointmentId, String diagnosis, String treatment, String prescription) {
        Appointment app = searchAppointment(appointmentId);
        if (app == null) {
            System.out.println("Error: Appointment not found.");
            return false;
        }
        if (!"SCHEDULED".equals(app.getStatus())) {
            System.out.println("Error: Appointment is not in SCHEDULED status.");
            return false;
        }

        Patient patient = patientMap.get(app.getPatientId());
        Doctor doctor = searchDoctor(app.getDoctorId());

        if (patient == null || doctor == null) {
            System.out.println("Error: Linked patient or doctor no longer exists.");
            return false;
        }

        // Generate visit record
        int vId = nextVisitId++;
        VisitRecord record = new VisitRecord(
                vId,
                patient.getPatientId(),
                doctor.getDoctorId(),
                doctor.getName(),
                LocalDate.now(),
                diagnosis,
                treatment,
                prescription
        );
        patient.addVisitRecord(record);
        app.setStatus("COMPLETED");
        return true;
    }

    // ==========================================
    // VISIT RECORDS AND HISTORY
    // ==========================================

    public boolean addVisitRecord(int patientId, int doctorId, String diagnosis, String treatment, String prescription) {
        Patient patient = patientMap.get(patientId);
        Doctor doctor = searchDoctor(doctorId);
        if (patient == null) {
            System.out.println("Error: Patient not found.");
            return false;
        }
        if (doctor == null) {
            System.out.println("Error: Doctor not found.");
            return false;
        }

        int vId = nextVisitId++;
        VisitRecord record = new VisitRecord(
                vId,
                patientId,
                doctorId,
                doctor.getName(),
                LocalDate.now(),
                diagnosis,
                treatment,
                prescription
        );
        patient.addVisitRecord(record);
        return true;
    }

    public List<VisitRecord> getPatientHistory(int patientId) {
        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            return patient.getVisitHistory();
        }
        return null;
    }

    // ==========================================
    // REPORTS & STATS
    // ==========================================

    public void generateReports() {
        System.out.println("===============================================");
        System.out.println("          HOSPITAL REPORTS & STATISTICS        ");
        System.out.println("===============================================");
        System.out.println("Total Registered Patients      : " + patientMap.size());
        System.out.println("Total Doctors                  : " + doctors.size());
        System.out.println("Emergency Patients Waiting     : " + emergencyQueue.size());
        System.out.println("Normal Patients Waiting        : " + normalQueue.size());

        int scheduled = 0, completed = 0, cancelled = 0;
        Map<Integer, Integer> docAppointments = new HashMap<>();
        for (Appointment app : appointments) {
            switch (app.getStatus()) {
                case "SCHEDULED": scheduled++; break;
                case "COMPLETED": completed++; break;
                case "CANCELLED": cancelled++; break;
            }
            docAppointments.put(app.getDoctorId(), docAppointments.getOrDefault(app.getDoctorId(), 0) + 1);
        }

        System.out.println("Total Scheduled Appointments   : " + scheduled);
        System.out.println("Total Completed Appointments   : " + completed);
        System.out.println("Total Cancelled Appointments   : " + cancelled);
        System.out.println("\n--- Doctor-wise Appointment Count ---");
        if (doctors.isEmpty()) {
            System.out.println("No doctors registered.");
        } else {
            for (Doctor d : doctors) {
                int count = docAppointments.getOrDefault(d.getDoctorId(), 0);
                System.out.println("Dr. " + d.getName() + " (ID: " + d.getDoctorId() + ", " + d.getSpecialization() + "): " + count + " appointment(s)");
            }
        }
        System.out.println("===============================================");
    }

    // ==========================================
    // PERSISTENCE WRAPPERS
    // ==========================================

    public void saveData() {
        DataManager.saveData(this);
    }

    public void loadData() {
        DataManager.loadData(this);
    }

    // ==========================================
    // STATE GETTERS & SETTERS (FOR DATA MANAGER)
    // ==========================================

    public HashMap<Integer, Patient> getPatientMap() {
        return patientMap;
    }

    public void setPatientMap(HashMap<Integer, Patient> patientMap) {
        this.patientMap = patientMap;
    }

    public void setDoctors(ArrayList<Doctor> doctors) {
        this.doctors = doctors;
    }

    public void setAppointments(ArrayList<Appointment> appointments) {
        this.appointments = appointments;
    }

    public Stack<Appointment> getCancelledAppointments() {
        return cancelledAppointments;
    }

    public void setCancelledAppointments(Stack<Appointment> cancelledAppointments) {
        this.cancelledAppointments = cancelledAppointments;
    }

    public ArrayList<EmergencyPatient> getEmergencyPatientsList() {
        return new ArrayList<>(emergencyQueue);
    }

    public ArrayList<Integer> getNormalPatientIdsList() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Patient p : normalQueue) {
            ids.add(p.getPatientId());
        }
        return ids;
    }

    public int getNextPatientId() {
        return nextPatientId;
    }

    public void setNextPatientId(int nextPatientId) {
        this.nextPatientId = nextPatientId;
    }

    public int getNextDoctorId() {
        return nextDoctorId;
    }

    public void setNextDoctorId(int nextDoctorId) {
        this.nextDoctorId = nextDoctorId;
    }

    public int getNextAppointmentId() {
        return nextAppointmentId;
    }

    public void setNextAppointmentId(int nextAppointmentId) {
        this.nextAppointmentId = nextAppointmentId;
    }

    public int getNextVisitId() {
        return nextVisitId;
    }

    public void setNextVisitId(int nextVisitId) {
        this.nextVisitId = nextVisitId;
    }

    public long getEmergencyArrivalCounter() {
        return emergencyArrivalCounter;
    }

    public void setEmergencyArrivalCounter(long emergencyArrivalCounter) {
        this.emergencyArrivalCounter = emergencyArrivalCounter;
    }

    // Reconstruction of priority queue and normal queue from saved state lists
    public void reconstructQueues(ArrayList<EmergencyPatient> emList, ArrayList<Integer> normList) {
        this.emergencyQueue.clear();
        if (emList != null) {
            this.emergencyQueue.addAll(emList);
        }

        this.normalQueue.clear();
        if (normList != null) {
            for (Integer pId : normList) {
                Patient p = patientMap.get(pId);
                if (p != null) {
                    this.normalQueue.add(p);
                }
            }
        }
    }
}
