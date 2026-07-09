import model.*;
import service.HospitalManagementSystem;
import util.InputValidator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final HospitalManagementSystem hms = new HospitalManagementSystem();

    public static void main(String[] args) {
        // Automatically load saved data on startup
        hms.loadData();

        while (true) {
            displayMainMenu();
            int choice = InputValidator.readInt(scanner, "Enter your choice: ", 0, 7);
            System.out.println();
            switch (choice) {
                case 1:
                    handlePatientManagement();
                    break;
                case 2:
                    handleQueueManagement();
                    break;
                case 3:
                    handleDoctorManagement();
                    break;
                case 4:
                    handleAppointmentManagement();
                    break;
                case 5:
                    handleVisitHistory();
                    break;
                case 6:
                    hms.generateReports();
                    break;
                case 7:
                    hms.saveData();
                    break;
                case 0:
                    // Auto-save data on exit
                    hms.saveData();
                    System.out.println("Thank you for using Smart Hospital Management System. Exiting...");
                    scanner.close();
                    System.exit(0);
            }
            System.out.println();
        }
    }

    private static void displayMainMenu() {
        System.out.println("===============================================");
        System.out.println("        SMART HOSPITAL MANAGEMENT SYSTEM       ");
        System.out.println("===============================================");
        System.out.println("1. Patient Management");
        System.out.println("2. Queue Management");
        System.out.println("3. Doctor Management");
        System.out.println("4. Appointment Management");
        System.out.println("5. Patient Visit History");
        System.out.println("6. Reports and Statistics");
        System.out.println("7. Save Data");
        System.out.println("0. Exit");
        System.out.println("===============================================");
    }

    // ==========================================
    // PATIENT MANAGEMENT
    // ==========================================

    private static void handlePatientManagement() {
        while (true) {
            System.out.println("--- PATIENT MANAGEMENT ---");
            System.out.println("1. Register Patient");
            System.out.println("2. Search Patient");
            System.out.println("3. Update Patient");
            System.out.println("4. Delete Patient");
            System.out.println("5. View All Patients");
            System.out.println("0. Back");
            int choice = InputValidator.readInt(scanner, "Enter your choice: ", 0, 5);
            System.out.println();
            switch (choice) {
                case 1:
                    registerPatient();
                    break;
                case 2:
                    searchPatient();
                    break;
                case 3:
                    updatePatient();
                    break;
                case 4:
                    deletePatient();
                    break;
                case 5:
                    viewAllPatients();
                    break;
                case 0:
                    return;
            }
            System.out.println();
        }
    }

    private static void registerPatient() {
        System.out.println("--- Register New Patient ---");
        String name = InputValidator.readString(scanner, "Enter Patient Name: ", false);
        int age = InputValidator.readInt(scanner, "Enter Patient Age (0-120): ", 0, 120);
        String gender = InputValidator.readString(scanner, "Enter Patient Gender (e.g., Male/Female/Other): ", false);
        String phone = InputValidator.readPhoneNumber(scanner, "Enter Phone Number: ");

        Patient patient = hms.registerPatient(name, age, gender, phone);
        System.out.println("\nPatient registered successfully!");
        System.out.println(patient);
    }

    private static void searchPatient() {
        System.out.println("--- Search Patient ---");
        int id = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        Patient patient = hms.searchPatient(id);
        if (patient != null) {
            System.out.println("\nPatient Found:");
            System.out.println(patient);
        } else {
            System.out.println("Error: Patient with ID " + id + " not found.");
        }
    }

    private static void updatePatient() {
        System.out.println("--- Update Patient ---");
        int id = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        Patient patient = hms.searchPatient(id);
        if (patient == null) {
            System.out.println("Error: Patient with ID " + id + " not found.");
            return;
        }

        System.out.println("\nCurrent Patient Info:");
        System.out.println(patient);
        System.out.println("\nEnter New Information:");
        String name = InputValidator.readString(scanner, "Enter New Name: ", false);
        int age = InputValidator.readInt(scanner, "Enter New Age (0-120): ", 0, 120);
        String gender = InputValidator.readString(scanner, "Enter New Gender: ", false);
        String phone = InputValidator.readPhoneNumber(scanner, "Enter New Phone Number: ");

        hms.updatePatient(id, name, age, gender, phone);
        System.out.println("\nPatient details updated successfully.");
    }

    private static void deletePatient() {
        System.out.println("--- Delete Patient ---");
        int id = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        boolean success = hms.deletePatient(id);
        if (success) {
            System.out.println("Patient ID " + id + " deleted successfully. Any active queues or scheduled appointments for this patient have been cleared.");
        } else {
            System.out.println("Error: Patient with ID " + id + " not found.");
        }
    }

    private static void viewAllPatients() {
        System.out.println("--- All Registered Patients ---");
        Collection<Patient> patients = hms.getAllPatients();
        if (patients.isEmpty()) {
            System.out.println("No patients registered in the system.");
        } else {
            for (Patient p : patients) {
                System.out.println("--------------------------------");
                System.out.println(p);
            }
            System.out.println("--------------------------------");
        }
    }

    // ==========================================
    // QUEUE MANAGEMENT
    // ==========================================

    private static void handleQueueManagement() {
        while (true) {
            System.out.println("--- QUEUE MANAGEMENT ---");
            System.out.println("1. Add Emergency Patient");
            System.out.println("2. Add Normal Patient");
            System.out.println("3. View Emergency Queue");
            System.out.println("4. View Normal Queue");
            System.out.println("5. Treat Next Patient");
            System.out.println("0. Back");
            int choice = InputValidator.readInt(scanner, "Enter your choice: ", 0, 5);
            System.out.println();
            switch (choice) {
                case 1:
                    addEmergencyPatient();
                    break;
                case 2:
                    addNormalPatient();
                    break;
                case 3:
                    viewEmergencyQueue();
                    break;
                case 4:
                    viewNormalQueue();
                    break;
                case 5:
                    treatNextPatient();
                    break;
                case 0:
                    return;
            }
            System.out.println();
        }
    }

    private static void addEmergencyPatient() {
        System.out.println("--- Add Patient to Emergency Queue ---");
        int patientId = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        System.out.println("Severity Levels:");
        System.out.println("1 - Critical");
        System.out.println("2 - Serious");
        System.out.println("3 - Moderate");
        System.out.println("4 - Low");
        int severity = InputValidator.readInt(scanner, "Select Severity Level (1-4): ", 1, 4);

        boolean success = hms.addEmergencyPatient(patientId, severity);
        if (success) {
            System.out.println("Patient ID " + patientId + " successfully added to the Emergency Queue.");
        }
    }

    private static void addNormalPatient() {
        System.out.println("--- Add Patient to Normal Queue ---");
        int patientId = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        boolean success = hms.addNormalPatient(patientId);
        if (success) {
            System.out.println("Patient ID " + patientId + " successfully added to the Normal Queue.");
        }
    }

    private static void viewEmergencyQueue() {
        System.out.println("--- Emergency Queue ---");
        PriorityQueue<EmergencyPatient> queue = hms.getEmergencyQueue();
        if (queue.isEmpty()) {
            System.out.println("Emergency Queue is empty.");
        } else {
            // Sort to display elements in priority order (iterator of PriorityQueue does not guarantee order)
            ArrayList<EmergencyPatient> sortedList = new ArrayList<>(queue);
            Collections.sort(sortedList);
            for (EmergencyPatient ep : sortedList) {
                System.out.println("--------------------------------");
                System.out.println("Patient ID: " + ep.getPatientId() + " | Name: " + ep.getName() + 
                                   " | Severity: " + ep.getSeverityLevel() + " | Arrival: " + ep.getArrivalOrder());
            }
            System.out.println("--------------------------------");
        }
    }

    private static void viewNormalQueue() {
        System.out.println("--- Normal Queue ---");
        Queue<Patient> queue = hms.getNormalQueue();
        if (queue.isEmpty()) {
            System.out.println("Normal Queue is empty.");
        } else {
            // LinkedList maintains FIFO insertion order
            for (Patient p : queue) {
                System.out.println("--------------------------------");
                System.out.println("Patient ID: " + p.getPatientId() + " | Name: " + p.getName());
            }
            System.out.println("--------------------------------");
        }
    }

    private static void treatNextPatient() {
        System.out.println("--- Treat Next Patient ---");
        Patient patient = hms.treatNextPatient();
        if (patient == null) {
            System.out.println("No patients are currently waiting.");
            return;
        }

        System.out.println("Treating Patient:");
        System.out.println(patient);
        System.out.println();

        ArrayList<Doctor> doctors = hms.getDoctors();
        if (doctors.isEmpty()) {
            System.out.println("Warning: No doctors registered in the system. Cannot proceed with treatment record.");
            return;
        }

        System.out.println("Select Doctor for Consultation:");
        for (Doctor d : doctors) {
            System.out.println("ID: " + d.getDoctorId() + " | Dr. " + d.getName() + " | " + d.getSpecialization());
        }
        int doctorId = InputValidator.readInt(scanner, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);
        Doctor doctor = hms.searchDoctor(doctorId);
        while (doctor == null) {
            System.out.println("Error: Doctor ID not found.");
            doctorId = InputValidator.readInt(scanner, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);
            doctor = hms.searchDoctor(doctorId);
        }

        String diagnosis = InputValidator.readString(scanner, "Enter Diagnosis: ", false);
        String treatment = InputValidator.readString(scanner, "Enter Treatment: ", false);
        String prescription = InputValidator.readString(scanner, "Enter Prescription: ", false);

        boolean success = hms.addVisitRecord(patient.getPatientId(), doctorId, diagnosis, treatment, prescription);
        if (success) {
            System.out.println("\nPatient treated successfully! Visit record added.");
        } else {
            System.out.println("\nError: Failed to record treatment.");
        }
    }

    // ==========================================
    // DOCTOR MANAGEMENT
    // ==========================================

    private static void handleDoctorManagement() {
        while (true) {
            System.out.println("--- DOCTOR MANAGEMENT ---");
            System.out.println("1. Add Doctor");
            System.out.println("2. View Doctors");
            System.out.println("3. Search Doctor");
            System.out.println("0. Back");
            int choice = InputValidator.readInt(scanner, "Enter your choice: ", 0, 3);
            System.out.println();
            switch (choice) {
                case 1:
                    addDoctor();
                    break;
                case 2:
                    viewDoctors();
                    break;
                case 3:
                    searchDoctor();
                    break;
                case 0:
                    return;
            }
            System.out.println();
        }
    }

    private static void addDoctor() {
        System.out.println("--- Add Doctor ---");
        String name = InputValidator.readString(scanner, "Enter Doctor Name: ", false);
        System.out.println("Select Specialization:");
        System.out.println("1. Cardiology");
        System.out.println("2. Neurology");
        System.out.println("3. Orthopedics");
        System.out.println("4. General Medicine");
        System.out.println("5. Dermatology");
        System.out.println("6. Other (Custom)");
        int specChoice = InputValidator.readInt(scanner, "Enter Choice (1-6): ", 1, 6);
        String specialization;
        if (specChoice == 6) {
            specialization = InputValidator.readString(scanner, "Enter Custom Specialization: ", false);
        } else {
            String[] specs = {"Cardiology", "Neurology", "Orthopedics", "General Medicine", "Dermatology"};
            specialization = specs[specChoice - 1];
        }

        Doctor d = hms.addDoctor(name, specialization);
        System.out.println("\nDoctor registered successfully!");
        System.out.println(d);
    }

    private static void viewDoctors() {
        System.out.println("--- Registered Doctors ---");
        ArrayList<Doctor> doctors = hms.getDoctors();
        if (doctors.isEmpty()) {
            System.out.println("No doctors registered.");
        } else {
            for (Doctor d : doctors) {
                System.out.println("--------------------------------");
                System.out.println(d);
            }
            System.out.println("--------------------------------");
        }
    }

    private static void searchDoctor() {
        System.out.println("--- Search Doctor ---");
        int id = InputValidator.readInt(scanner, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);
        Doctor d = hms.searchDoctor(id);
        if (d != null) {
            System.out.println("\nDoctor Found:");
            System.out.println(d);
        } else {
            System.out.println("Error: Doctor ID " + id + " not found.");
        }
    }

    // ==========================================
    // APPOINTMENT MANAGEMENT
    // ==========================================

    private static void handleAppointmentManagement() {
        while (true) {
            System.out.println("--- APPOINTMENT MANAGEMENT ---");
            System.out.println("1. Schedule Appointment");
            System.out.println("2. View All Appointments");
            System.out.println("3. View Appointments by Patient");
            System.out.println("4. View Appointments by Doctor");
            System.out.println("5. Cancel Appointment");
            System.out.println("6. Undo Last Cancellation");
            System.out.println("7. Complete Appointment");
            System.out.println("0. Back");
            int choice = InputValidator.readInt(scanner, "Enter your choice: ", 0, 7);
            System.out.println();
            switch (choice) {
                case 1:
                    scheduleAppointment();
                    break;
                case 2:
                    viewAllAppointments();
                    break;
                case 3:
                    viewAppointmentsByPatient();
                    break;
                case 4:
                    viewAppointmentsByDoctor();
                    break;
                case 5:
                    cancelAppointment();
                    break;
                case 6:
                    hms.undoLastCancellation();
                    break;
                case 7:
                    completeAppointment();
                    break;
                case 0:
                    return;
            }
            System.out.println();
        }
    }

    private static void scheduleAppointment() {
        System.out.println("--- Schedule Appointment ---");
        int patientId = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        if (hms.searchPatient(patientId) == null) {
            System.out.println("Error: Patient ID " + patientId + " does not exist.");
            return;
        }

        int doctorId = InputValidator.readInt(scanner, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);
        if (hms.searchDoctor(doctorId) == null) {
            System.out.println("Error: Doctor ID " + doctorId + " does not exist.");
            return;
        }

        LocalDate date = InputValidator.readDate(scanner, "Enter Appointment Date", true);
        LocalTime time = InputValidator.readTime(scanner, "Enter Appointment Time");

        Appointment app = hms.scheduleAppointment(patientId, doctorId, date, time);
        if (app != null) {
            System.out.println("\nAppointment scheduled successfully!");
            System.out.println(app);
        }
    }

    private static void viewAllAppointments() {
        System.out.println("--- All Appointments ---");
        ArrayList<Appointment> list = hms.getAppointments();
        if (list.isEmpty()) {
            System.out.println("No appointments scheduled.");
        } else {
            for (Appointment app : list) {
                System.out.println("--------------------------------");
                System.out.println(app);
            }
            System.out.println("--------------------------------");
        }
    }

    private static void viewAppointmentsByPatient() {
        System.out.println("--- View Appointments by Patient ---");
        int patientId = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        ArrayList<Appointment> list = hms.viewAppointmentsByPatient(patientId);
        if (list.isEmpty()) {
            System.out.println("No appointments found for Patient ID " + patientId + ".");
        } else {
            for (Appointment app : list) {
                System.out.println("--------------------------------");
                System.out.println(app);
            }
            System.out.println("--------------------------------");
        }
    }

    private static void viewAppointmentsByDoctor() {
        System.out.println("--- View Appointments by Doctor ---");
        int doctorId = InputValidator.readInt(scanner, "Enter Doctor ID: ", 1, Integer.MAX_VALUE);
        ArrayList<Appointment> list = hms.viewAppointmentsByDoctor(doctorId);
        if (list.isEmpty()) {
            System.out.println("No appointments found for Doctor ID " + doctorId + ".");
        } else {
            for (Appointment app : list) {
                System.out.println("--------------------------------");
                System.out.println(app);
            }
            System.out.println("--------------------------------");
        }
    }

    private static void cancelAppointment() {
        System.out.println("--- Cancel Appointment ---");
        int appointmentId = InputValidator.readInt(scanner, "Enter Appointment ID: ", 1, Integer.MAX_VALUE);
        boolean success = hms.cancelAppointment(appointmentId);
        if (success) {
            System.out.println("Appointment ID " + appointmentId + " successfully cancelled.");
        }
    }

    private static void completeAppointment() {
        System.out.println("--- Complete Appointment ---");
        int appointmentId = InputValidator.readInt(scanner, "Enter Appointment ID: ", 1, Integer.MAX_VALUE);
        Appointment app = hms.searchAppointment(appointmentId);
        if (app == null) {
            System.out.println("Error: Appointment ID not found.");
            return;
        }
        if (!"SCHEDULED".equals(app.getStatus())) {
            System.out.println("Error: Appointment is already " + app.getStatus() + ". Only SCHEDULED appointments can be completed.");
            return;
        }

        String diagnosis = InputValidator.readString(scanner, "Enter Diagnosis: ", false);
        String treatment = InputValidator.readString(scanner, "Enter Treatment: ", false);
        String prescription = InputValidator.readString(scanner, "Enter Prescription: ", false);

        boolean success = hms.completeAppointment(appointmentId, diagnosis, treatment, prescription);
        if (success) {
            System.out.println("\nAppointment completed successfully and visit history updated!");
        }
    }

    // ==========================================
    // PATIENT VISIT HISTORY
    // ==========================================

    private static void handleVisitHistory() {
        while (true) {
            System.out.println("--- VISIT HISTORY ---");
            System.out.println("1. View Patient History");
            System.out.println("0. Back");
            int choice = InputValidator.readInt(scanner, "Enter your choice: ", 0, 1);
            System.out.println();
            switch (choice) {
                case 1:
                    viewPatientHistory();
                    break;
                case 0:
                    return;
            }
            System.out.println();
        }
    }

    private static void viewPatientHistory() {
        System.out.println("--- Patient Visit History ---");
        int patientId = InputValidator.readInt(scanner, "Enter Patient ID: ", 1, Integer.MAX_VALUE);
        Patient patient = hms.searchPatient(patientId);
        if (patient == null) {
            System.out.println("Error: Patient ID " + patientId + " not found.");
            return;
        }

        List<VisitRecord> history = hms.getPatientHistory(patientId);
        System.out.println("\nVisit History for: " + patient.getName() + " (ID: " + patientId + ")");
        if (history == null || history.isEmpty()) {
            System.out.println("No visit records found.");
        } else {
            for (VisitRecord record : history) {
                System.out.println("--------------------------------");
                System.out.println(record);
            }
            System.out.println("--------------------------------");
        }
    }
}
