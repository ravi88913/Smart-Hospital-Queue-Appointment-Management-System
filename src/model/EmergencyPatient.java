package model;

import java.time.LocalDate;

public class EmergencyPatient extends Patient implements Comparable<EmergencyPatient> {
    private static final long serialVersionUID = 1L;

    private int severityLevel; // 1 = Critical, 2 = Serious, 3 = Moderate, 4 = Low
    private long arrivalOrder;

    public EmergencyPatient(int patientId, String name, int age, String gender, String phoneNumber, 
                            LocalDate registrationDate, int severityLevel, long arrivalOrder) {
        super(patientId, name, age, gender, phoneNumber, registrationDate);
        this.severityLevel = severityLevel;
        this.arrivalOrder = arrivalOrder;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public long getArrivalOrder() {
        return arrivalOrder;
    }

    public void setArrivalOrder(long arrivalOrder) {
        this.arrivalOrder = arrivalOrder;
    }

    @Override
    public int compareTo(EmergencyPatient other) {
        if (this.severityLevel != other.severityLevel) {
            return Integer.compare(this.severityLevel, other.severityLevel);
        }
        return Long.compare(this.arrivalOrder, other.arrivalOrder);
    }

    @Override
    public String toString() {
        return super.toString() +
               "\nPatient Type: Emergency" +
               "\nSeverity Level: " + severityLevel + " (" + getSeverityLabel() + ")" +
               "\nArrival Order: " + arrivalOrder;
    }

    private String getSeverityLabel() {
        switch (severityLevel) {
            case 1: return "Critical";
            case 2: return "Serious";
            case 3: return "Moderate";
            case 4: return "Low";
            default: return "Unknown";
        }
    }
}
