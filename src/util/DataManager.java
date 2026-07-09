package util;

import model.*;
import service.HospitalManagementSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class DataManager {
    private static final String FILE_PATH = "data/hospital_data.dat";

    private static class HospitalState implements Serializable {
        private static final long serialVersionUID = 1L;

        HashMap<Integer, Patient> patientMap;
        ArrayList<Doctor> doctors;
        ArrayList<Appointment> appointments;
        Stack<Appointment> cancelledAppointments;
        ArrayList<EmergencyPatient> emergencyPatients;
        ArrayList<Integer> normalPatientIds;
        int nextPatientId;
        int nextDoctorId;
        int nextAppointmentId;
        int nextVisitId;
        long emergencyArrivalCounter;
    }

    public static void saveData(HospitalManagementSystem hms) {
        File file = new File(FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        HospitalState state = new HospitalState();
        state.patientMap = hms.getPatientMap();
        state.doctors = hms.getDoctors();
        state.appointments = hms.getAppointments();
        state.cancelledAppointments = hms.getCancelledAppointments();
        state.emergencyPatients = hms.getEmergencyPatientsList();
        state.normalPatientIds = hms.getNormalPatientIdsList();
        state.nextPatientId = hms.getNextPatientId();
        state.nextDoctorId = hms.getNextDoctorId();
        state.nextAppointmentId = hms.getNextAppointmentId();
        state.nextVisitId = hms.getNextVisitId();
        state.emergencyArrivalCounter = hms.getEmergencyArrivalCounter();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(state);
            System.out.println("Data saved successfully to " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public static void loadData(HospitalManagementSystem hms) {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("No saved data found. Initializing empty system.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            HospitalState state = (HospitalState) ois.readObject();

            hms.setPatientMap(state.patientMap);
            hms.setDoctors(state.doctors);
            hms.setAppointments(state.appointments);
            hms.setCancelledAppointments(state.cancelledAppointments);
            hms.setNextPatientId(state.nextPatientId);
            hms.setNextDoctorId(state.nextDoctorId);
            hms.setNextAppointmentId(state.nextAppointmentId);
            hms.setNextVisitId(state.nextVisitId);
            hms.setEmergencyArrivalCounter(state.emergencyArrivalCounter);

            // Reconstruct the queues from the lists
            hms.reconstructQueues(state.emergencyPatients, state.normalPatientIds);
            System.out.println("Data loaded successfully from " + FILE_PATH);
        } catch (FileNotFoundException e) {
            System.out.println("No saved data file found. Initializing empty system.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage() + ". Starting with empty system.");
        }
    }
}
