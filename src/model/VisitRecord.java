package model;

import java.io.Serializable;
import java.time.LocalDate;

public class VisitRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int visitId;
    private int patientId;
    private int doctorId;
    private String doctorName;
    private LocalDate visitDate;
    private String diagnosis;
    private String treatment;
    private String prescription;

    public VisitRecord(int visitId, int patientId, int doctorId, String doctorName, LocalDate visitDate, 
                       String diagnosis, String treatment, String prescription) {
        this.visitId = visitId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.visitDate = visitDate;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
    }

    public int getVisitId() {
        return visitId;
    }

    public void setVisitId(int visitId) {
        this.visitId = visitId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    @Override
    public String toString() {
        return "Visit ID: " + visitId +
               "\nPatient ID: " + patientId +
               "\nDoctor ID: " + doctorId + " (Dr. " + doctorName + ")" +
               "\nVisit Date: " + visitDate +
               "\nDiagnosis: " + diagnosis +
               "\nTreatment: " + treatment +
               "\nPrescription: " + prescription;
    }
}
